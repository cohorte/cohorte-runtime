/**
 * File:   IForkerEventListener.java
 * Author: Thomas Calmant
 * Date:   5 juin 2012
 */
package org.psem2m.forker;

/**
 * Defines a forker event listener.
 * 
 * A listener is called when a forker is registered or unregistered by the
 * forker aggregator.
 * 
 * @author Thomas Calmant
 */
public interface IForkerEventListener {

    /**
     * Forker event types definition
     * 
     * @author Thomas Calmant
     */
    enum EForkerEventType {

        /** Forker registered */
        REGISTERED,

        /** Forker unregistered */
        UNREGISTERED,
    }

    /**
     * Method called back when a forker event occurs
     * 
     * @param aEventType
     *            The forker event type
     * @param aForkerId
     *            The forker isolate ID
     * @param aNode
     *            the forker node name
     * @param aHost
     *            The host handled by the forker
     */
    void handleForkerEvent(EForkerEventType aEventType, String aForkerId,
            String aNode, String aHost);
}
