/**
 * File:   INameable.java
 * Author: Thomas Calmant
 * Date:   12 janv. 2012
 */
package org.psem2m.sca.converter.model;

import org.psem2m.sca.converter.utils.QName;

/**
 * Represents an object which can have a qualified name (almost all SCA
 * elements, except wires)
 * 
 * @author Thomas Calmant
 */
public interface INameable {

    /**
     * Retrieves the complete name of the current element
     * 
     * @return The complete name with aliases
     */
    String getCompleteAlias();

    /**
     * Retrieves the complete name of the current element, using aliases instead
     * of local names
     * 
     * @return The complete name
     */
    String getCompleteName();

    /**
     * Retrieves the qualified name of this element
     * 
     * @return A qualified name, or null
     */
    QName getQualifiedName();
}
