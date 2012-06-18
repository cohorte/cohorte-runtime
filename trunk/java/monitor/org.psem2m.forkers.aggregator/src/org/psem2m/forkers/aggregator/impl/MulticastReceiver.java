/**
 * File:   MulticastReceiver.java
 * Author: Thomas Calmant
 * Date:   18 juin 2012
 */
package org.psem2m.forkers.aggregator.impl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * A multicast receiver
 * 
 * @author Thomas Calmant
 */
public class MulticastReceiver {

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

    /** The multicast group */
    private final InetAddress pAddress;

    /** The listeners invocation thread "pool" */
    private ExecutorService pExecutor;

    /** The packet listener */
    private final IPacketListener pListener;

    /** The multicast port */
    private final int pPort;

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
    public MulticastReceiver(final IPacketListener aListener,
            final InetAddress aAddress, final int aPort) {

        pListener = aListener;
        pAddress = aAddress;
        pPort = aPort;
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

                // Call the listener in a separate thread
                if (pListener != null) {
                    pExecutor.submit(new Runnable() {

                        @Override
                        public void run() {

                            pListener.handlePacket(packet);
                        }
                    });
                }

            } catch (final Exception ex) {
                // Call the listener...
                if (pListener != null && !pListener.handleError(ex)) {
                    // Listener told us to stop
                    break;
                }
            }
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

        if (pSocket != null) {
            return false;
        }

        pSocket = setupMulticast(pAddress, pPort);
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

        // Wait for it a little
        try {
            pThread.join(500);

        } catch (final InterruptedException e) {
            // Ignore
        }

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
        pThread.interrupt();
    }
}
