/**
 * File:   ISignalRequestReader.java
 * Author: Thomas Calmant
 * Date:   16 janv. 2012
 */
package org.psem2m.isolates.services.remote.signals;


/**
 * Definition of a signal request handler
 * 
 * @author Thomas Calmant
 */
public interface ISignalRequestReader {

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
    ISignalData handleSignalRequest(String aContentType, byte[] aData)
            throws InvalidDataException;

}
