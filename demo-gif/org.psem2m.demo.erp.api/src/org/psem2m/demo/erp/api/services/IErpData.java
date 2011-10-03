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
     * Retrieves all items from the given category. Returns an empty array on
     * error (never null).
     * 
     * @param aCategory
     *            Category to list
     * @return All elements of the category, or an empty array.
     */
    CachedItemBean[] getItems(String aCategory);

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
