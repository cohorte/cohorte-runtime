/**
 * File:   RemoteShellService.java
 * Author: Thomas Calmant
 * Date:   31 mai 2013
 */
package org.cohorte.remote.shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.cohorte.shell.IRemoteShell;
import org.osgi.framework.BundleContext;
import org.psem2m.isolates.base.IIsolateLoggerSvc;

/**
 * The remote shell service implementation
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-remote-shell-factory")
@Provides(specifications = IRemoteShell.class)
public class RemoteShellService implements IRemoteShell {

    /** Server binding address */
    @ServiceProperty(name = IRemoteShell.SHELL_ADDRESS, value = "127.0.0.1")
    private String pAddress;

    /** Gogo shell command processor */
    @Requires
    private CommandProcessor pCommandProcessor;

    /** The bundle context */
    private final BundleContext pContext;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Server listening port */
    @ServiceProperty(name = IRemoteShell.SHELL_PORT, value = "0")
    private int pPort;

    /** The remote shell server */
    private Server pServer;

    /**
     * Sets up members
     * 
     * @param aContext
     *            The bundle context
     */
    public RemoteShellService(final BundleContext aContext) {

        pContext = aContext;
    }

    /**
     * Creates a Gogo shell session
     * 
     * @param aStdin
     *            Shell input
     * @param aStdout
     *            Shell standard output
     * @param aStderr
     *            Shell error output
     * @return
     */
    public CommandSession createGogoSession(final InputStream aStdin,
            final PrintStream aStdout, final PrintStream aStderr) {

        return pCommandProcessor.createSession(aStdin, aStdout, aStderr);
    }

    /**
     * Logs an error message
     * 
     * @param aWhat
     *            The current task
     * @param aInfos
     *            The log line content
     */
    void error(final String aWhat, final Object... aInfos) {

        pLogger.logSevere(this, aWhat, aInfos);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.remote.shell.IRemoteShell#getAddress()
     */
    @Override
    public String getAddress() {

        if (pServer == null) {
            return null;
        }

        return pServer.getAddress();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.remote.shell.IRemoteShell#getPort()
     */
    @Override
    public int getPort() {

        if (pServer == null) {
            return -1;
        }

        return pServer.getPort();
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        stopServer();
    }

    /**
     * Stops the server, if not null
     */
    private void stopServer() {

        if (pServer != null) {
            try {
                pServer.close();

            } catch (final IOException ex) {
                error("invalidate", "Error stopping the server:", ex);
            }

            pServer = null;
        }
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Get the port number from the framework properties if necessary
        if (pPort <= 0) {
            try {
                pPort = Integer.parseInt(pContext.getProperty(SHELL_PORT));
                if (pPort < 0) {
                    // Use a random port if not given
                    pPort = 0;
                }

            } catch (final NumberFormatException ex) {
                // Use a random port if the given one is invalid
                pPort = 0;
            }
        }

        // Start the server
        pServer = new Server(this);
        try {
            pServer.open(pAddress, pPort);

            // Update the port property
            pPort = pServer.getPort();

            pLogger.logInfo(this, "validate", "Remote Shell bound to port=",
                    pPort);

        } catch (final IOException ex) {
            error("validate", "Error opening the server:", ex);
            stopServer();
        }
    }

    /**
     * Logs an warning message
     * 
     * @param aWhat
     *            The current task
     * @param aInfos
     *            The log line content
     */
    void warning(final String aWhat, final Object... aInfos) {

        pLogger.logWarn(this, aWhat, aInfos);
    }
}
