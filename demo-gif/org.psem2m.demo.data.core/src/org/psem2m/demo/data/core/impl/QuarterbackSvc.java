/**
 * File:   QuarterbackSvc.java
 * Author: Thomas Calmant
 * Date:   3 oct. 2011
 */
package org.psem2m.demo.data.core.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Controller;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.demo.data.cache.ICacheChannel;
import org.psem2m.demo.data.cache.ICacheFactory;
import org.psem2m.demo.data.cache.ICachedObject;
import org.psem2m.demo.data.core.ICartQueue;
import org.psem2m.demo.data.core.IQuarterback;
import org.psem2m.demo.erp.api.beans.CCart;
import org.psem2m.demo.erp.api.beans.CErpActionReport;
import org.psem2m.demo.erp.api.beans.CachedItemBean;
import org.psem2m.demo.erp.api.beans.CachedItemStockBean;
import org.psem2m.demo.erp.api.beans.IQualityLevels;
import org.psem2m.demo.erp.api.beans.ItemBean;
import org.psem2m.demo.erp.api.services.IErpDataProxy;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.remote.signals.ISignalData;
import org.psem2m.isolates.services.remote.signals.ISignalListener;
import org.psem2m.isolates.services.remote.signals.ISignalReceiver;

/**
 * Main strategy handler
 * 
 * @author Thomas Calmant
 */
