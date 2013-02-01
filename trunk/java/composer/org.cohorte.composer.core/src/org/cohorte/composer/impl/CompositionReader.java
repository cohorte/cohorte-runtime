/**
 * File:   ComposerConfiguration.java
 * Author: "Thomas Calmant"
 * Date:   30 janv. 2013
 */
package org.cohorte.composer.impl;

import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Requires;
import org.cohorte.composer.api.IComposer;

/**
 * Reads a composition file
 * 
 * @author "Thomas Calmant"
 */
public class CompositionReader {

    /** The composer */
    @Requires
    private IComposer pComposer;

    /** The composition file */
    @Property(name = "composition.file", value = "")
    private String pFile;
}
