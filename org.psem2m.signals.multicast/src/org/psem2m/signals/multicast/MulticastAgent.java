/**
 * File:   MulticastAgent.java
 * Author: Thomas Calmant
 * Date:   14 juin 2012
 */
package org.psem2m.signals.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.signals.HostAccess;
import org.psem2m.signals.ISignalBroadcaster;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalDirectory;
import org.psem2m.signals.ISignalListener;
import org.psem2m.signals.ISignalReceiver;

/**
 * Implementation of the multicast agent : sends a packet when validated and
 * completes the signals directory when it receives a packet.
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-multicast-agent-factory", publicFactory = false)
@Instantiate(name = "psem2m-multicast-agent")
public class MulticastAgent implements ISignalListener {

    /**
     * Leaves the group and closes the multicast socket.
     * 
     * @param aSocket
     *            A multicast socket
     * @param aAddress
     *            The multicast address
     * @throws IOException
     *             Error reading the address or leaving the group
     */
    public static void closeMulticast(final MulticastSocket aSocket,
            final InetAddress aAddress) throws IOException {

        if (aSocket == null) {
            // Nothing to do
            return;
        }

        try {
            // Leave the group
            aSocket.leaveGroup(aAddress);

        } finally {
            // Close the socket
            aSocket.close();
        }
    }

    /**
     * Sets up a multicast socket
     * 
     * @param aAddress
     *            The multicast address (group)
     * @param aPort
     *            The multicast port
     * @return The created socket
     * @throws IOException
     *             Something wrong occurred (bad address, bad port, ...)
     */
    public static MulticastSocket setupMulticast(final InetAddress aAddress,
            final int aPort) throws IOException {

        // Set up the socket
        final MulticastSocket socket = new MulticastSocket(aPort);
        socket.setLoopbackMode(true);
        socket.setReuseAddress(true);

        // Join the group
        try {
            socket.joinGroup(aAddress);

        } catch (final IOException ex) {
            // Be nice...
            socket.close();
            throw ex;
        }

        return socket;
    }

    /** The configuration service */
    @Requires
    private ISvcConfig pConfig;

    /** The signals directory */
    @Requires
    private ISignalDirectory pDirectory;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The multicast address */
    private InetAddress pMulticastGroup;

    /** The multicast port */
    private int pMulticastPort;

    /** The signals receiver */
    @Requires
    private ISignalReceiver pReceiver;

    /** The signals sender */
    @Requires
    private ISignalBroadcaster pSender;

    /** The socket */
    private MulticastSocket pSocket;

    /** The listening thread */
    private Thread pThread;

    /** The thread loop control */
    private boolean pThreadRun;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.signals.ISignalListener#handleReceivedSignal(java.lang.String,
     * org.psem2m.signals.ISignalData)
     */
    @Override
    public Object handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        if (IMulticastConstants.SIGNAL_CONFIRM_BEAT.equals(aSignalName)) {
            // Beat confirmation
            final String isolateId = pDirectory.getIsolateId();

            // Compute the groups
            // FIXME: this should be done in the configuration service
            final List<String> groups = new ArrayList<String>();

            // FIXME: this virtual group should be computed by the directory
            groups.add("ALL");

            if (isolateId
                    .startsWith(IPlatformProperties.SPECIAL_ISOLATE_ID_FORKER)) {
                // ... we are a forker (just in case)
                groups.add("FORKERS");

            } else {
                // ... we are an isolate (only the forkers are out of this
                // group)
                groups.add("ISOLATES");
            }

            if (isolateId
                    .startsWith(IPlatformProperties.SPECIAL_ISOLATE_ID_MONITOR)) {
                // ... we are a monitor
                groups.add("MONITORS");
            }

            // Return the result map
            final Map<String, Object> result = new HashMap<String, Object>();
            result.put("id", isolateId);
            result.put("node", pDirectory.getLocalNode());
            result.put("groups", groups);
            return result;
        }

        // Unknown service
        return null;
    }

    /**
     * Decodes a registration packet and registers the isolate if it responds to
     * a confirmation signal
     * 
     * @param aData
     *            Registration packet content
     * @param aHostAddress
     *            The sender address
     */
    protected void handleRegistration(final byte[] aData,
            final String aHostAddress) {

        // Make a little endian byte array reader
        final ByteBuffer buffer = ByteBuffer.wrap(aData, 1, aData.length - 1);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Get the port
        final int port = buffer.getShort();

        // Send the confirmation signal
        final Object[] results;
        try {
            results = pSender.sendTo(IMulticastConstants.SIGNAL_CONFIRM_BEAT,
                    null, aHostAddress, port);

        } catch (final Exception e) {
            // Error sending the signal, stop here
            pLogger.logWarn(this, "handleRegistration",
                    "Error sending a confirmation beat to host=", aHostAddress,
                    "port=", port, "ex=", e);
            return;
        }

        // We need at least one result
        if (results == null || results.length == 0) {
            pLogger.logWarn(
                    this,
                    "handleRegistration",
                    "Empty beat confirmation : not enough data to register the isolate. host=",
                    aHostAddress, "port=", port);
            return;
        }

        // Find the first map in the result
        Map<?, ?> map = null;
        for (final Object result : results) {
            if (result instanceof Map) {
                map = (Map<?, ?>) result;
                break;
            }
        }

        if (map == null) {
            pLogger.logWarn(this, "handleRegistration",
                    "No readable result for host=", aHostAddress, "port=", port);
            return;
        }

        // Get the response content
        final String isolate = (String) map.get("id");
        final String node = (String) map.get("node");
        final Object rawGroups = map.get("groups");

        if (isolate == null || isolate.isEmpty() || node == null
                || node.isEmpty() || rawGroups == null) {
            // Invalid content
            pLogger.logSevere(this, "handleRegistration",
                    "Invalid content for host=", aHostAddress, "port=", port,
                    "isolate=", isolate, "node=", node, "rawGroups=", rawGroups);
            return;
        }

        // Convert the raw groups
        final String[] groups;
        if (rawGroups instanceof Collection) {
            // Collection found, convert it
            groups = ((Collection<?>) rawGroups).toArray(new String[0]);

        } else if (rawGroups instanceof String[]) {
            // Strings array found, use it directly
            groups = (String[]) rawGroups;

        } else if (rawGroups instanceof Object[]) {
            // Objects array found, convert it
            final List<String> list = new ArrayList<String>();
            for (final Object object : (Object[]) rawGroups) {
                if (object != null) {
                    list.add((String) object);
                }
            }
            groups = list.toArray(new String[0]);

        } else {
            // Unknown type
            pLogger.logWarn(this, "handleRegistration",
                    "Unknown groups container type=", rawGroups.getClass()
                            .getName());
            return;
        }

        // Update the address associated to the node
        pDirectory.setNodeAddress(node, aHostAddress);

        // Register the isolate
        pDirectory.registerIsolate(isolate, node, port, groups);
    }

    /**
     * Component invalidated
     */
    public void invalidate() {

        // Unregister from signals receiver
        pReceiver
                .unregisterListener(IMulticastConstants.SIGNAL_MATCH_ALL, this);

        // Stop the thread...
        pThreadRun = false;

        // ... the hard way
        pThread.interrupt();

        // Close the socket
        try {
            closeMulticast(pSocket, pMulticastGroup);

        } catch (final IOException e) {
            pLogger.logSevere(this, "invalidate",
                    "Error closing the multicast socket, ex=", e);
        }
    }

    /**
     * Waits for packets on the multicast socket
     */
    protected void receivePackets() {

        // Set up the buffer
        final byte[] buffer = new byte[1500];
        final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (pThreadRun) {

            // Clear the buffer
            Arrays.fill(buffer, (byte) 0);

            try {
                // Wait for a packet (blocking)
                pSocket.receive(packet);

                // Get the content
                final byte[] data = packet.getData();

                pLogger.logInfo(this, "receivePackets", "Got packet, data=",
                        Arrays.toString(data));

                switch (data[0]) {
                case IMulticastConstants.PACKET_REGISTER:
                    // Register the isolate in another thread
                    new Thread(new Runnable() {

                        @Override
                        public void run() {

                            handleRegistration(data, packet.getAddress()
                                    .getHostAddress());
                        }
                    }).start();
                    break;
                }

            } catch (final IOException ex) {
                // Just log
                pLogger.logWarn(this, "receivePackets",
                        "Error receiving a packet, ex=", ex);
            }
        }
    }

    /**
     * Sends the beat
     * 
     * @throws IOException
     *             Error sending the beat
     */
    protected void sendBeat() throws IOException {

        // Get the access port
        final HostAccess access = pReceiver.getAccessInfo();
        if (access == null) {
            pLogger.logSevere(this, "sendBeat",
                    "This isolate is not accessible");
            return;
        }

        // Prepare the content : 3 bytes (type, port)
        final ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // ... packet type
        buffer.put(IMulticastConstants.PACKET_REGISTER);

        // ... access port
        buffer.putShort((short) access.getPort());

        // Prepare the packet
        buffer.position(0);
        final byte[] content = new byte[buffer.remaining()];
        buffer.get(content);

        final DatagramPacket packet = new DatagramPacket(content,
                content.length, pMulticastGroup, pMulticastPort);

        // Send it
        pSocket.send(packet);

        pLogger.logDebug(this, "sendBeat", "Sent beat=",
                Arrays.toString(content), "to=", pMulticastGroup, "port=",
                pMulticastPort);
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Register to the signals
        pReceiver.registerListener(IMulticastConstants.SIGNAL_MATCH_ALL, this);

        // Get the multicast group and port
        try {
            pMulticastGroup = InetAddress.getByName(pConfig.getApplication()
                    .getMulticast());

            pMulticastPort = IMulticastConstants.PSEM2M_MULTICAST_PORT;

        } catch (final UnknownHostException ex) {
            pLogger.logSevere(this, "validate",
                    "Couldn't read the multicast group=", pConfig
                            .getApplication().getMulticast());
            return;
        }

        try {
            // Create the socket (can fail)
            pSocket = setupMulticast(pMulticastGroup, pMulticastPort);

            // Socket created, start the thread
            pThreadRun = true;
            pThread = new Thread(new Runnable() {

                @Override
                public void run() {

                    receivePackets();
                }
            }, "Multicast-Agent");
            pThread.start();

        } catch (final IOException e) {
            pLogger.logSevere(this, "validate",
                    "Couldn't start the multicast socket - group=",
                    pMulticastGroup, "ex=", e);
            return;
        }

        try {
            // Send the beat
            sendBeat();
            pLogger.logInfo(this, "validate", "Multicast agent ready");

        } catch (final IOException e) {
            pLogger.logSevere(this, "validate",
                    "Error sending the registration beat, ex=", e);
        }
    }
}
