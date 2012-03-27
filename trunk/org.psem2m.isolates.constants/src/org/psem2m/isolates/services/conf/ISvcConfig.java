/**
 * File:   ISvcConfig.java
 * Author: Thomas Calmant
 * Date:   6 sept. 2011
 */
package org.psem2m.isolates.services.conf;

import org.psem2m.isolates.services.conf.beans.ApplicationDescription;

/**
 * PSEM2M Configuration service
 * 
 * @author Thomas Calmant
 */
public interface ISvcConfig {

    /**
     * Retrieve current application description
     * 
     * @return Current application description
     */
    ApplicationDescription getApplication();

    /**
     * Reload configuration
     * 
     * @return True on success, else false
     */
    boolean refresh();
}
