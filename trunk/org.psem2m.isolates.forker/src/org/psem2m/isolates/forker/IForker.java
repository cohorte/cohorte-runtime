/**
 * 
 */
package org.psem2m.isolates.forker;

import org.psem2m.isolates.commons.IReconfigurable;

/**
 * Description of a Forker
 */
public interface IForker extends IReconfigurable {

    /** The constant used in configuration to be replaced by the isolate ID */
    String ISOLATE_DIRECTORY_VARIABLE = "${isolate.id}";

    /**
     * Retrieves the process informations about the given isolate.
     * 
     * @param aIsolateId
     *            An isolate ID
     * 
     * @return The process associated with the isolate ID, null if not available
     */
    IProcess getProcess(String aIsolateId);

    /**
     * Starts the given isolate according to the current forker configuration
     * 
     * @param aIsolateId
     *            ID of the isolate to be started
     * @param aForce
     *            Restarts the existing isolate if any
     * 
     * @return True if the process has been started, else false
     */
    boolean startProcess(String aIsolateId, boolean aForce);
}
