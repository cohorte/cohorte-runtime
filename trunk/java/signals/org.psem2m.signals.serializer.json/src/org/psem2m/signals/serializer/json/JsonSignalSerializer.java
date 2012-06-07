/**
 * File:   JsonSignalSerializer.java
 * Author: Thomas Calmant
 * Date:   16 janv. 2012
 */
package org.psem2m.signals.serializer.json;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.jabsorb.ng.JSONSerializer;
import org.jabsorb.ng.serializer.MarshallException;
import org.jabsorb.ng.serializer.UnmarshallException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.BundlesClassLoader;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.remote.signals.ISignalDataSerializer;
import org.psem2m.isolates.services.remote.signals.InvalidDataException;
import org.psem2m.isolates.services.remote.signals.UnsendableDataException;

/**
 * Java Serializable object serializer for PSEM2M Signals
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-signals-data-json-factory", publicFactory = false)
@Provides(specifications = ISignalDataSerializer.class)
@Instantiate(name = "psem2m-signals-data-json")
public class JsonSignalSerializer extends CPojoBase implements
        ISignalDataSerializer {

    /** JSON HTTP Content type */
    public static final String CONTENT_TYPE_JSON = "application/json";

    /** The JSON serializer priority */
    public static final int SERIALIZER_PRIORITY = 1000;

    /** Default error code */
    protected final int DEFAULT_ERROR_CODE = 505;

    /** The bundle context */
    private BundleContext pBundleContext;

    /** The logger */
    @Requires(optional = true)
    private IIsolateLoggerSvc pLogger;

    /** The JSON Serializer (Jabsorb) */
    private JSONSerializer pSerializer;

    /**
     * Sets up the serializer
     * 
     * @param aBundleContext
     *            The bundle context
     */
    public JsonSignalSerializer(final BundleContext aBundleContext) {

        pBundleContext = aBundleContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalDataSerializer#
     * canHandleType(java.lang.String)
     */
    @Override
    public boolean canHandleType(final String aContentType) {

        // Accept proper content type
        return aContentType.equals(CONTENT_TYPE_JSON);
    }

    /**
     * Accepts the object if it seems to be usable with Jabsorb
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalDataSerializer#
     *      canSerialize(java.lang.Object)
     */
    @Override
    public boolean canSerialize(final Object aObject) {

        if (aObject != null) {

            // Try to serialize it (hard core...)
            // FIXME: be nicer ? use cache for successes ?
            try {
                pSerializer.toJSON(aObject);

            } catch (final Exception e) {
                // Can't do
                e.printStackTrace();
                return false;
            }

            final Class<?> clazz = aObject.getClass();
            if (!clazz.isArray()) {
                try {
                    // Test default constructor
                    clazz.getConstructor((Class<?>[]) null);

                } catch (final Exception e) {
                    // No default constructor accessible
                    return false;
                }
            }
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalDataSerializer#
     * getContentType()
     */
    @Override
    public String getContentType() {

        return CONTENT_TYPE_JSON;
    }

    /**
     * Returns {@link #SERIALIZER_PRIORITY} ({@value #SERIALIZER_PRIORITY})
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalDataSerializer#getPriority
     *      ()
     */
    @Override
    public int getPriority() {

        return SERIALIZER_PRIORITY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pSerializer = null;

        pLogger.logInfo(this, "invalidatePojo",
                "JSON signal data serializer gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalDataSerializer#
     * serializeData(java.lang.Object)
     */
    @Override
    public byte[] serializeData(final Object aObject)
            throws UnsendableDataException {

        if (aObject == null) {
            // Null object, return an empty byte array
            return new byte[0];
        }

        final String jsonStr;
        try {
            jsonStr = pSerializer.toJSON(aObject);

        } catch (final MarshallException e) {
            // Error during conversion
            throw new UnsendableDataException(
                    "Can't serialize the given object in JSON :", e);

        } catch (final Exception e) {
            // Internal, un-handled error
            throw new UnsendableDataException("Jabsorb internal error :", e);
        }

        return jsonStr.getBytes();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalDataSerializer#
     * unserializeData(byte[])
     */
    @Override
    public Object unserializeData(final byte[] aBytes)
            throws InvalidDataException {

        if (aBytes == null || aBytes.length == 0) {
            // Empty data : null data
            return null;
        }

        try {
            return pSerializer.fromJSON(new String(aBytes));

        } catch (final UnmarshallException e) {

            if (pLogger.isLogDebugOn()) {
                // Trace the complete exception when debugging
                Throwable ex = e;
                while (ex != null) {
                    pLogger.logDebug(this, "JSON.unserializeData",
                            "Error while reading JSON :\n", ex);
                    ex = ex.getCause();
                }
            }

            throw new InvalidDataException("Error reading the JSON string",
                    DEFAULT_ERROR_CODE, e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        // Set up the serializer
        pSerializer = new JSONSerializer(new BundlesClassLoader(pBundleContext));
        try {
            pSerializer.registerDefaultSerializers();

        } catch (final Exception e) {
            pLogger.logSevere(this, "",
                    "Error setting up the JSON serializer :", e);
            throw new BundleException("Error setting up the JSON serializer", e);
        }

        pLogger.logInfo(this, "validatePojo",
                "JSON signal data serializer ready");
    }
}
