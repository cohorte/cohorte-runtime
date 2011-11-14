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
import org.psem2m.composer.test.api.IComponent;
import org.psem2m.demo.data.cache.ICacheDequeueChannel;
import org.psem2m.demo.data.cache.ICacheFactory;
import org.psem2m.demo.data.cache.ICachedObject;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
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
    @Property(name = "cartItemId")
    private String pCartItemIdKey;

    /** The item quantity key in a cart line */
    @Property(name = "cartItemQuantity")
    private String pCartItemQuantitydKey;

    /** The instance name */
    @Property(name = DemoComponentsConstants.PROPERTY_INSTANCE_NAME)
    private String pInstanceName;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The next component of the chain */
    @Requires(id = DemoComponentsConstants.WIRE_NEXT)
    private IComponent pNext;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.test.api.IComponent#computeResult(java.util.Map)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> computeResult(final Map<String, Object> aData)
            throws Exception {

        // Call the chain...
        final Map<String, Object> computedMap = pNext.computeResult(aData);

        // Open the channel
        final ICacheDequeueChannel<Serializable, Serializable> channel = pCache
                .openDequeueChannel(pCartChannelName);

        // Get a copy of the queue
        final BlockingDeque<ICachedObject<Serializable>> queueCopy = new LinkedBlockingDeque<ICachedObject<Serializable>>(
                channel);

        // Reserved quantities map
        final Map<String, Integer> reservedQuantities = new HashMap<String, Integer>();

        // Get reserved quantities
        for (final ICachedObject<Serializable> cachedObject : queueCopy) {

            final Map<String, Object> cartMap = (Map<String, Object>) cachedObject
                    .getObject();

            for (final Map<String, Object> cartLine : ((Map<String, Object>[]) cartMap
                    .get("lines"))) {

                final String itemId = (String) cartLine.get(pCartItemIdKey);
                final Integer itemQuantity = (Integer) cartLine
                        .get(pCartItemQuantitydKey);

                reservedQuantities.put(itemId, itemQuantity);
            }
        }

        // Remove reserved quantities from the returned stock
        final ICachedObject<?> result = (ICachedObject<?>) computedMap
                .get(KEY_RESULT);

        final Map<String, Integer> resultMap = (Map<String, Integer>) result
                .getObject();

        for (final String itemId : resultMap.keySet()) {

            // Get the associated map (stock, quality, ...)
            final Integer currentStock = resultMap.get(itemId);

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
                    // Something went wrong ?
                    newStock = 0;
                }

                resultMap.put(itemId, newStock);
            }
        }

        computedMap.put(IComponent.KEY_RESULT, resultMap);
        return computedMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo", "Component", pInstanceName,
                "Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo", "Component", pInstanceName,
                "Ready");
    }
}
