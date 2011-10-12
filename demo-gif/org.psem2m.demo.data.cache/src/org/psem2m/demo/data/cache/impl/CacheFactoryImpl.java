/**
 * File:   CacheFactoryImpl.java
 * Author: Thomas Calmant
 * Date:   11 oct. 2011
 */
package org.psem2m.demo.data.cache.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.demo.data.cache.ICacheChannel;
import org.psem2m.demo.data.cache.ICacheDequeueChannel;
import org.psem2m.demo.data.cache.ICacheFactory;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * Implementation of a cache channel factory
 * 
 * @author Thomas Calmant
 */
@Component(name = "cache-channel-factory-factory", publicFactory = false)
@Provides(specifications = ICacheFactory.class)
@Instantiate(name = "cache-channel-factory")
public class CacheFactoryImpl extends CPojoBase implements ICacheFactory {

    /** Opened standard channels */
    private final Map<String, ICacheChannel<?, ?>> pChannels = new HashMap<String, ICacheChannel<?, ?>>();

    /** Log service */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Opened queued channels */
    private final Map<String, ICacheDequeueChannel<?, ?>> pQueueChannels = new HashMap<String, ICacheDequeueChannel<?, ?>>();

    /**
     * Default constructor
     */
    public CacheFactoryImpl() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.data.cache.ICacheFactory#closeChannel(java.lang.String)
     */
    @Override
    public void closeChannel(final String aName) {

        ICacheChannel<?, ?> channel = pChannels.get(aName);
        if (channel != null) {
            pChannels.remove(channel);
            channel.close();
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

        // Close all channels
        for (ICacheChannel<?, ?> channel : pChannels.values()) {
            channel.close();
        }
        pChannels.clear();

        pLogger.logInfo(this, "invalidatePojo", "Cache channel factory Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.data.cache.ICacheFactory#openChannel(java.lang.String)
     */
    @Override
    public <K, V> ICacheChannel<K, V> openChannel(final String aName) {

        // Get the existing one
        @SuppressWarnings("unchecked")
        ICacheChannel<K, V> channel = (ICacheChannel<K, V>) pChannels
                .get(aName);

        if (channel == null) {
            // Create a new one if needed
            channel = new CacheChannelImpl<K, V>();

            // Store it
            pChannels.put(aName, channel);
        }

        return channel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.data.cache.ICacheFactory#openDequeueChannel(java.lang
     * .String)
     */
    @Override
    public <K, V> ICacheDequeueChannel<K, V> openDequeueChannel(
            final String aName) {

        // Get the existing one
        @SuppressWarnings("unchecked")
        ICacheDequeueChannel<K, V> channel = (ICacheDequeueChannel<K, V>) pQueueChannels
                .get(aName);

        if (channel == null) {
            // Create a new one if needed
            channel = new CacheDequeueChannelImpl<K, V>();

            // Store it
            pQueueChannels.put(aName, channel);
        }

        return channel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo", "Cache channel factory Ready");
    }
}
