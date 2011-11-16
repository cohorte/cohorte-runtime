/**
 * File:   ExceptionCatcher.java
 * Author: Thomas Calmant
 * Date:   4 nov. 2011
 */
package org.psem2m.composer.core.test.chain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import org.psem2m.demo.data.cache.CachedObject;
import org.psem2m.demo.data.cache.ICacheChannel;
import org.psem2m.demo.data.cache.ICacheFactory;
import org.psem2m.demo.data.cache.ICachedObject;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * A standard component that retrieves the content of the given cache channel
 * entry or calls the next component if the cached value is too old.
 * 
 * @author Thomas Calmant
 */
@Component(name = "get-cache-if-recent")
@Provides(specifications = IComponent.class)
public class GetCacheIfRecent extends CPojoBase implements IComponent {

    /** The channel factory service */
    @Requires
    private ICacheFactory pChannelFactory;

    /** The interrogated channel name */
    @Property(name = "cacheChannel")
    private String pChannelName;

    /** The type of channel */
    @Property(name = "cacheChannelType")
    private String pChannelType;

    /** The key in the request map that indicated the cache entry to retrieve */
    @Property(name = "requestKeyName")
    private String pEntryName;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The maximum cached object age before calling next component */
    @Property(name = "maxCacheAge")
    private long pMaxAge;

    /** The component instance name */
    @Property(name = PROPERTY_INSTANCE_NAME)
    private String pName;

    /** The next component */
    @Requires(id = NEXT_FIELD_ID)
    private IComponent pNext;

    /**
     * Default constructor
     */
    public GetCacheIfRecent() {

        super();
    }

    /**
     * Tests if the given object has an acceptable age to be returned
     * immediately
     * 
     * @param aCachedObject
     *            The cached object to test
     * @return True if the object can be returned immediately
     */
    protected boolean acceptableAge(final ICachedObject<?> aCachedObject) {

        return !(aCachedObject == null || (System.currentTimeMillis() - aCachedObject
                .getCacheAge()) > pMaxAge);
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

        /* Try with the cache... */
        final Object cacheResult = tryUsingCache(aContext.getRequest());
        if (cacheResult != null) {
            // Cache result has been found
            aContext.getResults().clear();

            if (cacheResult instanceof Map) {
                aContext.getResults().add((Map<String, Object>) cacheResult);

            } else {
                aContext.addError(pName, "Don't known how to handle "
                        + cacheResult);
            }

            return aContext;
        }

        /* Call to the next component is needed */
        pLogger.logInfo(this, "get-cache-if-recent",
                "Cache too old, calling ERP");

        // Not stored or too old data, call the next component
        final IComponentContext result = pNext.computeResult(aContext);

        final List<Map<String, Object>> resultsList = result.getResults();

        for (final Map<String, Object> resultMap : resultsList) {
            // For each map in result

            for (final Entry<String, Object> entry : resultMap.entrySet()) {

                final Object resultObject = entry.getValue();
                if (resultObject instanceof Serializable
                        && !(resultObject instanceof ICachedObject)) {

                    // Create a cached object, if needed
                    resultMap.put(entry.getKey(),
                            new CachedObject<Serializable>(
                                    (Serializable) resultObject));
                }
            }
        }

        return result;
    }

    /**
     * Retrieves the cache key to use
     * 
     * @param aRequest
     *            The request associated to the treatment
     * @return The cache key to use, can be null
     */
    @SuppressWarnings("unchecked")
    protected Object getCacheKey(final Object aRequest) {

        if (aRequest == null) {
            // No information in the request, maybe we must retrieve a constant
            return pEntryName;

        } else if (pEntryName == null || !(aRequest instanceof Map)) {
            // The request is not a Map, or no map entry is given : use the
            // complete request as a key
            return aRequest;
        }

        // We have a map and a entry name
        return ((Map<Object, Object>) aRequest).get(pEntryName);
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

        if (isMapChannel && pChannelFactory.isChannelOpened(pChannelName)) {
            // Standard mapped channel
            return pChannelFactory.openChannel(pChannelName);
        }

        if (!isMapChannel
                && pChannelFactory.isDequeueChannelOpened(pChannelName)) {
            // The channel is queued one
            return pChannelFactory.openDequeueChannel(pChannelName);
        }

        return null;
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

    /**
     * Tries to find a valid value in the cache
     * 
     * @param aRequestData
     *            The treatment request data
     * @return The cached object, null if it's too old or not found
     */
    protected Object tryUsingCache(final Object aRequestData) {

        // Get the channel
        final ICacheChannel<Serializable, Serializable> channel = getChannel();
        if (channel == null) {
            // Channel not found...
            pLogger.logWarn(this, "tryUsingCache", pName,
                    ": Channel not found ", pChannelName);
            return null;
        }

        // Get the cached item key
        Object cachedObjectKey = getCacheKey(aRequestData);

        if (cachedObjectKey != null && cachedObjectKey.getClass().isArray()) {
            cachedObjectKey = Arrays.asList((Object[]) cachedObjectKey);
        }

        // Get the cached item(s)
        if (cachedObjectKey instanceof Iterable) {

            // Special case : the found key is an array or a list of keys
            final Map<Object, Object> resultMap = new HashMap<Object, Object>();

            // Flag to call the next element if needed
            boolean mustCallNext = false;

            // Loop on each keys
            for (final Object key : (Iterable<?>) cachedObjectKey) {

                if (key instanceof Serializable) {

                    final ICachedObject<?> cachedObject = channel
                            .get((Serializable) key);
                    if (acceptableAge(cachedObject)) {
                        // Acceptable data found...
                        resultMap.put(key, cachedObject);

                    } else {
                        // Too old data found, call the next component

                        pLogger.logInfo(this, "....getIfRecent...",
                                "Too old key=", key, ", object=", cachedObject);

                        mustCallNext = true;
                        break;
                    }
                }
            }

            if (!mustCallNext) {
                // Valid cached object
                return resultMap;
            }

        } else if (cachedObjectKey instanceof Serializable) {
            // Use the found key, directly
            final ICachedObject<?> cachedObject = channel
                    .get((Serializable) cachedObjectKey);

            if (acceptableAge(cachedObject)) {
                // Valid cached object
                return cachedObject;

            } else {
                pLogger.logInfo(this, "....getIfRecent...", "Too old key=",
                        cachedObjectKey, ", object=", cachedObject);
            }

        } else {

            pLogger.logInfo(this, "....getIfRecent...", "Unhandled type :",
                    cachedObjectKey != null ? cachedObjectKey.getClass()
                            : "<null>");
        }

        return null;
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
