/**
 * File:   ExceptionCatcher.java
 * Author: Thomas Calmant
 * Date:   4 nov. 2011
 */
package org.psem2m.composer.core.test.chain;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.test.api.IComponent;
import org.psem2m.demo.data.cache.CachedObject;
import org.psem2m.demo.data.cache.ICacheDequeueChannel;
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
    @Property(name = "channelName")
    private String pChannelName;

    /** The type of channel */
    @Property(name = "channelType")
    private String pChannelType;

    /** The cached entry to retrieve */
    @Property(name = "channelEntryName")
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
     * @see org.psem2m.composer.test.api.IComponent#computeResult(java.util.Map)
     */
    @Override
    public Map<String, Object> computeResult(final Map<String, Object> aData)
            throws Exception {

        // Prepare the result map reference
        Map<String, Object> result = aData;

        // The cached object, if any
        ICachedObject<Serializable> storedObject = null;

        try {
            // Get the cached object
            storedObject = getCachedObject();

        } catch (final FileNotFoundException ex) {
            // Channel not found, indicate an error
            pLogger.logWarn(this, "computeResult", pName, ex.getMessage());
        }

        if (storedObject == null
                || (System.currentTimeMillis() - storedObject.getCacheAge()) > pMaxAge) {
            // Not stored or too old data, call the next component
            result = pNext.computeResult(aData);

            final Serializable resultObject = (Serializable) result
                    .get(KEY_RESULT);

            if (!(result instanceof ICachedObject)) {
                // Create a cached object
                result.put(KEY_RESULT, new CachedObject<Serializable>(
                        resultObject));
            }

        } else {
            // Return the cached data
            result.put(KEY_RESULT, storedObject);
        }

        return result;
    }

    /**
     * Tries to get the stored data according to component properties
     * 
     * @return The stored data, can be null
     * @throws FileNotFoundException
     *             The channel to read doesn't exist
     */
    protected ICachedObject<Serializable> getCachedObject()
            throws FileNotFoundException {

        // Detect the channel type
        final boolean isMapChannel = pChannelType == null
                || pChannelType.isEmpty()
                || pChannelType.equalsIgnoreCase(CHANNEL_TYPE_MAP);

        if (isMapChannel && pChannelFactory.isChannelOpened(pChannelName)) {
            // Standard mapped channel
            return pChannelFactory.openChannel(pChannelName).get(pEntryName);

        } else if (!isMapChannel
                && pChannelFactory.isDequeueChannelOpened(pChannelName)) {
            // The channel is queued one
            final ICacheDequeueChannel<Serializable, Serializable> channel = pChannelFactory
                    .openDequeueChannel(pChannelName);

            if (pEntryName == null) {
                // Get the first data (don't remove it)
                return channel.getFirst();

            } else {
                // Get the named data
                return channel.get(pEntryName);
            }
        }

        throw new FileNotFoundException("Cache not found : " + pChannelName);
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
