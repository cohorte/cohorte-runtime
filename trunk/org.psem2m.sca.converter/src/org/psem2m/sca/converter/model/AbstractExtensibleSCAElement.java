/**
 * File:   AbstractExtensibleSCAElement.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.model;

/**
 * @author Thomas Calmant
 * 
 */
public abstract class AbstractExtensibleSCAElement<T> extends
        AbstractSCAElement<T> {

    /**
     * Returns the kind of extension, i.e. the tag name
     * 
     * @return the kind of extension
     */
    public String getKind() {

        return pXmlElement.getLocalName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.sca.converter.model.AbstractSCAElement#toString(java.lang.
     * StringBuilder, java.lang.String)
     */
    @Override
    public void toString(final StringBuilder aBuilder, final String aPrefix) {

        aBuilder.append(aPrefix);
        aBuilder.append(getClass().getSimpleName());
        aBuilder.append("(kind=").append(getKind()).append(")");
    }
}
