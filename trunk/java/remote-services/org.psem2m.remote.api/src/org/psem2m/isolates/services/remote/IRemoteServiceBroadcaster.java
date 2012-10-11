/**
 * File:   IRemoteServiceBroadcaster.java
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
public interface IRemoteServiceBroadcaster {

    /**
     * Sends a request to all other isolates to send notifications about their
     * current exported services.
     * 
     * They'll communicate directly with the sender RSR to register their end
     * points.
     */
    RemoteServiceEvent[] requestAllEndpoints();

    /**
     * Sends a request to the given isolate to send notifications about its
     * current exported services.
     * 
     * @param aIsolateId
     * @return
     */
    RemoteServiceEvent[] requestEndpoints(String aIsolateId);

    /**
     * Sends the given event to all other isolates
     * 
     * @param aEvent
     *            Remote service event to be sent
     */
    void sendNotification(RemoteServiceEvent aEvent);
}
