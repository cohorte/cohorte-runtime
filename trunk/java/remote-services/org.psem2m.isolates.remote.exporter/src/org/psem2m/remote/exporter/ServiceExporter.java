/**
 * File:   RemoteServiceExporter.java
 * Author: Thomas Calmant
 * Date:   26 juil. 2011
 */
package org.psem2m.remote.exporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.services.remote.IEndpointHandler;
import org.psem2m.isolates.services.remote.IRemoteServiceBroadcaster;
import org.psem2m.isolates.services.remote.IRemoteServiceRepository;
import org.psem2m.isolates.services.remote.IRemoteServicesConstants;
import org.psem2m.isolates.services.remote.InterfacePrefixUtils;
import org.psem2m.isolates.services.remote.beans.EndpointDescription;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent.ServiceEventType;
import org.psem2m.isolates.services.remote.beans.RemoteServiceRegistration;

/**
 * Tracks services to be exported and uses active handlers to create associated
 * end points
 * 
 * TODO: handle {@link IEndpointHandler} unbinding -&gt; send remote service
 * events
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-remote-exporter-factory")
public class ServiceExporter extends CPojoBase implements ServiceListener {

    /** End points handlers dependency ID */
    private static final String IPOJO_ENDPOINT_HANDLERS = "endpoint-handlers";

    /** Remote service broadcaster (RSB) */
    @Requires
    private IRemoteServiceBroadcaster pBroadcaster;

    /** The bundle context */
    private final BundleContext pBundleContext;

    /** End point handlers */
    @Requires(id = IPOJO_ENDPOINT_HANDLERS)
    private IEndpointHandler[] pEndpointHandlers;

    /** Mapping of local service ID -&gt; remote service registration */
    private final Map<ServiceReference<?>, RemoteServiceRegistration> pExportedServices = new HashMap<ServiceReference<?>, RemoteServiceRegistration>();

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Platform information service */
    @Requires
    private IPlatformDirsSvc pPlatform;

    /** Remote service repository (RSR) */
    @Requires
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
    private RemoteServiceRegistration createEndpoints(
            final ServiceReference<?> aServiceReference) {

        // Compute exported interfaces
        final Set<String> exportedInterfaces = getExportedInterfaces(aServiceReference);
        if (exportedInterfaces.isEmpty()) {
            // No interface to export
            return null;
        }

        // Create end points
        final List<EndpointDescription> resultEndpoints = new ArrayList<EndpointDescription>();
        for (final IEndpointHandler handler : pEndpointHandlers) {
            try {
                final EndpointDescription[] newEndpoints = handler
                        .createEndpoint(exportedInterfaces, aServiceReference);

                // Store end points if they are valid
                if (newEndpoints != null && newEndpoints.length != 0) {
                    resultEndpoints.addAll(Arrays.asList(newEndpoints));
                }

            } catch (final Throwable t) {
                // Log errors
                pLogger.logWarn(this, "createEndpoints",
                        "Error creating endpoint for service=",
                        exportedInterfaces, ":", t);
            }
        }

        if (resultEndpoints.isEmpty()) {
            // No end point created, return null
            pLogger.logWarn(this, "createEndpoints",
                    "No endpoint created for exports=", exportedInterfaces);
            return null;
        }

        // Add the synonyms
        exportedInterfaces.addAll(getSynonyms(aServiceReference));

        // Format the names of the exported interfaces
        final InterfacePrefixUtils prefixUtils = new InterfacePrefixUtils();
        final Set<String> prefixedExportedInterfaces = prefixUtils
                .formatNames(exportedInterfaces);

        return new RemoteServiceRegistration(pPlatform.getIsolateUID(),
                prefixedExportedInterfaces,
                Utilities.getServiceProperties(aServiceReference),
                resultEndpoints);
    }

    /**
     * Exports a service : creates end points, register them to the RSR, then
     * notifies other isolates through the RSB.
     * 
     * @param aServiceReference
     *            Reference to the service to be exported
     * @return True on success, False if no end point has been created
     */
    private synchronized boolean exportService(
            final ServiceReference<?> aServiceReference) {

        // Prepare end points
        final RemoteServiceRegistration serviceRegistration = createEndpoints(aServiceReference);
        if (serviceRegistration == null) {
            // Abort if no end point could be created
            return false;
        }

        // Register them to the local RSR
        pRepository.registerExportedService(serviceRegistration);
        pExportedServices.put(aServiceReference, serviceRegistration);

        // Send an RSB notification
        final RemoteServiceEvent broadcastEvent = new RemoteServiceEvent(
                ServiceEventType.REGISTERED, serviceRegistration);

        pBroadcaster.sendNotification(broadcastEvent);
        pLogger.logDebug(this, "exportService",
                "Export notification sent for ref=", aServiceReference);
        return true;
    }

    /**
     * Computes the interfaces to export
     * 
     * @param aServiceReference
     *            Reference of the service to export
     * @return A set of interfaces (can be empty)
     */
    private Set<String> getExportedInterfaces(
            final ServiceReference<?> aServiceReference) {

        // Choose the exported interface
        final String[] serviceInterfaces = (String[]) aServiceReference
                .getProperty(Constants.OBJECTCLASS);
        if (serviceInterfaces == null || serviceInterfaces.length == 0) {
            // No service to export
            return new HashSet<String>();
        }

        // Select the exported interface
        final Set<String> exportedInterfaces = new LinkedHashSet<String>();
        final Object rawExported = aServiceReference
                .getProperty(IRemoteServicesConstants.SERVICE_EXPORTED_INTERFACES);

        // Simplify treatment by converting the object into a collection if it
        // is an array.
        final Object exported = Utilities.arrayToIterable(rawExported);
        if (exported instanceof String) {
            // Trim the string
            final String trimmedExport = ((String) exported).trim();

            if (trimmedExport.equals("*") || trimmedExport.isEmpty()) {
                // Export all interfaces
                exportedInterfaces.addAll(Arrays.asList(serviceInterfaces));

            } else {
                // Exported interface is single
                exportedInterfaces.add((String) exported);
            }

        } else if (exported instanceof Collection) {
            // We got a list
            for (final Object rawName : (Collection<?>) exported) {
                if (rawName instanceof String) {
                    exportedInterfaces.add((String) rawName);
                }
            }
        }

        // Only export interfaces declared in the service objectClass property
        exportedInterfaces.retainAll(Arrays.asList(serviceInterfaces));

        return exportedInterfaces;
    }

    /**
     * Retrieves the exported interfaces synonyms from the service properties.
     * 
     * @param aServiceReference
     *            The exported service reference
     * @return A set of interface names, never null
     */
    private Set<String> getSynonyms(final ServiceReference<?> aServiceReference) {

        final Set<String> foundSynonyms = new LinkedHashSet<String>();

        // Get the raw value
        final Object rawSynonyms = aServiceReference
                .getProperty(IRemoteServicesConstants.SYNONYM_INTERFACES);

        // Simplify treatment by converting the object into a collection if it
        // is an array.
        final Object synonyms = Utilities.arrayToIterable(rawSynonyms);

        if (synonyms instanceof String) {
            // Got a simple string
            foundSynonyms.add((String) synonyms);

        } else if (synonyms instanceof Collection) {
            // We got a list
            for (final Object rawName : (Collection<?>) synonyms) {
                if (rawName instanceof String) {
                    foundSynonyms.add((String) rawName);
                }
            }
        }

        return foundSynonyms;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public synchronized void invalidatePojo() throws BundleException {

        // Unregister to service events
        pBundleContext.removeServiceListener(this);

        // Stop the exports
        for (final ServiceReference<?> reference : pExportedServices.keySet()) {
            unexportService(reference);
        }

        // Clean up the map
        pExportedServices.clear();
    }

    /**
     * Tests if the given service is already exported
     * 
     * @param aServiceReference
     *            A service reference
     * @return True if the service is already exported
     */
    private boolean isAlreadyExported(
            final ServiceReference<?> aServiceReference) {

        return pExportedServices.containsKey(aServiceReference);
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
        final ServiceReference<?> serviceReference = aServiceEvent
                .getServiceReference();

        switch (aServiceEvent.getType()) {

        case ServiceEvent.MODIFIED:
            // Service properties have been modified
            if (!isAlreadyExported(serviceReference)) {
                // Export the service if it just matched our filter
                exportService(serviceReference);

            } else {
                // Propagate the modification
                updateService(serviceReference);
            }
            break;

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
    private synchronized void unexportService(
            final ServiceReference<?> aServiceReference) {

        // Get the registration object
        final RemoteServiceRegistration serviceReg = pExportedServices
                .remove(aServiceReference);
        if (serviceReg == null) {
            // Unknown service
            return;
        }

        // Unregister the end points from the RSR
        pRepository
                .unregisterEndpoints(Arrays.asList(serviceReg.getEndpoints()));

        // Send an RSB notification
        final RemoteServiceEvent broadcastEvent = new RemoteServiceEvent(
                ServiceEventType.UNREGISTERED, serviceReg);

        pBroadcaster.sendNotification(broadcastEvent);

        // Remove end points, after sending the broadcast event
        for (final IEndpointHandler handler : pEndpointHandlers) {

            try {
                handler.destroyEndpoint(aServiceReference);

            } catch (final Exception ex) {
                // Log error
                pLogger.logWarn(this, "unexportService",
                        "Can't remove endpoint from handler=", handler,
                        "for reference=", aServiceReference, ":", ex);
            }
        }

        // Remote the export entry
        pExportedServices.remove(aServiceReference);
    }

    /**
     * Updates a service : sends a modification event for an already exported
     * service
     * 
     * @param aServiceReference
     *            Reference to the modified service
     * @return True on success, False if the end point wasn't known
     */
    private synchronized boolean updateService(
            final ServiceReference<?> aServiceReference) {

        // Get the end point
        final RemoteServiceRegistration serviceRegistration = pExportedServices
                .get(aServiceReference);
        if (serviceRegistration == null) {
            // Abort if no end point could be created
            return false;
        }

        // Update the registration
        serviceRegistration.setServiceProperties(Utilities
                .getServiceProperties(aServiceReference));

        // Send the notification
        final RemoteServiceEvent event = new RemoteServiceEvent(
                ServiceEventType.MODIFIED, serviceRegistration);
        pBroadcaster.sendNotification(event);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        /*
         * The filter to detect exported services only. Test the existence of
         * the service.exported.interfaces and service.exported.configs
         * properties.
         * 
         * Optionally, a psem2m.service.export property can be set to false to
         * block the export.
         */
        final StringBuilder serviceFilterBuilder = new StringBuilder("(&");

        /* PSEM2M flag (denies export if present and not true) */
        serviceFilterBuilder.append("(|(!(")
                .append(IRemoteServicesConstants.PSEM2M_SERVICE_EXPORT)
                .append("=*))(")
                .append(IRemoteServicesConstants.PSEM2M_SERVICE_EXPORT)
                .append("=true))");

        /* OSGi properties */
        serviceFilterBuilder.append("(|(")
                .append(IRemoteServicesConstants.SERVICE_EXPORTED_INTERFACES)
                .append("=*)(")
                .append(IRemoteServicesConstants.SERVICE_EXPORTED_CONFIGS)
                .append("=*))");

        // End of filter
        serviceFilterBuilder.append(")");

        // Get the string form
        final String serviceFilter = serviceFilterBuilder.toString();

        // Handle already registered services
        try {
            final ServiceReference<?>[] exportedServices = pBundleContext
                    .getAllServiceReferences(null, serviceFilter);

            if (exportedServices != null) {
                for (final ServiceReference<?> serviceRef : exportedServices) {
                    exportService(serviceRef);
                }
            }

        } catch (final InvalidSyntaxException ex) {
            pLogger.logWarn(this, "validatePojo",
                    "Error looking for services waiting to be exported:", ex);
        }

        // Register a listener for future exported services
        try {
            pBundleContext.addServiceListener(this, serviceFilter);

        } catch (final InvalidSyntaxException e) {

            pLogger.logSevere(this, "validatePojo",
                    "Error creating the service listener:", e);

            throw new BundleException(
                    "Error creating the service listener filter", e);
        }

        pLogger.logInfo(this, "validatePojo", "PSEM2M Service Exporter Ready.");
    }
}
