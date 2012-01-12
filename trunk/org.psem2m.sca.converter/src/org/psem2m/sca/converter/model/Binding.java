/**
 * File:   Reference.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.model;

/**
 * @author Thomas Calmant
 * 
 */
public class Binding extends AbstractExtensibleSCAElement<Binding> {

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.AbstractSCAElement#duplicate()
     */
    @Override
    public Binding duplicate() {

        final Binding copy = new Binding();
        copy.pContainer = pContainer;
        copy.pXmlElement = pXmlElement;
        return copy;
    }
}
