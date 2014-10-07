/**
 * File:   Reference.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.model;

/**
 * Represents an SCA interface
 * 
 * @author Thomas Calmant
 */
public class Interface extends AbstractExtensibleSCAElement {

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.AbstractSCAElement#duplicate()
     */
    @Override
    public Interface duplicate() {

        return (Interface) super.duplicate();
    }
}
