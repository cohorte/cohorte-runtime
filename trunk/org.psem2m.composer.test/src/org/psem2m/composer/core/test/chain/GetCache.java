/**
 * File:   ExceptionCatcher.java
 * Author: Thomas Calmant
 * Date:   4 nov. 2011
 */
package org.psem2m.composer.core.test.chain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
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
    @SuppressWarnings("unchecked")
    @Override
    public IComponentContext computeResult(final IComponentContext aContext)
            throws Exception {

        // Get the channel
        final ICacheChannel<Serializable, Serializable> channel = getChannel();
        if (channel == null) {
            // Channel not found...
            aContext.addError(pName, "Channel not found : " + pChannelName);
            return aContext;
        }

        // Get the key of the object to retrieve from the cache
        Object cachedObjectKey = getCacheKey(aContext.getRequest());

        if (cachedObjectKey != null && cachedObjectKey.getClass().isArray()) {
            // Convert arrays in lists, to pass the "instanceof" test
            cachedObjectKey = Arrays.asList((Object[]) cachedObjectKey);
        }

        // Get the data
        if (cachedObjectKey instanceof Iterable) {
            // Special case : the found key is an array or a list of keys
            final Map<String, Object> resultMap = new HashMap<String, Object>();

            // Loop on each keys
            for (final Object key : (Iterable<?>) cachedObjectKey) {

                if (key instanceof Serializable) {
                    resultMap
                            .put((String) key, channel.get((Serializable) key));
                }
            }

            // Set the result
            aContext.setResult(resultMap);

        } else if (cachedObjectKey instanceof Serializable) {
            // Use the found key, directly
            aContext.getResults().clear();

            final Object cachedObject = channel
                    .get((Serializable) cachedObjectKey);
            if (cachedObject instanceof Map) {
                aContext.getResults().add((Map<String, Object>) cachedObject);

            } else {
                aContext.addError(pName,
                        "Don't know how to handle cached object '"
                                + cachedObject + "' at key '" + cachedObjectKey
                                + "'");
            }

        } else {
            // Unresolved case
            aContext.getResults().clear();
            aContext.addError(pName, "No valid cache key found");
        }

        return aContext;
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
