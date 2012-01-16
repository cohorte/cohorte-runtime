/**
 * File:   JavaSignalSerializer.java
 * Author: Thomas Calmant
 * Date:   16 janv. 2012
 */
package org.psem2m.signals.serializer.java;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.OsgiObjectInputStream;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.remote.signals.ISignalDataSerializer;
import org.psem2m.isolates.services.remote.signals.InvalidDataException;
import org.psem2m.isolates.services.remote.signals.UnsendableDataException;

/**
 * Java Serializable object serializer for PSEM2M Signals
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-signals-data-java-factory", publicFactory = false)
@Provides(specifications = ISignalDataSerializer.class)
@Instantiate(name = "psem2m-signals-data-java")
public class JavaSignalSerializer extends CPojoBase implements
        ISignalDataSerializer {

    /** Java Serializable HTTP Content type */
    public static final String CONTENT_TYPE_SERIALIZABLE = "application/x-java-serialized-object";

    /** Default POST content type */
    protected static final String DEFAULT_POST_CONTENT_TYPE = "application/x-www-form-urlencoded";

    /** Default error code */
    protected final int DEFAULT_ERROR_CODE = 505;

    /** The bundles context */
    private BundleContext pBundleContext;

    /** The logger */
    @Requires(optional = true)
    private IIsolateLoggerSvc pLogger;

    /**
     * Sets up the serializer
     * 
     * @param aBundleContext
     *            The bundle context
     */
    public JavaSignalSerializer(final BundleContext aBundleContext) {

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

        if (aContentType == null || aContentType.isEmpty()
                || aContentType.equals(DEFAULT_POST_CONTENT_TYPE)) {
            // Accept invalid / default content type
            return true;
        }

        // Accept proper content type
        return aContentType.equals(CONTENT_TYPE_SERIALIZABLE);
    }

    /**
     * Accepts the object if it is null or if it implements {@link Serializable}
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalDataSerializer#
     *      canSerialize(java.lang.Object)
     */
    @Override
    public boolean canSerialize(final Object aObject) {

        return aObject == null || aObject instanceof Serializable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalDataSerializer#
     * getContentType()
     */
    @Override
    public String getContentType() {

        return CONTENT_TYPE_SERIALIZABLE;
    }

    /**
     * Returns {@link Integer#MAX_VALUE}, as this is the default serializer
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalDataSerializer#getPriority
     *      ()
     */
    @Override
    public int getPriority() {

        return Integer.MAX_VALUE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo",
                "Java signal data serializer gone");
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

        final ByteArrayOutputStream outArray = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = null;
        try {
            objectStream = new ObjectOutputStream(outArray);
            objectStream.writeObject(aObject);

        } catch (final IOException e) {
            throw new UnsendableDataException(
                    "Can't serialize the sent object", e);

        } finally {
            // Be nice, clean up the stream
            if (objectStream != null) {
                try {
                    objectStream.close();

                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return outArray.toByteArray();
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

        final ByteArrayInputStream inArray = new ByteArrayInputStream(aBytes);
        ObjectInputStream objectStream = null;
        try {
            objectStream = new OsgiObjectInputStream(pBundleContext, inArray);
            return objectStream.readObject();

        } catch (final IOException e) {
            throw new InvalidDataException("Error reading the object stream",
                    DEFAULT_ERROR_CODE, e);

        } catch (final ClassNotFoundException e) {
            throw new InvalidDataException(
                    "Can't find the signal data class : " + e.getMessage(),
                    DEFAULT_ERROR_CODE, e);

        } finally {
            // Be nice...
            if (objectStream != null) {
                try {
                    objectStream.close();

                } catch (final IOException e) {
                    // Ignore
                }
            }
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

        pLogger.logInfo(this, "validatePojo",
                "Java signal data serializer ready");
    }
}
