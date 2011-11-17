/**
 * File:   CacheCommons.java
 * Author: Thomas Calmant
 * Date:   17 nov. 2011
 */
package org.psem2m.composer.core.test.chain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.psem2m.composer.test.api.IComponentContext;
import org.psem2m.demo.data.cache.ICacheChannel;
import org.psem2m.demo.data.cache.ICacheFactory;
import org.psem2m.demo.data.cache.ICachedObject;

/**
 * Utility class for class readers
 * 
 * @author Thomas Calmant
 */
public class CacheCommons {

    /** Queued channel */
    public static final String CHANNEL_TYPE_DEQUEUE = "queue";

    /** Map channel */
    public static final String CHANNEL_TYPE_MAP = "map";

    /** Default key name to store the cache entry age */
    public static final String DEFAULT_CACHE_AGE_ENTRY = "__cache_age";

    /** The key to use in the result map to store the cache age */
    private String pCacheAgeEntry = DEFAULT_CACHE_AGE_ENTRY;

    /** The key in the request map that indicated the cache entry to retrieve */
    private String pEntryName;

    /** The calling component name */
    private final String pName;

    /**
     * Sets up the utility instance
     * 
     * @param aCallingComponentName
     *            Name of the calling component
     */
    public CacheCommons(final String aCallingComponentName) {

        pName = aCallingComponentName;
    }

    /**
     * Returns an a list if the given object is an array, else returns the given
     * object.
     * 
     * @param aObject
     *            An object, can be null
     * @return A list if aObject is an array, else aObject
     */
    public Object arrayToIterable(final Object aObject) {

        if (aObject != null && aObject.getClass().isArray()) {
            // Convert arrays into list
            return Arrays.asList((Object[]) aObject);
        }

        return aObject;
    }

    /**
     * Tries to find a valid value in the cache. If the found value is an array,
     * it is converted to a list with {@link Arrays#asList(Object...)}, to match
     * the "instanceof Iterable" conditions.
     * 
     * @param aRequestData
     *            The treatment request data
     * 
     * @return The cached object, an array of cached object or null if it's too
     *         old or not found
     */
    protected Object findInCache(
            final ICacheChannel<Serializable, Serializable> channel,
            final Map<String, Object> aRequest) {

        // Looking for a single element
        if (aRequest.containsKey(IComponentContext.REQUEST_KEY)) {

            // Get the object from the cache, if possible
            return arrayToIterable(getFromCache(channel,
                    aRequest.get(IComponentContext.REQUEST_KEY)));

        } else if (aRequest.containsKey(IComponentContext.REQUEST_KEYS)) {
            // Treat each keys
            final Object keys = arrayToIterable(aRequest
                    .get(IComponentContext.REQUEST_KEYS));

            if (keys instanceof Iterable) {
                // Iterable keys
                final List<Object> resultList = new ArrayList<Object>();

                for (final Object key : (Iterable<?>) keys) {

                    final Object fromCache = getFromCache(channel, key);
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
     * Retrieves the channel described with the given name and type if it is
     * already opened. Returns null if the channel has not yet been used.
     * 
     * @param aCache
     *            A cache service
     * @param aChannelName
     *            A channel name
     * @param aChannelType
     * @return Retrieves the requested channel, or null
     */
    public ICacheChannel<Serializable, Serializable> getChannel(
            final ICacheFactory aCache, final String aChannelName,
            final String aChannelType) {

        // Detect the channel type
        final boolean isMapChannel = aChannelType == null
                || aChannelType.isEmpty()
                || aChannelType.equalsIgnoreCase(CHANNEL_TYPE_MAP);

        if (isMapChannel && aCache.isChannelOpened(aChannelName)) {
            // Standard mapped channel
            return aCache.openChannel(aChannelName);
        }

        if (!isMapChannel && aCache.isDequeueChannelOpened(aChannelName)) {
            // The channel is queued one
            return aCache.openDequeueChannel(aChannelName);
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
     * @return The cached object, null if not found
     */
    protected Object getFromCache(
            final ICacheChannel<Serializable, Serializable> aChannel,
            final Object aKey) {

        // Find the configured key, if possible
        final Object cacheKey = getCacheKey(aKey);

        if (!(cacheKey instanceof Serializable)) {
            // Can't use the found key
            return null;
        }

        // Get the object
        final ICachedObject<?> cachedObject = aChannel
                .get((Serializable) cacheKey);

        // OK to use
        return cachedObject;
    }

    /**
     * Adds the cache age special value in a copy the cached map
     * 
     * @param aCachedMap
     *            Map read from the cache
     * @param aCacheAge
     *            Cache age
     * @return A copy of the cached map, with the cache age information
     */
    protected Map<String, Object> handleCachedMap(
            final Map<String, Object> aCachedMap, final long aCacheAge) {

        // Make a copy of the map
        final Map<String, Object> resultMap = new HashMap<String, Object>(
                aCachedMap);

        // Inject the cache age
        resultMap.put(pCacheAgeEntry, Long.valueOf(aCacheAge));

        return resultMap;
    }

    /**
     * Handles a {@link ICachedObject} and stores its value(s) into the context
     * 
     * @param aContext
     *            A component context
     * @param aCachedObject
     *            The found object
     */
    @SuppressWarnings("unchecked")
    public void handleFoundCachedObject(final IComponentContext aContext,
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
                    aContext.addError(pName,
                            "Don't know how to handle sub-result"
                                    + cacheResultElement);
                }
            }

        } else {
            aContext.addError(pName, "Don't know how to handle cache result"
                    + cacheResultContent);
        }
    }

    /**
     * Sets the key to use in the result map to store the cache age
     * 
     * @param aCacheAgeEntry
     *            The key to store the age of the cache entry
     */
    public void setCacheAgeEntry(final String aCacheAgeEntry) {

        pCacheAgeEntry = aCacheAgeEntry;
    }

    /**
     * Sets the key in the request map that indicates the cache entry to
     * retrieve
     * 
     * @param aEntryName
     *            the key that indicates the cache entry to retrieve
     */
    public void setEntryName(final String aEntryName) {

        pEntryName = aEntryName;
    }
}
