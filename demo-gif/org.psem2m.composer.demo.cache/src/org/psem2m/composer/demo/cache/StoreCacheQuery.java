/**
 * File:   StoreCacheQuery.java
 * Author: Thomas Calmant
 * Date:   17 nov. 2011
 */
package org.psem2m.composer.demo.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 * A standard component that store a query result, storing only result items IDs
 * in the query channel and storing result items maps to an item channel.
 * 
 * @author Thomas Calmant
 */
@Component(name = "store-cache-query")
@Provides(specifications = IComponent.class)
public class StoreCacheQuery extends CPojoBase implements IComponent {

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

    /** The next component */
    @Requires(id = NEXT_FIELD_ID)
    private IComponent pNext;

    /** Cache channel to store result items IDs */
    @Property(name = "queryChannelName")
    private String pQueryResultChannelName;

    /** ID of each result item */
    @Property(name = "itemsIdKey")
    private String pResultItemIdKey;

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

        // Call the next component
        final IComponentContext result = pNext.computeResult(aContext);
        if (!result.hasResult()) {
            // Nothing to store
            result.addError(pName, "No result to store in cache...");
            return result;
        }

        /* Open cache channels */
        // Open the query channel
        final ICacheChannel<Serializable, Serializable> queryChannel = pCacheCommons
                .openChannel(pChannelFactory, pQueryResultChannelName,
                        CacheCommons.CHANNEL_TYPE_MAP);
        if (queryChannel == null) {
            result.addError(pName, "Can't open channel : "
                    + pQueryResultChannelName);
            return result;
        }

        // Open the items channel
        final ICacheChannel<Serializable, Serializable> itemsChannel = pCacheCommons
                .openChannel(pChannelFactory, pItemsChannelName,
                        CacheCommons.CHANNEL_TYPE_MAP);
        if (itemsChannel == null) {
            result.addError(pName, "Can't open channel : " + pItemsChannelName);
            return result;
        }

        /* Make the query key */
        final String queryKey = pCacheCommons.makeKey(aContext.getRequest()
                .get(IComponentContext.REQUEST_CRITERIA));

        /* Handle each item */
        final List<String> itemsIds = new ArrayList<String>(result.getResults()
                .size());

        for (final Map<String, Object> item : result.getResults()) {

            // Get the item ID
            final String itemId = (String) item.get(pResultItemIdKey);
            if (itemId == null) {
                result.addError(pName, "Item ID=" + pResultItemIdKey
                        + " not found in" + item);
                continue;
            }

            // Store the item in the cache
            itemsChannel.put(itemId, (Serializable) item);

            // Store the item ID
            itemsIds.add(itemId);
        }

        /* Store the the query result map */
        queryChannel.put(queryKey, (Serializable) itemsIds);

        return result;
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
