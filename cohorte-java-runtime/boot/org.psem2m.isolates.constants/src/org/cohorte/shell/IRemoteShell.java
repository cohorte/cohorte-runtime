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
