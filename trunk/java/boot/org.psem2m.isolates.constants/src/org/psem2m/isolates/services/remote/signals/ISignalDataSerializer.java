/**
 * File:   ISignalDataSerializer.java
 * Author: Thomas Calmant
 * Date:   16 janv. 2012
 */
package org.psem2m.isolates.services.remote.signals;

/**
 * Handles the convertion between a signal data object and its stream
 * representation
 * 
 * @author Thomas Calmant
 */
public interface ISignalDataSerializer {

    /**
     * Tests if this serializer can handle a serialized stream of the given
     * content type
     * 
     * @param aContentType
     *            A content type
     * @return True if the content type can be handled
     */
    boolean canHandleType(String aContentType);

    /**
     * Tests if this serializer can handle the given object
     * 
     * @param aObject
     *            An object (can be null)
     * @return True if the object can be handled
     */
    boolean canSerialize(Object aObject);

    /**
     * Retrieves the serialized data MIME type
     * 
     * @return The serialized form MIME type
     */
    String getContentType();

    /**
     * Retrieves the priority of this serializer.
     * 
     * The lower the value, the higher the priority.
     * 
     * @return The serializer priority
     */
    int getPriority();

    /**
     * Serializes the given object in the given byte array
     * 
     * @param aObject
     *            An object
     * @return The serialized object
     * 
     * @throws UnsendableDataException
     *             The given object can be serialized
     */
    byte[] serializeData(Object aObject) throws UnsendableDataException;

    /**
     * Deserializes the given byte array
     * 
     * @param aBytes
     *            A serialized object
     * @return The deserialized object
     * @throws InvalidDataException
     *             The deserialization failed
     */
    Object unserializeData(byte[] aBytes) throws InvalidDataException;
}
