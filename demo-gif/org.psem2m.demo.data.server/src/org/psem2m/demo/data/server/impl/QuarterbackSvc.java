/**
 * File:   QuarterbackSvc.java
 * Author: Thomas Calmant
 * Date:   3 oct. 2011
 */
package org.psem2m.demo.data.server.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.demo.data.cache.IDataCache;
import org.psem2m.demo.data.server.IQuarterback;
import org.psem2m.demo.erp.api.beans.CachedItemBean;
import org.psem2m.demo.erp.api.beans.CachedItemStockBean;
import org.psem2m.demo.erp.api.beans.IQualityLevels;
import org.psem2m.demo.erp.api.beans.ItemBean;
import org.psem2m.demo.erp.api.services.IErpDataProxy;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * Main strategy handler
 * 
 * @author Thomas Calmant
 */
@Component(name = "demo-quarterback-factory", publicFactory = false)
@Provides(specifications = IQuarterback.class)
@Instantiate(name = "demo-quarterback")
public class QuarterbackSvc extends CPojoBase implements IQuarterback {

    /** ERP Proxy variable iPOJO ID */
    private static final String IPOJO_ID_ERP = "erp-proxy";

    /** ERP Proxy presence flag */
    @ServiceProperty(name = "erp.available")
    private boolean isProxyAvailable = false;

    /** The internal cache */
    @Requires
    private IDataCache pCache;

    /** ERP Proxy (imported service) */
    @Requires(id = IPOJO_ID_ERP, optional = true)
    private IErpDataProxy pErpProxy;

    /** Log service */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /**
     * Default constructor
     */
    public QuarterbackSvc() {

        super();
    }

