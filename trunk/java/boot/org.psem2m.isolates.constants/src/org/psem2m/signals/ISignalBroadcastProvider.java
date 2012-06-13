/**
 * File:   ISignalBroadcastProvider.java
 * Author: Thomas Calmant
 * Date:   23 sept. 2011
 */
package org.psem2m.signals;


/**
 * Represents a signal broadcast provider
 * 
 * @author Thomas Calmant
 */
public interface ISignalBroadcastProvider {

    /**
     * Sends the given data to the given access point
     * 
     * @param aAccess
     *            The access to use to communicate
     * @param aMode
     *            The request mode
     * @param aSignalName
     *            Signal name (URI like string)
     * @param aData
     *            Complete signal content (can't be null)
     * 
     * @return The response returned by the target, or null
     * @throws UnsendableDataException
     *             The given data can't be sent using signals
     * @throws Exception
     *             Something wrong happened
     */
    Object[] sendSignal(HostAccess aAccess, String aMode, String aSignalName,
            ISignalData aData) throws UnsendableDataException;
}
