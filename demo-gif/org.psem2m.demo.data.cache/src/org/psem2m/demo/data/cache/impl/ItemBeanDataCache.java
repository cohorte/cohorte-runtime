/**
 * File:   ItemBeanDataCache.java
 * Author: Thomas Calmant
 * Date:   3 oct. 2011
 */
package org.psem2m.demo.data.cache.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.demo.data.cache.IDataCache;
import org.psem2m.demo.erp.api.beans.ItemBean;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * Items informations cache
 * 
 * @author Thomas Calmant
 */
@Component(name = "demo-data-cache-factory", publicFactory = false)
@Provides(specifications = IDataCache.class)
@Instantiate(name = "demo-data-cache")
public class ItemBeanDataCache extends CPojoBase implements IDataCache {

    /** Categories content. Key = Category name, Value = Array of item IDs */
    private final Map<String, Set<String>> pCategoriesItems = new HashMap<String, Set<String>>();

    /** Age of category data. Key = item ID, Value = last update time stamp */
    private final Map<String, Long> pCategoriesItemsAge = new HashMap<String, Long>();

    /** Item cache. Key = Item ID, Value = Item description */
    private final Map<String, ItemBean> pItemsCache = new HashMap<String, ItemBean>();

    /** Age of item data. Key = item ID, Value = last update time stamp */
    private final Map<String, Long> pItemsCacheAge = new HashMap<String, Long>();

    /** Stock cache. Key = item ID, Value = Stock available */
    private final Map<String, Long> pItemsStock = new HashMap<String, Long>();

    /**
     * Age of item stock data. Key = item ID, Value = last stock update time
     * stamp
     */
    private final Map<String, Long> pItemsStockAge = new HashMap<String, Long>();

    /** Log service */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /**
     * Default constructor
     */
    public ItemBeanDataCache() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.data.cache.impl.IDataCache#getCategoryInformationAge(
     * java.lang.String)
     */
    @Override
    public long getCategoryInformationAge(final String aCategory) {

        final Long categoryAge = pCategoriesItemsAge.get(aCategory);
        if (categoryAge == null) {
            return -1;
        }

        return categoryAge.longValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.data.cache.impl.IDataCache#getCategoryItems(java.lang
     * .String)
     */
    @Override
    public Set<String> getCategoryItems(final String aCategory) {

        return pCategoriesItems.get(aCategory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.cache.impl.IDataCache#getItem(java.lang.String)
     */
    @Override
    public ItemBean getItem(final String aItemId) {

        return pItemsCache.get(aItemId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.data.cache.impl.IDataCache#getItemInformationAge(java
     * .lang.String)
     */
    @Override
    public long getItemInformationAge(final String aItemId) {

        final Long itemAge = pItemsCacheAge.get(aItemId);
        if (itemAge == null) {
            return -1;
        }

        return itemAge.longValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.data.cache.impl.IDataCache#getItemStock(java.lang.String)
     */
    @Override
    public long getItemStock(final String aItemId) {

        final Long stock = pItemsStock.get(aItemId);
        if (stock == null) {
            return -1;
        }

        return stock.longValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.data.cache.impl.IDataCache#getItemStockInformationAge
     * (java.lang.String)
     */
    @Override
    public long getItemStockInformationAge(final String aItemId) {

        final Long stockAge = pItemsStockAge.get(aItemId);
        if (stockAge == null) {
            return -1;
        }

        return stockAge.longValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.cache.IDataCache#getRandomItem()
     */
    @Override
    public ItemBean getRandomItem() {

        if (pItemsCache.isEmpty()) {
            return null;
        }

        final int nbItems = pItemsCache.size();
        final int randomItem = (int) Math.random() * nbItems;

        // Get the ID at the selected index
        String itemId = null;
        final Iterator<String> iterator = pItemsCache.keySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {

            if (i == randomItem) {
                // Found !
                itemId = iterator.next();
                break;
            }

            // Continue
            i++;
            iterator.next();
        }

        return pItemsCache.get(itemId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo", "Data cache Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.data.cache.impl.IDataCache#updateCategoryItems(java.lang
     * .String, java.util.Collection)
     */
    @Override
    public void updateCategoryItems(final String aCategory,
            final Collection<String> aItemIds) {

        if (aCategory == null) {
            // Invalid data
            return;
        }

        if (aItemIds == null) {
            // Category suppression
            pCategoriesItems.remove(aCategory);
            pCategoriesItemsAge.remove(aCategory);
            return;
        }

        // Set or clear the actual category content
        Set<String> categoryItems = pCategoriesItems.get(aCategory);
        if (categoryItems == null) {
            // Set up a new set
            categoryItems = new LinkedHashSet<String>();
            pCategoriesItems.put(aCategory, categoryItems);

        } else {
            // Reset the old one
            categoryItems.clear();
        }

        // Update values and age
        categoryItems.addAll(aItemIds);
        pCategoriesItemsAge.put(aCategory, System.currentTimeMillis());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.data.cache.impl.IDataCache#updateItemBean(java.lang.String
     * , org.psem2m.demo.erp.api.beans.ItemBean)
     */
    @Override
    public void updateItemBean(final String aItemId, final ItemBean aBean) {

        if (aItemId == null) {
            // Invalid data
            return;
        }

        if (aBean == null) {
            // Bean suppression
            pItemsCache.remove(aItemId);
            pItemsCache.remove(aItemId);
            return;
        }

        // Update stock
        pItemsCache.put(aItemId, aBean);

        // Update age
        pItemsCacheAge.put(aItemId, System.currentTimeMillis());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.data.cache.impl.IDataCache#updateItemStock(java.lang.
     * String, long)
     */
    @Override
    public void updateItemStock(final String aItemId, final long aStock) {

        if (aItemId == null) {
            // Invalid data
            return;
        }

        if (aStock < 0) {
            // Stock information suppression
            pItemsStock.remove(aItemId);
            pItemsStockAge.remove(aItemId);
            return;
        }

        // Update stock
        pItemsStock.put(aItemId, aStock);

        // Update age
        pItemsStockAge.put(aItemId, System.currentTimeMillis());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo", "Data cache Ready");
    }
}
