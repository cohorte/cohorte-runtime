/**
 * File:   IForker.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.commons.forker;

import java.io.IOException;

import org.psem2m.isolates.commons.PlatformConfiguration;

/**
 * Description of the Forker service
 * 
 * @author Thomas Calmant
 */
public interface IForker {

    /**
     * Kills the process with the given isolate ID
     * 
     * @param aIsolateId
     *            The ID of the isolate to kill
     */
    void killProcess(String aIsolateId);

    /**
     * Starts a process according to the given configuration
     * 
     * @param aPlatformConfiguration
     *            The platform configuration
     * @param aProcessConfiguration
     *            The configuration of the future process (contains the isolate
     *            ID)
     * @throws IOException
     *             An error occured while starting the process
     */
    void runProcess(PlatformConfiguration aPlatformConfiguration,
	    ProcessConfiguration aProcessConfiguration) throws IOException;
}
