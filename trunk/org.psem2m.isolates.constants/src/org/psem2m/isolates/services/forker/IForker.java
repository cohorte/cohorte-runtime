/**
 * File:   IForker.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.services.forker;

import java.io.IOException;
import java.security.InvalidParameterException;

import org.psem2m.isolates.services.conf.IIsolateDescr;

/**
 * Description of the Forker service
 * 
 * @author Thomas Calmant
 */
public interface IForker {

    /**
     * Describes a process state
     * 
     * @author Thomas Calmant
     */
    enum EProcessState {
        ALIVE, DEAD, STUCK,
    }

    /**
     * Describes the reason of a start failure
     * 
     * @author Thomas Calmant
     */
    enum EStartError {
        ALREADY_RUNNING, NO_PROCESS_REF, NO_WATCHER, RUNNER_EXCEPTION, SUCCESS, UNKNOWN_KIND
    }

    /**
     * Retrieves the name of the host machine of this forker
     * 
     * @return The name of the forker host
     */
    String getHostName();

    /**
     * Tests the given isolate state
     * 
     * @param aIsolateId
     *            The isolate ID
     * @return The isolate process state
     */
    EProcessState ping(String aIsolateId);

    /**
     * Starts a process according to the given configuration
     * 
     * @param aIsolateConfiguration
     *            The configuration of the isolate to start
     * 
     * @return The isolate process
     * 
     * @throws IOException
     *             An error occurred while starting the process
     * @throws InvalidParameterException
     *             An isolate with the given isolate ID is already running.
     * @throws Exception
     *             An error occurred while preparing or starting the isolate
     */
    EStartError startIsolate(IIsolateDescr aIsolateConfiguration);

    // throws IOException, InvalidParameterException, Exception;

    /**
     * Kills the process with the given isolate ID
     * 
     * @param aIsolateId
     *            The ID of the isolate to kill
     */
    void stopIsolate(String aIsolateId);
}
