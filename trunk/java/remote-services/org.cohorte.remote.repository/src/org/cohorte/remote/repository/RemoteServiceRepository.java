/**
 * File:   RemoteServiceRepository.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.cohorte.remote.repository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.remote.IRemoteServiceEventListener;
import org.cohorte.remote.IRemoteServiceRepository;
import org.cohorte.remote.beans.EndpointDescription;
import org.cohorte.remote.beans.RemoteServiceRegistration;
import org.osgi.service.log.LogService;

/**
 * Implementation of an RSR
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-remote-repository-factory")
@Provides(specifications = IRemoteServiceRepository.class)
@Instantiate(name = "cohorte-remote-repository")
public class RemoteServiceRepository implements IRemoteServiceRepository {

    /** Remote service event listeners */
    private final Set<IRemoteServiceEventListener> pListeners = new HashSet<IRemoteServiceEventListener>();

    /** Log service, injected by iPOJO */
    @Requires
    private LogService pLogger;

    /** Exported service registrations */
    private final Set<RemoteServiceRegistration> pRegistrations = new HashSet<RemoteServiceRegistration>();

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.IRemoteServiceRepository#addListener(org.cohorte.remote
     * .IRemoteServiceEventListener)
     */
    @Override
    public void addListener(final IRemoteServiceEventListener aListener) {

        if (aListener != null) {
            synchronized (pListeners) {
                pListeners.add(aListener);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.remote.IRemoteServiceRepository#getLocalRegistrations()
     */
    @Override
    public RemoteServiceRegistration[] getLocalRegistrations() {

        synchronized (pRegistrations) {
            return pRegistrations.toArray(new RemoteServiceRegistration[0]);
        }
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidatePojo() {

        pLogger.log(LogService.LOG_INFO,
                "COHORTE Remote Service Repository Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.IRemoteServiceRepository#
     * registerExportedService
     * (org.psem2m.isolates.services.remote.beans.RemoteServiceRegistration)
     */
    @Override
    public void registerExportedService(
            final RemoteServiceRegistration aRegistration) {

        if (aRegistration != null) {

            synchronized (pRegistrations) {
                pRegistrations.add(aRegistration);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.IRemoteServiceRepository#registerExportedServices(
     * java.util.Collection)
     */
    @Override
    public void registerExportedServices(
            final Collection<RemoteServiceRegistration> aRegistrations) {

        if (aRegistrations != null) {

            for (final RemoteServiceRegistration registration : aRegistrations) {
                registerExportedService(registration);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.IRemoteServiceRepository#removeListener(org.cohorte
     * .remote.IRemoteServiceEventListener)
     */
    @Override
    public void removeListener(final IRemoteServiceEventListener aListener) {

        if (aListener != null) {
            synchronized (pListeners) {
                pListeners.remove(aListener);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.IRemoteServiceRepository#unregisterEndpoint(org.cohorte
     * .remote.beans.EndpointDescription)
     */
    @Override
    public void unregisterEndpoint(
            final EndpointDescription aEndpointDescription) {

        synchronized (pRegistrations) {
            for (final RemoteServiceRegistration registration : pRegistrations) {
                registration.removeEndpoints(aEndpointDescription);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.IRemoteServiceRepository#unregisterEndpoints(java.
     * util.Collection)
     */
    @Override
    public void unregisterEndpoints(
            final Collection<EndpointDescription> aEndpointsDescriptions) {

        if (aEndpointsDescriptions == null || aEndpointsDescriptions.isEmpty()) {
            return;
        }

        // Convert the collection into an array
        final EndpointDescription[] endpointsDescriptionsArray = aEndpointsDescriptions
                .toArray(new EndpointDescription[aEndpointsDescriptions.size()]);

        synchronized (pRegistrations) {
            // Remove end points from registrations
            for (final RemoteServiceRegistration registration : pRegistrations) {
                registration.removeEndpoints(endpointsDescriptionsArray);
            }
        }
    }

    /**
     * Component validated
     */
    @Validate
    public void validatePojo() {

        pLogger.log(LogService.LOG_INFO,
                "COHORTE Remote Service Repository Ready");
    }
}
