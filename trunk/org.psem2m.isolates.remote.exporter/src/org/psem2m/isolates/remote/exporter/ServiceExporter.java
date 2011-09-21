/**
 * File:   RemoteServiceExporter.java
 * Author: Thomas Calmant
 * Date:   26 juil. 2011
 */
package org.psem2m.isolates.remote.exporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.remote.IEndpointHandler;
import org.psem2m.isolates.services.remote.IRemoteServiceRepository;
import org.psem2m.isolates.services.remote.IRemoteServiceBroadcaster;
import org.psem2m.isolates.services.remote.beans.EndpointDescription;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent.ServiceEventType;
import org.psem2m.isolates.services.remote.beans.RemoteServiceRegistration;

/**
 * Tracks services to be exported and uses active handlers to create associated
 * end points
 * 
 * @author Thomas Calmant
 */
public class ServiceExporter extends CPojoBase implements ServiceListener {

    /**
     * The filter to detect exported services only. Test the existence of the
     * service.exported.interfaces and service.exported.configs properties.
     */
    public static final String EXPORTED_SERVICE_FILTER = "(|(service.exported.interfaces=*)(service.exported.configs=*))";

    /** Remote service broadcaster (RSB) */
    private IRemoteServiceBroadcaster pBroadcaster;

    /** The bundle context */
    private final BundleContext pBundleContext;

    /** End point handlers */
    private IEndpointHandler[] pEndpointHandlers;

    /** The logger */
    private LogService pLogger;

    /** Remote service repository (RSR) */
    private IRemoteServiceRepository pRepository;

    /**
     * Base constructor
     * 
     * @param aBundleContext
     *            The bundle context
     */
    public ServiceExporter(final BundleContext aBundleContext) {

        super();
        pBundleContext = aBundleContext;
    }

