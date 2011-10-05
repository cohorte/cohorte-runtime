/**
 * File:   IErpData.java
 * Author: Thomas Calmant
 * Date:   3 oct. 2011
 */
package org.psem2m.demo.erp.api.services;

import org.psem2m.demo.erp.api.beans.CachedItemBean;
import org.psem2m.demo.erp.api.beans.CachedItemStockBean;

/**
 * Describes the Data Server exported service
 * 
 * @author Thomas Calmant
 */
public interface IErpData {

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
     * @return The corresponding stock entries
     */
    CachedItemStockBean[] getItemsStock(String[] aItemIds);
}
