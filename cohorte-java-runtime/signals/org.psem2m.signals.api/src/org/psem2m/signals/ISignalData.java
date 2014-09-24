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
     * Retrieves the sender address
     * 
     * @return the sender address
     */
    String getSenderAddress();

    /**
     * Retrieves the name of the isolate which sent the signal (non-unique)
     * 
     * @return The name of the sender
     */
    String getSenderName();

    /**
     * Retrieves the name of the node of the isolate which sent the signal
     * 
     * @return the name of the node hosting the sender
     */
    String getSenderNodeName();

    /**
     * Retrieves the UID of the node of the isolate which sent the signal
     * 
     * @return the UID of the node hosting the sender
     */
    String getSenderNodeUID();

    /**
     * Retrieves the UID of the isolate which sent the signal (unique)
     * 
     * @return the UID of the sender
     */
    String getSenderUID();

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