    /**
     * Creates the end points corresponding to the given service
     * 
     * @param aServiceReference
     *            Reference to the service to use behind the end point
     * @return An exported service registration
     */
    protected RemoteServiceRegistration createEndpoints(
            final ServiceReference aServiceReference) {

        // Choose the exported interface
        final String[] serviceInterfaces = (String[]) aServiceReference
                .getProperty(Constants.OBJECTCLASS);
        if (serviceInterfaces == null || serviceInterfaces.length == 0) {
            // No service to export
            return null;
        }

        // TODO Choose it more wisely
        final String exportedInterface = serviceInterfaces[0];

        // Create end points
        final List<EndpointDescription> resultEndpoints = new ArrayList<EndpointDescription>();
        for (IEndpointHandler handler : pEndpointHandlers) {
            try {
                EndpointDescription[] newEndpoints = handler.createEndpoint(
                        exportedInterface, aServiceReference);

                // Store end points if they are valid
                if (newEndpoints != null && newEndpoints.length != 0) {
                    resultEndpoints.addAll(Arrays.asList(newEndpoints));
                }

            } catch (Throwable t) {
                // Log errors
                pLogger.log(LogService.LOG_WARNING,
                        "Error creating an end point", t);
            }
        }

        return new RemoteServiceRegistration(exportedInterface,
                Utilities.getServiceProperties(aServiceReference),
                resultEndpoints.toArray(new EndpointDescription[0]));
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

    /**
     * Exports a service : creates end points, register them to the RSR, then
     * notifies other isolates through the RSB.
     * 
     * @param aServiceReference
     *            Reference to the service to be exported
     * @return True on success, False if no end point has been created
     */
    protected boolean exportService(final ServiceReference aServiceReference) {

        // Prepare end points
        final RemoteServiceRegistration serviceRegistration = createEndpoints(aServiceReference);
        if (serviceRegistration == null) {
            // Abort if no end point could be created
            return false;
        }

        // Register them to the local RSR
        pRepository.registerExportedService(serviceRegistration);

        // Send an RSB notification
        final RemoteServiceEvent broadcastEvent = new RemoteServiceEvent(
                ServiceEventType.REGISTERED, serviceRegistration);

        pBroadcaster.sendNotification(broadcastEvent);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() throws BundleException {

        pBundleContext.removeServiceListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.
     * ServiceEvent)
     */
    @Override
    public void serviceChanged(final ServiceEvent aServiceEvent) {

        // Get the changed service reference
        final ServiceReference serviceReference = aServiceEvent
                .getServiceReference();

        switch (aServiceEvent.getType()) {
        // FIXME handle MODIFIED event (maybe we can register the service again)
        case ServiceEvent.REGISTERED:
            // Export service
            exportService(serviceReference);
            break;

        case ServiceEvent.MODIFIED_ENDMATCH:
            /*
             * The service properties doesn't match anymore : it must not be
             * exported by now.
             */
        case ServiceEvent.UNREGISTERING:
            // Unregistering exported service
            unexportService(serviceReference);
            break;

        // Ignore other events
        }
    }

    /**
     * Stops the export of the given service.
     * 
     * @param aServiceReference
     *            Reference to the service to stop to export
     */
    protected void unexportService(final ServiceReference aServiceReference) {

        // Grab end points list
        final List<EndpointDescription> serviceEndpoints = new ArrayList<EndpointDescription>();
        for (IEndpointHandler handler : pEndpointHandlers) {

            // Get handler end points for this service
            final EndpointDescription[] handlerEndpoints = handler
                    .getEndpoints(aServiceReference);

            if (handlerEndpoints != null && handlerEndpoints.length > 0) {
                serviceEndpoints.addAll(Arrays.asList(handlerEndpoints));
            }
        }

        if (serviceEndpoints.isEmpty()) {
            // No end point corresponds to this service, abort.
            return;
        }

        // Unregister them from the RSR
        pRepository.unregisterEndpoints(serviceEndpoints);

        // Find the exported interface
        final String[] exportedInterfaces = (String[]) aServiceReference
                .getProperty(Constants.OBJECTCLASS);

        if (exportedInterfaces != null && exportedInterfaces.length != 0) {
            // Only send notification if there is something to unregister

            // Prepare a service registration object
            final RemoteServiceRegistration serviceReg = new RemoteServiceRegistration(
                    exportedInterfaces[0],
                    Utilities.getServiceProperties(aServiceReference),
                    serviceEndpoints.toArray(new EndpointDescription[0]));

            // Send an RSB notification
            final RemoteServiceEvent broadcastEvent = new RemoteServiceEvent(
                    ServiceEventType.UNREGISTERED, serviceReg);

            pBroadcaster.sendNotification(broadcastEvent);
        }

        // Remove end points, after sending the broadcast event
        for (IEndpointHandler handler : pEndpointHandlers) {

            try {
                handler.destroyEndpoint(aServiceReference);

            } catch (Exception ex) {
                // Log error
                pLogger.log(LogService.LOG_WARNING,
                        "Can't remove endpoint from " + handler
                                + " for reference " + aServiceReference, ex);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() throws BundleException {

        pLogger.log(LogService.LOG_INFO, "validatePojo()");

        // Handle already registered services
        try {
            ServiceReference[] exportedServices = pBundleContext
                    .getAllServiceReferences(null, EXPORTED_SERVICE_FILTER);

            if (exportedServices != null) {
                for (ServiceReference serviceRef : exportedServices) {
                    // Fake event to have the same behavior
                    ServiceEvent serviceEvent = new ServiceEvent(
                            ServiceEvent.REGISTERED, serviceRef);

                    serviceChanged(serviceEvent);
                }
            }

        } catch (InvalidSyntaxException ex) {
            ex.printStackTrace();
        }

        // Register a listener for future exported services
        try {
            pBundleContext.addServiceListener(this, EXPORTED_SERVICE_FILTER);

        } catch (InvalidSyntaxException e) {

            pLogger.log(LogService.LOG_ERROR,
                    "Error creating the service listener", e);

            throw new BundleException(
                    "Error creating the service listener filter", e);
        }
    }
}
