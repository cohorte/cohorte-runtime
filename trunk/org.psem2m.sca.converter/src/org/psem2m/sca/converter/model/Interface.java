/**
 * File:   Reference.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.model;

/**
 * @author Thomas Calmant
 */
public class Interface extends AbstractExtensibleSCAElement<Interface> {

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.AbstractSCAElement#duplicate()
     */
    @Override
    public Interface duplicate() {

        final Interface copy = new Interface();
        copy.pContainer = pContainer;
        copy.pXmlElement = pXmlElement;
        return copy;
    }
}
