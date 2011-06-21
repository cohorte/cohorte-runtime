/**
 * 
 */
package org.psem2m.isolates.monitor;

import java.util.Collection;

import org.psem2m.isolates.commons.IIsolateConfiguration;
import org.psem2m.isolates.commons.forker.ProcessConfiguration;

/**
 * Description of the interface IIsolateManager.
 * 
 */

public interface IIsolateManager {

    /**
     * Description of the method getPossibleIsolates.
     * 
     * 
     * @return ret
     */
    public Collection<IIsolateConfiguration> getPossibleIsolates();

    /**
     * Description of the method getRunningIsolates.
     * 
     * 
     * @return ret
     */
    public Collection<ProcessConfiguration> getRunningIsolates();

    /**
     * Description of the method restartPlatform.
     * 
     * 
     * @param aForce
     * @return ret
     */
    public boolean restartPlatform(boolean aForce);

    /**
     * Description of the method startIsolate.
     * 
     * 
     * @param aIsolateId
     * @param aForceRestart
     * @return ret
     */
    public boolean startIsolate(String aIsolateId, boolean aForceRestart);

    /**
     * Description of the method stopIsolate.
     * 
     * 
     * @param aIsolateId
     * @return ret
     */
    public boolean stopIsolate(String aIsolateId);

    /**
     * Description of the method stopPlatform.
     * 
     * 
     */
    public void stopPlatform();
}
