/**
 * File:   IIsolateRunner.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.forker;

import org.psem2m.isolates.services.conf.IIsolateDescr;

/**
 * Describes an isolate runner
 * 
 * @author Thomas Calmant
 */
public interface IIsolateRunner {

    /**
     * Tests if the runner can start the given kind of isolate
     * 
     * @param aIsolateKind
     *            A kind of isolate
     * @return True if the runner can start the given kind of isolate
     */
    boolean canRun(String aIsolateKind);

    /**
     * Tries to run the isolate described by the given configuration
     * 
     * @param aIsolateConfiguration
     *            The isolate configuration
     * 
     * @return A reference to the isolate process
     * @throws Exception
     *             An error occurred while preparing or starting the isolate
     */
    IProcessRef startIsolate(IIsolateDescr aIsolateConfiguration)
	    throws Exception;
}
