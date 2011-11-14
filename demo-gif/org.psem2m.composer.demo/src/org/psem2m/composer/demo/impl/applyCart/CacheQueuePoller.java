/**
 * File:   ErpCaller.java
 * Author: Thomas Calmant
 * Date:   14 nov. 2011
 */
package org.psem2m.composer.demo.impl.applyCart;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.framework.BundleException;
import org.psem2m.composer.demo.DemoComponentsConstants;
import org.psem2m.composer.test.api.IComponent;
import org.psem2m.demo.data.cache.ICacheDequeueChannel;
import org.psem2m.demo.data.cache.ICacheFactory;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * getItem treatment chain entry point
 * 
 * @author Thomas Calmant
 */
@Component(name = DemoComponentsConstants.COMPONENT_CACHE_QUEUE_POLLER)
public class CacheQueuePoller extends CPojoBase implements Runnable {

    /** The cache factory */
    @Requires
    private ICacheFactory pCache;

    /** The name of the channel to poll */
    @Property(name = "cacheChannel")
    private String pCacheChannelName;

    /** The instance name */
    @Property(name = DemoComponentsConstants.PROPERTY_INSTANCE_NAME)
    private String pInstanceName;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The next component of the chain */
    @Requires(id = DemoComponentsConstants.WIRE_NEXT)
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
    public void invalidatePojo() throws BundleException {

        // Stop running thread
        pRunning = false;
        pThread.interrupt();

        pLogger.logInfo(this, "invalidatePojo", "Component", pInstanceName,
                "Gone");
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

                @SuppressWarnings("unchecked")
                final Map<String, Object> content = (Map<String, Object>) channel
                        .poll(500, TimeUnit.MILLISECONDS);

                if (content != null) {
                    // Call the next component
                    try {
                        pNext.computeResult(content);

                    } catch (final Exception e) {
                        pLogger.logSevere(this, "Polling Cache Queue",
                                "Error handling a cached queue object", e);
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
    public void validatePojo() throws BundleException {

        // Start the polling thread
        pThread = new Thread(this);
        pThread.setDaemon(true);

        pRunning = true;
        pThread.start();

        pLogger.logInfo(this, "validatePojo", "Component", pInstanceName,
                "Ready");
    }
}
