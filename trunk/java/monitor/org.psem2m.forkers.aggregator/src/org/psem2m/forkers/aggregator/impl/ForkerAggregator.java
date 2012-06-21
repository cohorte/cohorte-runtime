/**
 * File:   ForkerAggregator.java
 * Author: Thomas Calmant
 * Date:   25 mai 2012
 */
package org.psem2m.forkers.aggregator.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.forker.IForker;
import org.psem2m.isolates.services.forker.IForkerEventListener;
import org.psem2m.isolates.services.forker.IForkerEventListener.EForkerEventType;
import org.psem2m.signals.ISignalBroadcaster;
import org.psem2m.signals.ISignalDirectory;
import org.psem2m.signals.ISignalDirectoryConstants;
import org.psem2m.signals.ISignalReceiver;
import org.psem2m.signals.ISignalSendResult;

/**
 * The forker aggregator
 * 
 * Provides the forker interface and sends signals to real forkers to start/stop
 * isolates.
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-forker-aggregator-factory", publicFactory = false)
@Provides(specifications = IForker.class)
@Instantiate(name = "psem2m-forker-aggregator")
public class ForkerAggregator implements IForker, IPacketListener, Runnable {

    /** Maximum time without forker notification : 5 seconds */
    public static final long FORKER_TTL = 5000;

    /** UDP Packet: Forker heart beat */
    public static final byte PACKET_FORKER_HEARTBEAT = 1;

    /** The configuration service */
    @Requires
    private ISvcConfig pConfig;

    /** The isolates directory */
    @Requires
    private ISignalDirectory pDirectory;

    /** Forkers ID -&gt; Last seen time (LST) */
    private final Map<String, Long> pForkersLST = new HashMap<String, Long>();

    /** Isolate -&gt; Associated forker map */
    private final Map<String, String> pIsolateForkers = new HashMap<String, String>();

    /** The forker events listeners */
    private final Set<IForkerEventListener> pListeners = new HashSet<IForkerEventListener>();

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The multicast receiver */
    private MulticastReceiver pMulticast;

    /** The signal receiver, it must be online to retrieve its access point */
    @Requires(filter = "(" + ISignalReceiver.PROPERTY_ONLINE + "=true)")
    private ISignalReceiver pReceiver;

    /** Order results */
    private final Map<Integer, Integer> pResults = new HashMap<Integer, Integer>();

    /** The signal sender */
    @Requires
    private ISignalBroadcaster pSender;

    /** The TTL thread */
    private Thread pThread;

    /** The thread stopper */
    private boolean pThreadRunning = false;

    /**
     * Extracts a string from the given buffer
     * 
     * @param aBuffer
     *            A bytes buffer
     * @return The read string, or null
     */
    protected String extractString(final ByteBuffer aBuffer) {

        // Get the length
        final int length = aBuffer.getShort();

        // Get the bytes
        final byte[] buffer = new byte[length];

        try {
            aBuffer.get(buffer);

        } catch (final BufferUnderflowException e) {
            pLogger.logSevere(this, "extractString", "Missing data:", e);
            return null;
        }

        // Return the string form
        try {
            return new String(buffer, "UTF-8");

        } catch (final UnsupportedEncodingException ex) {
            pLogger.logSevere(this, "extractString", "Unknown encoding:", ex);
            return null;
        }
    }

    /**
     * Notifies listeners of a forker event
     * 
     * @param aEventType
     *            The kind of event
     * @param aForkerId
     *            The forker isolate ID
     * @param aForkerHost
     *            The forker host
     */
    protected void fireForkerEvent(final EForkerEventType aEventType,
            final String aForkerId, final String aForkerHost) {

        synchronized (pListeners) {
            for (final IForkerEventListener listener : pListeners) {
                try {
                    listener.handleForkerEvent(aEventType, aForkerId,
                            aForkerHost);
                } catch (final Exception e) {
                    // A listener failed
                    pLogger.logSevere(this, "fireForkerEvent",
                            "A forker event listener failed:\n", e);
                }
            }
        }
    }

    /**
     * Finds the first isolate with a forker ID on the given node
     * 
     * @param aNodeName
     *            The name of a node
     * @return The first forker found on the node, or null
     */
    protected String getForkerForNode(final String aNodeName) {

        final String[] isolates = pDirectory.getIsolatesOnNode(aNodeName);
        if (isolates == null) {
            return null;
        }

        for (final String isolate : isolates) {
            if (isolate
                    .startsWith(IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER)) {
                // Node forker found
                return isolate;
            }
        }

        return null;
    }

    /**
     * Posts an order to the given forker and waits for the result. Returns
     * {@link IForker#REQUEST_TIMEOUT} if the time out expires before.
     * 
     * @param aForkerId
     *            ID of the forker to contact
     * @param aSignalName
     *            Signal name
     * @param aContent
     *            Signal content
     * @param aTimeout
     *            Maximum time to wait for an answer (in milliseconds)
     * @return The forker result (&ge;0) or an error code (&lt;0) (see
     *         {@link IForker})
     */
    protected int getForkerIntResult(final String aForkerId,
            final String aSignalName, final Object aContent, final long aTimeout) {

        // Send the order
        final Future<ISignalSendResult> waiter = pSender.post(aSignalName,
                aContent, aForkerId);

        try {
            // Wait a little...
            final ISignalSendResult result = waiter.get(aTimeout,
                    TimeUnit.MILLISECONDS);

            final Map<String, Object[]> results = result.getResults();
            if (results == null) {
                // No results at all
                pLogger.logWarn(this, "startIsolate", "No results from forker");
                return IForker.REQUEST_NO_RESULT;
            }

            final Object[] forkerResults = results.get(aForkerId);
            if (forkerResults == null || forkerResults.length != 1) {
                pLogger.logWarn(this, "startIsolate", "Unreadable result=",
                        forkerResults);
                return IForker.REQUEST_NO_RESULT;
            }

            if (forkerResults[0] instanceof Number) {
                // Retrieve the forker result
                return ((Number) forkerResults[0]).intValue();

            } else {
                // Bad result
                pLogger.logWarn(this, "startIsolate", "Invalid result=",
                        forkerResults[0]);
                return IForker.REQUEST_NO_RESULT;
            }

        } catch (final InterruptedException ex) {
            // Thread interrupted (end of the monitor ?), consider a time out
            pLogger.logDebug(this, "startIsolate",
                    "Interrupted while waiting for an answer of forker=",
                    aForkerId, "sending signal=", aSignalName);

            return IForker.REQUEST_TIMEOUT;

        } catch (final TimeoutException e) {
            // Forker timed out
            pLogger.logWarn(this, "startIsolate", "Forker=", aForkerId,
                    "timed out sending signal=", aSignalName);

            return IForker.REQUEST_TIMEOUT;

        } catch (final ExecutionException e) {
            // Error sending the request
            pLogger.logSevere(this, "startIsolate", "Error sending signal=",
                    aSignalName, "to=", aForkerId, ":", e);
            return IForker.REQUEST_ERROR;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.forker.IForker#getHostName()
     */
    @Override
    public String getNodeName() {

        return System
                .getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_NODE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.forkers.aggregator.impl.IPacketListener#handleError(java.lang
     * .Exception)
     */
    @Override
    public boolean handleError(final Exception aException) {

        pLogger.logWarn(this, "handleError",
                "Error while receiving a UDP packet:", aException);

        // Continue...
        return true;
    }

    /**
     * Handles a forker heart beat
     * 
     * @param aSenderAddress
     *            The packet sender address
     * @param aData
     *            The packet content decoder
     */
    protected void handleHeartBeat(final String aSenderAddress,
            final ByteBuffer aData) {

        /* Extract packet content */
        // ... the port (2 bytes)
        final int port = aData.getShort();

        // ... the isolate ID (string)
        final String forkerId = extractString(aData);

        // ... the node ID (string)
        final String nodeId = extractString(aData);

        // Registration flag
        final boolean needsRegistration;
        synchronized (pForkersLST) {
            // Test if it's a new forker
            needsRegistration = !pForkersLST.containsKey(forkerId);

            // Update the Last Seen Time
            pForkersLST.put(forkerId, System.currentTimeMillis());
        }

        if (needsRegistration) {
            // TODO: use pSender.postTo() to test the access

            // Register the forker in the internal directory
            registerForker(forkerId, nodeId, aSenderAddress, port);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.forkers.aggregator.impl.IPacketListener#handlePacket(java.
     * net.DatagramPacket)
     */
    @Override
    public void handlePacket(final DatagramPacket aPacket) {

        // Get the content
        final byte[] data = aPacket.getData();

        // Make a little endian byte array reader, to extract the packet content
        final ByteBuffer buffer = ByteBuffer.wrap(data, 0, data.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        final byte packetType = buffer.get();
        switch (packetType) {
        case PACKET_FORKER_HEARTBEAT:
            handleHeartBeat(aPacket.getAddress().getHostAddress(), buffer);
            break;

        default:
            pLogger.logInfo(this, "handlePacket", "Unknown packet type=",
                    packetType);
            break;
        }
    }

    /**
     * Component invalidation
     */
    @Invalidate
    public void invalidate() {

        // Clear all collections
        pForkersLST.clear();
        pResults.clear();
        pIsolateForkers.clear();

        // Stop the multicast listener
        if (pMulticast != null) {
            try {
                pMulticast.stop();

            } catch (final IOException ex) {
                pLogger.logWarn(this, "invalidate",
                        "Error stopping the multicast listener:", ex);
            }

            pMulticast = null;
        }

        // Wait a second for the thread to stop
        pThreadRunning = false;
        try {
            pThread.join(1000);

        } catch (final InterruptedException e) {
            // Join interrupted
        }

        if (pThread.isAlive()) {
            // Force interruption if necessary
            pThread.interrupt();
        }
        pThread = null;

        pLogger.logInfo(this, "invalidate", "Forker Aggregator invalidated");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.forker.IForker#isOnHost(java.lang.String,
     * java.lang.String)
     */
    @Override
    public boolean isOnNode(final String aForkerId, final String aNodeName) {

        if (aForkerId == null) {
            // Invalid ID
            return false;
        }

        final String[] nodeIsolates = pDirectory.getIsolatesOnNode(aNodeName);
        if (nodeIsolates == null) {
            // No isolates on this node
            return false;
        }

        for (final String isolate : nodeIsolates) {
            if (aForkerId.equals(isolate)) {
                return true;
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.forker.IForker#ping(java.lang.String)
     */
    @Override
    public int ping(final String aIsolateId) {

        final String forker = pIsolateForkers.get(aIsolateId);
        if (forker == null) {
            pLogger.logSevere(this, "ping", "No forker known for isolate ID=",
                    aIsolateId);
            return -1;
        }

        // Prepare the order
        final Map<String, Object> order = new HashMap<String, Object>();
        order.put("isolateId", aIsolateId);

        return getForkerIntResult(forker, IForkerOrders.SIGNAL_PING_ISOLATE,
                order, 1000);
    }

    /**
     * Registers a forker in the internal directory, using a heart beat signal
     * data, and notifies listeners on success.
     * 
     * @param aForkerId
     *            The forker isolate ID
     * @param aForkerNode
     *            The node hosting the forker
     * @param aHost
     *            The node host address
     * @param aPort
     *            The forker signals access port
     */
    protected void registerForker(final String aForkerId,
            final String aForkerNode, final String aHost, final int aPort) {

        // Update the node host
        if (!pDirectory.getLocalNode().equals(aForkerNode)) {
            // Don't update our node
            pDirectory.setNodeAddress(aForkerNode, aHost);
        }

        // Register the forker (it can already be in the directory)
        if (pDirectory
                .registerIsolate(aForkerId, aForkerNode, aPort, "FORKERS")) {
            // Fresh forker: we can send it a contact signal as someone else
            // may not known it
            sendContactSignal(aHost, aPort);
        }

        pLogger.logInfo(this, "registerForker", "Registered forker ID=",
                aForkerId, "Node=", aForkerNode, "Port=", aPort);

        // Notify listeners
        fireForkerEvent(EForkerEventType.REGISTERED, aForkerId, aForkerNode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.forker.IForker#registerListener(org.psem2m
     * .isolates.services.forker.IForkerEventListener)
     */
    @Override
    public boolean registerListener(final IForkerEventListener aListener) {

        return pListeners.add(aListener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        final List<String> toDelete = new ArrayList<String>();

        while (pThreadRunning) {

            synchronized (pForkersLST) {
                for (final Entry<String, Long> entry : pForkersLST.entrySet()) {
                    // First loop to detect forkers to delete
                    final String forkerId = entry.getKey();
                    final long lastSeen = entry.getValue();

                    if (System.currentTimeMillis() - lastSeen > FORKER_TTL) {
                        // TTL reached
                        toDelete.add(forkerId);
                    }
                }

                for (final String forkerId : toDelete) {
                    // Unregister the forker
                    pLogger.logInfo(this, "run", "Forker=", forkerId,
                            "reached TTL");
                    pForkersLST.remove(forkerId);
                    unregisterForker(forkerId);
                }
            }

            toDelete.clear();

            try {
                Thread.sleep(1000);

            } catch (final InterruptedException e) {
                // Interrupted
                return;
            }
        }
    }

    /**
     * Sends a CONTACT signal to the given access point.
     * 
     * @param aHost
     *            A host address
     * @param aPort
     *            A signal access port
     */
    protected void sendContactSignal(final String aHost, final int aPort) {

        try {
            // Set up the signal content
            final Map<String, Object> content = new HashMap<String, Object>();

            // Local access port
            content.put("port", pReceiver.getAccessInfo().getPort());

            // Send the signal
            final Object[] results = pSender.sendTo(
                    ISignalDirectoryConstants.SIGNAL_CONTACT, content, aHost,
                    aPort);
            if (results == null) {
                // No response...
                pLogger.logWarn(this, "sendContactSignal",
                        "No response from host=", aHost, "port=", aPort);
            }

        } catch (final Exception e) {
            // Log...
            pLogger.logWarn(this, "sendContactSignal",
                    "Error sending the contact signal to host=", aHost,
                    "port=", aPort, ":", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.forker.IForker#setPlatformStopping()
     */
    @Override
    public void setPlatformStopping() {

        final String[] forkers = pDirectory.getAllIsolates(
                IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER, true);
        if (forkers == null) {
            return;
        }

        for (final String forker : forkers) {
            pSender.fire(IForkerOrders.SIGNAL_PLATFORM_STOPPING, null, forker);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.forker.IForker#startIsolate(java.util.Map)
     */
    @Override
    public int startIsolate(final Map<String, Object> aIsolateConfiguration) {

        String hostName = (String) aIsolateConfiguration.get("node");
        if (hostName == null || hostName.isEmpty()) {
            hostName = getNodeName();
        }

        final String forker = getForkerForNode(hostName);
        if (forker == null) {
            pLogger.logSevere(this, "startIsolate",
                    "No forker known for host=", hostName);
            return -20;
        }

        // Get the started isolate ID
        final String isolateId = (String) aIsolateConfiguration.get("id");

        // Prepare the order
        final Map<String, Object> order = new HashMap<String, Object>();
        order.put("isolateDescr", aIsolateConfiguration);

        // Associate the isolate to the found forker
        pIsolateForkers.put(isolateId, forker);

        return getForkerIntResult(forker, IForkerOrders.SIGNAL_START_ISOLATE,
                order, 1000);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.forker.IForker#stopIsolate(java.lang.String)
     */
    @Override
    public void stopIsolate(final String aIsolateId) {

        final String forker = pIsolateForkers.get(aIsolateId);
        if (forker == null) {
            pLogger.logSevere(this, "stopIsolate",
                    "No forker known for isolate ID=", aIsolateId);
            return;
        }

        // Prepare the order
        final Map<String, Object> order = new HashMap<String, Object>();
        order.put("isolateId", aIsolateId);

        // Send the order (don't care about the result)
        pSender.fire(IForkerOrders.SIGNAL_STOP_ISOLATE, order, forker);
    }

    /**
     * Unregisters the given forker and notifies listeners on success
     * 
     * @param aForkerId
     *            The ID of the forker to unregister
     */
    protected synchronized void unregisterForker(final String aForkerId) {

        // Get the forker host
        final String forkerNode = pDirectory.getIsolateNode(aForkerId);

        pLogger.logDebug(this, "unregisterForker", "Unregistering forker=",
                aForkerId, "for node=", forkerNode);

        if (pDirectory.unregisterIsolate(aForkerId)) {
            // Forker has been removed
            fireForkerEvent(EForkerEventType.UNREGISTERED, aForkerId,
                    forkerNode);
        }

        // Clean up corresponding isolates
        final String[] isolates = pDirectory.getIsolatesOnNode(forkerNode);
        if (isolates != null) {

            for (final String isolate : isolates) {
                // Forget this isolate
                pIsolateForkers.remove(isolate);
                pDirectory.unregisterIsolate(isolate);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.forker.IForker#registerListener(org.psem2m
     * .isolates.services.forker.IForkerEventListener)
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.forker.IForker#unregisterListener(org.psem2m
     * .isolates.services.forker.IForkerEventListener)
     */
    @Override
    public boolean unregisterListener(final IForkerEventListener aListener) {

        return pListeners.remove(aListener);
    }

    /** Component validation */
    @Validate
    public void validate() {

        // Start the UDP heart beat listener
        // Get the multicast group and port
        final InetAddress group;
        final int port = pConfig.getApplication().getMulticastPort();
        try {
            group = InetAddress.getByName(pConfig.getApplication()
                    .getMulticastGroup());

        } catch (final UnknownHostException ex) {
            pLogger.logSevere(this, "validate",
                    "Couldn't read the multicast group=", pConfig
                            .getApplication().getMulticastGroup());
            return;
        }

        // Create the multicast receiver
        try {
            pMulticast = new MulticastReceiver(this, group, port);
            pMulticast.start();

        } catch (final IOException ex) {
            try {
                // Clean up
                pMulticast.stop();

            } catch (final IOException e) {
                // Ignore
            }

            pMulticast = null;
            pLogger.logSevere(this, "validate",
                    "Couldn't start the multicast receiver for group=", group,
                    ex);
            return;
        }

        // Start the TTL thread
        pThreadRunning = true;
        pThread = new Thread(this);
        pThread.start();

        pLogger.logInfo(this, "validate", "Forker Aggregator validated");
    }
}
