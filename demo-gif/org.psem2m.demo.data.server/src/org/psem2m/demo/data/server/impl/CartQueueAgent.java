/**
 * File:   CartQueueAgent.java
 * Author: Thomas Calmant
 * Date:   11 oct. 2011
 */
package org.psem2m.demo.data.server.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
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
import org.psem2m.demo.data.server.ICartQueue;
import org.psem2m.demo.erp.api.beans.CCart;
import org.psem2m.demo.erp.api.beans.CErpActionReport;
import org.psem2m.demo.erp.api.services.IErpDataProxy;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * Peek the first item of the cache cart queue and try to send it to the ERP
 * 
 * @author Thomas Calmant
 */
@Component(name = "cart-queue-agent-factory", publicFactory = false)
@Provides(specifications = ICartQueue.class)
@Instantiate(name = "cart-queue-agent")
public class CartQueueAgent extends CPojoBase implements ICartQueue {

    /** ERP proxy iPOJO dependency ID */
    private static final String IPOJO_ERP_PROXY_ID = "erp-proxy";

    /** Reports map */
    private final Map<String, CErpActionReport> pActionReports = new HashMap<String, CErpActionReport>();

    /** The cart queue */
    private final BlockingDeque<CCart> pCartsQueue = new LinkedBlockingDeque<CCart>();

    /** The ERP proxy */
    @Requires(id = IPOJO_ERP_PROXY_ID, optional = true)
    private IErpDataProxy pErpProxy;

    /** Log Service */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Cart - Semaphore association */
    private final Map<String, Semaphore> pSemaphores = new HashMap<String, Semaphore>();

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
    public Semaphore enqueueCart(final CCart aCart) {

        if (aCart == null || aCart.getCartId() == null) {
            return null;
        }

        try {
            // Associate a semaphore to the cart
            final Semaphore cartSemaphore = new Semaphore(0);
            pSemaphores.put(aCart.getCartId(), cartSemaphore);

            if (pCartsQueue.add(aCart)) {
                return cartSemaphore;
            }

        } catch (Exception e) {
            pLogger.logSevere(this, "enqueueCart",
                    "Error adding a cart to the queue :", e);
        }

        return null;
    }

    /**
     * Retrieves and removes the ERP action report from the agent result map
     * 
     * @param aCart
     *            A cart
     * @return The associated report, null on error or if the cart has not yet
     *         been treated.
     */
    @Override
    public CErpActionReport getActionReport(final CCart aCart) {

        if (aCart == null || aCart.getCartId() == null) {
            // Invalid cart
            return null;
        }

        // Get and remove the report
        final String cartId = aCart.getCartId();
        final CErpActionReport report = pActionReports.get(cartId);
        pActionReports.remove(cartId);

        return report;
    }

    /**
     * Handles the cart queue
     * 
     * @throws InterruptedException
     *             The treatment was interrupted
     */
    protected void handleCarts() throws InterruptedException {

        // Get the first cart in the queue
        final CCart workingCart = pCartsQueue.poll(1, TimeUnit.SECONDS);
        if (workingCart == null) {
            return;
        }

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
            pCartsQueue.addFirst(workingCart);
            return;
        }

        // On success, associate the report to the cart
        final String workingCartId = workingCart.getCartId();
        pActionReports.put(workingCartId, report);

        // Release the semaphore and remove it from the map
        pSemaphores.get(workingCartId).release();
        pSemaphores.remove(workingCartId);
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
        pCartsQueue.clear();

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

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        // Reset the cart queue
        pCartsQueue.clear();

        pLogger.logInfo(this, "validatePojo", "Cart Queue Agent Ready");
    }
}
