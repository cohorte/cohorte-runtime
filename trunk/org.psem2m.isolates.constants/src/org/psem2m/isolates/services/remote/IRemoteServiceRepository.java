/**
 * File:   IRemoteServiceRepository.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.psem2m.isolates.services.remote;

import java.util.Collection;

import org.psem2m.isolates.services.remote.beans.EndpointDescription;

/**
 * Description of an RSR (Remote Service Repository)
 * 
 * @author Thomas Calmant
 */
public interface IRemoteServiceRepository {

    /**
     * Adds a listener to the RSR
     * 
     * @param aListener
     *            A new listener
     */
    void addListener(IRemoteServiceEventListener aListener);

    /**
     * Retrieves all remote end points currently known by the RSR. The RSR
     * should not send requests to update its state to return to this method.
     * 
     * @return The current state of the RSR
     */
    EndpointDescription[] getKnownEndpoints();

    /**
     * Retrieves all local end points registered to this RSR.
     * 
     * @return All local end points known by this RSR
     */
    EndpointDescription[] getLocalEndpoints();

    /**
     * Registers a local end point. Doesn't asks the RSB to notify other
     * isolates.
     * 
     * @param aEndpointDescription
     *            A end point description
     */
    void registerEndpoint(EndpointDescription aEndpointDescription);

    /**
     * Registers multiple local end points at once. Doesn't asks the RSB to
     * notify other isolates.
     * 
     * @param aEndpointDescription
     *            A list of end points descriptions
     */
    void registerEndpoints(
            Collection<EndpointDescription> aEndpointsDescriptions);

    /**
     * Removes a listener from the RSR
     * 
     * @param aListener
     *            Listener to be removed
     */
    void removeListener(IRemoteServiceEventListener aListener);

    /**
     * Unregisters a local end point. Doesn't asks the RSB to notify other
     * isolates.
     * 
     * @param aEndpointDescription
     *            A end point description
     */
    void unregisterEndpoint(EndpointDescription aEndpointDescription);

    /**
     * Unregisters multiple local end points at once. Doesn't asks the RSB to
     * notify other isolates.
     * 
     * @param aEndpointDescription
     *            A list of end points descriptions
     */
    void unregisterEndpoints(
            Collection<EndpointDescription> aEndpointsDescriptions);
}
