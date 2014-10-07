/**
 * File:   Reference.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.model;

/**
 * Represents a basic SCA binding
 * 
 * @author Thomas Calmant
 */
public class Binding extends AbstractExtensibleSCAElement {

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.AbstractSCAElement#duplicate()
     */
    @Override
    public Binding duplicate() {

        return (Binding) super.duplicate();
    }
}
