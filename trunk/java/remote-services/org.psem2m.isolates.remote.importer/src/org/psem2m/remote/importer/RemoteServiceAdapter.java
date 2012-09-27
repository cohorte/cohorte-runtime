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
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.remote.IRemoteServiceBroadcaster;
import org.psem2m.isolates.services.remote.IRemoteServiceClientHandler;
import org.psem2m.isolates.services.remote.IRemoteServiceEventListener;
import org.psem2m.isolates.services.remote.IRemoteServicesConstants;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent;
import org.psem2m.isolates.services.remote.beans.RemoteServiceRegistration;

/**
 * Core the Remote Service Importer (RSI). Responds to RSR events to create and
 * delete remote services proxies.
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-remote-service-importer-factory", publicFactory = false)
@Provides(specifications = IRemoteServiceEventListener.class)
@Instantiate(name = "psem2m-remote-service-importer")
public class RemoteServiceAdapter extends CPojoBase implements
        IRemoteServiceEventListener {

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

    /** Isolate ID -&gt; Services IDs mapping */
    private final Map<String, Set<String>> pIsolatesServices = new HashMap<String, Set<String>>();

    /** Log service */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Registered services */
    private final Map<String, ProxyServiceInfo> pRegisteredServices = new HashMap<String, ProxyServiceInfo>();

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
    protected boolean acceptInterface(final String aInterfaceName) {

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

    /**
     * Filters the given properties to remove service export ones
     * 
     * @param aServiceProperties
     *            Imported service properties
     * @return The service properties, without the remote service ones
     */
    protected Dictionary<String, Object> filterProperties(
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
    public void handleIsolateLost(final String aIsolateId) {

        final Set<String> services = pIsolatesServices.get(aIsolateId);
        if (services == null) {
            // Nothing to do
            pLogger.logDebug(this, "handleIsolateLost", "No services for",
                    aIsolateId);
            return;
        }

        synchronized (services) {

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
        }

        pLogger.logDebug(this, "handleIsolateLost", aIsolateId, "handled.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.remote.IRemoteServiceEventListener#
     * handleRemoteEvent(org.psem2m.isolates.commons.remote.RemoteServiceEvent)
     */
    @Override
    public void handleRemoteEvent(final RemoteServiceEvent aServiceEvent) {

        pLogger.logDebug(this, "handleRemoteEvent",
                "Handling remote service event :", aServiceEvent);

        final RemoteServiceRegistration registration = aServiceEvent
                .getServiceRegistration();

        switch (aServiceEvent.getEventType()) {
        case REGISTERED: {
            registerService(aServiceEvent);
            break;
        }

        case MODIFIED: {
            updateService(registration);
            break;
        }

        case UNREGISTERED: {
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
    public void invalidatePojo() throws BundleException {

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
        pRegisteredServices.clear();
        pIsolatesServices.clear();

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
    protected void parseFilter(final String aFilterStr,
            final Set<String> aFilters) {

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
    protected void prepareFilters() {

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
     * @param aServiceEvent
     *            A service registration event
     */
    protected synchronized void registerService(
            final RemoteServiceEvent aServiceEvent) {

        // Get the service registration object
        final RemoteServiceRegistration registration = aServiceEvent
                .getServiceRegistration();

        for (final String exportedInterface : registration
                .getExportedInterfaces()) {
            // Test include / exclude filters
            if (!acceptInterface(exportedInterface)) {
                return;
            }
        }

        // Store the remote service ID
        final String serviceId = registration.getServiceId();

        if (pRegisteredServices.containsKey(serviceId)) {

            // Ignore already registered IDs
            pLogger.logWarn(this, "registerService",
                    "Already registered service : " + serviceId);
            return;
        }

        // Create a proxy
        Object serviceProxy = null;
        for (final IRemoteServiceClientHandler clientHandler : pClientHandlers) {
            try {
                serviceProxy = clientHandler.getRemoteProxy(aServiceEvent);

            } catch (final ClassNotFoundException e) {
                pLogger.logSevere(this, "registerService",
                        "Error looking for proxyfied class", e);
                return;
            }

            if (serviceProxy != null) {
                break;
            }
        }

        if (serviceProxy == null) {
            pLogger.logSevere(this, "registerService",
                    "No proxy created for service : " + serviceId);
            return;
        }

        // Get the publishing isolate ID
        final String publisherId = registration.getHostIsolate();

        // Filter properties, if any
        final Dictionary<String, Object> filteredProperties = filterProperties(registration
                .getServiceProperties());

        // Add the publishing isolate information
        filteredProperties.put(IRemoteServicesConstants.SERVICE_IMPORTED_FROM,
                publisherId);

        // Used in the thread
        final Object finalServiceProxy = serviceProxy;

        // // Register the service
        // new Thread(new Runnable() {
        //
        // @Override
        // public void run() {
        //
        // This call is synchronous and may take a while
        // -> use a thread
        final ServiceRegistration<?> serviceReg = pBundleContext
                .registerService(registration.getExportedInterfaces(),
                        finalServiceProxy, filteredProperties);

        // Store the registration information
        if (serviceReg != null) {
            final ProxyServiceInfo serviceInfo = new ProxyServiceInfo(
                    serviceReg, finalServiceProxy);

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

        }
        // }
        // }).start();
    }

    /**
     * Unregisters the given service and destroys its proxy
     * 
     * @param aHostIsolate
     *            The service host isolate
     * @param aServiceId
     *            The removed service ID
     */
    protected synchronized void unregisterService(final String aHostIsolate,
            final String aServiceId) {

        // Retrieve the service registration
        final ProxyServiceInfo serviceInfo = pRegisteredServices
                .get(aServiceId);
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

        // Remove the informations
        pRegisteredServices.remove(aServiceId);

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
    protected synchronized void updateService(
            final RemoteServiceRegistration aRegistration) {

        final String serviceId = aRegistration.getServiceId();

        // Retrieve the service registration
        final ProxyServiceInfo serviceInfo = pRegisteredServices.get(serviceId);
        if (serviceInfo == null) {
            // Unknown service
            pLogger.logWarn(this, "updateService",
                    "No service informations for ID=", serviceId);
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
    public void validatePojo() throws BundleException {

        // Prepare in/exclusion filters
        prepareFilters();

        // Request other isolates state with the RSB
        pBroadcaster.requestAllEndpoints();

        pLogger.logInfo(this, "validatePojo", "RemoteServiceAdapter Ready");
    }
}
