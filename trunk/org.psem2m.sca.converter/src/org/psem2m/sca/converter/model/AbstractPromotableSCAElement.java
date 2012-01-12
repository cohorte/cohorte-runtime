/**
 * File:   AbstractPromotableSCAElement.java
 * Author: Thomas Calmant
 * Date:   9 janv. 2012
 */
package org.psem2m.sca.converter.model;

import org.psem2m.sca.converter.core.QName;

/**
 * @author Thomas Calmant
 * 
 */
public abstract class AbstractPromotableSCAElement<T> extends
        AbstractSCAElement<T> {

    /** The promoted service name */
    protected QName pPromotedElementName;

    /** This service promotes another */
    protected boolean pPromotes;

    /**
     * @return the promotedElementName
     */
    public QName getPromotedElementName() {

        return pPromotedElementName;
    }

    /**
     * @return the promotes
     */
    public boolean isPromotion() {

        return pPromotes;
    }

    /**
     * @param aPromotedElementName
     *            the promotedElementName to set
     */
    public void setPromotedElementName(final QName aPromotedElementName) {

        pPromotedElementName = aPromotedElementName;
    }

    /**
     * @param aPromotes
     *            the promotes to set
     */
    public void setPromotes(final boolean aPromotes) {

        pPromotes = aPromotes;
    }
}
