/**
 * File:   ISvcConfig.java
 * Author: Thomas Calmant
 * Date:   6 sept. 2011
 */
package org.psem2m.isolates.services.conf;

import org.psem2m.isolates.services.conf.beans.ApplicationDescription;
import org.psem2m.isolates.services.conf.beans.IsolateDescription;

/**
 * PSEM2M Configuration service
 * 
 * @author Thomas Calmant
 */
public interface ISvcConfig {

    /**
     * Retrieves the description of current application
     * 
     * @return the description of current application
     */
    ApplicationDescription getApplication();

    /**
     * Retrieves the description of the current isolate
     * 
     * @return the description of the current isolate
     */
    IsolateDescription getCurrentIsolate();

    /**
     * Reloads configuration
     * 
     * @return True on success, else false
     */
    boolean refresh();
}
