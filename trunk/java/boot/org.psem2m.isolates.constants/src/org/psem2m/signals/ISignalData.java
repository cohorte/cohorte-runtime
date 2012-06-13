/**
 * File:   ISignalData.java
 * Author: Thomas Calmant
 * Date:   21 sept. 2011
 */
package org.psem2m.signals;

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
    String getIsolateId();

    /**
     * Retrieves the node of the isolate which sent the signal
     * 
     * @return the node hosting the sender
     */
    String getIsolateNode();

    /**
     * Retrieves the data associated to the signal (can be null)
     * 
     * @return the signal data
     */
    Object getSignalContent();

    /**
     * Retrieves the time stamp of the signal creation
     * 
     * @return The signal creation time stamp
     */
    long getTimestamp();
}
