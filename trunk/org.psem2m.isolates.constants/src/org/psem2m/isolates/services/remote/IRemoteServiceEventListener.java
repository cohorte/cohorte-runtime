/**
 * File:   IServiceImporter.java
 * Author: Thomas Calmant
 * Date:   26 juil. 2011
 */
package org.psem2m.isolates.services.remote;

import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent;

/**
 * Describes a remote service event listener
 * 
 * @author Thomas Calmant
 */
public interface IRemoteServiceEventListener {

    /**
     * Notifies the listener that a remote event has been received.
     * 
     * @param aServiceEvent
     *            The remote service event
     */
    void handleRemoteEvent(RemoteServiceEvent aServiceEvent);
}
