/**
 * File:   ExceptionCatcher.java
 * Author: Thomas Calmant
 * Date:   4 nov. 2011
 */
package org.psem2m.composer.core.test.chain;

import java.io.Serializable;

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
    private String pCacheAgeEntry = CacheCommons.DEFAULT_CACHE_AGE_ENTRY;

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

    /** The key in the request map that indicates the cache entry to retrieve */
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
        boolean useCache = false;

        // Open the channel
        final ICacheChannel<Serializable, Serializable> channel = pCacheCommons
                .getChannel(pChannelFactory, pChannelName, pChannelType);
        if (channel != null) {
            useCache = getFromCache(aContext, channel);
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

        return pNext.computeResult(aContext);
    }

    /**
     * Try to get the requested data using the cache. Returns false if the cache
     * data is missing or too old. Clear the context results if this method
     * returns false.
     * 
     * @param aContext
     *            The current component context
     * @param channel
     *            The cache channel to be used
     * @return True if all requested cache data has been added to the context
     *         results, False if at least one data is too old or missing.
     */
    protected boolean getFromCache(final IComponentContext aContext,
            final ICacheChannel<Serializable, Serializable> channel) {

        // Clear the existing results
        aContext.getResults().clear();

        final Object cacheResult = pCacheCommons.findInCache(channel,
                aContext.getRequest());

        if (cacheResult instanceof ICachedObject) {
            // A single cache result has been found
            final ICachedObject<?> cachedObject = (ICachedObject<?>) cacheResult;

            if (cachedObject.isAcceptable(pMaxAge)) {
                // Recent enought to be used
                pCacheCommons.handleFoundCachedObject(aContext, cachedObject);

            } else {
                // Too old data
                return false;
            }

        } else if (cacheResult instanceof Iterable) {
            // Multiple data found

            for (final Object cacheResultElement : (Iterable<?>) cacheResult) {

                if (cacheResultElement instanceof ICachedObject) {

                    // A single cache result has been found
                    final ICachedObject<?> cachedObject = (ICachedObject<?>) cacheResultElement;

                    if (cachedObject.isAcceptable(pMaxAge)) {
                        // Data acceptable
                        pCacheCommons.handleFoundCachedObject(aContext,
                                cachedObject);

                    } else {
                        // Too old data
                        return false;
                    }

                } else {
                    pLogger.logInfo(this, "computeResult",
                            "Unknown element in cache :", cacheResultElement);

                    // An error occurred, do not use the cache
                    return false;
                }
            }
        }

        return true;
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
        pCacheCommons.setEntryName(pEntryName);
        pCacheCommons.setCacheAgeEntry(pCacheAgeEntry);

        pLogger.logInfo(this, "validatePojo", "Component '" + pName + "' Ready");
    }
}
