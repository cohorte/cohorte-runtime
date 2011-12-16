/**
 * File:   IRemoteServiceRepository.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.psem2m.isolates.services.remote;

import java.util.Collection;

import org.psem2m.isolates.services.remote.beans.EndpointDescription;
import org.psem2m.isolates.services.remote.beans.RemoteServiceRegistration;

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
     * Retrieves all local end points registered to this RSR.
     * 
     * @return All exported service known by this RSR
     */
    RemoteServiceRegistration[] getLocalRegistrations();

    /**
     * Registers a local end point. Doesn't asks the RSB to notify other
     * isolates.
     * 
     * @param aRegistration
     *            An exported service registration
     */
    void registerExportedService(RemoteServiceRegistration aRegistration);

    /**
     * Registers multiple local end points at once. Doesn't asks the RSB to
     * notify other isolates.
     * 
     * @param aRegistrations
     *            A list of remote service registrations done
     */
    void registerExportedServices(
            Collection<RemoteServiceRegistration> aRegistrations);

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
     * @param aEndpointsDescriptions
     *            A list of end points descriptions
     */
    void unregisterEndpoints(
            Collection<EndpointDescription> aEndpointsDescriptions);
}
