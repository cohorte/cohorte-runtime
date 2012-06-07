/**
 * File:   IAlias.java
 * Author: Thomas Calmant
 * Date:   12 janv. 2012
 */
package org.psem2m.sca.converter.model;

/**
 * Represents an element from the SCA model that can have an alias
 * 
 * @author Thomas Calmant
 */
public interface IAlias extends INameable {

    /**
     * Retrieves the alias of this element, or the local part of its local name
     * 
     * @return The alias or the local name
     */
    String getAlias();
}