@Component(name = "demo-quarterback-factory", publicFactory = false)
@Provides(specifications = IQuarterback.class)
@Instantiate(name = "demo-quarterback")
public class QuarterbackSvc extends CPojoBase implements IQuarterback,
        ISignalListener {

    /** Items categories cache channel name */
    protected static final String CACHE_CATEGORIES_NAME = "org.psem2m.demo.quarterback.categories";

    /** Items cache channel name */
    protected static final String CACHE_ITEMS_NAME = "org.psem2m.demo.quarterback.items";

    /** Items stock cache channel name */
    protected static final String CACHE_STOCKS_NAME = "org.psem2m.demo.quarterback.stock";

    /** ERP Proxy variable iPOJO ID */
    private static final String IPOJO_ID_ERP = "erp-proxy";

    /** The cart agent timeout system property (in seconds) */
    public static final String PROPERTY_CART_AGENT_TIMEOUT = "org.psem2m.quarterback.cartAgentTimeout";

    /** Signal to toggle the component state */
    public static final String SIGNAL_TOGGLE_COMPONENT = "/demo/core/quarterback/toggle";

    /** ERP Proxy presence flag */
    @ServiceProperty(name = "erp.available")
    private boolean isProxyAvailable = false;

    /** Cached ERP */
    private CachedErp pCachedErp;

    /** The internal cache */
    @Requires
    private ICacheFactory pCacheFactory;

    /** The cart queue agent */
    @Requires
    private ICartQueue pCartAgent;

    /** The cart agent timeout (in seconds) */
    private int pCartAgentTimeout;

    /** Items categories cache channel */
    private ICacheChannel<String, HashSet<String>> pChannelCategories;

    /** Items cache channel */
    private ICacheChannel<String, ItemBean> pChannelItems;

    /** Items stock cache */
    private ICacheChannel<String, Integer> pChannelStocks;

    /** ERP Proxy (imported service) */
    @Requires(id = IPOJO_ID_ERP, optional = true)
    private IErpDataProxy pErpProxy;

    /** Log service */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** iPOJO component controller */
    @Controller
    private boolean pQuarterbackActivated;

    /** The cache executor */
    private ScheduledExecutorService pUpdateExecutor;

    /** The cache updater runnable */
    private Runnable pUpdateRunnable = new Runnable() {

        @Override
        public void run() {

            updateCache();
        }
    };

    /**
     * Default constructor
     */
    public QuarterbackSvc() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.erp.api.services.IErpData#applyCart(org.psem2m.demo.erp
     * .api.beans.CCartLine[])
     */
    @Override
    public CErpActionReport applyCart(final CCart aCart) {

        // Enqueue the cart in the agent
        final CartQueueItem queuedItem = pCartAgent.enqueueCart(aCart);
        if (queuedItem == null) {
            return new CErpActionReport(500, "Error enqueueing the cart");
        }

        // Wait for an answer during 2 seconds
        try {
            queuedItem.getSemaphore().tryAcquire(pCartAgentTimeout,
                    TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            // We've been interrupted, ignore that...
        }

        CErpActionReport report = queuedItem.getReport();
        if (report == null) {
            // The cart has not yet been handled
            report = new CErpActionReport(300, "The cart '" + aCart.getCartId()
                    + "' is in the treatment queue",
                    "The agent took more than " + pCartAgentTimeout
                            + " seconds to do its job.");
        }

        return report;
    }

    /**
     * Called by iPOJO when the ERP proxy is bound
     */
    @Bind(id = IPOJO_ID_ERP)
    protected void bindErp() {

        // Update the flag
        isProxyAvailable = true;
    }

    /**
     * Called by iPOJO when the SSR is bound
     * 
     * @param aSignalReceiver
     *            The SSR service
     */
    @Bind
    protected void bindSignalReceiver(final ISignalReceiver aSignalReceiver) {

        // Register to the component toggle signal
        aSignalReceiver.registerListener(SIGNAL_TOGGLE_COMPONENT, this);
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
                    pChannelItems.put(aItemId, itemBean);

                    // Flush the cache
                    pCacheFactory.flush();

                    // Return the bean
                    return new CachedItemBean(itemBean,
                            IQualityLevels.CACHE_LEVEL_SYNC);
                }

            } catch (Exception e) {
                // An error occurred using the ERP, continue with the cache
            }
        }

        // The ERP failed, use the cache
        synchronized (pChannelItems) {
            return pCachedErp.getItem(pChannelItems, aItemId);
        }
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

        if (isProxyAvailable) {
            // Proxy available : use it
            try {
                final ItemBean[] erpResult = pErpProxy.getItems(aCategory,
                        aItemsCount, aRandomize, aBaseId);

                if (erpResult != null) {

                    // Result list
                    final List<CachedItemBean> pItems = new ArrayList<CachedItemBean>();

                    // Category content for cache update
                    final HashSet<String> categoryItemIds = new LinkedHashSet<String>();

                    // Add the currently cached category IDs
                    final ICachedObject<HashSet<String>> cachedCategory = pChannelCategories
                            .get(aCategory);
                    if (cachedCategory != null) {
                        categoryItemIds.addAll(cachedCategory.getObject());
                    }

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
                        synchronized (pChannelItems) {
                            pChannelItems.put(beanId, bean);
                        }
                    }

                    // Update the category cache
                    synchronized (pChannelCategories) {
                        pChannelCategories.put(aCategory, categoryItemIds);
                    }

                    // Flush the cache
                    pCacheFactory.flush();

                    return pItems.toArray(new CachedItemBean[pItems.size()]);
                }

            } catch (Exception ex) {
                // If ERP returned null or if an error occurred -> use cache
                pLogger.logInfo(this, "getItems",
                        "Error calling the ERP Proxy service ", ex);
            }
        }

        // Grab data from the cache
        synchronized (pChannelCategories) {
            synchronized (pChannelItems) {

                return pCachedErp.getItems(pChannelCategories, pChannelItems,
                        aCategory, aItemsCount, aRandomize, aBaseId);
            }
        }
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

        // Compute the carts information
        final Map<String, Integer> cartsReservations = pCartAgent
                .getReservedQuantites();

        if (isProxyAvailable) {
            // Use the ERP if possible
            try {
                int[] itemsStock = pErpProxy.getItemsStock(aItemIds);
                if (itemsStock != null) {

                    // Result array
                    final CachedItemStockBean[] resultArray = new CachedItemStockBean[itemsStock.length];

                    // Update the cache
                    synchronized (pChannelStocks) {

                        for (int i = 0; i < aItemIds.length; i++) {

                            final String itemId = aItemIds[i];
                            final Integer cartReservation = cartsReservations
                                    .get(itemId);

                            int resultStock = itemsStock[i];
                            if (cartReservation != null) {
                                // Remove reserved items
                                resultStock -= cartReservation.intValue();
                            }

                            // Prepare a result bean
                            resultArray[i] = new CachedItemStockBean(itemId,
                                    resultStock,
                                    IQualityLevels.CACHE_LEVEL_SYNC);

                            // Update cache with the real ERP value
                            pChannelStocks.put(itemId, itemsStock[i]);
                        }
                    }

                    // Flush the cache
                    pCacheFactory.flush();

                    // Return the result immediately
                    return resultArray;
                }

            } catch (Exception e) {
                // If ERP returned null or if an error occurred -> use cache
            }

        }

        // Returns the cached ERP result
        synchronized (pChannelStocks) {
            return pCachedErp.getItemsStock(pChannelStocks, aItemIds,
                    cartsReservations);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalListener#
     * handleReceivedSignal(java.lang.String,
     * org.psem2m.isolates.services.remote.signals.ISignalData)
     */
    @Override
    public void handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        if (SIGNAL_TOGGLE_COMPONENT.equals(aSignalName)) {

            pLogger.logInfo(this, "handleReceivedSignal",
                    "Quarterback toggle signal received");

            // Toggle the component state : (in)validate will trace the change
            pQuarterbackActivated = !pQuarterbackActivated;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        // Stop the cache updater
        pUpdateExecutor.shutdownNow();

        // Close cache channels
        // pCacheFactory.closeChannel(CACHE_CATEGORIES_NAME);
        // pCacheFactory.closeChannel(CACHE_ITEMS_NAME);
        // pCacheFactory.closeChannel(CACHE_STOCKS_NAME);

        // Flush the cache
        pCacheFactory.flush();

        // Forget the cached ERP
        pCachedErp = null;
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
     */
    @Unbind(id = IPOJO_ID_ERP)
    protected void unbindErp() {

        // Update the flag
        isProxyAvailable = false;
    }

    /**
     * Called by the scheduler at fixed intervals to update the cache if
     * possible
     */
    public void updateCache() {

        if (!isProxyAvailable) {
            // ERP is absent, do nothing
            pLogger.logInfo(this, "updateCache", "ERP is absent");
            return;
        }

        pLogger.logInfo(this, "updateCache", "Update cache");

        // Update categories
        updateCategoryCache("screens");
        updateCategoryCache("mouses");
    }

    /**
     * Calls the ERP to get all items in the given category and their stock.
     * Those calls will update the cache.
     * 
     * @param aCategory
     *            Category to be updated
     */
    protected void updateCategoryCache(final String aCategory) {

        if (!isProxyAvailable) {
            // No ERP, no update
            return;
        }

        // Retrieve **all** items of the given category
        final CachedItemBean[] items = getItems(aCategory, -1, false, "");
        if (items == null) {
            // No items for the given category...
            return;
        }

        // Get items IDs
        final List<String> itemsIds = new ArrayList<String>(items.length);
        for (CachedItemBean item : items) {
            if (item.getQualityLevel() == IQualityLevels.CACHE_LEVEL_SYNC) {
                // Only look for synchronized items
                itemsIds.add(item.getId());
            }
        }

        // Update the stocks (don't care about error)
        if (!itemsIds.isEmpty()) {
            getItemsStock(itemsIds.toArray(new String[itemsIds.size()]));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        try {
            // Read the cart agent timeout value (seconds)
            final String timeoutStr = System
                    .getProperty(PROPERTY_CART_AGENT_TIMEOUT);

            pCartAgentTimeout = Integer.parseInt(timeoutStr);

        } catch (NumberFormatException e) {
            // Default : 3 seconds
            pCartAgentTimeout = 3;
        }

        // Open cache channels
        pChannelCategories = pCacheFactory.openChannel(CACHE_CATEGORIES_NAME);
        pChannelItems = pCacheFactory.openChannel(CACHE_ITEMS_NAME);
        pChannelStocks = pCacheFactory.openChannel(CACHE_STOCKS_NAME);

        // Prepare the cached ERP
        pCachedErp = new CachedErp();

        // Prepare the cache updater
        pUpdateExecutor = Executors.newScheduledThreadPool(1);
        pUpdateExecutor.scheduleAtFixedRate(pUpdateRunnable, 10, 60,
                TimeUnit.SECONDS);

        pLogger.logInfo(this, "validatePojo", "QuarterbackSvc Ready");
    }
}
