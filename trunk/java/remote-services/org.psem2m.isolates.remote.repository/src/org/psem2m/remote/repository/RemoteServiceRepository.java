/**
 * File:   RemoteServiceRepository.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.psem2m.remote.repository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.remote.IRemoteServiceEventListener;
import org.psem2m.isolates.services.remote.IRemoteServiceRepository;
import org.psem2m.isolates.services.remote.beans.EndpointDescription;
import org.psem2m.isolates.services.remote.beans.RemoteServiceRegistration;

/**
 * Implementation of an RSR
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-remote-rsr-factory", publicFactory = false)
@Provides(specifications = IRemoteServiceRepository.class)
@Instantiate(name = "psem2m-remote-rsr")
public class RemoteServiceRepository extends CPojoBase implements
        IRemoteServiceRepository {

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
     * org.psem2m.isolates.services.remote.IRemoteServiceRepository#addListener
     * (org.psem2m.isolates.services.remote.IRemoteServiceEventListener)
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
     * @see org.psem2m.utilities.CXObjectBase#destroy()
     */
    @Override
    public void destroy() {

        // ...
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.IRemoteServiceRepository#
     * getLocalRegistrations()
     */
    @Override
    public RemoteServiceRegistration[] getLocalRegistrations() {

        synchronized (pRegistrations) {
            return pRegistrations.toArray(new RemoteServiceRegistration[0]);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.log(LogService.LOG_INFO, "RSR gone");
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
     * @see org.psem2m.isolates.services.remote.IRemoteServiceRepository#
     * registerExportedServices(java.util.Collection)
     */
    @Override
    public void registerExportedServices(
            final Collection<RemoteServiceRegistration> aRegistrations) {

        if (aRegistrations != null) {

            for (RemoteServiceRegistration registration : aRegistrations) {
                registerExportedService(registration);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.IRemoteServiceRepository#removeListener
     * (org.psem2m.isolates.services.remote.IRemoteServiceEventListener)
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
     * @see org.psem2m.isolates.services.remote.IRemoteServiceRepository#
     * unregisterEndpoint
     * (org.psem2m.isolates.services.remote.beans.EndpointDescription)
     */
    @Override
    public void unregisterEndpoint(
            final EndpointDescription aEndpointDescription) {

        synchronized (pRegistrations) {
            for (RemoteServiceRegistration registration : pRegistrations) {
                registration.removeEndpoints(aEndpointDescription);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.IRemoteServiceRepository#
     * unregisterEndpoints(java.util.Collection)
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
            for (RemoteServiceRegistration registration : pRegistrations) {
                registration.removeEndpoints(endpointsDescriptionsArray);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.log(LogService.LOG_INFO, "RSR ready");
    }
}
