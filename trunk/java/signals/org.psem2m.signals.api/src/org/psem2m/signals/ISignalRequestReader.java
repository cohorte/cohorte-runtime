/**
 * File:   ISignalRequestReader.java
 * Author: Thomas Calmant
 * Date:   16 janv. 2012
 */
package org.psem2m.signals;

/**
 * Definition of a signal request handler
 * 
 * @author Thomas Calmant
 */
public interface ISignalRequestReader {

    /**
     * Handles the received signal
     * 
     * @param aSignalName
     *            The received signal name
     * @param aSignalData
     *            Signal data (result of
     *            {@link #unserializeSignalContent(String, byte[])})
     * @param aMode
     *            Request mode
     * @return The central signal receiver result
     */
    SignalResult handleSignal(String aSignalName,
            ISignalData aSignalData, String aMode);

    /**
     * Serializes the given result. Tries the preferred content type first.
     * 
     * @param aPreferredContentType
     *            The preferred serialization format
     * @param aResult
     *            The result of
     *            {@link #handleSignal(String, ISignalData, String)}
     * @return The serialized result, or null
     */
    SignalContent serializeSignalResult(String aPreferredContentType,
            SignalResult aResult);

    /**
     * Uses serializers to read the given byte array
     * 
     * @param aContentType
     *            The byte array content type (can be null)
     * @param aData
     *            A byte array (can be null)
     * @return The read signal data
     * 
     * @throws InvalidDataException
     *             The given data array can't be understood
     */
    ISignalData unserializeSignalContent(String aContentType, byte[] aData)
            throws InvalidDataException;
}
