/**
 * 
 */
package org.psem2m.isolates.monitor;

/**
 * Describes the platform monitor.
 */
public interface IPlatformMonitor {

    /**
     * Uses a forker to start the isolate configured with the given ID.
     * 
     * @param aIsolateId
     *            ID of the isolate to launch
     * @return True on success.
     */
    boolean startIsolate(String aIsolateId);

    /**
     * Stops (or kills) the isolate with the given ID.
     * 
     * @param aIsolateId
     *            The ID of the isolate to stop
     * @return True on success, false if it was not running.
     */
    boolean stopIsolate(String aIsolateId);

    /**
     * Stops the whole platform, with isolates.
     */
    void stopPlatform();
}
