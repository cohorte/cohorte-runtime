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
 * entry. Returns null if the entry is not found.
 * 
 * @author Thomas Calmant
 */
@Component(name = "get-cache")
@Provides(specifications = IComponent.class)
public class GetCache extends CPojoBase implements IComponent {

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

    /** The key in the request map that indicated the cache entry to retrieve */
    @Property(name = "requestKeyName")
    private String pEntryName;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The component instance name */
    @Property(name = PROPERTY_INSTANCE_NAME)
    private String pName;

    /**
     * Default constructor
     */
    public GetCache() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.test.api.IComponent#computeResult(java.util.Map)
     */
    @Override
    public IComponentContext computeResult(final IComponentContext aContext)
            throws Exception {

        // Get the channel
        final ICacheChannel<Serializable, Serializable> channel = pCacheCommons
                .getChannel(pChannelFactory, pChannelName, pChannelType);
        if (channel == null) {
            // Channel not found...
            aContext.addError(pName, "Channel not found : " + pChannelName);
            return aContext;
        }

        // Clear the existing results
        aContext.getResults().clear();

        final Object cacheResult = pCacheCommons.findInCache(channel,
                aContext.getRequest());

        if (cacheResult instanceof ICachedObject) {
            // A single cache result has been found
            pCacheCommons.handleFoundCachedObject(aContext,
                    (ICachedObject<?>) cacheResult);

        } else if (cacheResult instanceof Iterable) {
            // Multiple data found

            for (final Object cacheResultElement : (Iterable<?>) cacheResult) {

                if (cacheResultElement instanceof ICachedObject) {

                    // A single cache result has been found
                    pCacheCommons.handleFoundCachedObject(aContext,
                            (ICachedObject<?>) cacheResultElement);

                } else {
                    pLogger.logInfo(this, "computeResult",
                            "Unknown element in cache :", cacheResultElement);

                    // An error occurred, do not use the cache
                    aContext.getResults().clear();
                    aContext.addError(pName, "Unknown element in cache :"
                            + cacheResultElement);
                }
            }
        }

        return aContext;
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
            pCacheCommons.handleFoundCachedObject(aContext,
                    (ICachedObject<?>) cacheResult);

        } else if (cacheResult instanceof Iterable) {
            // Multiple data found

            for (final Object cacheResultElement : (Iterable<?>) cacheResult) {

                if (cacheResultElement instanceof ICachedObject) {

                    // A single cache result has been found
                    pCacheCommons.handleFoundCachedObject(aContext,
                            (ICachedObject<?>) cacheResultElement);

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

        pLogger.logInfo(this, "validatePojo", "Component '" + pName + "' Ready");
    }
}
