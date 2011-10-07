/**
 * File:   ISignalData.java
 * Author: Thomas Calmant
 * Date:   21 sept. 2011
 */
package org.psem2m.isolates.services.remote.signals;

import java.io.Serializable;

/**
 * Represents the object associated to a signal
 * 
 * @author Thomas Calmant
 */
public interface ISignalData {

    /**
     * Retrieves the ID of the isolate which sent the signal
     * 
     * @return the ID of the sender
     */
    String getIsolateSender();

    /**
     * Retrieves the signal sender host name, null if unusable.
     * 
     * @return The sender host name
     */
    String getSenderHostName();

    /**
     * Retrieves the data associated to the signal (can be null)
     * 
     * @return the signal data
     */
    Serializable getSignalContent();

    /**
     * Retrieves the time stamp of the signal creation
     * 
     * @return The signal creation time stamp
     */
    long getTimestamp();
}
