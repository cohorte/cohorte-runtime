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
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.OsgiObjectInputStream;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.signals.ISignalSerializer;
import org.psem2m.signals.ISignalSerializerConstants;
import org.psem2m.signals.InvalidDataException;
import org.psem2m.signals.UnsendableDataException;

/**
 * Java Serializable object serializer for PSEM2M Signals
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-signals-data-java-factory", publicFactory = false)
@Provides(specifications = ISignalSerializer.class)
@Instantiate(name = "psem2m-signals-data-java")
public class JavaSignalSerializer extends CPojoBase implements
        ISignalSerializer {

    /** The bundles context */
    private final BundleContext pBundleContext;

    /** The logger */
    @Requires(optional = true)
    private IIsolateLoggerSvc pLogger;

    /** The serializer priority */
    @ServiceProperty(name = ISignalSerializerConstants.PROPERTY_PRIORITY, value = ""
            + Integer.MAX_VALUE, mandatory = true)
    private int pSerializerPriority;

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
     * @see org.psem2m.signals.ISignalSerializer#
     * canHandleType(java.lang.String)
     */
    @Override
    public boolean canHandleType(final String aContentType) {

        if (aContentType == null
                || aContentType.isEmpty()
                || aContentType
                        .equals(ISignalSerializerConstants.DEFAULT_POST_CONTENT_TYPE)) {
            // Accept invalid / default content type
            return true;
        }

        // Accept proper content type
        return aContentType
                .equals(ISignalSerializerConstants.CONTENT_TYPE_SERIALIZABLE);
    }

    /**
     * Accepts the object if it is null or if it implements {@link Serializable}
     * 
     * @see org.psem2m.signals.ISignalSerializer# canSerialize(java.lang.Object)
     */
    @Override
    public boolean canSerialize(final Object aObject) {

        return aObject == null || aObject instanceof Serializable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalSerializer# getContentType()
     */
    @Override
    public String getContentType() {

        return ISignalSerializerConstants.CONTENT_TYPE_SERIALIZABLE;
    }

    /**
     * Returns {@link Integer#MAX_VALUE}, as this is the default serializer
     * 
     * @see org.psem2m.signals.ISignalSerializer#getPriority ()
     */
    @Override
    public int getPriority() {

        return pSerializerPriority;
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
     * @see org.psem2m.signals.ISignalSerializer#
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
     * @see org.psem2m.signals.ISignalSerializer# unserializeData(byte[])
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
                    ISignalSerializerConstants.HTTP_INTERNAL_ERROR, e);

        } catch (final ClassNotFoundException e) {
            throw new InvalidDataException(
                    "Can't find the signal data class : " + e.getMessage(),
                    ISignalSerializerConstants.HTTP_INTERNAL_ERROR, e);

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
