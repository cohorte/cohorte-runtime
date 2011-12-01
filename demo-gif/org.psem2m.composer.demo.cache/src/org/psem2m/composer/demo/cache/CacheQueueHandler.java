/**
 * File:   CacheQueueHandler.java
 * Author: Thomas Calmant
 * Date:   18 nov. 2011
 */
package org.psem2m.composer.demo.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.demo.CComponentPojo;
import org.psem2m.composer.demo.CComponentContextBean;
import org.psem2m.composer.demo.CComponentContextDumper;
import org.psem2m.composer.demo.CComponentsConstants;
import org.psem2m.composer.demo.IComponent;
import org.psem2m.composer.demo.IComponentContext;
import org.psem2m.demo.data.cache.ICacheDequeueChannel;
import org.psem2m.demo.data.cache.ICacheFactory;
import org.psem2m.demo.data.cache.ICachedObject;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;

/**
 * @author Thomas Calmant
 * 
 */
@Component(name = "cache-queue-handler")
@Provides(specifications = IComponent.class)
public class CacheQueueHandler extends CComponentPojo implements IComponent,
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
    @Property(name = CComponentsConstants.PROPERTY_INSTANCE_NAME)
    private String pName;

    /**
     * The next component of the chain => nullable = false => no mock, pNext
     * could be null !
     */
    @Requires(id = CComponentsConstants.WIRE_NEXT, nullable = false, optional = true)
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
            // log the error
            logContextError(pLogger, aContext);
            return aContext;
        }

        final Object cartId = cartMap.get(pCartIdKey);
        if (cartId == null) {
            aContext.addError(pName, "Cart doesn't have an ID");
            // log the error
            logContextError(pLogger, aContext);
            return aContext;
        }

        /* Test cart lines */
        final Object cartLinesObject = Utilities.arrayToIterable(cartMap
                .get(pCartLinesKey));

        if (!(cartLinesObject instanceof Collection)
                || ((Collection<?>) cartLinesObject).isEmpty()) {

            aContext.addError(pName, "Empty cart or invalid lines");
            // log the error
            logContextError(pLogger, aContext);
            return aContext;
        }

        // Set up the cart semaphore (initially blocking)
        final Semaphore semaphore = new Semaphore(0);
        synchronized (pSemaphores) {
            pSemaphores.put(cartId, semaphore);
        }

        pLogger.logDebug(this, "computeResult",
                "cpnt=[%25s] Inserting cart in cache : %s", getShortName(),
                cartMap);

        /* Store the current context in the cache */
        pChannel.add(aContext);

        pLogger.logDebug(this, "computeResult",
                "cpnt=[%25s] Waiting [%d] ms max.", getShortName(),
                pWaitTimeout);

        // Wait for the semaphore
        final boolean semaphoreAcquired = semaphore.tryAcquire(pWaitTimeout,
                TimeUnit.MILLISECONDS);

        // "Critical section"
        synchronized (pSemaphores) {

            // Clean the map in any case
            pSemaphores.remove(cartId);

            pLogger.logDebug(this, "computeResult",
                    "cpnt=[%25s] Acquired=[%b]", getShortName(),
                    semaphoreAcquired);

            if (!semaphoreAcquired) {
                // Cart has not yet been treated, prepare a copy of the context

                pLogger.logDebug(this, "computeResult",
                        "cpnt=[%25s] So sad...", getShortName());

                final CComponentContextBean context = new CComponentContextBean();
                // context.setErrors(aContext.getErrors());
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

        pLogger.logInfo(this, "computeResult", "cpnt=[%25s] Good, cart sent !",
                getShortName());

        // Return the current context
        return aContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.demo.impl.CComposable#getName()
     */
    @Override
    public String getName() {

        return pName;
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
        threadStop();

        // Release references...
        pChannel = null;

        pLogger.logInfo(this, "invalidatePojo", "cpnt=[%25s] Gone",
                getShortName());
    }

    /**
     * @param aNext
     */
    @Bind(id = NEXT_FIELD_ID)
    public void nextBind(final IComponent aNext) {

        pLogger.logInfo(this, "nextBind", "cpnt=[%25s] next=[%s]",
                getShortName(), String.valueOf(aNext));

        // Start the polling thread
        threadStart();
    }

    /**
     * @param aNext
     */
    @Unbind(id = NEXT_FIELD_ID)
    public void nextUnbind(final IComponent aNext) {

        pLogger.logInfo(this, "nextUnbind", "cpnt=[%25s] next=[%s]",
                getShortName(), String.valueOf(aNext));

        // Stop running thread
        threadStop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        pLogger.logDebug(this, "run", "cpnt=[%25s] Thread started",
                getShortName());

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

                        // if pNext is a Mock ( pNext is not null )
                        needsReinjection = resultContext == null;

                    } catch (final Exception e) {
                        pLogger.logSevere(this, "run",
                                "cpnt=[%25s] Polling Cache Queue ERROR : %s",
                                getShortName(), e);

                        // Ask for cart re-injection
                        needsReinjection = true;
                    }

                    if (pLogger.isLogDebugOn()) {
                        pLogger.logDebug(
                                this,
                                "run",
                                "cpnt=[%25s] needsReinjection=[%b] result=[%s]",
                                getShortName(),
                                needsReinjection,
                                resultContext != null ? CComponentContextDumper
                                        .dump(resultContext)
                                        : "resultContext is null");
                    }

                    if (needsReinjection || resultContext.hasError()
                            && !resultContext.hasResult()) {
                        // Something happened further in the chain : re-inject
                        pChannel.addFirst(cachedObject);

                        if (pLogger.isLogDebugOn()) {
                            pLogger.logDebug(this, "run",
                                    "cpnt=[%25s] Reinjection OK",
                                    getShortName());
                        }

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
                pLogger.logInfo(this, "run",
                        "cpnt=[%25s] Polling thread interrupted.",
                        getShortName());
            }
        }

        pLogger.logDebug(this, "run", "cpnt=[%25s] Thread stopped",
                getShortName());
    }

    /**
     * 
     */
    private void threadStart() {

        pLogger.logInfo(this, "threadStart", "cpnt=[%25s]", getShortName());

        pRunning = true;
        pThread = new Thread(this);
        pThread.setDaemon(true);
        pThread.start();
    }

    /**
     * 
     */
    private void threadStop() {

        pLogger.logInfo(this, "threadStop", "cpnt=[%25s]", getShortName());

        pRunning = false;
        if (pThread != null) {
            pThread.interrupt();
            pThread = null;
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

        pLogger.logInfo(this, "validatePojo", "cpnt=[%25s] Ready",
                getShortName());
    }
}
