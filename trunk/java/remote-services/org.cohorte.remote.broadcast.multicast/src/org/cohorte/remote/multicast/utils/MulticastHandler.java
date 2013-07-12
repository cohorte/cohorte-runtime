/**
 * File:   MulticastReceiver.java
 * Author: Thomas Calmant
 * Date:   18 juin 2012
 */
package org.cohorte.remote.multicast.utils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ProtocolFamily;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.osgi.service.log.LogService;

/**
 * A multicast receiver
 * 
 * @author Thomas Calmant
 * 
 * @see <a
 *      href="http://atastypixel.com/blog/the-making-of-talkie-multi-interface-broadcasting-and-multicast/">The
 *      Making of Talkie: Multi-interface broadcasting and multicast</a>
 */
public class MulticastHandler {

    /** Reception buffer size */
    private static final int BUFFER_SIZE = 1500;

    /** The bundle name */
    private static final String BUNDLE_NAME = "org.cohorte.remote.multicast";

    /** The multicast group */
    private final InetAddress pAddress;

    /** Channel */
    private DatagramChannel pChannel;

    /** The listeners invocation thread "pool" */
    private ExecutorService pExecutor;

    /** Joined group on different interfaces */
    private final List<MembershipKey> pJoinedGroups = new LinkedList<>();

    /** The packet listener */
    private final IPacketListener pListener;

    /** An associated log service */
    private LogService pLogger;

    /** The multicast port */
    private final int pPort;

    /** The listening thread */
    private Thread pThread;

    /** The thread loop control */
    private boolean pThreadRun;

    /**
     * Sets up the receiver
     * 
     * @param aListener
     *            The multicast packets listener
     * @param aAddress
     *            A multicast group address
     * @param aPort
     *            A socket port
     * @throws IOException
     *             Error opening the socket
     */
    public MulticastHandler(final IPacketListener aListener,
            final InetAddress aAddress, final int aPort) {

        pListener = aListener;
        pAddress = aAddress;
        pPort = aPort;
    }

    /**
     * Leaves the group and closes the multicast socket.
     * 
     * @throws IOException
     *             Error reading the address or leaving the group
     */
    private void closeMulticast() throws IOException {

        if (pChannel == null) {
            // Nothing to do
            return;
        }

        try {
            // Leave the group, on all interfaces
            for (final MembershipKey key : pJoinedGroups) {
                key.drop();
            }

        } finally {
            // Close the socket
            pChannel.close();
        }
    }

    /**
     * Retrieves all network interfaces that supports multicast
     * 
     * @return All network interfaces that supports multicast
     * @throws SocketException
     *             An error occurred retrieving network interfaces
     */
    private NetworkInterface[] getMulticastInterfaces() throws SocketException {

        final List<NetworkInterface> multicastItfs = new ArrayList<NetworkInterface>();

        // Loop over all interfaces
        final Enumeration<NetworkInterface> itfEnum = NetworkInterface
                .getNetworkInterfaces();

        while (itfEnum.hasMoreElements()) {
            final NetworkInterface itf = itfEnum.nextElement();
            try {
                if (itf.supportsMulticast()) {
                    // Multicast is supported
                    multicastItfs.add(itf);
                }

            } catch (final SocketException ex) {
                log(LogService.LOG_WARNING,
                        "Error testing if an interface supports Multicast", ex);
            }
        }

        return multicastItfs
                .toArray(new NetworkInterface[multicastItfs.size()]);
    }

    /**
     * Logs an entry if a logger is present, else prints on the error output
     * 
     * @param aLevel
     *            Log level
     * @param aMessage
     *            Log entry
     */
    private void log(final int aLevel, final String aMessage) {

        log(aLevel, aMessage, null);
    }

    /**
     * Logs an entry if a logger is present, else prints on the error output
     * 
     * @param aLevel
     *            Log level
     * @param aMessage
     *            Log entry
     * @param aThrowable
     *            Associated error
     */
    private void log(final int aLevel, final String aMessage,
            final Throwable aThrowable) {

        if (pLogger != null) {
            // Use the log service
            pLogger.log(aLevel, aMessage, aThrowable);

        } else {
            // Use a Java logger

            // Convert the level
            Level level;
            switch (aLevel) {
            case LogService.LOG_ERROR:
                level = Level.SEVERE;
                break;

            case LogService.LOG_WARNING:
                level = Level.WARNING;
                break;

            case LogService.LOG_INFO:
                level = Level.INFO;
                break;

            case LogService.LOG_DEBUG:
            default:
                level = Level.FINE;
                break;
            }

            // Log a record
            final LogRecord record = new LogRecord(level, aMessage);
            record.setThrown(aThrowable);

            Logger.getLogger(BUNDLE_NAME).log(record);
        }
    }

