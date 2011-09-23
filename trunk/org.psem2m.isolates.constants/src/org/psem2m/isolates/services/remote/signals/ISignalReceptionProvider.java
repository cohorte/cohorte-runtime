/**
 * File:   ISignalReceptionProvider.java
 * Author: "Thomas Calmant"
 * Date:   23 sept. 2011
 */
package org.psem2m.isolates.services.remote.signals;

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
     * Registers the given listener to all received signal
     * 
     * @param aListener
     *            Signal listener
     */
    void registerListener(ISignalListener aListener);

    /**
     * Unregisters the given listener
     * 
     * @param aListener
     *            Signal listener
     */
    void unregisterListener(ISignalListener aListener);
}
