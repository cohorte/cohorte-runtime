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
package org.psem2m.composer.demo;

import java.util.List;
import java.util.Map;

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
     *            An instance of CCart
     * @return a bean CErpActionReport
     */
    Map<String, Object> applyCart(Map<String, Object> aCart);

    /**
     * Retrieves the item bean corresponding to the given ID. Uses the cache if
     * the ERP is not available. Returns null if the item is not found.
     * 
     * @param aItemId
     *            An item ID
     * @return The item, null if not available
     */
    Map<String, Object> getItem(String aItemId);

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
    Map<String, Object> getItems(String aCategory, int aItemsCount,
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
    List<Map<String, Object>> getItemsStock(String[] aItemIds);
}
