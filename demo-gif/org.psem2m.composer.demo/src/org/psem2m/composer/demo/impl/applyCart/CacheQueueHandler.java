/**
 * File:   CacheQueueHandler.java
 * Author: Thomas Calmant
 * Date:   18 nov. 2011
 */
package org.psem2m.composer.demo.impl.applyCart;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.demo.DemoComponentsConstants;
import org.psem2m.composer.test.api.ComponentContextBean;
import org.psem2m.composer.test.api.IComponent;
import org.psem2m.composer.test.api.IComponentContext;
import org.psem2m.demo.data.cache.ICacheDequeueChannel;
import org.psem2m.demo.data.cache.ICacheFactory;
import org.psem2m.demo.data.cache.ICachedObject;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * @author Thomas Calmant
 * 
 */
@Component(name = "cache-queue-handler")
@Provides(specifications = IComponent.class)
public class CacheQueueHandler extends CPojoBase implements IComponent,
        Runnable {

    /** The cache factory */
    @Requires
    private ICacheFactory pCache;

    /** The name of the channel to poll */
    @Property(name = "cacheChannel")
    private String pCacheChannelName;

    /** The cart ID key */
    @Property(name = "cartIdKey", value = "id")
    private String pCartIdKey;

    /** The cart lines key */
    @Property(name = "cartLinesKey", value = "lines")
    private String pCartLinesKey;

    /** The cart channel */
    private ICacheDequeueChannel<Serializable, Serializable> pChannel;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The instance name */
    @Property(name = DemoComponentsConstants.PROPERTY_INSTANCE_NAME)
    private String pName;

    /** The next component of the chain */
    @Requires(id = DemoComponentsConstants.WIRE_NEXT)
    private IComponent pNext;

    /** The run/stop flag */
    private boolean pRunning;

    /** Waiting semaphores... */
    private final Map<Object, Semaphore> pSemaphores = new HashMap<Object, Semaphore>();

    /** The polling thread */
    private Thread pThread;

    /** The maximum time to wait for an answer (in milliseconds) */
    @Property(name = "timeout", value = "0")
    private long pWaitTimeout;

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

        final Map<String, Object> cartMap = aContext.getRequest();

        /* Test cart ID */
        if (cartMap == null) {
            aContext.addError(pName, "Null cart");
            return aContext;
        }

        final Object cartId = cartMap.get(pCartIdKey);
        if (cartId == null) {
            aContext.addError(pName, "Cart doesn't have an ID");
            return aContext;
        }

        /* Test cart lines */
        final Object cartLinesObject = Utilities.arrayToIterable(cartMap
                .get(pCartLinesKey));

        if (!(cartLinesObject instanceof Collection)
                || ((Collection<?>) cartLinesObject).isEmpty()) {

            aContext.addError(pName, "Empty cart or invalid lines");
            return aContext;
        }

        // Set up the cart semaphore (initially blocking)
        final Semaphore semaphore = new Semaphore(0);
        synchronized (pSemaphores) {
            pSemaphores.put(cartId, semaphore);
        }

        pLogger.logInfo(this, pName, "Inserting cart in cache...", cartMap);

        /* Store the current context in the cache */
        pChannel.add(aContext);

        pLogger.logInfo(this, pName, "Waiting", pWaitTimeout, "ms max.");

        // Wait for the semaphore
        final boolean semaphoreAcquired = semaphore.tryAcquire(pWaitTimeout,
                TimeUnit.MILLISECONDS);

        // "Critical section"
        synchronized (pSemaphores) {

            // Clean the map in any case
            pSemaphores.remove(cartId);

            pLogger.logInfo(this, pName, "Acquired=", semaphoreAcquired);

            if (!semaphoreAcquired) {
                // Cart has not yet been treated, prepare a copy of the context

                pLogger.logInfo(this, pName, "So sad...");

                final ComponentContextBean context = new ComponentContextBean();
                context.setErrors(aContext.getErrors());
                context.setMetadata(aContext.getMetadata());

                // Prepare a result message
                final Map<String, Object> statusMap = new HashMap<String, Object>();
                statusMap.put("code", 300);
                statusMap.put("message", "Cart in queue.");
                statusMap.put("reason", "");

                context.setResult(statusMap);

                return context;
            }
        }

        pLogger.logInfo(this, pName, "Good !");

        // Return the current context
        return aContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        // Stop running thread
        pRunning = false;
        pThread.interrupt();

        // Release references...
        pChannel = null;
        pThread = null;

        pLogger.logInfo(this, "invalidatePojo", "Component", pName, "Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        // Get the first available object
        try {
            while (pRunning) {

                final ICachedObject<Serializable> cachedObject = pChannel.poll(
                        500, TimeUnit.MILLISECONDS);

                if (cachedObject == null) {
                    continue;
                }

                // Something to work on...
                final Object cachedObjectContent = cachedObject.getObject();

                if (cachedObjectContent instanceof IComponentContext) {

                    boolean needsReinjection = false;
                    IComponentContext resultContext = null;

                    final IComponentContext context = (IComponentContext) cachedObjectContent;
                    // Call the next component
                    try {
                        resultContext = pNext.computeResult(context);

                    } catch (final Exception e) {
                        pLogger.logSevere(this, "Polling Cache Queue",
                                "Error handling a cached queue object", e);

                        // Ask for cart re-injection
                        needsReinjection = true;
                    }

                    if (needsReinjection
                            || (resultContext.hasError() && !resultContext
                                    .hasResult())) {
                        // Something happened further in the chain : re-inject
                        pChannel.addFirst(cachedObject);

                        // Wait a little...
                        Thread.sleep(1000);

                        // Loop again
                        continue;
                    }

                    // Signal the end of treatment
                    synchronized (pSemaphores) {

                        if (resultContext != null) {
                            // Modify the calling context
                            // Errors were previously already
                            context.getErrors().clear();
                            context.getErrors().addAll(
                                    resultContext.getErrors());

                            context.getResults().clear();
                            context.getResults().addAll(
                                    resultContext.getResults());
                        }

                        // Get the cart ID
                        final Object cartId = context.getRequest().get(
                                pCartIdKey);

                        // Release the cart semaphore
                        final Semaphore semaphore = pSemaphores.get(cartId);
                        if (semaphore != null) {
                            semaphore.release();
                        }
                    }
                }
            }

        } catch (final InterruptedException e) {
            // Interrupted, ...
            if (pRunning) {
                // .. but not by this component...
                pLogger.logInfo(this, pName, "Polling thread interrupted.");
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

        // Open the cart channel
        pChannel = pCache.openDequeueChannel(pCacheChannelName);

        // Start the polling thread
        pRunning = true;
        pThread = new Thread(this);
        pThread.setDaemon(true);
        pThread.start();

        pLogger.logInfo(this, "validatePojo", "Component", pName, "Ready");
    }
}
