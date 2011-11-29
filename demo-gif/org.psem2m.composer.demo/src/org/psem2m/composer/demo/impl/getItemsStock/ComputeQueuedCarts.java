/**
 * File:   ErpCaller.java
 * Author: Thomas Calmant
 * Date:   14 nov. 2011
 */
package org.psem2m.composer.demo.impl.getItemsStock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.demo.DemoComponentsConstants;
import org.psem2m.composer.demo.IComponent;
import org.psem2m.composer.demo.IComponentContext;
import org.psem2m.demo.data.cache.ICacheDequeueChannel;
import org.psem2m.demo.data.cache.ICacheFactory;
import org.psem2m.demo.data.cache.ICachedObject;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * getItem treatment chain entry point
 * 
 * @author Thomas Calmant
 */
@Component(name = DemoComponentsConstants.COMPONENT_COMPUTE_QUEUED_CARTS)
@Provides(specifications = IComponent.class)
public class ComputeQueuedCarts extends CPojoBase implements IComponent {

    /** The cache factory */
    @Requires
    private ICacheFactory pCache;

    /** The cart channel name */
    @Property(name = "cartCacheChannel")
    private String pCartChannelName;

    /** The item ID key in a cart line */
    @Property(name = "cartItemIdKey", value = "id")
    private String pCartItemIdKey;

    /** The item quantity key in a cart line */
    @Property(name = "cartItemQuantityKey", value = "quantity")
    private String pCartItemQuantitydKey;

    /** The cart lines key */
    @Property(name = "cartLinesKey", value = "lines")
    private String pCartLinesKey;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The instance name */
    @Property(name = DemoComponentsConstants.PROPERTY_INSTANCE_NAME)
    private String pName;

    /** The next component of the chain */
    @Requires(id = DemoComponentsConstants.WIRE_NEXT)
    private IComponent pNext;

    /** The stock value in the result map */
    @Property(name = "resultStockKey", value = "stock")
    private String pResultStockKey = "stock";

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.test.api.IComponent#computeResult(org.psem2m.composer
     * .test.api.IComponentContext)
     */
    @Override
    public IComponentContext computeResult(final IComponentContext aContext)
            throws Exception {

        /* Call the chain... */
        final IComponentContext computedResult = pNext.computeResult(aContext);

        /* Reserved quantities map */
        final Map<String, Integer> reservedQuantities = getReservedQuantities();

        /* Remove reserved quantities from the returned stock */
        for (final Map<String, Object> itemStockMap : computedResult
                .getResults()) {

            // Get the associated data (stock, quality, ...)
            final String itemId = (String) itemStockMap.get(pCartItemIdKey);
            final Number currentStock = (Number) itemStockMap
                    .get(pResultStockKey);
            if (currentStock == null) {
                // Invalid stock
                continue;
            }

            // Find the item ID in the cache
            final Integer reservedQuantity = reservedQuantities.get(itemId);
            if (reservedQuantity != null) {
                // Valid value, reduce the current item stock
                int newStock = currentStock.intValue()
                        - reservedQuantity.intValue();
                if (newStock < 0) {
                    // Something went wrong ? Too old conflicting values ?
                    newStock = 0;
                }

                // Prepare a new "cached" object
                itemStockMap.put(pResultStockKey, Integer.valueOf(newStock));
            }
        }

        /*
         * Modifications are made "in-place", so there is no need to create a
         * temporary list
         */
        return computedResult;
    }

    /**
     * Computes the reserved quantity of each item referenced in carts
     * 
     * @return A itemID -&gt; reserved quantity map
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Integer> getReservedQuantities() {

        // Open the carts channel
        final ICacheDequeueChannel<Serializable, Serializable> channel = pCache
                .openDequeueChannel(pCartChannelName);

        // Get a copy of the queue
        final BlockingDeque<ICachedObject<Serializable>> queueCopy = new LinkedBlockingDeque<ICachedObject<Serializable>>(
                channel);

        // Reserved quantities map
        final Map<String, Integer> reservedQuantities = new HashMap<String, Integer>();

        // Get reserved quantities
        for (final ICachedObject<Serializable> cachedObject : queueCopy) {

            // Get the cached applyCart request context
            final IComponentContext cartContext = (IComponentContext) cachedObject
                    .getObject();

            // Get the lines (test if it's iterable)
            final Object cartLines = Utilities.arrayToIterable(cartContext
                    .getRequest().get(pCartLinesKey));
            if (!(cartLines instanceof Iterable)) {
                pLogger.logWarn(this, "getReservedQuantities",
                        "Don't know  how to read cart :", cartLines);
                continue;
            }

            for (final Map<String, Object> cartLine : ((Iterable<Map<String, Object>>) cartLines)) {

                // Get line item data
                final String itemId = (String) cartLine.get(pCartItemIdKey);

                final Number itemQuantity;
                final Object quantityObject = cartLine
                        .get(pCartItemQuantitydKey);

                if (itemId == null || quantityObject == null) {
                    // Invalid data
                    pLogger.logWarn(this, "getReservedQuantities",
                            "Unreadable cart line :", cartLine);
                    continue;
                }

                if (quantityObject instanceof Number) {
                    itemQuantity = (Number) quantityObject;

                } else {
                    // USe the object as a string
                    try {
                        itemQuantity = Integer.valueOf(String
                                .valueOf(quantityObject));
                    } catch (final Exception e) {
                        // Oups
                        pLogger.logWarn(this, "getReservedQuantities",
                                "Error reading object", itemId,
                                "reserved quantity :", quantityObject);
                        continue;
                    }
                }

                // Get reserved item stock
                final Integer alreadyReservedQuantity = reservedQuantities
                        .get(itemId);

                // Compute new reservation
                int toReserve = itemQuantity.intValue();
                if (alreadyReservedQuantity != null) {
                    toReserve += alreadyReservedQuantity.intValue();
                }

                reservedQuantities.put(itemId, Integer.valueOf(toReserve));
            }
        }

        return reservedQuantities;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo", "Component", pName, "Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo", "Component", pName, "Ready");
    }
}
