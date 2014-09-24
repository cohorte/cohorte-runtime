/**
 * File:   IConfigurationReader.java
 * Author: Thomas Calmant
 * Date:   6 sept. 2011
 */
package org.psem2m.isolates.services.conf;

import org.psem2m.isolates.services.conf.beans.ApplicationDescription;
import org.psem2m.isolates.services.conf.beans.BundleDescription;
import org.psem2m.isolates.services.conf.beans.IsolateDescription;

/**
 * Defines a PSEM2M configuration reader
 * 
 * @author Thomas Calmant
 */
public interface IConfigurationReader {

    /**
     * Retrieves the description of the application corresponding to the given
     * ID
     * 
     * @param aApplicationId
     *            An available application ID
     * @return A description of an application
     */
    ApplicationDescription getApplication(String aApplicationId);

    /**
     * Retrieves all available application IDs
     * 
     * @return Available application IDs
     */
    String[] getApplicationIds();

    /**
     * Loads the given configuration file
     * 
     * @param aConfigurationFile
     *            A PSEM2M configuration file
     * 
     * @return True if the file was successfully read
     */
    boolean load(String aConfigurationFile);

    /**
     * Retrieves the description of the bundle described in the given string
     * 
     * @param aBundleConfiguration
     *            A configuration string
     * @return The parsed description, or null
     */
    BundleDescription parseBundle(String aBundleConfiguration);

    /**
     * Retrieves the description of the isolate described in the given string
     * 
     * @param aIsolateConfiguration
     *            A configuration string
     * @return The parsed description, or null
     */
    IsolateDescription parseIsolate(String aIsolateConfiguration);
}
