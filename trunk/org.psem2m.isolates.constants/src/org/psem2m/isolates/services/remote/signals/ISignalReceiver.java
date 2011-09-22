/**
 * File:   ISignalReceiver.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.psem2m.isolates.services.remote.signals;

/**
 * Defines a signal receiver.
 * 
 * As it may be based on a single listening point and on asynchronous accesses,
 * listeners must be registered with a valid URI-like string to differentiate
 * themselves.
 * 
 * @author Thomas Calmant
 */
public interface ISignalReceiver {

    /**
     * Registers the given listener to the given signal
     * 
     * @param aSignalName
     *            Signal name (URI like string)
     * @param aListener
     *            Signal listener
     */
    void registerListener(String aSignalName, ISignalListener aListener);

    /**
     * Unregisters the given listener of the given signal
     * 
     * @param aSignalName
     *            Signal name (URI like string)
     * @param aListener
     *            Signal listener
     */
    void unregisterListener(String aSignalName, ISignalListener aListener);
}
