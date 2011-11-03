/**
 * File:   IComposerConfigReader.java
 * Author: Thomas Calmant
 * Date:   3 nov. 2011
 */
package org.psem2m.composer.config;

import org.psem2m.composer.model.ComponentSet;

/**
 * Defines a PSEM2M Composer configuration file reader
 * 
 * @author Thomas Calmant
 */
public interface IComposerConfigReader {

    /**
     * Finds and loads the first file found in the platform directories matching
     * the given name.
     * 
     * @param aFileName
     *            A configuration file name
     * @return The root component set, null on error
     */
    ComponentSet load(String aFileName);
}