    /**
     * Waits for packets on the multicast socket
     */
    private void receivePackets() {

        while (pThreadRun) {

            // Set up the buffer
            final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            try {
                // Reset the buffer
                buffer.clear();

                // Wait for a packet (blocking)
                final SocketAddress sender = pChannel.receive(buffer);
                if (!(sender instanceof InetSocketAddress)) {
                    // Unhandled kind of address, try next time
                    log(LogService.LOG_WARNING,
                            "Unhandled kind of socket address: "
                                    + sender.getClass().getName());
                    continue;
                }

                // Get the sender address
                final InetSocketAddress senderAddress = (InetSocketAddress) sender;

                // Extract the content of the packet
                final byte[] content = new byte[buffer.position()];
                buffer.get(content);

                // Call the listener in a separate thread
                if (pListener != null) {
                    pExecutor.submit(new Runnable() {

                        @Override
                        public void run() {

                            try {
                                pListener.handlePacket(senderAddress, content);

                            } catch (final Exception e) {
                                // Let the listener handle its own exception
                                pListener.handleError(e);
                            }
                        }
                    });
                }

            } catch (final Exception ex) {
                // Call the listener only if the thread is still running...
                if (pThreadRun && pListener != null
                        && !pListener.handleError(ex)) {
                    // Listener told us to stop
                    break;
                }
            }
        }
    }

    /**
     * Sends the given packet to the multicast group
     * 
     * @param aData
     *            Data to be sent
     * @throws IOException
     *             Error sending the packet
     */
    public void send(final byte[] aData) throws IOException {

        send(aData, pAddress, pPort);
    }

    /**
     * Sends the given packet to the given address
     * 
     * @param aData
     *            Data to be sent
     * @param aAddress
     *            Target address
     * @param aPort
     *            Target port
     * @throws IOException
     *             Error sending the packet
     */
    public void send(final byte[] aData, final InetAddress aAddress,
            final int aPort) throws IOException {

        // Send the datagram
        pChannel.send(ByteBuffer.wrap(aData), new InetSocketAddress(aAddress,
                aPort));
    }

    /**
     * Sets the log service to use
     * 
     * @param aLogService
     *            A log service instance, or null
     */
    public void setLogger(final LogService aLogService) {

        pLogger = aLogService;
    }

    /**
     * Sets up the multicast channel
     * 
     * @throws IOException
     *             Something wrong occurred (bad address, bad port, ...)
     */
    private void setupMulticast() throws IOException {

        // Compute the address family
        final ProtocolFamily family;
        if (pAddress instanceof Inet4Address) {
            // IPv4
            family = StandardProtocolFamily.INET;

        } else if (pAddress instanceof Inet6Address) {
            // IPv6
            family = StandardProtocolFamily.INET6;

        } else {
            // Unknown
            throw new SocketException("Unknown multicast group family");
        }

        // Create the UDP channel
        pChannel = DatagramChannel.open(family);
        pChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        pChannel.bind(new InetSocketAddress(pPort));

        try {
            // Join the group on all interfaces
            for (final NetworkInterface itf : getMulticastInterfaces()) {
                pJoinedGroups.add(pChannel.join(pAddress, itf));
            }

        } catch (final SocketException ex) {
            // Be nice...
            pChannel.close();
            throw ex;
        }
    }

    /**
     * Creates the multicast socket and starts the listening thread
     * 
     * @return True if the socket was created, false if it was already opened
     * @throws IOException
     *             Error creating the socket
     */
    public boolean start() throws IOException {

        if (pChannel != null) {
            return false;
        }

        setupMulticast();
        startThread();
        return true;
    }

    /**
     * Starts the listening thread
     */
    private void startThread() {

        if (pThread != null) {
            stopThread();
        }

        // Prepare the thread object
        pThread = new Thread(new Runnable() {

            @Override
            public void run() {

                receivePackets();
            }
        });

        // Start the listener notifier
        pExecutor = Executors.newFixedThreadPool(1);

        // Start the thread
        pThreadRun = true;
        pThread.start();
    }

    /**
     * Stops the multicast receiver
     * 
     * @throws IOException
     *             Error closing the socket
     */
    public void stop() throws IOException {

        // Stop the thread
        stopThread();

        // Stop the notifying thread
        pExecutor.shutdownNow();
        pExecutor = null;

        // Close the socket
        closeMulticast();
        pChannel = null;
    }

    /**
     * Stops the listening thread
     */
    private void stopThread() {

        pThreadRun = false;
        if (pThread == null) {
            return;
        }

        pThread.interrupt();

        // Wait for it a little
        try {
            pThread.join(500);

        } catch (final InterruptedException e) {
            // Ignore
        }

        // Delete the reference
        pThread = null;
    }
}
