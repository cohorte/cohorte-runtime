/**
 * File:   IComposerCore.java
 * Author: Thomas Calmant
 * Date:   28 mars 2013
 */
package org.cohorte.composer.api;

import java.util.List;
import java.util.Map;

/**
 * Represents the Composer core service, used by agents
 * 
 * @author Thomas Calmant
 */
public interface IComposerCore {

    /**
     * 
     * @param aIsolate
     *            The isolate identifier: [UID, Name]
     * @param aUID
     *            UID of the component
     * @param aName
     *            Name of the component
     * @param aFactory
     *            The component factory name
     * @param aEvent
     *            A Composer FSM event
     */
    void component_changed(String[] aIsolate, String aUID, String aName,
            String aFactory, String aEvent);

    /**
     * Notifies the Composer core of an instantiation result
     * 
     * @param aIsolate
     *            The isolate identifier: [UID, Name]
     * @param aSuccesses
     *            The component UID -&gt; Name map of the components
     *            successfully instantiated
     * @param aRunning
     *            The list of names of the components that are already running,
     *            or if there name is already used
     * @param aErrors
     *            The component UID -&gt; Error description map when the
     *            instantiation failed
     */
    void components_instantiation(String[] aIsolate,
            Map<String, String> aSuccesses, List<String> aRunning,
            Map<String, String> aErrors);
}
