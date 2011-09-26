/**
 * File:   IForkerHandler.java
 * Author: Thomas Calmant
 * Date:   23 sept. 2011
 */
package org.psem2m.isolates.base.isolates;

/**
 * Represents a forker handler : a service that is able to start and stop a
 * forker (a special isolate) from scratch.
 * 
 * @author Thomas Calmant
 */
public interface IForkerHandler {

    /**
     * Registers an isolate status listener to the forker handler.
     * 
     * @param aListener
     *            An IsolateStatus listener
     */
    void registerIsolateEventListener(IIsolateStatusEventListener aListener);

    /**
     * Starts the forker. Returns false if no forker can be created. Returns
     * true on success or if a forker is already running.
     * 
     * @return True if a forker is present, false on error
     */
    boolean startForker();

    /**
     * Stops the forker. Be sure to be in a stopping platform state, to avoid an
     * automatic forker restart.
     * 
     * @return True on success, false on error.
     */
    boolean stopForker();

    /**
     * Unregisters the given isolate status listener from the forker handler.
     * 
     * @param aListener
     *            An IsolateStatus listener
     */
    void unregisterIsolateEventListener(IIsolateStatusEventListener aListener);
}
