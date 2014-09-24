/**
 * File:   AbstractPromotableSCAElement.java
 * Author: Thomas Calmant
 * Date:   9 janv. 2012
 */
package org.psem2m.sca.converter.model;

import org.psem2m.sca.converter.utils.QName;

/**
 * Basic implementation of an {@link IPromotable} class
 * 
 * @author Thomas Calmant
 */
public abstract class AbstractPromotableSCAElement extends
        AbstractNameableSCAElement implements IPromotable {

    /** The promoted service name */
    protected QName pPromotedElementName;

    /** This service promotes another */
    protected boolean pPromotes;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.sca.converter.model.AbstractNameableSCAElement#duplicate()
     */
    @Override
    public AbstractPromotableSCAElement duplicate() {

        final AbstractPromotableSCAElement copy = (AbstractPromotableSCAElement) super
                .duplicate();

        copy.pPromotedElementName = pPromotedElementName;
        copy.pPromotes = pPromotes;

        return copy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.IPromotable#getPromotedElementName()
     */
    @Override
    public QName getPromotedElementName() {

        return pPromotedElementName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.IPromotable#isPromotion()
     */
    @Override
    public boolean isPromotion() {

        return pPromotes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.sca.converter.model.IPromotable#setPromotedElementName(org
     * .psem2m.sca.converter.core.QName)
     */
    @Override
    public void setPromotedElementName(final QName aPromotedElementName) {

        pPromotedElementName = aPromotedElementName;
        pPromotes = pPromotedElementName != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.IPromotable#setPromotes(boolean)
     */
    @Override
    public void setPromotes(final boolean aPromotes) {

        pPromotes = aPromotes;
    }
}
