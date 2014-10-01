/**
 * Copyright 2014 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cohorte.remote.shell;

import java.io.IOException;
import java.net.Socket;

import org.apache.felix.service.command.CommandSession;

/**
 * Client shell thread
 *
 * @author Thomas Calmant
 */
public class ShellClientHandler implements Runnable {

    /** The client socket */
    private final Socket pClient;

    /** The client output */
    private volatile TerminalPrintStream pOutput;

    /** The remote shell service that owns this session */
    private RemoteShellService pShellService;

    /**
     * Sets up the client session
     *
     * @param aOwner
     *            The remote shell service that owns this session
     * @param aClient
     *            The client socket
     */
    public ShellClientHandler(final RemoteShellService aOwner,
            final Socket aClient) {

        pShellService = aOwner;
        pClient = aClient;
    }

    /**
     * Print an exit message and closes the socket
     */
    private void exit() {

        // Farewell message
        pOutput.println("Good Bye!");
        pOutput.close();

        try {
            pClient.close();

        } catch (final IOException ex) {
            pShellService.error("Shell::exit()", ex);
        }

        // Clean up
        pShellService = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        try {
            // Create the shell output
            pOutput = new TerminalPrintStream(pShellService,
                    pClient.getOutputStream());

            // Create a command session
            final CommandSession session = pShellService.createGogoSession(
                    pClient.getInputStream(), pOutput, pOutput);

            // Run it
            runGogoShell(session);

        } catch (final IOException ex) {
            // Something went wrong
            pShellService.error("ShellClientHandler::run()", ex);

        } finally {
            // Clean up
            exit();
        }
    }

    /**
     * Runs a Gogo shell session
     *
     * @param aSession
     *            A Gogo shell session
     */
    private void runGogoShell(final CommandSession aSession) {

        try {
            aSession.execute("gosh --login --noshutdown");

        } catch (final Exception ex) {
            // Log the exception
            pShellService.error("ShellClientHandler::runGogoShell()", ex);

        } finally {
            // Close the session in the end
            aSession.close();
        }
    }
}
