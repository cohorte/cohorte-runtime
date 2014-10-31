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

package org.cohorte.pyboot.shell.agent;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.cohorte.herald.HeraldException;
import org.cohorte.herald.IConstants;
import org.cohorte.herald.IHerald;
import org.cohorte.herald.IMessageListener;
import org.cohorte.herald.MessageReceived;
import org.cohorte.pyboot.api.IPyBridge;
import org.cohorte.shell.IRemoteShell;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

/**
 * The shell agent, to answer Pelix shell agent commands
 *
 * @author Thomas Calmant
 */
@Component(name = "cohorte-shell-agent-factory")
@Provides(specifications = IMessageListener.class)
@Instantiate(name = "cohorte-shell-agent")
public class ShellAgent implements IMessageListener {

    /** HTTP service port property */
    private static final String HTTP_SERVICE_PORT = "org.osgi.service.http.port";

    /** HTTPService dependency ID */
    private static final String IPOJO_ID_HTTP = "http.service";

    /** Common prefix to agent messages */
    private static final String MESSAGES_PREFIX = "cohorte/shell/agent";

    /** Signal to request HTTP ports */
    private static final String MSG_GET_HTTP = ShellAgent.MESSAGES_PREFIX
            + "/get_http";

    /** Signal to request the isolate PID */
    private static final String MSG_GET_PID = ShellAgent.MESSAGES_PREFIX
            + "/get_pid";

    /** Signal to request shell accesses */
    private static final String MSG_GET_SHELLS = ShellAgent.MESSAGES_PREFIX
            + "/get_shells";

    /** Filter to match agent messages */
    private static final String MSG_MATCH_ALL = ShellAgent.MESSAGES_PREFIX
            + "/*";

    /** The Python bridge */
    @Requires
    private IPyBridge pBridge;

    /** HTTP service port */
    private int pHttpPort;

    /** The logger */
    @Requires(optional = true)
    private LogService pLogger;

    /** Message filters */
    @ServiceProperty(name = IConstants.PROP_FILTERS, value = MSG_MATCH_ALL)
    private String pMessageFilter;

    /** The remote shell */
    @Requires(optional = true, nullable = true)
    private IRemoteShell pRemoteShell;

    /**
     * HTTP service ready
     *
     * @param aHttpService
     *            The bound service
     * @param aServiceProperties
     *            The HTTP service properties
     */
    @Bind(id = IPOJO_ID_HTTP, optional = true)
    private void bindHttpService(final HttpService aHttpService,
            final Map<?, ?> aServiceProperties) {

        final Object rawPort = aServiceProperties.get(HTTP_SERVICE_PORT);

        if (rawPort instanceof Number) {
            // Get the integer
            pHttpPort = ((Number) rawPort).intValue();

        } else if (rawPort instanceof CharSequence) {
            // Parse the string
            pHttpPort = Integer.parseInt(rawPort.toString());

        } else {
            // Unknown port type
            pLogger.log(LogService.LOG_WARNING, "Couldn't read access port="
                    + rawPort);
            pHttpPort = -1;
        }

        pLogger.log(LogService.LOG_INFO, "HTTP Receiver bound to port="
                + pHttpPort);
    }

    /**
     * Returns the port used by the OSGi remote shell, or -1
     *
     * @return the port used by the OSGi remote shell, or -1
     */
    private int getOsgiRemoteShellPort() {

        if (pRemoteShell == null) {
            return -1;
        }

        return pRemoteShell.getPort();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.herald.IMessageListener#heraldMessage(org.cohorte.herald.
     * IHerald, org.cohorte.herald.MessageReceived)
     */
    @Override
    public void heraldMessage(final IHerald aHerald,
            final MessageReceived aMessage) {

        try {
            switch (aMessage.getSubject()) {
            case MSG_GET_PID: {
                // Isolate Process ID
                final Map<String, Integer> result = new HashMap<>();
                result.put("pid", pBridge.getPid());
                aHerald.reply(aMessage, result);
                break;
            }

            case MSG_GET_SHELLS: {
                // Remote shells ports
                final Map<String, Integer> result = new HashMap<>();

                // Get the Pelix shell port
                final int pelixPort = pBridge.getRemoteShellPort();
                if (pelixPort > 0) {
                    result.put("pelix", pelixPort);
                }

                // Get the OSGi shell port
                final int osgiPort = getOsgiRemoteShellPort();
                if (osgiPort > 0) {
                    result.put("osgi", osgiPort);
                }

                aHerald.reply(aMessage, result);
                break;
            }

            case MSG_GET_HTTP: {
                // Make the result map
                final Map<String, Integer> result = new HashMap<>();
                result.put("http.port", pHttpPort);
                aHerald.reply(aMessage, result);
                break;
            }

            default:
                // Unknown message
                pLogger.log(LogService.LOG_DEBUG, "Unhandled message: "
                        + aMessage.getSubject());
                break;
            }

        } catch (final HeraldException ex) {
            pLogger.log(LogService.LOG_ERROR,
                    "Error replying to a shell message: " + ex, ex);
        }
    }

    /**
     * HTTP service gone
     */
    @Unbind(id = IPOJO_ID_HTTP)
    private void unbindHttpService() {

        // Forget the port
        pHttpPort = -1;
    }
}
