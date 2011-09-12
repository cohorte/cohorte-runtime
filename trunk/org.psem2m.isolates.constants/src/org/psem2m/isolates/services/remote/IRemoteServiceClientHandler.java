/**
 * File:   IRemoteServiceClientHandler.java
 * Author: Thomas Calmant
 * Date:   27 juil. 2011
 */
package org.psem2m.isolates.services.remote;

import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent;

/**
 * Represents a client-side remote services handler
 * 
 * @author Thomas Calmant
 */
public interface IRemoteServiceClientHandler {

    /**
     * Destroys the given proxy object
     * 
     * @param aProxy
     *            A proxy object
     */
    void destroyProxy(final Object aProxy);

    /**
     * Creates a proxy from the given event, if possible
     * 
     * @param aServiceEvent
     *            Remote service event
     * @return A service proxy, null on error
     * @throws ClassNotFoundException
     *             The interface to proxify is not visible
     */
    Object getRemoteProxy(final RemoteServiceEvent aServiceEvent)
	    throws ClassNotFoundException;
}
