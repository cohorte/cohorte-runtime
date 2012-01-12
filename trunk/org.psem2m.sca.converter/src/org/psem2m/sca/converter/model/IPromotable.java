/**
 * File:   IPromotable.java
 * Author: Thomas Calmant
 * Date:   12 janv. 2012
 */
package org.psem2m.sca.converter.model;

import org.psem2m.sca.converter.core.QName;

/**
 * Represents a SCA element which can be promoted (reference, service...)
 * 
 * @author Thomas Calmant
 */
public interface IPromotable {

    /**
     * Retrieves the qualified name of the promoted element
     * 
     * @return the name of the promoted element
     */
    QName getPromotedElementName();

    /**
     * Tests if this element promotes another one
     * 
     * @return True if this element is a promotion
     */
    boolean isPromotion();

    /**
     * Sets the qualified name of the promoted element
     * 
     * @param aPromotedElementName
     *            the name of the promoted element
     */
    void setPromotedElementName(QName aPromotedElementName);

    /**
     * Forces the promotion flag
     * 
     * @param aPromotes
     *            the promotion flag to set
     */
    void setPromotes(boolean aPromotes);
}
