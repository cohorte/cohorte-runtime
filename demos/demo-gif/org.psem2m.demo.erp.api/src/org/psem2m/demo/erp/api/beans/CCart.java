/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz  (isandlaTech) - 10 oct. 2011 - initial API and implementation
 *******************************************************************************/
package org.psem2m.demo.erp.api.beans;

import java.io.Serializable;

/**
 * @author ogattaz
 * 
 */
public class CCart implements Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    private String pCartId;

    private CCartLine[] pCartLines;

    /**
     * @return the pCartId
     */
    public String getCartId() {

        return pCartId;
    }

    /**
     * @return the pCartLines
     */
    public CCartLine[] getCartLines() {

        return pCartLines;
    }

    /**
     * @param pCartId
     *            the pCartId to set
     */
    public void setCartId(final String aCartId) {

        this.pCartId = aCartId;
    }

    /**
     * @param pCartLines
     *            the pCartLines to set
     */
    public void setCartLines(final CCartLine[] aCartLines) {

        this.pCartLines = aCartLines;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return String.format(" cartId=[%s] nbLine=[%d]", getCartId(),
                pCartLines != null ? pCartLines.length : -1);
    }
}
