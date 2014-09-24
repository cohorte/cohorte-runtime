/**
 * File:   IRemoteShell.java
 * Author: Thomas Calmant
 * Date:   31 mai 2013
 */
package org.cohorte.shell;

/**
 * Specification of a remote shell service, using the same constants as the
 * Felix Remote Shell.
 * 
 * @author Thomas Calmant
 */
public interface IRemoteShell {

    /** IP Address on which the remote shell is accessible */
    String SHELL_ADDRESS = "osgi.shell.telnet.ip";

    /** Port on which the remote shell is accessible. */
    String SHELL_PORT = "osgi.shell.telnet.port";

    /**
     * Returns the address the server socket is bound to (generally "::" or
     * "0.0.0.0"). Returns null if the server is down.
     * 
     * @return The server binding address or null.
     */
    String getAddress();

    /**
     * Returns the port the server is listening to. Returns -1 if the server is
     * down.
     * 
     * @return The listening port (or -1)
     */
    int getPort();
}
