/**
 * File:   IIsolatePresenceListener.java
 * Author: Thomas Calmant
 * Date:   11 juil. 2012
 */
package org.cohorte.monitor.api;

/**
 * Represents an isolate presence listener
 * 
 * @author Thomas Calmant
 */
public interface IIsolatePresenceListener {

    void isolateLost(String aUID, String aName, String aNode);

    void isolateReady(String aUID, String aName, String aNode);
}
