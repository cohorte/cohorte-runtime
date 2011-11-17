/**
 * File:   GetCacheQuery.java
 * Author: Thomas Calmant
 * Date:   17 nov. 2011
 */
package org.psem2m.composer.core.test.chain;

import java.io.Serializable;
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
 * A standard component that retrieves the cached result of a previous query.
 * 
 * @author Thomas Calmant
 */
@Component(name = "get-cache-query")
@Provides(specifications = IComponent.class)
public class GetCacheQuery extends CPojoBase implements IComponent {

    /** The key to use in the result map to store the cache age */
    @Property(name = "cacheAgeEntry")
    private String pCacheAgeEntry = CacheCommons.DEFAULT_CACHE_AGE_ENTRY;

    /** Utility cache methods */
    private CacheCommons pCacheCommons;

    /** The channel factory service */
    @Requires
    private ICacheFactory pChannelFactory;

    /** Cache channel to store result items beans */
    @Property(name = "itemsChannelName")
    private String pItemsChannelName;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The component instance name */
    @Property(name = PROPERTY_INSTANCE_NAME)
    private String pName;

    /** Cache channel to store result items IDs */
    @Property(name = "queryChannelName")
    private String pQueryResultChannelName;

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

        /* Get cache channels */
        // Open the query channel
        final ICacheChannel<Serializable, Serializable> queryChannel = pCacheCommons
                .getChannel(pChannelFactory, pQueryResultChannelName,
                        CacheCommons.CHANNEL_TYPE_MAP);
        if (queryChannel == null) {
            aContext.addError(pName, "Can't open channel : "
                    + pQueryResultChannelName);
            return aContext;
        }

        // Open the items channel
        final ICacheChannel<Serializable, Serializable> itemsChannel = pCacheCommons
                .getChannel(pChannelFactory, pItemsChannelName,
                        CacheCommons.CHANNEL_TYPE_MAP);
        if (itemsChannel == null) {
            aContext.addError(pName, "Can't open channel : "
                    + pItemsChannelName);
            return aContext;
        }

        /* Make the query key */
        final String queryKey = pCacheCommons.makeKey(aContext.getRequest()
                .get(IComponentContext.REQUEST_CRITERIA));

        /* Get the cached query result */
        final ICachedObject<?> cachedIds = queryChannel.get(queryKey);
        final List<String> itemsIds = (List<String>) cachedIds.getObject();

        /* Grab all possible beans */
        aContext.getResults().clear();

        for (final String itemId : itemsIds) {

            final ICachedObject<?> cachedItem = itemsChannel.get(itemId);
            final Object itemBean = cachedItem.getObject();

            if (itemBean instanceof Map) {
                // Valid bean

                // Use a copy of it, to avoid problems...
                final Map<String, Object> itemBeanCopy = new HashMap<String, Object>(
                        (Map<String, Object>) itemBean);

                // Add the cache age information
                itemBeanCopy.put(pCacheAgeEntry,
                        Long.valueOf(cachedItem.getCacheAge()));

                aContext.addResult(itemBeanCopy);

            } else {
                // Problem
                pLogger.logWarn(this, "computeResult", pName,
                        ": Don't know how to handle item bean :", itemBean);

                aContext.addError(pName,
                        "Don't know how to handle item bean : " + itemBean);
            }
        }

        return aContext;
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

        pLogger.logInfo(this, "validatePojo", "Component '" + pName + "' Ready");
    }
}
