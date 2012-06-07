/**
 * File:   IIsolateHandler.java
 * Author: Thomas Calmant
 * Date:   23 sept. 2011
 */
package org.psem2m.isolates.base.isolates;

import org.psem2m.isolates.services.conf.IIsolateDescr;

/**
 * Represents an isolate handler : a service that is able to start and stop an
 * isolate using a forker.
 * 
 * @author Thomas Calmant
 */
public interface IIsolateHandler {

    /**
     * Registers an isolate status listener to the isolate handler.
     * 
     * @param aListener
     *            An IsolateStatus listener
     */
    void registerIsolateEventListener(IIsolateStatusEventListener aListener);

    /**
     * Uses a forker to start the isolate configured with the given ID. If it is
     * already running, the aForceRestart parameter allows to force its restart.
     * 
     * @param aIsolateDescr
     *            Description of the isolate to launch
     * @param aForceRestart
     *            Force isolate restart if already running
     * 
     * @return True on success.
     */
    boolean startIsolate(IIsolateDescr aIsolateDescr, boolean aForceRestart);

    /**
     * Stops (or kills) the isolate with the given ID.
     * 
     * @param aIsolateId
     *            The ID of the isolate to stop
     * 
     * @return True on success, false on error
     */
    boolean stopIsolate(String aIsolateId);

    /**
     * Unregisters the given isolate status listener from the isolate handler.
     * 
     * @param aListener
     *            An IsolateStatus listener
     */
    void unregisterIsolateEventListener(IIsolateStatusEventListener aListener);
}
