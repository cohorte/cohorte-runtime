/**
 * 
 */
package org.psem2m.isolates.monitor;

import java.util.Collection;

/**
 * Describes an IsolateMaanger, the "monitor".
 */
public interface IIsolateManager {

    /**
     * Retrieves the list of isolate IDs started by the monitor and considered
     * as running.
     * 
     * @return The list of isolate IDs started by this monitor.
     */
    public Collection<String> getRunningIsolates();

    /**
     * Restarts the whole platform and its isolates.
     * 
     * @param aForce
     *            Force remaining isolates to stop
     * @return True on success, False on error.
     */
    public boolean restartPlatform(boolean aForce);

    /**
     * Uses a forker to start the isolate configured with the given ID. If it is
     * already running, the aForceRestart parameter allows to force its restart.
     * 
     * @param aIsolateId
     *            ID of the isolate to launch
     * @param aForceRestart
     *            Force isolate restart if already running
     * @return True on success.
     */
    public boolean startIsolate(String aIsolateId, boolean aForceRestart);

    /**
     * Stops (or kills) the isolate with the given ID.
     * 
     * @param aIsolateId
     *            The ID of the isolate to stop
     * @return True on success, false if it was not running.
     */
    public boolean stopIsolate(String aIsolateId);

    /**
     * Stops the whole platform, with isolates.
     */
    public void stopPlatform();
}
