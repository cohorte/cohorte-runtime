/**
 * File:   ExceptionCatcher.java
 * Author: Thomas Calmant
 * Date:   4 nov. 2011
 */
package org.psem2m.composer.demo.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.demo.IComponent;
import org.psem2m.composer.demo.IComponentContext;
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

    /** Utility cache methods */
    private CacheCommons pCacheCommons;

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
        if (!result.hasResult()) {
            // Nothing to store
            result.addError(pName, "No result to store in cache...");
            return result;
        }

        // Open the store channel
        final ICacheChannel<Serializable, Serializable> channel = pCacheCommons
                .openChannel(pChannelFactory, pChannelName, pChannelType);

        if (channel == null) {
            result.addError(pName, "Can't open channel : " + pChannelName);
            return result;
        }

        /* Get the data to be stored */
        final Object foundObject = getObjectToStore(result);
        if (foundObject == null) {
            // No object to store...
            result.addError(pName,
                    "Object to store in cache not found or null.");
            return result;

        } else if (!(foundObject instanceof Serializable)) {
            // Can't store it...
            result.addError(pName, "The found value is not Serializable : "
                    + foundObject);
            return result;
        }

        /* Store the data */
        Serializable objectToStore = (Serializable) foundObject;

        if (objectToStore.getClass().isArray()) {
            // Make arrays iterable
            objectToStore = (Serializable) Arrays
                    .asList((Object[]) objectToStore);
        }

        if (objectToStore instanceof Map) {
            // Special case : the object to be stored is a map
            final Map<String, Object> mapToStore = (Map<String, Object>) objectToStore;

            // Store the result map
            storeMap(channel, mapToStore);
            result.setResult(mapToStore);

        } else if (objectToStore instanceof Iterable) {
            // Special case : the object is a list
            result.getResults().clear();

            for (final Object subObjectToStore : (Iterable<?>) objectToStore) {

                if (subObjectToStore instanceof Map) {
                    // Store sub maps
                    final Map<String, Object> mapToStore = (Map<String, Object>) subObjectToStore;

                    storeMap(channel, mapToStore);
                    result.addResult(mapToStore);

                } else {
                    // Not what we were waiting for...
                    pLogger.logInfo(this, "computeResult", "Not a map :",
                            subObjectToStore);
                    result.addError(pName, "Not a map : " + subObjectToStore);
                }
            }

        } else {
            // Don't know what to do...
            result.addError(pName, "Don't know how to store object : "
                    + objectToStore);
        }

        // Flush the cache
        pChannelFactory.flush();

        return result;
    }

    /**
     * Retrieves the object to be stored in cache
     * 
     * @param aData
     *            The treated data map
     * @return The found object, can be null
     */
    protected Object getObjectToStore(final IComponentContext aContext) {

        if (!aContext.hasResult()) {
            // No result : nothing to store
            pLogger.logInfo(this, "getObjectToStore", "No result to store");
            return null;
        }

        if (aContext.getResults().size() > 1) {
            // More than one item, return a copy the complete list
            pLogger.logInfo(this, "getObjectToStore",
                    "Multiple results to store :", aContext.getResults().size());

            return new ArrayList<Map<String, Object>>(aContext.getResults());
        }

        // One result value
        final Map<String, Object> dataResult = aContext.getResults().get(0);

        pLogger.logInfo(this, "getObjectToStore", "Only one result to store");

        if (pStoredKey != null) {
            // A stored key is given, try to find the corresponding value

            if (dataResult instanceof Map) {
                // The data is a map, grab the value at the corresponding key
                return ((Map<?, ?>) dataResult).get(pStoredKey);

            }
        }

        // No key given, store a copy of the complete result
        return new HashMap<String, Object>(dataResult);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pCacheCommons = null;

        pLogger.logInfo(this, "invalidatePojo", "Component '" + pName
                + "' Gone");
    }

    /**
     * Stores the given map into the cache
     * 
     * @param aChannel
     *            The cache channel to use
     * @param aMapToStore
     *            The map to be stored
     */
    protected void storeMap(
            final ICacheChannel<Serializable, Serializable> aChannel,
            final Map<String, Object> aMapToStore) {

        // Get the store key, if any
        final Object storeKey = aMapToStore.get(pEntryName);

        if (storeKey == null) {

            if (pEntryName != null) {
                pLogger.logWarn(this, "storeMap", "Warning :", pEntryName,
                        "not found in ", aMapToStore);
            }

            pLogger.logInfo(this, "STORE", "-----");
            // No entry name, store the map as is
            for (final Entry<String, Object> entry : aMapToStore.entrySet()) {
                aChannel.put(entry.getKey(), (Serializable) entry.getValue());
                pLogger.logInfo(this, "STORE", entry.getKey(), "=>",
                        entry.getValue());
            }
            pLogger.logInfo(this, "STORE", "-----");

        } else {
            // We have a key to store a copy of the whole map

            final Map<String, Object> storedMap = new HashMap<String, Object>(
                    aMapToStore);
            aChannel.put((Serializable) storeKey, (Serializable) storedMap);
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

        // Set up the utility instance
        pCacheCommons = new CacheCommons(pName);

        pLogger.logInfo(this, "validatePojo", "Component '" + pName + "' Ready");
    }
}
