/**
 * 
 */
package org.psem2m.isolates.monitor;

import java.util.Collection;

import org.psem2m.isolates.commons.IIsolateConfiguration;
import org.psem2m.isolates.commons.forker.ProcessConfiguration;

/**
 * Describes an IsolateMaanger, the "monitor".
 */
public interface IIsolateManager {

    /**
     * Retrieves the list of the isolates defined in the configuration file.
     * 
     * @return The list of defined isolates
     */
    public Collection<IIsolateConfiguration> getPossibleIsolates();

    /**
     * Retrieves the list of isolates started by the monitor and considered as
     * running.
     * 
     * @return The list of isolates started by this monitor.
     */
    public Collection<ProcessConfiguration> getRunningIsolates();

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
