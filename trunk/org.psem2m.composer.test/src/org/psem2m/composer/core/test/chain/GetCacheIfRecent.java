/**
 * File:   ExceptionCatcher.java
 * Author: Thomas Calmant
 * Date:   4 nov. 2011
 */
package org.psem2m.composer.core.test.chain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /** The key to use in the result map to store the cache age */
    @Property(name = "cacheAgeEntry")
    private String pCacheAgeEntry = "__cache_age";

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
    @Override
    public IComponentContext computeResult(final IComponentContext aContext)
            throws Exception {

        // Just to be sure...
        aContext.getResults().clear();

        /* Try with the cache... */
        boolean useCache = true;
        Object cacheResult = findInCache(aContext.getRequest());

        if (cacheResult != null && cacheResult.getClass().isArray()) {
            // Convert arrays into iterable
            cacheResult = Arrays.asList((Object[]) cacheResult);
        }

        if (cacheResult instanceof ICachedObject) {
            // A single cache result has been found
            handleFoundCachedObject(aContext, (ICachedObject<?>) cacheResult);

        } else if (cacheResult instanceof Iterable) {
            // Multiple data found

            for (final Object cacheResultElement : (Iterable<?>) cacheResult) {

                if (cacheResultElement instanceof ICachedObject) {
                    handleFoundCachedObject(aContext,
                            (ICachedObject<?>) cacheResultElement);

                } else {
                    pLogger.logInfo(this, "computeResult",
                            "Unknown element in cache :", cacheResultElement);

                    // An error occurred, do not use the cache
                    useCache = false;
                }
            }
        }

        if (useCache && aContext.hasResult()) {
            // We have something to return...
            return aContext;
        }

        /* Call to the next component is needed */
        pLogger.logInfo(this, "get-cache-if-recent",
                "Cache failed to return a valid or a recent value, calling ERP");

        // IMPORTANT: Reset the context result
        aContext.getResults().clear();

        // Not stored or too old data, call the next component
        final IComponentContext result = pNext.computeResult(aContext);

        final List<Map<String, Object>> resultsList = result.getResults();

        // For each map in result, add the cache age information
        // (set to -1 for "sync")
        for (final Map<String, Object> resultMap : resultsList) {
            resultMap.put(pCacheAgeEntry, Long.valueOf(-1));
        }

        return result;
    }

    /**
     * Tries to find a valid value in the cache
     * 
     * @param aRequestData
     *            The treatment request data
     * @return The cached object, an array of cached object or null if it's too
     *         old or not found
     */
    protected Object findInCache(final Map<String, Object> aRequest) {

        // Get the channel
        final ICacheChannel<Serializable, Serializable> channel = getChannel();
        if (channel == null) {
            // Channel not found...
            pLogger.logWarn(this, "findInCache", pName, ": Channel not found ",
                    pChannelName);
            return null;
        }

        // Looking for a single element
        if (aRequest.containsKey(IComponentContext.REQUEST_KEY)) {

            // Get the object from the cache, if possible
            final Object fromCache = getFromCache(channel,
                    aRequest.get(IComponentContext.REQUEST_KEY));

            pLogger.logInfo(this, "findInCache - KEY", "fromCache=", fromCache,
                    "; key =", aRequest.get(IComponentContext.REQUEST_KEY));

            return fromCache;

        } else if (aRequest.containsKey(IComponentContext.REQUEST_KEYS)) {
            // Treat each keys
            Object keys = aRequest.get(IComponentContext.REQUEST_KEYS);

            if (keys != null && keys.getClass().isArray()) {
                // Convert arrays to list
                keys = Arrays.asList((Object[]) keys);
            }

            if (keys instanceof Iterable) {
                // Iterable keys
                final List<Object> resultList = new ArrayList<Object>();

                for (final Object key : (Iterable<?>) keys) {

                    final Object fromCache = getFromCache(channel, key);

                    pLogger.logInfo(this, "findInCache - KEYS", "fromCache=",
                            fromCache, "; key =", key);

                    if (fromCache == null) {
                        // Object missing or too old
                        return null;
                    }

                    // Add found object to the result list
                    resultList.add(fromCache);
                }

                if (resultList.isEmpty()) {
                    // Refuse empty lists
                    return null;
                }

                return resultList;
            }
        }

        pLogger.logWarn(this, "findInCache", pName,
                "can't handle the request with keys=", aRequest.keySet());
        return null;
    }

    /**
     * Retrieves the cache key to use
     * 
     * @param aRequest
     *            The request associated to the treatment
     * @return The cache key to use, can be null
     */
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
        return ((Map<?, ?>) aRequest).get(pEntryName);
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

    /**
     * Tries to retrieve the cached object with the given key
     * 
     * @param aChannel
     *            Channel to use
     * @param aKey
     *            Key of the object to retrieve
     * @return The cached object, null if it is too old or not found
     */
    protected Object getFromCache(
            final ICacheChannel<Serializable, Serializable> aChannel,
            final Object aKey) {

        // Find the configured key, if possible
        final Object cacheKey = getCacheKey(aKey);

        pLogger.logInfo(this, "getFromCache", "Key=", aKey, "; cacheKey=",
                cacheKey);

        if (!(cacheKey instanceof Serializable)) {
            // Can't use the found key
            return null;
        }

        // Get the object
        final ICachedObject<?> cachedObject = aChannel
                .get((Serializable) cacheKey);

        pLogger.logInfo(this, "getFromCache", "cachedObject=", cachedObject);

        if (!acceptableAge(cachedObject)) {
            // Object too old
            return null;
        }

        // OK to use
        return cachedObject;
    }

    protected Map<String, Object> handleCachedMap(
            final Map<String, Object> aCachedMap, final long aCacheAge) {

        // Make a copy of the map
        final Map<String, Object> resultMap = new HashMap<String, Object>(
                aCachedMap);

        // Inject the cache age
        resultMap.put(pCacheAgeEntry, Long.valueOf(aCacheAge));

        return resultMap;
    }

    @SuppressWarnings("unchecked")
    protected void handleFoundCachedObject(final IComponentContext aContext,
            final ICachedObject<?> aCachedObject) {

        Object cacheResultContent = aCachedObject.getObject();
        final long cacheAge = aCachedObject.getCacheAge();

        if (cacheResultContent != null
                && cacheResultContent.getClass().isArray()) {
            // Make arrays iterable...
            cacheResultContent = Arrays.asList((Object[]) cacheResultContent);
        }

        if (cacheResultContent instanceof Map) {
            // Handle a single element
            final Map<String, Object> preparedMap = handleCachedMap(
                    (Map<String, Object>) cacheResultContent, cacheAge);

            aContext.addResult(preparedMap);

        } else if (cacheResultContent instanceof Iterable) {
            // Handle a list of elements

            for (final Object cacheResultElement : (Iterable<?>) cacheResultContent) {

                if (cacheResultElement instanceof Map) {
                    // List of map
                    final Map<String, Object> preparedMap = handleCachedMap(
                            (Map<String, Object>) cacheResultElement, cacheAge);

                    aContext.addResult(preparedMap);

                } else {
                    pLogger.logInfo(this, "computeResult",
                            "Don't know how to handle sub-result",
                            cacheResultElement);

                    aContext.addError(pName,
                            "Don't know how to handle sub-result"
                                    + cacheResultElement);
                }
            }

        } else {
            pLogger.logInfo(this, "computeResult",
                    "Don't know how to handle cache result", cacheResultContent);

            aContext.addError(pName, "Don't know how to handle cache result"
                    + cacheResultContent);
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
