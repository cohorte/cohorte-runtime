/**
 * File:   IForkerPresenceListener.java
 * Author: "Thomas Calmant"
 * Date:   29 janv. 2013
 */
package org.cohorte.monitor.api;

/**
 * @author "Thomas Calmant"
 * 
 */
public interface IForkerPresenceListener {

    void forkerLost(String aUID, String aNode);

    void forkerReady(String aUID, String aNode);
}
