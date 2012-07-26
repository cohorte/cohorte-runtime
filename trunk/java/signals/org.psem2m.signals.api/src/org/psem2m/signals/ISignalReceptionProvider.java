/**
 * File:   ISignalReceptionProvider.java
 * Author: "Thomas Calmant"
 * Date:   23 sept. 2011
 */
package org.psem2m.signals;

/**
 * Represents a signal reception provider.
 * 
 * The service providing this interface must set the property IS_READY to true
 * in order to indicate that it is ready to receive messages from the outside
 * world.
 * 
 * @author Thomas Calmant
 */
public interface ISignalReceptionProvider {

    /** Provider "ready" flag */
    String PROPERTY_READY = "signal.provider.ready";

    /**
     * Retrieves the (host, port) couple to access this signal reception
     * provider. The result can be null, if the provider doesn't declare itself
     * as an access point.
     * 
     * @return An (host, port) couple
     */
    HostAccess getAccessInfo();

    /**
     * Registers the given receiver all received signal. Does nothing if a
     * receiver as already been set.
     * 
     * @param aListener
     *            A signal receiver service
     * @return True if the receiver has been set
     */
    boolean setReceiver(ISignalReceiver aReceiver);

    /**
     * Unregisters the given receiver. Does nothing if it wasn't the current
     * receiver.
     * 
     * @param aListener
     *            A signal receiver service
     */
    void unsetReceiver(ISignalReceiver aReceiver);
}
