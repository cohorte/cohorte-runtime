/**
 * File:   ISvcConfig.java
 * Author: Thomas Calmant
 * Date:   6 sept. 2011
 */
package org.psem2m.isolates.base.conf;


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
    IApplicationDescr getApplication();

    /**
     * Reload configuration
     * 
     * @return True on success, else false
     */
    boolean refresh();
}
