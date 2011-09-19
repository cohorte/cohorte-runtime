/**
 * File:   IRemoveServiceBroadcaster.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.psem2m.isolates.services.remote;

import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent;

/**
 * Defines the Remote Service Broadcaster (RSB)
 * 
 * @author Thomas Calmant
 */
public interface IRemoveServiceBroadcaster {

    /**
     * Sends a request to all other isolates to send notifications about their
     * current state.
     * 
     * They'll communicate directly with the sender RSR to register their end
     * points.
     */
    void requestAllEndpoints();

    /**
     * Sends the given event to all other isolates
     * 
     * @param aEvent
     *            Remote service event to be sent
     */
    void sendNotification(RemoteServiceEvent aEvent);
}
