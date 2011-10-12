/**
 * File:   CachedErp.java
 * Author: Thomas Calmant
 * Date:   6 oct. 2011
 */
package org.psem2m.demo.data.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.psem2m.demo.data.cache.ICacheChannel;
import org.psem2m.demo.data.cache.ICachedObject;
import org.psem2m.demo.erp.api.beans.CachedItemBean;
import org.psem2m.demo.erp.api.beans.CachedItemStockBean;
import org.psem2m.demo.erp.api.beans.ItemBean;
import org.psem2m.demo.erp.api.services.IErpDataProxy;

/**
 * Behaves like the ERP, but using the cache
 * 
 * @author Thomas Calmant
 */
public class CachedErp {

    /**
     * Uses the cache to act like {@link IErpDataProxy#getItem(String)}
     * 
     * If ID is '?', returns a randomly chosen item. Returns null on error.
     * 
     * @param aCache
     *            The items cache channel
     * @param aItemId
     *            The ID of item to retrieve
     * @return The selected item from cache, or null
     */
    public CachedItemBean getItem(final ICacheChannel<String, ItemBean> aCache,
            final String aItemId) {

        final ICachedObject<ItemBean> cachedObject;

        if ("?".equals(aItemId)) {
            // Random object
            cachedObject = aCache.getRandomObject();

        } else {
            // Specific object
            cachedObject = aCache.get(aItemId);
        }

        if (cachedObject != null) {
            // Return the cached bean
            return new CachedItemBean(cachedObject.getObject(),
                    cachedObject.getCacheAge());
        }

        return null;
    }

    /**
     * Uses the cache to to act like
     * {@link IErpDataProxy#getItems(String, int, boolean, String)}
     * 
     * @param aCategoryCache
     *            The categories cache channel
     * @param aItemsCache
     *            The items cache channel
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
    public CachedItemBean[] getItems(
            final ICacheChannel<String, HashSet<String>> aCategoryCache,
            final ICacheChannel<String, ItemBean> aItemsCache,
            final String aCategory, final int aItemsCount,
            final boolean aRandomize, final String aBaseId) {

        // Get the items of the category
        final ICachedObject<HashSet<String>> cachedCategory = aCategoryCache
                .get(aCategory);
        if (cachedCategory == null) {
            return new CachedItemBean[0];
        }

        final Collection<String> categoryItems = cachedCategory.getObject();
        if (categoryItems == null) {
            // Empty category
            return new CachedItemBean[0];
        }

        // Selected IDs
        final List<String> itemsIds = new ArrayList<String>();

        if (aRandomize) {
            // Case 1 : random
            itemsIds.addAll(getRandomItems(categoryItems, aItemsCount));

        } else {
            // Case 2 : base Id
            itemsIds.addAll(paginateItems(categoryItems, aBaseId, aItemsCount));
        }

        // Result array
        final CachedItemBean[] resultArray = new CachedItemBean[itemsIds.size()];
        int i = 0;

        for (String itemId : itemsIds) {
            // Get information about the item
            final ICachedObject<ItemBean> cacheItem = aItemsCache.get(itemId);
            final ItemBean item = cacheItem.getObject();
            final long itemAge = cacheItem.getCacheAge();

            // Store the bean
            resultArray[i++] = new CachedItemBean(item, itemAge);
        }

        return resultArray;
    }

    /**
     * Uses the cache to act like {@link IErpDataProxy#getItemsStock(String[])}
     * 
     * @param aStockCache
     *            The items stock cache channel
     * @param aItemIds
     *            An array of items
     * @param aReservedItems
     *            Stock reserved for each item (carts)
     * @return The cached stock values, or an empty array
     */
    public CachedItemStockBean[] getItemsStock(
            final ICacheChannel<String, Integer> aStockCache,
            final String[] aItemIds, final Map<String, Integer> aReservedItems) {

        // Result array
        final CachedItemStockBean[] resultArray = new CachedItemStockBean[aItemIds.length];

        for (int i = 0; i < aItemIds.length; i++) {

            // Get the ID
            final String itemId = aItemIds[i];

            // Read the cache
            final ICachedObject<Integer> cachedStock = aStockCache.get(itemId);
            if (cachedStock == null) {
                continue;
            }

            // Read cached information
            int beanStock = cachedStock.getObject();
            final long stockAge = cachedStock.getCacheAge();

            // Remove the reserved stock
            final Integer cartReservation = aReservedItems.get(itemId);
            if (beanStock != -1 && cartReservation != null) {
                beanStock -= cartReservation.intValue();
            }

            if (beanStock != -1) {
                // Cache is valid, prepare a bean
                final CachedItemStockBean stockBean = new CachedItemStockBean(
                        itemId, beanStock, stockAge);
                resultArray[i] = stockBean;
            }
        }

        return resultArray;
    }

    /**
     * Retrieves aItemsCount random items from the given set.
     * 
     * If aItemsCount &lt;= 0, returns the whole list.
     * 
     * @param aItemsIds
     *            A set of item IDs
     * @param aCount
     *            Maximum number of IDs to retrieve
     * @return A random set
     */
    protected Collection<String> getRandomItems(
            final Collection<String> aItemsIds, final int aCount) {

        // Compute the number of items to retrieve
        final int itemsCount;
        if (aCount <= 0 || aCount > aItemsIds.size()) {
            itemsCount = aItemsIds.size();
        } else {
            itemsCount = aCount;
        }

        // Shuffle the IDs
        final List<String> idsList = new ArrayList<String>(aItemsIds);
        Collections.shuffle(idsList);

        // Return a sub list
        return idsList.subList(0, itemsCount);
    }

    /**
     * Returns a page of items from the given items
     * 
     * @param aItemsIds
     *            IDs to paginate
     * @param aBaseId
     *            Base page ID (excluded) or null
     * @param aItemsCount
     *            Maximum number of items in the page
     * @return The IDs to show in the page
     */
    protected Collection<String> paginateItems(
            final Collection<String> aItemsIds, final String aBaseId,
            final int aItemsCount) {

        // Basic test of items count
        if (aItemsCount <= 0 || aItemsCount >= aItemsIds.size()) {
            return aItemsIds;
        }

        if (aBaseId == null || aBaseId.isEmpty()) {
            // No base : make a sub list from the set
            final List<String> resultList = new ArrayList<String>(aItemsCount);

            int i = 0;
            for (String id : aItemsIds) {

                if (i == aItemsCount) {
                    break;
                }

                resultList.add(id);
                i++;
            }

            return resultList;

        } else if (aItemsIds.contains(aBaseId)) {
            // Base ID exists in the set

            final Iterator<String> iterator = aItemsIds.iterator();
            while (iterator.hasNext()) {
                // Go to the base ID
                final String id = iterator.next();
                if (aBaseId.equals(id)) {
                    break;
                }
            }

            // We are just after the base ID
            final List<String> resultList = new ArrayList<String>(aItemsCount);

            try {
                // Get count elements after it
                for (int i = 0; i < aItemsCount; i++) {
                    resultList.add(iterator.next());
                }

            } catch (NoSuchElementException e) {
                // There were less count elements after the base ID, ignore
                // error
            }

            // Return the list
            return resultList;

        }

        // Invalid base
        return null;
    }
}
