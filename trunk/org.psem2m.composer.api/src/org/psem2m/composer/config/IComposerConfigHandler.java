/**
 * File:   IComposerConfigHandler.java
 * Author: Thomas Calmant
 * Date:   3 nov. 2011
 */
package org.psem2m.composer.config;

import java.io.IOException;

import org.psem2m.composer.model.ComponentsSetBean;

/**
 * Defines a PSEM2M Composer configuration file reader
 * 
 * @author Thomas Calmant
 */
public interface IComposerConfigHandler {

    /**
     * Finds and loads the first file found in the platform directories matching
     * the given name.
     * 
     * @param aFileName
     *            A configuration file name
     * @return The root component set, null on error
     */
    ComponentsSetBean load(String aFileName);

    /**
     * Writes the composition model configuration into the given file. Creates
     * the file and parent folders if needed. If the given file name is null,
     * the handler should write the result in the standard output.
     * 
     * @param aComponentsSet
     *            A components set
     * @param aFileName
     *            An output file name, can be null
     * 
     * @throws IOException
     *             An error occurred while writing the configuration file
     */
    void write(ComponentsSetBean aComponentsSet, String aFileName)
            throws IOException;
}
