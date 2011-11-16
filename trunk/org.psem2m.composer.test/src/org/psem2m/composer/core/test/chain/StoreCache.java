/**
 * File:   ExceptionCatcher.java
 * Author: Thomas Calmant
 * Date:   4 nov. 2011
 */
package org.psem2m.composer.core.test.chain;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.test.api.IComponent;
import org.psem2m.composer.test.api.IComponentContext;
import org.psem2m.demo.data.cache.ICacheChannel;
import org.psem2m.demo.data.cache.ICacheFactory;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * A standard component that stores some data from the result of the next
 * component in the given cache channel entry.
 * 
 * @author Thomas Calmant
 */
@Component(name = "store-cache")
@Provides(specifications = IComponent.class)
public class StoreCache extends CPojoBase implements IComponent {

    /** The channel factory service */
    @Requires
    private ICacheFactory pChannelFactory;

    /** The interrogated channel name */
    @Property(name = "cacheChannel")
    private String pChannelName;

    /** The type of channel */
    @Property(name = "cacheChannelType")
    private String pChannelType;

    /** The key in the result map that indicated the cache entry to retrieve */
    @Property(name = "resultKeyName")
    private String pEntryName;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The component instance name */
    @Property(name = PROPERTY_INSTANCE_NAME)
    private String pName;

    /** The next component */
    @Requires(id = NEXT_FIELD_ID)
    private IComponent pNext;

    @Property(name = "storedDataKey")
    private String pStoredKey;

    /**
     * Default constructor
     */
    public StoreCache() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.test.api.IComponent#computeResult(org.psem2m.composer
     * .test.api.IComponentContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    public IComponentContext computeResult(final IComponentContext aContext)
            throws Exception {

        // Call the next component
        final IComponentContext result = pNext.computeResult(aContext);

        // Open the store channel
        final ICacheChannel<Serializable, Serializable> channel = getChannel();
        if (channel == null) {
            result.addError(pName, "Can't open channel : " + pChannelName);
            return result;
        }

        /* Get the data to be stored */
        final Object foundObject = getObjectToStore(result);
        final Serializable objectToStore;

        if (foundObject == null || foundObject instanceof Serializable) {
            // The found value can be stored...
            objectToStore = (Serializable) foundObject;

        } else {
            // Can't store it...
            result.addError(pName, "The found value is not Serializable : "
                    + foundObject);
            return result;
        }

        /* Store the data */
        if (objectToStore instanceof Map) {
            // Special case : the object to be stored is a map
            final Map<String, Object> storedMap = (Map<String, Object>) objectToStore;
            final Object storeKey = storedMap.get(pEntryName);

            if (storeKey == null) {
                // No entry name, store the map as is
                for (final Entry<String, Object> entry : storedMap.entrySet()) {

                    channel.put(entry.getKey(), (Serializable) entry.getValue());
                }

            } else {
                // We have a key to store the result
                channel.put((Serializable) storeKey, objectToStore);
            }

            // Store the result map
            result.setResult(storedMap);

        } else {
            // Don't know what to do...
            result.addError(pName, "Don't know how to store object : "
                    + objectToStore);
        }

        return result;
    }

    /**
     * Retrieves the channel described by {@link #pChannelName}
     * 
     * @return The cache channel to use, null if not yet opened
     */
    protected ICacheChannel<Serializable, Serializable> getChannel() {

        // Detect the channel type
        final boolean isMapChannel = pChannelType == null
                || pChannelType.isEmpty()
                || pChannelType.equalsIgnoreCase(CHANNEL_TYPE_MAP);

        try {
            if (isMapChannel) {
                // Standard mapped channel
                return pChannelFactory.openChannel(pChannelName);
            }

            // The channel is queued one
            return pChannelFactory.openDequeueChannel(pChannelName);

        } catch (final Exception e) {
            pLogger.logWarn(this, "getChannel", "Error openning channel",
                    pChannelName, e);
        }

        return null;
    }

    /**
     * Retrieves the object to be stored in cache
     * 
     * @param aData
     *            The treated data map
     * @return The found object, can be null
     */
    protected Object getObjectToStore(final IComponentContext aContext) {

        // Data result value
        final Map<String, Object> dataResult = aContext.getResults().get(0);

        if (pStoredKey != null) {
            // A stored key is given, try to find the corresponding value

            if (dataResult instanceof Map) {
                // The data is a map, grab the value at the corresponding key
                return ((Map<?, ?>) dataResult).get(pStoredKey);

            }
        }

        // No key given, store the complete result
        return dataResult;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo", "Component '" + pName
                + "' Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo", "Component '" + pName + "' Ready");
    }
}
