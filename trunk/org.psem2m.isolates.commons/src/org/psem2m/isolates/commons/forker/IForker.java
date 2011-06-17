/**
 * File:   IForker.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.commons.forker;

import java.io.IOException;
import java.security.InvalidParameterException;

import org.psem2m.isolates.commons.PlatformConfiguration;

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
     * Kills the process with the given isolate ID
     * 
     * @param aIsolateId
     *            The ID of the isolate to kill
     */
    void killProcess(String aIsolateId);

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
     * @param aPlatformConfiguration
     *            The platform configuration
     * @param aProcessConfiguration
     *            The configuration of the future process (contains the isolate
     *            ID)
     * 
     * @return The isolate process
     * @throws IOException
     *             An error occurred while starting the process
     * @throws InvalidParameterException
     *             An isolate with the given isolate ID is already running.
     */
    Process runProcess(PlatformConfiguration aPlatformConfiguration,
	    ProcessConfiguration aProcessConfiguration) throws IOException,
	    InvalidParameterException;
}
