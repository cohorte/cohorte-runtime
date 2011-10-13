/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz  (isandlaTech) - 6 oct. 2011 - initial API and implementation
 *******************************************************************************/
package org.psem2m.demo.erp.api.beans;

import java.io.Serializable;

/**
 * The bean representing a line of a Shopping cart.
 * 
 * @author ogattaz
 * 
 */
public class CCartLine implements Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    private String pItemId;

    private String pLineId;

    private double pQuantity;

    /**
     * Explicit public constructor
     */
    public CCartLine() {

        super();
    }

    /**
     * @return the item id
     */
    public String getItemId() {

        return pItemId;
    }

    /**
     * @return the line id
     */
    public String getLineId() {

        return pLineId;
    }

    /**
     * @return the quantity
     */
    public double getQuantity() {

        return pQuantity;
    }

    /**
     * @param aItemId
     */
    public void setItemId(final String aItemId) {

        this.pItemId = aItemId;
    }

    /**
     * @param aLineId
     */
    public void setLineId(final String aLineId) {

        this.pLineId = aLineId;
    }

    /**
     * @param aQuantity
     */
    public void setQuantity(final double aQuantity) {

        this.pQuantity = aQuantity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return String.format(" lineId=[%s] itemId=[%s] quantity=[%f]",
                getLineId(), getItemId(), getQuantity());
    }
}
