/**
 * File:   IRemoteServiceClientHandler.java
 * Author: Thomas Calmant
 * Date:   27 juil. 2011
 */
package org.psem2m.isolates.commons.remote;

/**
 * @author Thomas Calmant
 * 
 */
public interface IRemoteServiceClientHandler {

    /**
     * Destroys the given proxy object
     * 
     * @param aProxy
     *            A proxy object
     */
    public abstract void destroyProxy(final Object aProxy);

    /**
     * Creates a proxy from the given event, if possible
     * 
     * @param aServiceEvent
     *            Remote service event
     * @return A service proxy, null on error
     * @throws ClassNotFoundException
     *             The interface to proxify is not visible
     */
    public abstract Object getRemoteProxy(final RemoteServiceEvent aServiceEvent)
	    throws ClassNotFoundException;
}