    /**
     * Called by iPOJO when the ERP proxy is bound
     * 
     * @param aErpDataProxy
     *            The ERP Proxy service
     */
    @Bind(id = IPOJO_ID_ERP)
    protected void bindErp(final IErpDataProxy aErpDataProxy) {

        // Update the flag
        isProxyAvailable = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.erp.api.services.IErpData#getItem(java.lang.String)
     */
    @Override
    public CachedItemBean getItem(final String aItemId) {

        if (isProxyAvailable) {
            // Try with the ERP

            try {
                final ItemBean itemBean = pErpProxy.getItem(aItemId);

                if (itemBean != null) {
                    // Update the cache
                    pCache.updateItemBean(aItemId, itemBean);

                    // Return the bean
                    return new CachedItemBean(itemBean,
                            IQualityLevels.CACHE_LEVEL_SYNC);
                }

            } catch (Exception e) {
                // An error occurred using the ERP, continue with the cache
            }
        }

        // The ERP failed
        synchronized (pCache) {

            final ItemBean item = pCache.getItem(aItemId);
            final long itemAge = pCache.getItemInformationAge(aItemId);

            if (item != null && itemAge != -1) {
                // Return the cached bean
                return new CachedItemBean(item, itemAge);
            }
        }

        // Nothing found, return null
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.erp.api.services.IErpData#getItems(java.lang.String,
     * int, boolean, java.lang.String)
     */
    @Override
    public CachedItemBean[] getItems(final String aCategory,
            final int aItemsCount, final boolean aRandomize,
            final String aBaseId) {

        // Result list
        final List<CachedItemBean> pItems = new ArrayList<CachedItemBean>();

        // Flag to use the cache
        boolean useCache = true;

        if (isProxyAvailable) {
            // Proxy available : use it
            try {
                final ItemBean[] erpResult = pErpProxy.getItems(aCategory,
                        aItemsCount, aRandomize, aBaseId);

                if (erpResult != null) {
                    // ERP answered, don't use the cache
                    useCache = false;

                    // Category content for cache update
                    final List<String> categoryItemIds = new ArrayList<String>();

                    for (ItemBean bean : erpResult) {

                        // Prepare the information
                        final CachedItemBean cachedBean = new CachedItemBean(
                                bean, IQualityLevels.CACHE_LEVEL_SYNC);
                        final String beanId = bean.getId();

                        // Add it to the result list
                        pItems.add(cachedBean);

                        // Add it to the category content
                        categoryItemIds.add(beanId);

                        // Update the cache
                        synchronized (pCache) {
                            pCache.updateItemBean(beanId, bean);
                        }
                    }

                    // Update the category cache
                    synchronized (pCache) {
                        pCache.updateCategoryItems(aCategory, categoryItemIds);
                    }
                }

            } catch (Exception ex) {
                // If ERP returned null or if an error occurred -> use cache
                pLogger.logInfo(this, "getItems",
                        "Error calling the ERP Proxy service ", ex);

                useCache = true;
            }
        }

        if (useCache) {
            // Grab data from the cache
            synchronized (pCache) {

                final Set<String> categoryItems = pCache
                        .getCategoryItems(aCategory);
                if (categoryItems == null) {
                    // Empty category
                    return new CachedItemBean[0];
                }

                // Get item beans
                for (String itemId : categoryItems) {
                    synchronized (pCache) {

                        final ItemBean item = pCache.getItem(itemId);
                        final long itemAge = pCache
                                .getItemInformationAge(itemId);

                        if (item != null && itemAge != -1) {

                            // Prepare and add the cached bean
                            final CachedItemBean cachedBean = new CachedItemBean(
                                    item, itemAge);
                            pItems.add(cachedBean);
                        }
                    }
                }
            }
        }

        return pItems.toArray(new CachedItemBean[pItems.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.erp.api.services.IErpData#getItemsStock(java.lang.String
     * [])
     */
    @Override
    public CachedItemStockBean[] getItemsStock(final String[] aItemIds) {

        if (isProxyAvailable) {

            try {
                int[] itemsStock = pErpProxy.getItemsStock(aItemIds);
                if (itemsStock != null) {

                    // Result array
                    final CachedItemStockBean[] resultArray = new CachedItemStockBean[itemsStock.length];

                    // Update the cache
                    synchronized (pCache) {

                        for (int i = 0; i < aItemIds.length; i++) {

                            final String itemId = aItemIds[i];

                            // Prepare a result bean
                            resultArray[i] = new CachedItemStockBean(itemId,
                                    itemsStock[i],
                                    IQualityLevels.CACHE_LEVEL_SYNC);

                            // Update cache
                            pCache.updateItemStock(itemId, itemsStock[i]);
                        }
                    }

                    // Return the result immediately
                    return resultArray;
                }

            } catch (Exception e) {
                // If ERP returned null or if an error occurred -> use cache
            }

        }

        // Result list
        final CachedItemStockBean[] resultArray = new CachedItemStockBean[aItemIds.length];

        // Read data from the cache
        synchronized (pCache) {

            for (int i = 0; i < aItemIds.length; i++) {

                // Get the ID
                final String itemId = aItemIds[i];

                // Read the cache
                final long beanStock = pCache.getItemStock(itemId);
                final long stockAge = pCache.getItemStockInformationAge(itemId);

                if (beanStock != -1 && stockAge != -1) {
                    // Cache is valid, prepare a bean
                    final CachedItemStockBean stockBean = new CachedItemStockBean(
                            itemId, beanStock, stockAge);
                    resultArray[i] = stockBean;
                }
            }
        }

        return resultArray;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo", "QuarterbackSvc Gone");
    }

    /**
     * Tests if the ERP proxy service is available
     * 
     * @return true if the ERP proxy is available
     */
    public boolean isProxyAvailable() {

        return isProxyAvailable;
    }

    /**
     * Called by iPOJO when the ERP proxy is unbound
     * 
     * @param aErpDataProxy
     *            The ERP Proxy service
     */
    @Unbind(id = IPOJO_ID_ERP)
    protected void unbindErp(final IErpDataProxy aErpDataProxy) {

        // Update the flag
        isProxyAvailable = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo", "QuarterbackSvc Ready");
    }
}
