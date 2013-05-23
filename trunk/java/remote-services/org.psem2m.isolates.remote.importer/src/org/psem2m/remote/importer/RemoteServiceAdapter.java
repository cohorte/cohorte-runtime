/**
 * File:   RemoteServiceImporter.java
 * Author: Thomas Calmant
 * Date:   26 juil. 2011
 */
package org.psem2m.remote.importer;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.remote.IRemoteServiceBroadcaster;
import org.psem2m.isolates.services.remote.IRemoteServiceClientHandler;
import org.psem2m.isolates.services.remote.IRemoteServiceEventListener;
import org.psem2m.isolates.services.remote.IRemoteServicesConstants;
import org.psem2m.isolates.services.remote.InterfacePrefixUtils;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent;
import org.psem2m.isolates.services.remote.beans.RemoteServiceRegistration;

/**
 * Core the Remote Service Importer (RSI). Responds to RSR events to create and
 * delete remote services proxies.
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-remote-importer-factory")
@Provides(specifications = IRemoteServiceEventListener.class)
public class RemoteServiceAdapter extends CPojoBase implements
        IRemoteServiceEventListener, BundleListener {

    /** Remote service broadcaster (RSB) */
    @Requires
    private IRemoteServiceBroadcaster pBroadcaster;

    /** The component bundle context */
    private final BundleContext pBundleContext;

    /** Remote service proxy handlers */
    @Requires
    private IRemoteServiceClientHandler[] pClientHandlers;

    /** The service interfaces exclude filters */
    private final Set<String> pExcludeFilters = new HashSet<String>();

    /** The service interfaces include filters */
    private final Set<String> pIncludeFilters = new HashSet<String>();

    /** Isolate UID -&gt; Services IDs mapping */
    private final Map<String, Set<String>> pIsolatesServices = new HashMap<String, Set<String>>();

    /** Log service */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Registered services: Service ID -&gt; Proxy information */
    private final Map<String, ProxyServiceInfo> pRegisteredServices = new HashMap<String, ProxyServiceInfo>();

    /** Registrations waiting for at least one class */
    private final Set<RemoteServiceRegistration> pWaitingRegistrations = new HashSet<RemoteServiceRegistration>();

    /**
     * Constructor
     * 
     * @param aBundleContext
     *            The bundle context
     */
    public RemoteServiceAdapter(final BundleContext aBundleContext) {

        super();
        pBundleContext = aBundleContext;
    }

    /**
     * Tests if the given interface can be accepted by the remote service
     * importer
     * 
     * @param aInterfaceName
     *            An imported interface name
     * @return True if the service can be imported
     */
    private boolean acceptInterface(final String aInterfaceName) {

        // Tests results
        boolean include = false;
        boolean exclude = false;

        // Test include filters
        for (final String includeFilter : pIncludeFilters) {
            if (Utilities.matchFilter(aInterfaceName, includeFilter)) {
                include = true;
                break;
            }
        }

        if (pIncludeFilters.isEmpty()) {
            // Be friendly when no filter is given
            include = true;
        }

        // Test exclude filters
        for (final String excludeFilter : pExcludeFilters) {
            if (Utilities.matchFilter(aInterfaceName, excludeFilter)) {
                exclude = true;
            }
        }

        if (pExcludeFilters.isEmpty() && !pIncludeFilters.isEmpty()) {
            // An inclusion filter is given, exclude everything else
            exclude = !include;
        }

        if (include ^ exclude) {
            // Only one possibility (include or not include)
            return include;

        } else if (!include) {
            // Inclusion refused, exclusion not indicated : refuse the interface
            return false;

        } else {
            // Worst case : both inclusion and exclusion flags are set
            // Only accept if the interface full name is included
            return pIncludeFilters.contains(aInterfaceName);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.
     * BundleEvent)
     */
    @Override
    public void bundleChanged(final BundleEvent aEvent) {

        switch (aEvent.getType()) {
        case BundleEvent.STARTED:
            // Call handler after the bundle started
            bundleStarted(aEvent.getBundle());
            break;

        case BundleEvent.STOPPING:
            // Call handler before the bundle has stopped
            bundleStopped(aEvent.getBundle());
            break;
        }
    }

    /**
     * Tries to update the imported service and to import new services after a
     * bundle started
     * 
     * @param aBundle
     *            The started bundle
     */
    private synchronized void bundleStarted(final Bundle aBundle) {

        // Find the services to update
        final Set<String> serviceIds = new HashSet<String>();
        for (final IRemoteServiceClientHandler handler : pClientHandlers) {
            serviceIds.addAll(handler.bundleStarted(aBundle));
        }

        pLogger.logDebug(this, "bundleStarted", "Services to update=",
                serviceIds);

        for (final String serviceId : serviceIds) {
            // Get the associated registration
            final RemoteServiceRegistration registration = pRegisteredServices
                    .get(serviceId).getRemoteRegistration();

            // Delete the service
            unregisterService(registration.getHostIsolate(), serviceId);

            // Create it again
            registerService(registration);
        }

        // Use a temporary set and clear the global one, as the latter will be
        // reconstructed
        final Set<RemoteServiceRegistration> waitings = new HashSet<RemoteServiceRegistration>(
                pWaitingRegistrations);
        pWaitingRegistrations.clear();

        // Try to import the waiting services
        for (final RemoteServiceRegistration registration : waitings) {
            pLogger.logDebug(this, "bundleStarted",
                    "Trying to import waiting=",
                    registration.getExportedInterfaces());
            registerService(registration);
        }
    }

    /**
     * Tries to update the imported service and to import new services after a
     * bundle stopped
     * 
     * @param aBundle
     *            The stopped bundle
     */
    private synchronized void bundleStopped(final Bundle aBundle) {

        // Find the services to update
        final Set<String> serviceIds = new HashSet<String>();
        for (final IRemoteServiceClientHandler handler : pClientHandlers) {
            serviceIds.addAll(handler.bundleStopped(aBundle));
        }

        for (final String serviceId : serviceIds) {
            // Get the associated registration
            final RemoteServiceRegistration registration = pRegisteredServices
                    .get(serviceId).getRemoteRegistration();

            // Delete the service
            unregisterService(registration.getHostIsolate(), serviceId);

            // Create it again
            registerService(registration);
        }
    }

    /**
     * Filters the given properties to remove service export ones
     * 
     * @param aServiceProperties
     *            Imported service properties
     * @return The service properties, without the remote service ones
     */
    private Dictionary<String, Object> filterProperties(
            final Map<String, Object> aServiceProperties) {

        if (aServiceProperties == null) {
            return null;
        }

        final Dictionary<String, Object> filteredProperties = new Hashtable<String, Object>(
                aServiceProperties.size());

        // Copy properties, omitting "export" keys
        for (final Entry<String, Object> entry : aServiceProperties.entrySet()) {

            final String property = entry.getKey();
            if (!property
                    .startsWith(IRemoteServicesConstants.SERVICE_EXPORTED_PREFIX)) {
                // Ignore export properties
                filteredProperties.put(property, entry.getValue());
            }

        }

        // Add "import" properties
        filteredProperties.put(IRemoteServicesConstants.SERVICE_IMPORTED,
                "true");

        final Object exportedProperty = aServiceProperties
                .get(IRemoteServicesConstants.SERVICE_EXPORTED_CONFIGS);
        if (exportedProperty != null) {
            filteredProperties.put(
                    IRemoteServicesConstants.SERVICE_IMPORTED_CONFIGS,
                    exportedProperty);
        }

        return filteredProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.IRemoteServiceEventListener#
     * handleIsolateLost(java.lang.String)
     */
    @Override
    public synchronized void handleIsolateLost(final String aIsolateId) {

        final Set<String> services = pIsolatesServices.get(aIsolateId);
        if (services == null) {
            // Nothing to do
            pLogger.logDebug(this, "handleIsolateLost",
                    "No services associated to lost isolate=", aIsolateId);
            return;
        }

        // Use an array, as the map will be modified by unregisterService
        final String[] servicesIds = services.toArray(new String[0]);

        // Unregister all corresponding services
        for (final String serviceId : servicesIds) {
            unregisterService(aIsolateId, serviceId);

            pLogger.logDebug(this, "handleIsolateLost", aIsolateId,
                    "unregisters", serviceId);
        }

        // Clear the map list (just to be sure)
        services.clear();

        pLogger.logDebug(this, "handleIsolateLost", aIsolateId, "handled.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.IRemoteServiceEventListener#
     * handleIsolateReady(java.lang.String)
     */
    @Override
    public void handleIsolateReady(final String aIsolateId) {

        // Ask for its end points
        requestEndpoints(aIsolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.remote.IRemoteServiceEventListener#
     * handleRemoteEvent(org.psem2m.isolates.commons.remote.RemoteServiceEvent)
     */
    @Override
    public void handleRemoteEvent(final RemoteServiceEvent aServiceEvent) {

        // Get the service registration
        final RemoteServiceRegistration registration = aServiceEvent
                .getServiceRegistration();

        switch (aServiceEvent.getEventType()) {
        case REGISTERED: {
            // New service registered
            registerService(registration);
            break;
        }

        case MODIFIED: {
            // Existing service properties updated
            updateService(registration);
            break;
        }

        case UNREGISTERED: {
            // Service gone
            unregisterService(registration.getHostIsolate(),
                    registration.getServiceId());
            break;
        }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public synchronized void invalidatePojo() throws BundleException {

        // Unregister to bundle events
        pBundleContext.removeBundleListener(this);

        // Use a copy of the keySet, as it will be modified
        final String[] servicesIds = pRegisteredServices.keySet().toArray(
                new String[0]);

        // Unregister all exported services
        for (final String serviceId : servicesIds) {
            unregisterService(null, serviceId);
        }

        // Clear collections
        pExcludeFilters.clear();
        pIncludeFilters.clear();
        pIsolatesServices.clear();
        pRegisteredServices.clear();
        pWaitingRegistrations.clear();

        pLogger.logInfo(this, "invalidatePojo", "RemoteServiceAdapter Gone");
    }

    /**
     * Parses the given filters string and add found ones to the given set
     * 
     * @param aFilterStr
     *            A filters string
     * @param aFilters
     *            Set that will receive found filters
     */
    private void parseFilter(final String aFilterStr, final Set<String> aFilters) {

        if (aFilterStr == null || aFilterStr.isEmpty() || aFilters == null) {
            // Do nothing if not necessary...
            return;
        }

        // "Parse" the string
        final String[] filtersArray = aFilterStr.split(",");

        for (final String filter : filtersArray) {

            // Trim the string for filter efficiency
            final String trimmedFilter = filter.trim();
            if (!trimmedFilter.isEmpty()) {
                // Filter seems valid
                aFilters.add(trimmedFilter);
            }
        }
    }

    /**
     * Sets up the inclusion and exclusion filters
     */
    private void prepareFilters() {

        final String inclusionFilters = System
                .getProperty(IPlatformProperties.PROP_REMOTE_SERVICE_FILTERS_INCLUDE);
        if (inclusionFilters != null) {
            parseFilter(inclusionFilters, pIncludeFilters);
        }

        final String exclusionFilters = System
                .getProperty(IPlatformProperties.PROP_REMOTE_SERVICE_FILTERS_EXCLUDE);
        if (exclusionFilters != null) {
            parseFilter(exclusionFilters, pExcludeFilters);
        }
    }

    /**
     * Registers the service described in the given event
     * 
     * @param aRegistration
     *            A service registration event
     */
    private synchronized void registerService(
            final RemoteServiceRegistration aRegistration) {

        // Check if the service ID is already known
        if (pRegisteredServices.containsKey(aRegistration.getServiceId())) {
            pLogger.logInfo(this, "registerService",
                    "Already registered service=", aRegistration);
            return;
        }

        // Compute the interfaces to import
        final Set<String> javaInterfaces = new InterfacePrefixUtils()
                .extractInterfaces(aRegistration.getExportedInterfaces());
        for (final String exportedInterface : javaInterfaces) {
            // Test include / exclude filters
            if (!acceptInterface(exportedInterface)) {
                pLogger.logWarn(this, "registerService",
                        "Event filtered due to interface=", exportedInterface);
                return;
            }
        }

        // Store the remote service ID
        final String serviceId = aRegistration.getServiceId();
        if (pRegisteredServices.containsKey(serviceId)) {
            // Ignore already registered IDs
            pLogger.logWarn(this, "registerService",
                    "Already registered service ID=", serviceId);
            return;
        }

        // Get the proxy from the first handler that can manage this event
        Object serviceProxy = null;
        for (final IRemoteServiceClientHandler clientHandler : pClientHandlers) {
            try {
                // Create a proxy
                serviceProxy = clientHandler.getRemoteProxy(aRegistration);

            } catch (final ClassNotFoundException e) {
                // Try next handler
                pLogger.logWarn(this, "registerService",
                        "Error looking for proxyfied class:", e.getMessage());
                continue;
            }

            if (serviceProxy != null) {
                // An handler has been able to create the proxy, stop there
                break;
            }
        }

        if (serviceProxy == null) {
            // No proxy create, wait for another handler
            pWaitingRegistrations.add(aRegistration);

            pLogger.logSevere(this, "registerService",
                    "No proxy created for remote service=", serviceId);
            return;
        }

        // Get the publishing isolate ID
        final String publisherId = aRegistration.getHostIsolate();

        // Filter properties, if any
        final Dictionary<String, Object> filteredProperties = filterProperties(aRegistration
                .getServiceProperties());

        // Add the publishing isolate information
        filteredProperties.put(IRemoteServicesConstants.SERVICE_IMPORTED_FROM,
                publisherId);

        // Register the service
        final Set<String> interfacesNames = new HashSet<String>();
        for (final Class<?> clazz : serviceProxy.getClass().getInterfaces()) {
            interfacesNames.add(clazz.getName());
        }

        final ServiceRegistration<?> serviceReg = pBundleContext
                .registerService(interfacesNames.toArray(new String[0]),
                        serviceProxy, filteredProperties);

        // Store the registration information
        final ProxyServiceInfo serviceInfo = new ProxyServiceInfo(
                aRegistration, serviceReg, serviceProxy);
        pRegisteredServices.put(serviceId, serviceInfo);

        // Map the service with its host isolate
        Set<String> isolateServices = pIsolatesServices.get(publisherId);
        if (isolateServices == null) {
            // Prepare a new list
            isolateServices = new HashSet<String>();
            pIsolatesServices.put(publisherId, isolateServices);
        }

        // Add the service ID to the list
        isolateServices.add(serviceId);

        // Log the import
        pLogger.logDebug(this, "registerService", "Imported remote service=",
                serviceId);
    }

    /**
     * Requests the end points of the given isolate. If no isolate is given, the
     * request is sent to all known isolates.
     * 
     * @param aIsolateId
     *            An isolate ID, or null
     */
    private void requestEndpoints(final String aIsolateId) {

        final RemoteServiceEvent[] events;
        if (aIsolateId == null) {
            // Ask to all known isolates
            events = pBroadcaster.requestAllEndpoints();

        } else {
            // Ask to a specific isolate
            events = pBroadcaster.requestEndpoints(aIsolateId);
        }

        if (events == null) {
            // Nothing to do
            return;
        }

        for (final RemoteServiceEvent event : events) {
            // Handle all events
            handleRemoteEvent(event);
        }
    }

    /**
     * Unregisters the given service and destroys its proxy
     * 
     * @param aHostIsolate
     *            The service host isolate
     * @param aServiceId
     *            The removed service ID
     */
    private synchronized void unregisterService(final String aHostIsolate,
            final String aServiceId) {

        // Pop the service registration
        final ProxyServiceInfo serviceInfo = pRegisteredServices
                .remove(aServiceId);
        if (serviceInfo == null) {
            // Unknown service
            pLogger.logWarn(this, "unregisterService",
                    "No service informations for", aServiceId);
            return;
        }

        final ServiceRegistration<?> serviceReg = serviceInfo
                .getServiceRegistration();
        if (serviceReg != null) {
            // Unregister it
            serviceReg.unregister();
        }

        final Object proxy = serviceInfo.getProxy();
        for (final IRemoteServiceClientHandler handler : pClientHandlers) {
            try {
                handler.destroyProxy(proxy);

            } catch (final Throwable t) {
                // Ignore exceptions
                pLogger.logWarn(this, "unregisterService",
                        "Error destroying a remote service proxy:", t);
            }
        }

        // Remove from the isolate mapping
        final Set<String> isolateServices = pIsolatesServices.get(aHostIsolate);
        if (isolateServices != null) {
            synchronized (isolateServices) {
                isolateServices.remove(aServiceId);
            }
        }
    }

    /**
     * Updates the properties of an imported service
     * 
     * @param aRegistration
     *            A remote service registration bean
     */
    private synchronized void updateService(
            final RemoteServiceRegistration aRegistration) {

        final String serviceId = aRegistration.getServiceId();

        // Retrieve the service registration
        final ProxyServiceInfo serviceInfo = pRegisteredServices.get(serviceId);
        if (serviceInfo == null) {
            // Unknown service (do not log, as it may fill the logs)
            return;
        }

        final ServiceRegistration<?> serviceReg = serviceInfo
                .getServiceRegistration();
        if (serviceReg != null) {
            // Set up the new properties
            final Dictionary<String, Object> properties = filterProperties(aRegistration
                    .getServiceProperties());
            properties.put(IRemoteServicesConstants.SERVICE_IMPORTED_FROM,
                    aRegistration.getHostIsolate());

            // Update service properties
            serviceReg.setProperties(properties);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public synchronized void validatePojo() throws BundleException {

        // Prepare in/exclusion filters
        prepareFilters();

        // Register to bundle events
        pBundleContext.addBundleListener(this);

        // Request other isolates state with the RSB
        requestEndpoints(null);

        pLogger.logInfo(this, "validatePojo", "RemoteServiceAdapter Ready");
    }
}
