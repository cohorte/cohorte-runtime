/**
 * File:   ShellServer.java
 * Author: Thomas Calmant
 * Date:   31 mai 2013
 */
package org.cohorte.remote.shell;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * The shell server: accepts clients and starts shell session threads
 * 
 * @author Thomas Calmant
 */
public class Server implements Runnable {

    /** The client acceptance thread */
    private Thread pListeningThread;

    /** The server socket */
    private ServerSocket pServer;

    /** The parent shell service */
    private final RemoteShellService pShellService;

    /** The stop flag */
    private boolean pStop;

    /**
     * Sets up the server members
     * 
     * @param aShellService
     *            The parent shell service
     */
    public Server(final RemoteShellService aShellService) {

        pShellService = aShellService;
    }

    /**
     * Closes the server socket
     * 
     * @throws IOException
     *             Error closing the socket
     */
    public void close() throws IOException {

        // Set up the stop flag
        pStop = true;

        if (pServer != null) {
            try {
                pServer.close();

            } finally {
                pServer = null;
            }
        }

        // Wait for the listening thread
        try {
            pListeningThread.join();

        } catch (final InterruptedException ex) {
            // Ignore
        }

        pListeningThread = null;
    }

    /**
     * Returns the address the server socket is bound to (generally "::" or
     * "0.0.0.0"). Returns null if the server is down.
     * 
     * @return The server binding address or null.
     */
    public String getAddress() {

        if (pServer == null) {
            return null;
        }

        final InetAddress boundAddress = pServer.getInetAddress();
        if (boundAddress == null) {
            return null;
        }

        return boundAddress.toString();
    }

    /**
     * Returns the port the server is listening to. Returns -1 if the server is
     * down.
     * 
     * @return The listening port or -1
     */
    public int getPort() {

        if (pServer == null) {
            return -1;
        }

        return pServer.getLocalPort();
    }

    /**
     * Open the server, listening the given address and port
     * 
     * @param aAddress
     *            A binding address (can be null)
     * @param aPort
     *            A listening port (can be 0)
     * @throws IOException
     *             Error opening the server servlet
     */
    public void open(final String aAddress, final int aPort) throws IOException {

        if (pServer != null) {
            // Refuse to re-open a connection
            throw new IOException("Server is already running");
        }

        // Compute the binding address
        final InetAddress bindAddr;
        if (aAddress == null) {
            // Accept all connections
            bindAddr = null;

        } else {
            // Bind to a specific address
            bindAddr = InetAddress.getByName(aAddress);
        }

        // Reset the stop flag
        pStop = false;

        // Create the server
        pServer = new ServerSocket(aPort, 1, bindAddr);

        // Wait for clients in another thread
        pListeningThread = new Thread(this, "cohorte.remote.shell.acceptor");
        pListeningThread.start();
    }

    /**
     * The server client acceptance loop
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        try {
            while (!pStop) {
                try {
                    final Socket client = pServer.accept();

                    // Start a new client thread
                    final Thread clientThread = new Thread(
                            new ShellClientHandler(pShellService, client));
                    clientThread.setName("cohorte.remote.shell="
                            + client.getRemoteSocketAddress());
                    clientThread.start();

                } catch (final SocketException ex) {
                    // Log the exception
                    pShellService.error("acceptLoop",
                            "Error accepting client:", ex);

                } catch (final SocketTimeoutException ex) {
                    // Ignore
                }
            }

        } catch (final IOException ex) {
            // Log the exception
            pStop = true;
            pShellService.error("acceptLoop", "Error waiting for clients:", ex);

            try {
                // Kill the server
                close();

            } catch (final IOException ex2) {
                // Ignore
            }
        }
    }

    /**
     * Sets the socket timeout. Does nothing if the server has not been opened
     * 
     * @param aTimeout
     *            The timeout to set
     * @throws SocketException
     *             Error setting up the timeout
     */
    public void setSocketTimeout(final int aTimeout) throws SocketException {

        if (pServer != null) {
            pServer.setSoTimeout(aTimeout);
        }
    }
}
