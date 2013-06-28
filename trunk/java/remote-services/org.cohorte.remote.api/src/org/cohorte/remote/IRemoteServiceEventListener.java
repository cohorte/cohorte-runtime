/**
 * File:   IServiceImporter.java
 * Author: Thomas Calmant
 * Date:   26 juil. 2011
 */
package org.cohorte.remote;

import org.cohorte.remote.beans.RemoteServiceEvent;

/**
 * Describes a remote service event listener
 * 
 * @author Thomas Calmant
 */
public interface IRemoteServiceEventListener {

    /**
     * Notifies the listener that an isolate has been lost
     * 
     * @param aIsolateUID
     *            The UID of the lost isolate
     */
    void handleIsolateLost(String aIsolateUID);

    /**
     * Notifies the listener that an isolate has been registered
     * 
     * @param aIsolateUID
     *            The UID of the new isolate
     */
    void handleIsolateReady(String aIsolateUID);

    /**
     * Notifies the listener that a remote event has been received.
     * 
     * @param aServiceEvent
     *            The remote service event
     */
    void handleRemoteEvent(RemoteServiceEvent aServiceEvent);
}
