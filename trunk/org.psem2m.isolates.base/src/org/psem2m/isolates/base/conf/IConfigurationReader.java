/**
 * File:   IConfigurationReader.java
 * Author: Thomas Calmant
 * Date:   6 sept. 2011
 */
package org.psem2m.isolates.base.conf;

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
    IApplicationDescr getApplication(String aApplicationId);

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
     * @return True if the file was successfully read
     */
    boolean load(String aConfigurationFile);
}
