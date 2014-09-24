/**
 * File:   ILocalConfiguration.java
 * Author: Thomas Calmant
 * Date:   23 janv. 2013
 */
package org.psem2m.isolates.services.conf;

import org.psem2m.isolates.services.conf.beans.ApplicationDescription;
import org.psem2m.isolates.services.conf.beans.IsolateConf;

/**
 * Retrieves the configuration that was used to start this isolate
 * 
 * @author Thomas Calmant
 */
public interface IStartConfiguration {

    /**
     * Retrieves the configuration of the application
     * 
     * @return the configuration of the application
     */
    ApplicationDescription getApplication();

    /**
     * Retrieves the configuration used to start the isolate
     * 
     * @return The boot configuration
     */
    IsolateConf getConfiguration();
}
