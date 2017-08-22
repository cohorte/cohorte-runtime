/**
 * File:   Property.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.model;

/**
 * Represents a SCA property
 * 
 * @author Thomas Calmant
 */
public class Property extends AbstractNameableSCAElement {

    /** Property must be supplied for a component to be valid */
    private boolean pMustSupply;

    /** Raw property value */
    private String pValue;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.AbstractSCAElement#duplicate()
     */
    @Override
    public Property duplicate() {

        final Property copy = (Property) super.duplicate();
        copy.pMustSupply = pMustSupply;
        copy.pValue = pValue;
        return copy;
    }

    /**
     * @return the value
     */
    public String getValue() {

        return pValue;
    }

    /**
     * @return the mustSupply
     */
    public boolean isMustSupply() {

        return pMustSupply;
    }

    /**
     * @param aMustSupply
     *            the mustSupply to set
     */
    public void setMustSupply(final boolean aMustSupply) {

        pMustSupply = aMustSupply;
    }

    /**
     * @param aValue
     *            the value to set
     */
    public void setValue(final String aValue) {

        pValue = aValue;
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
        aBuilder.append("Property(name=").append(pQName.getLocalName());
        aBuilder.append(", value='").append(pValue).append("')");
    }
}
