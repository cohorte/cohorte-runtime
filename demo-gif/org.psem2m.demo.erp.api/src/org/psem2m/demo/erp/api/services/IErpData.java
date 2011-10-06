/**
 * File:   IErpData.java
 * Author: Thomas Calmant
 * Date:   3 oct. 2011
 */
/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thomas Calmant (isandlaTech) -  3 oct. 2011 - initial API and implementation
 *    ogattaz  (isandlaTech) -  6 oct. 2011 - Adds the method "applyCart()"
 *******************************************************************************/
package org.psem2m.demo.erp.api.services;

import org.psem2m.demo.erp.api.beans.CCartLine;
import org.psem2m.demo.erp.api.beans.CErpActionReport;
import org.psem2m.demo.erp.api.beans.CachedItemBean;
import org.psem2m.demo.erp.api.beans.CachedItemStockBean;

/**
 * Describes the Data Server exported service
 * 
 * @author Thomas Calmant
 */
public interface IErpData {

    /**
     * Submit an array of cart lines to the ERP.
     * 
     * Return always a bean CErpActionReport containing
     * 
     * @param aCartLines
     *            the array of beans CCartLine
     * @return a bean CErpActionReport
     */
    CErpActionReport applyCart(CCartLine[] aCartLines);

    /**
     * Retrieves the item bean corresponding to the given ID. Uses the cache if
     * the ERP is not available. Returns null if the item is not found.
     * 
     * @param aItemId
     *            An item ID
     * @return The item, null if not available
     */
    CachedItemBean getItem(String aItemId);

    /**
     * Retrieves all items from the given category. Returns an empty array on
     * error (never null). aBaseId and aRandomize are exclusive, aRandomize has
     * the priority on aBaseId.
     * 
     * Uses the cache if the ERP is unavailable.
     * 
     * @param aCategory
     *            Category to list
     * @param aItemsCount
     *            Maximum number of items to retrieve (&lt;= 0 for unlimited)
     * @param aRandomize
     *            Retrieve "aItemsCount" randomly from the category
     * @param aBaseId
     *            Retrieve items starting by item at aBaseId + 1
     * 
     * @return All elements of the category, or an empty array.
     */
    CachedItemBean[] getItems(String aCategory, int aItemsCount,
            boolean aRandomize, String aBaseId);

    /**
     * Retrieves the stocks available for the given item IDs. Result entries
     * index maps parameters entries index. Stock is set to -1 if the
     * corresponding item ID is invalid.
     * 
     * @param aItemIds
     *            Some items IDs
     * @return The corresponding stock entries, or an empty array.
     */
    CachedItemStockBean[] getItemsStock(String[] aItemIds);
}
