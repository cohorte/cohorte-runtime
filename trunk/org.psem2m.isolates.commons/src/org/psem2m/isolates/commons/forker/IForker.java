/**
 * File:   IForker.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.commons.forker;

import java.io.IOException;
import java.security.InvalidParameterException;

import org.psem2m.isolates.commons.IIsolateConfiguration;
import org.psem2m.isolates.commons.IPlatformConfiguration;

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
     * Tests the given isolate state
     * 
     * @param aIsolateId
     *            The isolate ID
     * @return The isolate process state
     */
    EProcessState ping(String aIsolateId);

    /**
     * Updates the known platform configuration
     * 
     * @param aPlatformConfiguration
     *            The platform configuration
     */
    void setConfiguration(IPlatformConfiguration aPlatformConfiguration);

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
    void startIsolate(IIsolateConfiguration aIsolateConfiguration)
	    throws IOException, InvalidParameterException, Exception;

    /**
     * Kills the process with the given isolate ID
     * 
     * @param aIsolateId
     *            The ID of the isolate to kill
     */
    void stopIsolate(String aIsolateId);
}
