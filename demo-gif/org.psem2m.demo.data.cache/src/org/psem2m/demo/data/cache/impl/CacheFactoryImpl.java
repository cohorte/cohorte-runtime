/**
 * File:   CacheFactoryImpl.java
 * Author: Thomas Calmant
 * Date:   11 oct. 2011
 */
package org.psem2m.demo.data.cache.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

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
    public <K extends Serializable, V extends Serializable> ICacheChannel<K, V> openChannel(
            final String aName) {

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
    public <K extends Serializable, V extends Serializable> ICacheDequeueChannel<K, V> openDequeueChannel(
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

    /**
     * Custom de-serialization
     * 
     * @param aObjectInputStream
     *            An object input stream
     * @throws IOException
     *             An error occurred while reading the object
     * @throws ClassNotFoundException
     *             A required class wasn't found
     */
    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream aObjectInputStream)
            throws IOException, ClassNotFoundException {

        // The standard channels
        final Map<String, ICacheChannel<?, ?>> readStdChannels = (Map<String, ICacheChannel<?, ?>>) aObjectInputStream
                .readObject();
        if (readStdChannels != null) {
            pChannels.putAll(readStdChannels);
        }

        // The queued channels
        Map<String, ICacheDequeueChannel<?, ?>> readQueueChannels = (Map<String, ICacheDequeueChannel<?, ?>>) aObjectInputStream
                .readObject();
        if (readQueueChannels != null) {
            pQueueChannels.putAll(readQueueChannels);
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

        pLogger.logInfo(this, "validatePojo", "Cache channel factory Ready");
    }

    /**
     * Custom serialization
     * 
     * @param aObjectOutputStream
     *            The object output stream
     * @throws IOException
     *             An error occurred while writing objects
     */
    private void writeObject(final ObjectOutputStream aObjectOutputStream)
            throws IOException {

        // The standard channels
        aObjectOutputStream.writeObject(pChannels);

        // The queued channels
        aObjectOutputStream.writeObject(pQueueChannels);
    }
}
