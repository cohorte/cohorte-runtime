/**
 * File:   Reference.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.model;

import org.psem2m.sca.converter.core.SCAConstants;

/**
 * @author Thomas Calmant
 * 
 */
public class Implementation extends
        AbstractExtensibleSCAElement<Implementation> {

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.AbstractSCAElement#duplicate()
     */
    @Override
    public Implementation duplicate() {

        final Implementation copy = new Implementation();
        copy.pContainer = pContainer;
        copy.pXmlElement = pXmlElement;
        return copy;
    }

    /**
     * Tests if this implementation represents a composite
     * 
     * @return True if this implementation is a composite
     */
    public boolean isComposite() {

        return pXmlElement.getLocalName().equals(
                SCAConstants.IMPLEMENTATION_COMPOSITE)
                && pXmlElement.getNamespaceURI().equals(SCAConstants.SCA_NS);
    }
}
