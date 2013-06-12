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
import org.cohorte.shell.IRemoteShell;
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

    /** Signal to request the isolate PID */
    private static final String SIGNAL_GET_PID = ShellAgent.SIGNALS_PREFIX
            + "/get_pid";

    /** Signal to request shell accesses */
    private static final String SIGNAL_GET_SHELLS = ShellAgent.SIGNALS_PREFIX
            + "/get_shells";

    /** Filter to match agent signals */
    private static final String SIGNALS_MATCH_ALL = ShellAgent.SIGNALS_PREFIX
            + "/*";

    /** Common prefix to agent signals */
    private static final String SIGNALS_PREFIX = "/cohorte/shell/agent";

    /** The Python bridge */
    @Requires
    private IPyBridge pBridge;

    /** The signal receiver */
    @Requires
    private ISignalReceiver pReceiver;

    /** The remote shell */
    @Requires(optional = true, nullable = true)
    private IRemoteShell pRemoteShell;

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
     * org.psem2m.signals.ISignalListener#handleReceivedSignal(java.lang.String,
     * org.psem2m.signals.ISignalData)
     */
    @Override
    public Object handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        if (SIGNAL_GET_SHELLS.equals(aSignalName)) {
            // Remote shells ports
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

        } else if (SIGNAL_GET_PID.equals(aSignalName)) {
            // Isolate Process ID
            final Map<String, Integer> result = new HashMap<String, Integer>();
            result.put("pid", pBridge.getPid());

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
        pReceiver.unregisterListener(SIGNALS_MATCH_ALL, this);
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Register to the signals
        pReceiver.registerListener(SIGNALS_MATCH_ALL, this);
    }
}
