/**
 * File:   ISignalReceiver.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.psem2m.signals;

/**
 * Defines a signal receiver.
 * 
 * As it may be based on a single listening point and on asynchronous accesses,
 * listeners must be registered with a valid URI-like string to differentiate
 * themselves.
 * 
 * Implementations must set the service property {@link #PROPERTY_ONLINE} to
 * true (default: false), when it's ready to hear from other isolates.
 * Theoretically, it should be set when an {@link ISignalReceptionProvider}
 * service is available with the property
 * {@link ISignalReceptionProvider#PROPERTY_READY} set to true.
 * 
 * This service must be available for local-only reception event if there is no
 * reception provider.
 * 
 * @author Thomas Calmant
 */
public interface ISignalReceiver {

    /** If true, the receiver can hear other isolates */
    String PROPERTY_ONLINE = "signal-receiver-online";

    /**
     * Retrieves the (host, port) couple to access this signal receiver.
     * 
     * <b>WARNING:</b> The host might often be "localhost"
     * 
     * @return An (host, port) couple
     */
    HostAccess getAccessInfo();

    /**
     * Reception of a signal by a provider
     * 
     * @param aSignalName
     *            Signal name
     * @param aData
     *            Signal content
     * @param aMode
     *            The request mode
     * @return The result of all listeners and a result code
     */
    SignalResult handleReceivedSignal(String aSignalName,
            ISignalData aData, String aMode);

    /**
     * Reception of a locally sent message, without using a reception provider
     * 
     * @param aSignalName
     *            Signal name
     * @param aData
     *            Signal content
     * @param aMode
     *            The request mode
     * @return The result of all listeners and a result code
     */
    SignalResult localReception(String aSignalName, ISignalData aData,
            String aMode);

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
