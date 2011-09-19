/**
 * File:   RemoteServiceRepository.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.psem2m.isolates.remote.repository;

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

/**
 * Implementation of an RSR
 * 
 * @author Thomas Calmant
 */
@Component(name = "remote-rsr-factory", publicFactory = false)
@Provides(specifications = IRemoteServiceRepository.class)
@Instantiate(name = "remote-rsr")
public class RemoteServiceRepository extends CPojoBase implements
        IRemoteServiceRepository {

    /** Remote service event listeners */
    private final Set<IRemoteServiceEventListener> pListeners = new HashSet<IRemoteServiceEventListener>();

    /** Locally registered end points */
    private final Set<EndpointDescription> pLocalEndpoints = new HashSet<EndpointDescription>();

    /** Log service, injected by iPOJO */
    @Requires
    private LogService pLogger;

    /** Known remote end points */
    private final Set<EndpointDescription> pRemoteEndpoins = new HashSet<EndpointDescription>();

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
            pListeners.add(aListener);
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
     * getKnownEndpoints()
     */
    @Override
    public EndpointDescription[] getKnownEndpoints() {

        // FIXME always empty
        return pRemoteEndpoins.toArray(new EndpointDescription[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.IRemoteServiceRepository#
     * getLocalEndpoints()
     */
    @Override
    public EndpointDescription[] getLocalEndpoints() {

        return pLocalEndpoints.toArray(new EndpointDescription[0]);
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
     * @see
     * org.psem2m.isolates.services.remote.IRemoteServiceRepository#registerEndpoint
     * (org.psem2m.isolates.services.remote.beans.EndpointDescription)
     */
    @Override
    public void registerEndpoint(final EndpointDescription aEndpointDescription) {

        // Just add it to the local end points
        pLocalEndpoints.add(aEndpointDescription);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.IRemoteServiceRepository#
     * registerEndpoints(java.util.Collection)
     */
    @Override
    public void registerEndpoints(
            final Collection<EndpointDescription> aEndpointsDescriptions) {

        // Just add them to the local end points
        pLocalEndpoints.addAll(aEndpointsDescriptions);
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
            pListeners.remove(aListener);
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

        // Just remove it
        pLocalEndpoints.remove(aEndpointDescription);
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

        // Just remove them
        pLocalEndpoints.removeAll(aEndpointsDescriptions);
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
