/**
 * File:   ErpCaller.java
 * Author: Thomas Calmant
 * Date:   14 nov. 2011
 */
package org.psem2m.composer.demo.cache;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.demo.CComponentsConstants;
import org.psem2m.composer.demo.IComponent;
import org.psem2m.composer.demo.IComponentContext;
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
@Component(name = CComponentsConstants.COMPONENT_CACHE_QUEUE_POLLER)
public class CacheQueuePoller extends CPojoBase implements Runnable {

    /** The cache factory */
    @Requires
    private ICacheFactory pCache;

    /** The name of the channel to poll */
    @Property(name = "cacheChannel")
    private String pCacheChannelName;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The instance name */
    @Property(name = CComponentsConstants.PROPERTY_INSTANCE_NAME)
    private String pName;

    /** The next component of the chain */
    @Requires(id = CComponentsConstants.WIRE_NEXT)
    private IComponent pNext;

    /** The run/stop flag */
    private boolean pRunning;

    /** The polling thread */
    private Thread pThread;

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

        pLogger.logInfo(this, "invalidatePojo", "Component", pName, "Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        // Get the channel
        final ICacheDequeueChannel<?, Serializable> channel = pCache
                .openDequeueChannel(pCacheChannelName);

        // Get the first available object
        try {
            while (pRunning) {

                final ICachedObject<?> cachedObject = channel.poll(500,
                        TimeUnit.MILLISECONDS);

                if (cachedObject != null) {
                    // Something to work on...
                    final Object cachedObjectContent = cachedObject.getObject();

                    if (cachedObjectContent instanceof IComponentContext) {

                        final IComponentContext context = (IComponentContext) cachedObjectContent;
                        // Call the next component
                        try {
                            pNext.computeResult(context);

                        } catch (final Exception e) {
                            pLogger.logSevere(this, "Polling Cache Queue",
                                    "Error handling a cached queue object", e);

                            context.addError(pName,
                                    "Error handling a cached queue object", e);
                        }

                        // TODO signal the end of treatment
                    }

                }
            }

        } catch (final InterruptedException e) {
            // Interrupted, do nothing more
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

        // Start the polling thread
        pThread = new Thread(this);
        pThread.setDaemon(true);

        pRunning = true;
        pThread.start();

        pLogger.logInfo(this, "validatePojo", "Component", pName, "Ready");
    }
}
