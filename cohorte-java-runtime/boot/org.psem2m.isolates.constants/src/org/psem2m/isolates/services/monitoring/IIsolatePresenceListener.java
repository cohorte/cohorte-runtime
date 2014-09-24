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
     * Notifies the loss of an isolate
     * 
     * @param aUID
     *            Isolate UID
     * @param aName
     *            Isolate name
     * @param aNodeUID
     *            Isolate node UID
     */
    void isolateLost(String aUID, String aName, String aNodeUID);

    /**
     * Notifies the registration of an isolate
     * 
     * @param aUID
     *            Isolate UID
     * @param aName
     *            Isolate name
     * @param aNodeUID
     *            Isolate node UID
     */
    void isolateReady(String aUID, String aName, String aNodeUID);
}
