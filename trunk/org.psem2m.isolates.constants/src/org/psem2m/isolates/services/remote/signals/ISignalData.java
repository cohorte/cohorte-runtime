/**
 * File:   ISignalData.java
 * Author: "Thomas Calmant"
 * Date:   21 sept. 2011
 */
package org.psem2m.isolates.services.remote.signals;

import java.io.Serializable;

/**
 * @author "Thomas Calmant"
 *
 */
public interface ISignalData {

    /**
     * Retrieves the ID of the isolate which sent the signal
     * 
     * @return the ID of the sender
     */
    public abstract String getIsolateSender();

    /**
     * Retrieves the data associated to the signal (can be null)
     * 
     * @return the signal data
     */
    public abstract Serializable getSignalContent();

}
