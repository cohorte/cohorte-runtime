/**
 * File:   IIsolatePresenceListener.java
 * Author: Thomas Calmant
 * Date:   11 juil. 2012
 */
package org.psem2m.isolates.services.monitoring;

/**
 * Represents an isolate presence listener
 * 
 * @author Thomas Calmant
 */
public interface IIsolatePresenceListener {

    /**
     * Defines the presence event type
     * 
     * @author Thomas Calmant
     */
    enum EPresence {
        /** Isolate registered in directory */
        REGISTERED,
        /** Isolate unregistered or lost */
        UNREGISTERED,
    }

    /**
     * Notifies the listener of an isolate presence event
     * 
     * @param aIsolateId
     *            Isolate ID
     * @param aNode
     *            Node of the isolate
     * @param aPresence
     *            Presence event type
     */
    void handleIsolatePresence(String aIsolateId, String aNode,
            EPresence aPresence);
}
