/**
 * File:   Reference.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.model;

import org.psem2m.sca.converter.core.QName;

/**
 * Represents a SCA wire
 * 
 * @author Thomas Calmant
 */
public class Wire extends AbstractSCAElement {

    /** The wire source reference */
    private Reference pSource;

    /** The wire source name */
    private QName pSourceName;

    /** The wire target */
    private INameable pTarget;

    /** The wire target name */
    private QName pTargetName;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.AbstractSCAElement#duplicate()
     */
    @Override
    public Wire duplicate() {

        final Wire copy = new Wire();
        copy.pContainer = pContainer;
        copy.pSource = pSource;
        copy.pTarget = pTarget;
        copy.pSourceName = pSourceName;
        copy.pTargetName = pTargetName;
        copy.pXmlElement = pXmlElement;

        return copy;
    }

    /**
     * @return the source
     */
    public Reference getSource() {

        return pSource;
    }

    /**
     * @return the sourceName
     */
    public QName getSourceName() {

        return pSourceName;
    }

    /**
     * @return the target
     */
    public INameable getTarget() {

        return pTarget;
    }

    /**
     * @return the targetName
     */
    public QName getTargetName() {

        return pTargetName;
    }

    /**
     * @param source
     *            the source reference to set
     */
    public void setSource(final Reference source) {

        pSource = source;
    }

    /**
     * @param aReferenceName
     *            the qualified name of the source
     */
    public void setSourceName(final QName aReferenceName) {

        pSourceName = aReferenceName;
    }

    /**
     * @param target
     *            the target to set
     */
    public void setTarget(final INameable target) {

        pTarget = target;
    }

    /**
     * @param aTargetName
     *            the targetName to set
     */
    public void setTargetName(final QName aTargetName) {

        pTargetName = aTargetName;
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
        aBuilder.append("Wire('").append(pSourceName).append("' -> '");
        aBuilder.append(pTargetName).append("')");
    }
}
