/**
 * File:   ISignalListener.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.psem2m.isolates.services.remote.signals;

import java.io.Serializable;

/**
 * Represents a signal listener
 * 
 * @author Thomas Calmant
 */
public interface ISignalListener {

    /**
     * Notifies listener that a signal has been received
     * 
     * @param aSignalName
     *            Received signal name
     * @param aSignalData
     *            Signal data
     */
    void handleReceivedSignal(String aSignalName, Serializable aSignalData);
}
