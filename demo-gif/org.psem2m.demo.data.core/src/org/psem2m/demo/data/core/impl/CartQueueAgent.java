/**
 * File:   CartQueueAgent.java
 * Author: Thomas Calmant
 * Date:   11 oct. 2011
 */
package org.psem2m.demo.data.core.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.demo.data.cache.ICacheDequeueChannel;
import org.psem2m.demo.data.cache.ICacheFactory;
import org.psem2m.demo.data.cache.ICachedObject;
import org.psem2m.demo.data.core.ICartQueue;
import org.psem2m.demo.erp.api.beans.CCart;
import org.psem2m.demo.erp.api.beans.CCartLine;
import org.psem2m.demo.erp.api.beans.CErpActionReport;
import org.psem2m.demo.erp.api.services.IErpDataProxy;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * Peek the first item of the cache cart queue and try to send it to the ERP
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-cart-queue-agent-factory", publicFactory = false)
@Provides(specifications = ICartQueue.class)
@Instantiate(name = "psem2m-cart-queue-agent")
public class CartQueueAgent extends CPojoBase implements ICartQueue {

    /** The cart queue cache channel */
    private static final String CACHE_CHANNEL_CART = "psem2m.demo.cartQueueAgent.cartQueue";

    /** ERP proxy iPOJO dependency ID */
    private static final String IPOJO_ERP_PROXY_ID = "erp-proxy";

    /** The cache factory service */
    @Requires
    private ICacheFactory pCacheFactory;

    /** The cart cache queue channel */
    private ICacheDequeueChannel<Serializable, CartQueueItem> pCartsChannel;

    /** The ERP proxy */
    @Requires(id = IPOJO_ERP_PROXY_ID, optional = true)
    private IErpDataProxy pErpProxy;

    /** Log Service */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Reserved item quantities */
    private final Map<String, Integer> pReservedItems = new HashMap<String, Integer>();

    /** Working thread */
    private Thread pWorker;

    /**
     * Default constructor
     */
    public CartQueueAgent() {

        super();
    }

    /**
     * Called by iPOJO when the ERP proxy is bound
     * 
     * @param aDataProxy
     *            The ERP proxy
     */
    @Bind(id = IPOJO_ERP_PROXY_ID)
    protected void bindErpProxy(final IErpDataProxy aDataProxy) {

        if (pWorker != null) {
            // Be sure we're alone
            pWorker.interrupt();
        }

        // Start the working thread
        pWorker = new Thread("cart-queue-worker") {

            @Override
            public void run() {

                while (!isInterrupted()) {
                    try {
                        // Poll and send carts
                        handleCarts();

                    } catch (InterruptedException e) {
                        // Stop on interruption
                        return;
                    }
                }
            }
        };

        pWorker.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.data.server.ICartQueue#enqueueCart(org.psem2m.demo.erp
     * .api.beans.CCart)
     */
    @Override
    public CartQueueItem enqueueCart(final CCart aCart) {

        if (aCart == null || aCart.getCartId() == null) {
            return null;
        }

        try {
            // Store the queue item
            synchronized (pCartsChannel) {

                final CartQueueItem queueItem = new CartQueueItem(aCart);
                pCartsChannel.put(queueItem, queueItem);

                // Update items reservations
                updateReservations(aCart, false);
                return queueItem;
            }

        } catch (Exception e) {
            pLogger.logSevere(this, "enqueueCart",
                    "Error adding a cart to the queue :", e);
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.server.ICartQueue#getReservedQuantites()
     */
    @Override
    public Map<String, Integer> getReservedQuantites() {

        synchronized (pReservedItems) {
            return Collections.unmodifiableMap(pReservedItems);
        }
    }

    /**
     * Handles the cart queue
     * 
     * @throws InterruptedException
     *             The treatment was interrupted
     */
    protected void handleCarts() throws InterruptedException {

        // Get and remove the first cart in the queue
        final ICachedObject<CartQueueItem> cachedItem = pCartsChannel.poll(1,
                TimeUnit.SECONDS);
        if (cachedItem == null) {
            return;
        }

        final CartQueueItem workingItem = cachedItem.getObject();
        final CCart workingCart = workingItem.getCart();

        // Try to apply the cart
        CErpActionReport report = null;
        try {
            report = pErpProxy.applyCart(workingCart);

        } catch (Exception e) {
            // Treat exceptions as an ERP error
            report = null;
        }

        if (report == null) {
            // Re-insert the cart on error
            pCartsChannel.addFirst(cachedItem);
            return;
        }

        // On success, associate the report to the cart item
        workingItem.setReport(report);

        // Release items reservations
        updateReservations(workingCart, true);

        // Release the semaphore
        workingItem.getSemaphore().release();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        // Empty the cart queue
        pCartsChannel.clear();

        // Stop the working thread
        if (pWorker != null) {
            pWorker.interrupt();
            pWorker = null;
        }

        pLogger.logInfo(this, "invalidatePojo", "Cart Queue Agent Gone");
    }

    /**
     * Called by iPOJO when the ERP proxy service is gone
     * 
     * @param aDataProxy
     *            The ERP proxy
     */
    @Unbind(id = IPOJO_ERP_PROXY_ID)
    protected void unbindErpProxy(final IErpDataProxy aDataProxy) {

        // Stop the working thread
        if (pWorker != null) {
            pWorker.interrupt();
            pWorker = null;
        }
    }

    /**
     * Updates the items reservation map
     * 
     * @param aCart
     *            A new cart to take into account
     * @param aAppliedCart
     *            The cart has just been applied (remove items from reservation)
     */
    protected void updateReservations(final CCart aCart,
            final boolean aAppliedCart) {

        synchronized (pReservedItems) {

            for (CCartLine cartLine : aCart.getCartLines()) {
                // For each line...
                final String itemId = cartLine.getItemId();
                final Integer previousReservedQuantityObj = pReservedItems
                        .get(itemId);

                // Get the cart reserved quantity
                final int cartQuantity = (int) cartLine.getQuantity();

                // Compute the previous reserved quantity
                int previousReservedQuantity = 0;
                if (previousReservedQuantityObj != null) {
                    previousReservedQuantity = previousReservedQuantityObj
                            .intValue();
                }

                // Compute the new reserved quantity
                int newReservedQuantity = previousReservedQuantity;

                if (aAppliedCart) {
                    // Cart has been applied and is no more reserved
                    newReservedQuantity -= cartQuantity;

                } else {
                    // Cart waiting for treatment
                    newReservedQuantity += cartQuantity;
                }

                // Normalize (just in case)
                if (newReservedQuantity < 0) {
                    newReservedQuantity = 0;
                }

                pReservedItems.put(itemId, newReservedQuantity);
            }
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

        // Reset the cart queue
        pCartsChannel = pCacheFactory.openDequeueChannel(CACHE_CHANNEL_CART);

        pLogger.logInfo(this, "validatePojo", "Cart Queue Agent Ready");
    }
}
