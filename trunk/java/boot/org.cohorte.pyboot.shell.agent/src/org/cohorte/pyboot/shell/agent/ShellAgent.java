/**
 * File:   ShellAgent.java
 * Author: Thomas Calmant
 * Date:   31 mai 2013
 */
package org.cohorte.pyboot.shell.agent;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.pyboot.api.IPyBridge;
import org.osgi.framework.BundleContext;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalListener;
import org.psem2m.signals.ISignalReceiver;

/**
 * The shell agent, to answer Pelix shell agent commands
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-shell-agent-factory")
@Instantiate(name = "cohorte-shell-agent")
public class ShellAgent implements ISignalListener {

    /** OSGi shell port framework property */
    private static final String OSGI_SHELL_PORT = "osgi.shell.telnet.port";

    /** Signal to request shell accesses */
    private static final String SIGNAL_GET_SHELLS = "/cohorte/shell/agent/get_shells";

    /** The Python bridge */
    @Requires
    private IPyBridge pBridge;

    /** Bundle context */
    private final BundleContext pContext;

    /** The signal receiver */
    @Requires
    private ISignalReceiver pReceiver;

    /**
     * Injection of the bundle context
     * 
     * @param aContext
     *            The bundle context
     */
    private ShellAgent(final BundleContext aContext) {

        pContext = aContext;
    }

    /**
     * Returns the port used by the OSGi remote shell, or -1
     * 
     * @return the port used by the OSGi remote shell, or -1
     */
    private int getOsgiRemoteShellPort() {

        final String strValue = pContext.getProperty(OSGI_SHELL_PORT);
        if (strValue == null) {
            // No value
            return -1;
        }

        try {
            // Get the integer value
            return Integer.parseInt(strValue);

        } catch (final NumberFormatException ex) {
            // Invalid string
            return -1;
        }
    }

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

        if (SIGNAL_GET_SHELLS.equals(aSignalName)) {
            final Map<String, Integer> result = new HashMap<String, Integer>();

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

            return result;
        }

        return null;
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        // Unregister from signals
        pReceiver.unregisterListener(SIGNAL_GET_SHELLS, this);
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Register to the signals
        pReceiver.registerListener(SIGNAL_GET_SHELLS, this);
    }
}
