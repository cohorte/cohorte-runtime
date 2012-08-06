/**
 * File:   IForkerStatus.java
 * Author: Thomas Calmant
 * Date:   6 août 2012
 */
package org.psem2m.forker;

/**
 * Defines the possible values to describe the status of a forker service. Using
 * integer values instead of an enumeration to allow custom values.
 * 
 * @author Thomas Calmant
 */
public interface IForkerStatus {

    /** Isolate is alive */
    int ALIVE = 0;

    /** The isolate is already running */
    int ALREADY_RUNNING = 1;

    /** Process is dead (not running) */
    int DEAD = 1;

    /** No reference to the isolate process, unknown state */
    int NO_PROCESS_REF = 2;

    /** No isolate watcher could be started (active isolate waiter) */
    int NO_WATCHER = 3;

    /** Error sending the request */
    int REQUEST_ERROR = -3;

    /** Forker didn't returned any result */
    int REQUEST_NO_RESULT = -2;

    /** Forker timed out */
    int REQUEST_TIMEOUT = -1;

    /** An error occurred calling the runner */
    int RUNNER_EXCEPTION = 4;

    /** Process is stuck (running, but not responding) */
    int STUCK = 2;

    /** Successful operation */
    int SUCCESS = 0;

    /** Unknown kind of isolate */
    int UNKNOWN_KIND = 5;
}
