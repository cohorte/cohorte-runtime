/**
 * File:   AgentMonitor.java
 * Author: "Thomas Calmant"
 * Date:   30 janv. 2013
 */
package org.cohorte.composer.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.cohorte.composer.api.IAgentListener;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalListener;

/**
 * The composer agent monitor
 * 
 * @author "Thomas Calmant"
 */
@Component(name = "cohorte-composer-agent-monitor-factory")
public class AgentMonitor implements ISignalListener {

    /** Agents events listeners dependency ID */
    private final static String ID_LISTENERS = "agent-listeners";

    @Requires(id = ID_LISTENERS, optional = true)
    private IAgentListener[] pListeners;

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

        // TODO: notify listeners according to signal name & content
        return null;
    }
}
