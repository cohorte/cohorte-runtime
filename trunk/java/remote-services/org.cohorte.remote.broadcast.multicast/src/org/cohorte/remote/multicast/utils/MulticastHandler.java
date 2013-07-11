/**
 * File:   MulticastReceiver.java
 * Author: Thomas Calmant
 * Date:   18 juin 2012
 */
package org.cohorte.remote.multicast.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
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
    private final InetSocketAddress pAddress;

    /** The listeners invocation thread "pool" */
    private ExecutorService pExecutor;

    /** The packet listener */
    private final IPacketListener pListener;

    /** An associated log service */
    private LogService pLogger;

    /** The multicast socket */
    private MulticastSocket pSocket;

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
            final String aAddress, final int aPort) {

        pListener = aListener;
        pAddress = new InetSocketAddress(aAddress, aPort);
    }

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
    private void closeMulticast(final MulticastSocket aSocket,
            final SocketAddress aAddress) throws IOException {

        if (aSocket == null) {
            // Nothing to do
            return;
        }

        try {
            // Leave the group, on all interfaces
            for (final NetworkInterface itf : getMulticastInterfaces()) {
                aSocket.leaveGroup(aAddress, itf);
            }

        } finally {
            // Close the socket
            aSocket.close();
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
    protected void receivePackets() {

        while (pThreadRun) {

            // Set up the buffer
            final byte[] buffer = new byte[BUFFER_SIZE];

            // Use a new buffer each time, or it will be erased on next packet
            final DatagramPacket packet = new DatagramPacket(buffer,
                    buffer.length);

            try {
                // Wait for a packet (blocking)
                pSocket.receive(packet);

                // Call the listener in a separate thread
                if (pListener != null) {
                    pExecutor.submit(new Runnable() {

                        @Override
                        public void run() {

                            try {
                                pListener.handlePacket(packet);

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

        send(aData, pAddress.getAddress(), pAddress.getPort());
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

        // Prepare the packet
        final DatagramPacket packet = new DatagramPacket(aData, aData.length,
                aAddress, aPort);

        // Send it
        pSocket.send(packet);
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
    private MulticastSocket setupMulticast(final SocketAddress aAddress,
            final int aPort) throws IOException {

        // Set up the socket
        final MulticastSocket socket = new MulticastSocket(aPort);
        socket.setLoopbackMode(false);
        socket.setReuseAddress(true);

        try {
            // Join the group on all interfaces
            for (final NetworkInterface itf : getMulticastInterfaces()) {
                socket.joinGroup(aAddress, itf);
            }

        } catch (final SocketException ex) {
            // Be nice...
            socket.close();
            throw ex;
        }

        return socket;
    }

    /**
     * Creates the multicast socket and starts the listening thread
     * 
     * @return True if the socket was created, false if it was already opened
     * @throws IOException
     *             Error creating the socket
     */
    public boolean start() throws IOException {

        if (pSocket != null) {
            return false;
        }

        pSocket = setupMulticast(pAddress, pAddress.getPort());
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
        closeMulticast(pSocket, pAddress);
        pSocket = null;
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
