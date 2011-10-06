/**
 * File:   IDataCache.java
 * Author: Thomas Calmant
 * Date:   3 oct. 2011
 */
package org.psem2m.demo.data.cache;

import java.util.Collection;
import java.util.Set;

import org.psem2m.demo.erp.api.beans.ItemBean;

/**
 * Represents a DataServer cache
 * 
 * @author Thomas Calmant
 */
public interface IDataCache {

    /**
     * Retrieves the time stamp of the last category update, -1 if the category
     * is unknown.
     * 
     * @param aCategory
     *            A category
     * @return The last update time stamp, or -1
     */
    long getCategoryInformationAge(String aCategory);

    /**
     * Retrieves the items IDs of the given category
     * 
     * @param aCategory
     *            A category
     * @return Items IDs of the category, null if none.
     */
    Set<String> getCategoryItems(String aCategory);

    /**
     * Retrieves the cached item bean, null if not present
     * 
     * @param aItemId
     *            An item ID
     * @return The cached item bean, or null
     */
    ItemBean getItem(String aItemId);

    /**
     * Retrieves the time stamp of the last item update, -1 if the item ID is
     * unknown.
     * 
     * @param aItemId
     *            An item ID
     * @return The last update time stamp, or -1
     */
    long getItemInformationAge(String aItemId);

    /**
     * Retrieves the cached stock value of the given item. Returns -1 if no
     * cache is available.
     * 
     * @param aItemId
     *            An item ID
     * @return The cached stock value, -1 if none
     */
    long getItemStock(String aItemId);

    /**
     * Retrieves the time stamp of the last item stock update, -1 if the item ID
     * is unknown.
     * 
     * @param aItemId
     *            An item ID
     * @return The last update time stamp, or -1
     */
    long getItemStockInformationAge(String aItemId);

    /**
     * Retrieves a random item from the cache. Returns null if no item is
     * available in the cache.
     * 
     * @return A random item, null if cache is empty.
     */
    ItemBean getRandomItem();

    /**
     * Updates a category items content. If the collection is null, the category
     * is removed from the cache
     * 
     * @param aCategory
     *            A category
     * @param aItemIds
     *            IDs of the items of the category
     */
    void updateCategoryItems(String aCategory, Collection<String> aItemIds);

    /**
     * Updates an item's stock value
     * 
     * @param aItemId
     *            An item ID
     * @param aStock
     *            Its stock
     */
    void updateItemBean(String aItemId, ItemBean aBean);

    /**
     * Updates an item's stock value
     * 
     * @param aItemId
     *            An item ID
     * @param aStock
     *            Its stock
     */
    void updateItemStock(String aItemId, long aStock);

}
