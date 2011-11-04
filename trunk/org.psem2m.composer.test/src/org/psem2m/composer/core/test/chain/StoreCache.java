/**
 * File:   ExceptionCatcher.java
 * Author: Thomas Calmant
 * Date:   4 nov. 2011
 */
package org.psem2m.composer.core.test.chain;

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

    /** The channel factory service */
    @Requires
    private ICacheFactory pChannelFactory;

    /** The interrogated channel name */
    @Property(name = "channelName")
    private String pChannelName;

    /** The type of channel */
    @Property(name = "channelType")
    private String pChannelType;

    /** The cached entry for storage */
    @Property(name = "channelEntryName")
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
     * @see org.psem2m.composer.test.api.IComponent#computeResult(java.util.Map)
     */
    @Override
    public Map<String, Object> computeResult(final Map<String, Object> aData)
            throws Exception {

        // Call the next component
        final Map<String, Object> result = pNext.computeResult(aData);

        // Get the data to be stored
        final Object foundObject = getObjectToStore(result);
        final Serializable storedObject;

        if (foundObject == null || foundObject instanceof Serializable) {
            // The found value can be stored...
            storedObject = (Serializable) foundObject;

        } else {
            // Can't store it...
            result.put(KEY_ERROR, pName
                    + ": the found value is not Serializable : " + foundObject);
            return result;
        }

        // Open the corresponding channel
        if (pChannelType == null || pChannelType.isEmpty()
                || pChannelType.equalsIgnoreCase(CHANNEL_TYPE_MAP)) {

            // Standard channel
            pChannelFactory.openChannel(pChannelName).put(pEntryName,
                    storedObject);

        } else if (pChannelType.equalsIgnoreCase(CHANNEL_TYPE_DEQUEUE)) {
            // Queued channel
            pChannelFactory.openDequeueChannel(pChannelName).add(storedObject);

        } else {
            // Unknown type
            result.put(KEY_ERROR, pName + ": unknown channel type '"
                    + pChannelType + "'");
        }

        return result;
    }

    /**
     * Retrieves the object to be stored in cache
     * 
     * @param aData
     *            The treated data map
     * @return The found object, can be null
     */
    protected Object getObjectToStore(final Map<String, Object> aData) {

        // Data result value
        final Object dataResult = aData.get(KEY_RESULT);

        if (pStoredKey != null) {
            // A stored key is given, try to find the corresponding value

            if (dataResult instanceof Map<?, ?>) {
                // The data is a map, grab the value at the corresponding key
                return ((Map<?, ?>) dataResult).get(pStoredKey);

            } else {
                // The key may be from the data map itself
                return aData.get(pStoredKey);
            }

        }

        // No key given, store the complete result
        return dataResult;
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
