/**
 * File:   MulticastBroadcaster.java
 * Author: Thomas Calmant
 * Date:   28 juin 2013
 */
package org.cohorte.remote.multicast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceController;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.remote.IRemoteServiceBroadcaster;
import org.cohorte.remote.IRemoteServiceEventListener;
import org.cohorte.remote.IRemoteServiceRepository;
import org.cohorte.remote.IRemoteServicesConstants;
import org.cohorte.remote.beans.RemoteServiceEvent;
import org.cohorte.remote.beans.RemoteServiceEvent.ServiceEventType;
import org.cohorte.remote.beans.RemoteServiceRegistration;
import org.cohorte.remote.multicast.beans.PelixEndpointDescription;
import org.cohorte.remote.multicast.beans.PelixMulticastPacket;
import org.cohorte.remote.multicast.utils.IPacketListener;
import org.cohorte.remote.multicast.utils.MulticastHandler;
import org.cohorte.remote.utilities.RSUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

/**
 * Multicast/servlet remote service discovery, compatible with the Pelix remote
 * services implementation
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-remote-broadcast-multicast")
@Provides(specifications = IRemoteServiceBroadcaster.class)
public class MulticastBroadcaster implements IRemoteServiceBroadcaster,
        IPacketListener {

    /** UTF-8 charset name */
    private static final String CHARSET_UTF8 = "UTF-8";

    /** HTTP 200 OK */
    private static final int HTTP_OK = 200;

    /** HTTP service port property */
    private static final String HTTP_SERVICE_PORT = "org.osgi.service.http.port";

    /** HTTPService dependency ID */
    private static final String IPOJO_ID_HTTP = "http.service";

    /** The bundle context */
    private final BundleContext pBundleContext;

    /** The HTTP server port */
    private int pHttpPort;

    /** The HTTP service */
    @Requires(id = IPOJO_ID_HTTP, filter = "(" + HTTP_SERVICE_PORT + "=*)")
    private HttpService pHttpService;

    /** End point UID -&gt; Remote Service Registration */
    private final Map<String, RemoteServiceRegistration> pImportedEndpoints = new LinkedHashMap<String, RemoteServiceRegistration>();

    /** The isolate UID */
    private String pIsolateUID;

    /** The logger */
    @Requires
    private LogService pLogger;

    /** The multicast socket */
    private MulticastHandler pMulticast;

    /** The multicast group */
    @Property(name = "multicast.group", value = "239.0.0.1")
    // ff05::5
    private String pMulticastGroup;

    /** The multicast port */
    @Property(name = "multicast.port", value = "42000")
    private int pMulticastPort;

    /** Remote service events listeners */
    @Requires(optional = true)
    private IRemoteServiceEventListener[] pRemoteEventsListeners;

    /** The remote service repository */
    @Requires
    private IRemoteServiceRepository pRepository;

    /** The service controller: active only if the validation succeeded */
    @ServiceController
    private boolean pServiceController;

    /** The registry servlet */
    private RegistryServlet pServlet;

    /** The servlet registration path */
    @Property(name = "servlet.path", value = "/pelix-dispatcher")
    private String pServletPath;

    /**
     * Sets up members
     * 
     * @param aBundleContext
     *            The bundle context
     */
    public MulticastBroadcaster(final BundleContext aBundleContext) {

        pBundleContext = aBundleContext;
    }

    /**
     * HTTP service ready
     * 
     * @param aHttpService
     *            The bound service
     * @param aServiceProperties
     *            The HTTP service properties
     */
    @Bind(id = IPOJO_ID_HTTP)
    private void bindHttpService(final HttpService aHttpService,
            final Map<?, ?> aServiceProperties) {

        final Object rawPort = aServiceProperties.get(HTTP_SERVICE_PORT);

        if (rawPort instanceof Number) {
            // Get the integer
            pHttpPort = ((Number) rawPort).intValue();

        } else if (rawPort instanceof CharSequence) {
            // Parse the string
            pHttpPort = Integer.parseInt(rawPort.toString());

        } else {
            // Unknown port type
            pLogger.log(LogService.LOG_WARNING, "Couldn't read access port "
                    + rawPort);
            pHttpPort = -1;
        }
    }

    /**
     * Returns the response of a HTTP server, or throws an exception
     * 
     * @param aAddress
     *            Server address
     * @param aPort
     *            Server port
     * @param aPath
     *            Request URI
     * @return The raw response of the server
     */
    private String grabData(final InetAddress aAddress, final int aPort,
            final String aPath) {

        // Forge the URL
        final URL url;
        try {
            url = new URL("http", aAddress.getHostAddress(), aPort, aPath);

        } catch (final MalformedURLException ex) {
            pLogger.log(LogService.LOG_ERROR,
                    "Couldn't forge the URL to access: " + aAddress + " : "
                            + aPort + " - " + aPath, ex);
            return null;
        }

        // Open the connection
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.connect();

            // Flush the request
            final int responseCode = httpConnection.getResponseCode();
            if (responseCode != HTTP_OK) {
                // Incorrect answer
                pLogger.log(LogService.LOG_WARNING, "Error: " + url
                        + " responded with code " + responseCode);
                return null;
            }

            // Get the response content
            final byte[] rawResult = RSUtils.inputStreamToBytes(httpConnection
                    .getInputStream());

            // Construct corresponding string
            return new String(rawResult);

        } catch (final IOException ex) {
            // Connection error
            pLogger.log(LogService.LOG_ERROR,
                    "Error requesting information from " + url.toString(), ex);

        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }

        return null;
    }

    /**
     * @param aAddress
     * @param aPort
     * @param aPath
     * @param aEndpointUID
     * @return The description of the given Pelix end point
     */
    private RemoteServiceRegistration grabEndpoint(final InetAddress aAddress,
            final int aPort, final String aPath, final String aEndpointUID) {

        // Get the raw servlet result
        final String rawResponse = grabData(aAddress, aPort, aPath
                + "/endpoint/" + aEndpointUID);
        if (rawResponse == null || rawResponse.isEmpty()) {
            // No response
            pLogger.log(LogService.LOG_WARNING, "No response from the server "
                    + aAddress + " for end point " + aEndpointUID);
            return null;
        }

        try {
            // Parse it
            final JSONObject rawEndpoint = new JSONObject(rawResponse);

            // Convert the result
            final PelixEndpointDescription endpoint = new PelixEndpointDescription(
                    rawEndpoint);
            endpoint.setServerAddress(aAddress.getHostAddress());

            return endpoint.toRegistration();

        } catch (final JSONException ex) {
            // Invalid response
            pLogger.log(LogService.LOG_WARNING,
                    "Invalid response from the server " + aAddress
                            + " for end point " + aEndpointUID + "\n"
                            + rawResponse, ex);

        } catch (final MalformedURLException ex) {
            // Invalid end point URL
            pLogger.log(LogService.LOG_WARNING, "Invalid end point URL for "
                    + aEndpointUID + "\n" + rawResponse, ex);
        }

        return null;
    }

    /**
     * @param aAddress
     * @param aPort
     * @param aPath
     * @return The list of the end points of the given isolate
     */
    private List<RemoteServiceRegistration> grabEndpoints(
            final InetAddress aAddress, final int aPort, final String aPath) {

        // Get the raw servlet result
        final String rawResponse = grabData(aAddress, aPort, aPath
                + "/endpoints");
        if (rawResponse == null || rawResponse.isEmpty()) {
            // No response
            pLogger.log(LogService.LOG_WARNING, "No response from the server "
                    + aAddress + " for endpoints/");
            return null;
        }

        // Prepare the result list
        final List<RemoteServiceRegistration> registrations = new LinkedList<RemoteServiceRegistration>();

        // Parse it
        try {
            final JSONArray endpointsArray = new JSONArray(rawResponse);

            for (int i = 0; i < endpointsArray.length(); i++) {
                // Get the next end point
                final JSONObject rawEndpoint = endpointsArray.getJSONObject(i);

                try {
                    // Convert it
                    final PelixEndpointDescription endpoint = new PelixEndpointDescription(
                            rawEndpoint);
                    endpoint.setServerAddress(aAddress.getHostAddress());
                    registrations.add(endpoint.toRegistration());

                } catch (final JSONException ex) {
                    // Conversion error: just log
                    pLogger.log(LogService.LOG_WARNING,
                            "Error parsing an end point:\n" + rawEndpoint, ex);

                } catch (final MalformedURLException ex) {
                    // Conversion error: just log
                    pLogger.log(LogService.LOG_WARNING,
                            "Invalid end point URL:\n" + rawEndpoint, ex);
                }
            }

        } catch (final JSONException ex) {
            // Invalid response
            pLogger.log(LogService.LOG_WARNING,
                    "Invalid response from the server " + aAddress
                            + " for endpoints/\n" + rawResponse, ex);
            // Return null in case of error
            return null;
        }

        return registrations;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.multicast.utils.IPacketListener#handleError(java.lang
     * .Exception)
     */
    @Override
    public boolean handleError(final Exception aException) {

        // Log the error
        pLogger.log(LogService.LOG_ERROR,
                "Error reading a packet from the multicast handler", aException);

        // Continue if the exception is not "important"
        return !(aException instanceof SocketException || aException instanceof NullPointerException);
    }

    /**
     * Handles a remote service event packet
     * 
     * @param aEndpointPacket
     *            Received end point packet
     * @param aAddress
     *            Sender address
     * @param aPort
     *            Sender port
     */
    private void handleEvent(final PelixMulticastPacket aEndpointPacket,
            final InetAddress aAddress, final int aPort) {

        final String event = aEndpointPacket.getEvent();
        final String endpointUID = aEndpointPacket.getUID();

        if (IPacketConstants.EVENT_ADD.equals(event)) {
            // Store the new service
            handleEventAdd(endpointUID, aEndpointPacket, aAddress);

        } else if (IPacketConstants.EVENT_REMOVE.equals(event)) {
            // Remove it
            handleEventRemove(endpointUID);

        } else if (IPacketConstants.EVENT_UPDATE.equals(event)) {
            // Update it
            handleEventUpdate(endpointUID, aEndpointPacket);

        } else {
            // Unknown...
            pLogger.log(LogService.LOG_WARNING,
                    "Unknown kind of remote service event: " + event);
        }
    }

    /**
     * Handles a registered end point event
     * 
     * @param aEndpointUID
     *            The UID of the registered end point
     * @param aEndpointPacket
     *            Received packet content
     * @param aAddress
     *            Sender address
     */
    private void handleEventAdd(final String aEndpointUID,
            final PelixMulticastPacket aEndpointPacket,
            final InetAddress aAddress) {

        // Check if the end point is already known
        if (pImportedEndpoints.containsKey(aEndpointUID)) {
            // Nothing to do
            return;
        }

        // Get the access to the dispatcher servlet
        final String path = aEndpointPacket.getAccessPath();
        final int port = aEndpointPacket.getAccessPort();

        // Grab the end point
        final RemoteServiceRegistration registration = grabEndpoint(aAddress,
                port, path, aEndpointUID);

        if (registration != null) {
            // Store the service registration
            pImportedEndpoints.put(aEndpointUID, registration);

            // Notify listeners
            final RemoteServiceEvent remoteEvent = new RemoteServiceEvent(
                    ServiceEventType.REGISTERED, registration);
            for (final IRemoteServiceEventListener listener : pRemoteEventsListeners) {
                listener.handleRemoteEvent(remoteEvent);
            }
        }
    }

    /**
     * Handles an unregistered end point event
     * 
     * @param aEndpointUID
     *            The UID of the unregistered end point
     */
    private void handleEventRemove(final String aEndpointUID) {

        // Find the associated registration
        final RemoteServiceRegistration registration = pImportedEndpoints
                .remove(aEndpointUID);
        if (registration == null) {
            pLogger.log(LogService.LOG_DEBUG, "Unknown end point UID: "
                    + aEndpointUID);
            return;
        }

        // Notify listeners
        final RemoteServiceEvent remoteEvent = new RemoteServiceEvent(
                ServiceEventType.UNREGISTERED, registration);
        for (final IRemoteServiceEventListener listener : pRemoteEventsListeners) {
            listener.handleRemoteEvent(remoteEvent);
        }
    }

    /**
     * Handles a registered end point event
     * 
     * @param aEndpointUID
     *            The UID of the registered end point
     * @param aEndpointPacket
     *            Received packet content
     */
    private void handleEventUpdate(final String aEndpointUID,
            final PelixMulticastPacket aEndpointPacket) {

        // Find the associated registration
        final RemoteServiceRegistration registration = pImportedEndpoints
                .remove(aEndpointUID);
        if (registration == null) {
            pLogger.log(LogService.LOG_DEBUG, "Unknown end point UID: "
                    + aEndpointUID);
            return;
        }

        // New service properties (they will be filtered by listeners)
        final Map<String, Object> newProperties = aEndpointPacket
                .getNewProperties();

        // Update the registration
        registration.setServiceProperties(newProperties);

        // Notify listeners
        final RemoteServiceEvent remoteEvent = new RemoteServiceEvent(
                ServiceEventType.MODIFIED, registration);
        for (final IRemoteServiceEventListener listener : pRemoteEventsListeners) {
            listener.handleRemoteEvent(remoteEvent);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.multicast.utils.IPacketListener#handlePacket(java.
     * net.SocketAddress, byte[])
     */
    @Override
    public void handlePacket(final InetSocketAddress aSender,
            final byte[] aContent) {

        // Read the packet content
        final String content;
        try {
            content = new String(aContent, CHARSET_UTF8).trim();

        } catch (final UnsupportedEncodingException ex) {
            pLogger.log(LogService.LOG_ERROR,
                    "Error reading multicast packet data", ex);
            return;
        }

        // Parse it from JSON
        final PelixMulticastPacket endpointPacket;
        try {
            final JSONObject parsed = new JSONObject(content);
            endpointPacket = new PelixMulticastPacket(parsed);

        } catch (final JSONException ex) {
            // Log
            pLogger.log(LogService.LOG_ERROR,
                    "Error parsing a multicast packet", ex);
            return;
        }

        // Avoid handling our own packets
        if (endpointPacket.isFromSender(pIsolateUID)) {
            return;
        }

        // Get information about the sender
        final InetAddress senderAddress = aSender.getAddress();
        final int senderPort = aSender.getPort();

        // Get the kind of event
        final String event = endpointPacket.getEvent();

        // Dispatch...
        if (IPacketConstants.EVENT_DISCOVERY.equals(event)) {
            // Discovery request: send a packet back
            sendDiscovered(senderAddress, senderPort);

        } else if (IPacketConstants.EVENT_DISCOVERED.equals(event)) {
            // Discovered: grab end points
            final List<RemoteServiceRegistration> endpoints = grabEndpoints(
                    senderAddress, endpointPacket.getAccessPort(),
                    endpointPacket.getAccessPath());

            pLogger.log(LogService.LOG_DEBUG, "GOT EVENT_DISCOVERED by "
                    + endpointPacket.getSender() + " - " + endpoints.size()
                    + " end points found");

            // Notify listeners
            for (final RemoteServiceRegistration registration : endpoints) {

                final RemoteServiceEvent regEvent = new RemoteServiceEvent(
                        ServiceEventType.REGISTERED, registration);
                for (final IRemoteServiceEventListener listener : pRemoteEventsListeners) {
                    listener.handleRemoteEvent(regEvent);
                }
            }

        } else {
            // Handle an end point event
            handleEvent(endpointPacket, senderAddress, senderPort);
        }
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        if (pMulticast != null) {
            try {
                pMulticast.stop();

            } catch (final IOException ex) {
                pLogger.log(LogService.LOG_ERROR,
                        "Error stopping the multicast receiver", ex);
            }

            pMulticast.setLogger(null);
            pMulticast = null;
        }

        if (pServlet != null) {
            pHttpService.unregister(pServletPath);
            pServlet = null;
        }

        pLogger.log(LogService.LOG_INFO, "Multicast broadcaster gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.remote.IRemoteServiceBroadcaster#requestAllEndpoints()
     */
    @Override
    public RemoteServiceEvent[] requestAllEndpoints() {

        // Send a global discovery packet
        sendDiscovery();

        // Asynchronous mode: return null and wait for future responses
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.IRemoteServiceBroadcaster#requestEndpoints(java.lang
     * .String)
     */
    @Override
    public RemoteServiceEvent[] requestEndpoints(final String aIsolateUID) {

        // FIXME Send a targeted discovery packet
        sendDiscovery();

        // Asynchronous mode: return null and wait for future responses
        return null;
    }

    /**
     * Signal to another isolate that we got its discovery packet
     * 
     * 
     * @param aSender
     *            Address of the sender of the discovery packet
     * @param aPort
     *            Port to answer to
     */
    private void sendDiscovered(final InetAddress aSender, final int aPort) {

        // Prepare the content
        final Map<String, Object> content = new HashMap<String, Object>();
        content.put(IPacketConstants.KEY_EVENT,
                IPacketConstants.EVENT_DISCOVERED);
        content.put(IPacketConstants.KEY_SENDER, pIsolateUID);

        final Map<String, Object> access = new HashMap<String, Object>();
        access.put(IPacketConstants.KEY_ACCESS_PATH, pServletPath);
        access.put(IPacketConstants.KEY_ACCESS_PORT, pHttpPort);
        content.put(IPacketConstants.KEY_ACCESS, access);

        // Send the packet
        sendPacket(content, aSender, aPort);
    }

    /**
     * Sends a discovery request on the multicast group
     */
    private void sendDiscovery() {

        // Prepare the content
        final Map<String, Object> content = new HashMap<String, Object>();
        content.put(IPacketConstants.KEY_EVENT,
                IPacketConstants.EVENT_DISCOVERY);
        content.put(IPacketConstants.KEY_SENDER, pIsolateUID);

        final Map<String, Object> access = new HashMap<String, Object>();
        access.put(IPacketConstants.KEY_ACCESS_PATH, pServletPath);
        access.put(IPacketConstants.KEY_ACCESS_PORT, pHttpPort);
        content.put(IPacketConstants.KEY_ACCESS, access);

        // Send the packet
        sendPacket(content);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.IRemoteServiceBroadcaster#sendNotification(org.cohorte
     * .remote.beans.RemoteServiceEvent)
     */
    @Override
    public void sendNotification(final RemoteServiceEvent aEvent) {

        // Get the registration bean
        final RemoteServiceRegistration registration = aEvent
                .getServiceRegistration();

        // The packet content
        final Map<String, Object> packet = new LinkedHashMap<String, Object>();

        // Indicate our ID
        packet.put(IPacketConstants.KEY_SENDER, pIsolateUID);

        // Compute the kind of event
        switch (aEvent.getEventType()) {
        case REGISTERED:
            // Service added
            packet.put(IPacketConstants.KEY_EVENT, IPacketConstants.EVENT_ADD);
            break;

        case MODIFIED:
            // Service modified
            packet.put(IPacketConstants.KEY_EVENT,
                    IPacketConstants.EVENT_UPDATE);

            // Also add the new service properties
            packet.put(IPacketConstants.KEY_ENDPOINT_NEW_PROPERTIES,
                    registration.getServiceProperties());
            break;

        case UNREGISTERED:
            // Service removed
            packet.put(IPacketConstants.KEY_EVENT,
                    IPacketConstants.EVENT_REMOVE);
            break;
        }

        // Set the end point UID
        packet.put(IPacketConstants.KEY_ENDPOINT_UID,
                registration.getServiceId());

        // Set the servlet access
        final Map<String, Object> access = new LinkedHashMap<String, Object>();
        access.put(IPacketConstants.KEY_ACCESS_PATH, pServletPath);
        access.put(IPacketConstants.KEY_ACCESS_PORT, pHttpPort);
        packet.put(IPacketConstants.KEY_ACCESS, access);

        pLogger.log(LogService.LOG_DEBUG, "Sending notification:\n" + packet);

        // Send the packet
        sendPacket(packet);
    }

    /**
     * Sends a packet to the multicast, with the given content
     * 
     * @param aContent
     *            Content of the packet
     */
    private void sendPacket(final Map<String, ?> aContent) {

        sendPacket(aContent, null, 0);
    }

    /**
     * Sends a packet to the given target, with the given content
     * 
     * @param aContent
     *            Content of the packet
     * @param aTarget
     *            Target address
     * @param aPort
     *            Target port
     */
    private void sendPacket(final Map<String, ?> aContent,
            final InetAddress aTarget, final int aPort) {

        // Convert data to JSON
        final String data = new JSONObject(aContent).toString();

        try {
            // Convert to bytes
            final byte[] rawData = data.getBytes(CHARSET_UTF8);

            // Send bytes
            if (aTarget == null) {
                pMulticast.send(rawData);

            } else {
                pMulticast.send(rawData, aTarget, aPort);
            }

        } catch (final UnsupportedEncodingException ex) {
            // Log
            pLogger.log(LogService.LOG_ERROR, "System does not support "
                    + CHARSET_UTF8, ex);

        } catch (final IOException ex) {
            // Log
            pLogger.log(LogService.LOG_ERROR,
                    "Error sending a multicast packet", ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder(
                "MulticastBroadcaster({");
        // End point
        builder.append(pMulticastGroup).append("}:").append(pMulticastPort);

        // Service state
        builder.append(", ");
        if (pServiceController) {
            builder.append("up and running");
        } else {
            builder.append("stopped");
        }
        builder.append(")");

        return builder.toString();
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Preparation: deactivate the service
        pServiceController = false;

        // Setup the isolate UID
        pIsolateUID = RSUtils.setupUID(pBundleContext,
                IRemoteServicesConstants.ISOLATE_UID);

        // Set up the servlet
        pServlet = new RegistryServlet(pIsolateUID, pRepository);
        try {
            pHttpService.registerServlet(pServletPath, pServlet, null, null);

        } catch (final Exception ex) {
            pLogger.log(LogService.LOG_ERROR,
                    "Error registering the dispatcher servlet. Abandon.", ex);
            invalidate();
            return;
        }

        // Compute the group address
        InetAddress groupAddress;
        try {
            groupAddress = InetAddress.getByName(pMulticastGroup);

        } catch (final UnknownHostException ex) {
            pLogger.log(LogService.LOG_ERROR,
                    "Error computing the multicast group address", ex);
            invalidate();
            return;
        }

        // Create the handler
        pMulticast = new MulticastHandler(this, groupAddress, pMulticastPort);
        pMulticast.setLogger(pLogger);

        try {
            // Start it
            pMulticast.start();

        } catch (final IOException ex) {
            // Error...
            pLogger.log(LogService.LOG_ERROR,
                    "Error starting the multicast receiver. Abandon.", ex);
            invalidate();
            return;
        }

        pLogger.log(LogService.LOG_INFO, "Multicast broadcaster ready");

        // No error: activate the service
        pServiceController = true;
    }
}
