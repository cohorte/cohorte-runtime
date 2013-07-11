/**
 * File:   RemoteServiceImporter.java
 * Author: Thomas Calmant
 * Date:   26 juil. 2011
 */
package org.cohorte.remote.importer;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.PostRegistration;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.remote.IRemoteServiceBroadcaster;
import org.cohorte.remote.IRemoteServiceClientHandler;
import org.cohorte.remote.IRemoteServiceEventListener;
import org.cohorte.remote.IRemoteServicesConstants;
import org.cohorte.remote.InterfacePrefixUtils;
import org.cohorte.remote.beans.RemoteServiceEvent;
import org.cohorte.remote.beans.RemoteServiceRegistration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

/**
 * Core the Remote Service Importer (RSI). Responds to RSR events to create and
 * delete remote services proxies.
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-remote-importer-factory")
@Provides(specifications = IRemoteServiceEventListener.class)
@Instantiate(name = "cohorte-remote-importer")
public class RemoteServiceAdapter implements IRemoteServiceEventListener,
        BundleListener {

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
    private LogService pLogger;

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
            if (matchFilter(aInterfaceName, includeFilter)) {
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
            if (matchFilter(aInterfaceName, excludeFilter)) {
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

        pLogger.log(LogService.LOG_DEBUG,
                String.format("Services to update=%s", serviceIds));

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
            pLogger.log(LogService.LOG_DEBUG, String.format(
                    "Trying to import waiting=%s",
                    Arrays.toString(registration.getExportedInterfaces())));
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
            if (!property.startsWith("service.exported.")) {
                // Ignore export properties
                filteredProperties.put(property, entry.getValue());
            }

        }

        // Add "import" properties
        filteredProperties.put(IRemoteServicesConstants.SERVICE_IMPORTED,
                "true");

        final Object exportedProperty = aServiceProperties
                .get(Constants.SERVICE_EXPORTED_CONFIGS);
        if (exportedProperty != null) {
            filteredProperties.put(Constants.SERVICE_IMPORTED_CONFIGS,
                    exportedProperty);
        }

        return filteredProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.IRemoteServiceEventListener#handleIsolateLost(java
     * .lang.String)
     */
    @Override
    public synchronized void handleIsolateLost(final String aIsolateId) {

        final Set<String> services = pIsolatesServices.get(aIsolateId);
        if (services == null) {
            // Nothing to do
            pLogger.log(LogService.LOG_DEBUG, String.format(
                    "No services associated to lost isolate=%s", aIsolateId));
            return;
        }

        // Use an array, as the map will be modified by unregisterService
        final String[] servicesIds = services.toArray(new String[0]);

        // Unregister all corresponding services
        for (final String serviceId : servicesIds) {
            unregisterService(aIsolateId, serviceId);

            pLogger.log(LogService.LOG_DEBUG,
                    String.format("%s unregisters %s", aIsolateId, serviceId));
        }

        // Clear the map list (just to be sure)
        services.clear();

        pLogger.log(LogService.LOG_DEBUG, String.format("%s handled."));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.IRemoteServiceEventListener#handleIsolateReady(java
     * .lang.String)
     */
    @Override
    public void handleIsolateReady(final String aIsolateId) {

        // Ask for its end points
        requestEndpoints(aIsolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.IRemoteServiceEventListener#handleRemoteEvent(org.
     * cohorte.remote.beans.RemoteServiceEvent)
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

    /**
     * Component invalidated
     */
    @Invalidate
    public synchronized void invalidatePojo() {

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

        pLogger.log(LogService.LOG_INFO,
                "COHORTE Remote Service Importer Gone.");
    }

    /**
     * Tests if the given string matches the filter.
     * 
     * The filter is the regular filename filter and not a regular expression.
     * Allowed are *.* or ???.xml, etc.
     * 
     * Found at : <a href="http://blogs.igalia.com/eocanha/?p=67">blogs.igalia
     * .com/eocanha/?p=67</a>
     * 
     * @param aTested
     *            Tested string
     * @param aFilter
     *            Filename filter-like string
     * 
     * @return True if matches and false if either null or no match
     */
    private boolean matchFilter(final String aTested, final String aFilter) {

        if (aTested == null || aFilter == null) {
            return false;
        }

        final StringBuffer f = new StringBuffer();
        final StringTokenizer tokenizer = new StringTokenizer(aFilter, "?*",
                true);

        while (tokenizer.hasMoreTokens()) {

            final String token = tokenizer.nextToken();

            if (token.equals("?")) {
                f.append(".");

            } else if (token.equals("*")) {
                f.append(".*");

            } else {
                f.append(Pattern.quote(token));
            }
        }

        return aTested.matches(f.toString());
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
     * Called right after the registration of the provided service
     * 
     * @param aReference
     *            Reference to the provided service
     */
    @PostRegistration
    public synchronized void postListenerServiceRegistration(
            final ServiceReference<?> aReference) {

        /*
         * Request end points now that our component is validated and its
         * "listener" service can be used by the discovery layer.
         */
        requestEndpoints(null);
    }

    /**
     * Sets up the inclusion and exclusion filters
     */
    private void prepareFilters() {

        final String inclusionFilters = System
                .getProperty(IRemoteServicesConstants.FILTERS_INCLUDE);
        if (inclusionFilters != null) {
            parseFilter(inclusionFilters, pIncludeFilters);
        }

        final String exclusionFilters = System
                .getProperty(IRemoteServicesConstants.FILTERS_EXCLUDE);
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
            pLogger.log(LogService.LOG_INFO, String.format(
                    "Already registered service=%s", aRegistration));
            return;
        }

        // Compute the interfaces to import
        final Set<String> javaInterfaces = new InterfacePrefixUtils()
                .extractInterfaces(aRegistration.getExportedInterfaces());

        final Iterator<String> iterator = javaInterfaces.iterator();
        while (iterator.hasNext()) {
            // Get next interface
            final String exportedInterface = iterator.next();

            // Test include / exclude filters
            if (!acceptInterface(exportedInterface)) {
                // Remove it when checked
                pLogger.log(LogService.LOG_DEBUG, String.format(
                        "Filtered interface=%s", exportedInterface));

                iterator.remove();
            }
        }

        if (javaInterfaces.isEmpty()) {
            // All interfaces have been filtered: stop
            pLogger.log(LogService.LOG_WARNING, String.format(
                    "Import aborted: all interfaces have been filtered=%s",
                    Arrays.toString(aRegistration.getExportedInterfaces())));
            return;
        }

        // Store the remote service ID
        final String serviceId = aRegistration.getServiceId();
        if (pRegisteredServices.containsKey(serviceId)) {
            // Ignore already registered IDs
            pLogger.log(LogService.LOG_WARNING, String.format(
                    "Already registered service ID=%s", serviceId));
            return;
        }

        // Get the proxy from the first handler that can manage this event
        Object serviceProxy = null;
        for (final IRemoteServiceClientHandler clientHandler : pClientHandlers) {
            try {
                // Create a proxy
                serviceProxy = clientHandler.getRemoteProxy(aRegistration,
                        javaInterfaces);

            } catch (final ClassNotFoundException e) {
                // Try next handler
                pLogger.log(LogService.LOG_WARNING,
                        String.format("Error looking for proxyfied class: %s",
                                e.getMessage()));
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

            pLogger.log(LogService.LOG_ERROR, String.format(
                    "No proxy created for remote service=%s", serviceId));
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
        pLogger.log(LogService.LOG_DEBUG,
                String.format("Imported remote service=%s", serviceId));
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

        if (events != null) {
            for (final RemoteServiceEvent event : events) {
                // Handle each event
                handleRemoteEvent(event);
            }
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
            pLogger.log(LogService.LOG_WARNING,
                    String.format("No service informations for %s", aServiceId));
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

            } catch (final Exception ex) {
                // Ignore exceptions
                pLogger.log(LogService.LOG_WARNING, String.format(
                        "Error destroying a remote service proxy:", ex), ex);
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

    /**
     * Component validated
     */
    @Validate
    public synchronized void validatePojo() {

        // Prepare in/exclusion filters
        prepareFilters();

        // Register to bundle events
        pBundleContext.addBundleListener(this);

        pLogger.log(LogService.LOG_INFO,
                "COHORTE Remote Service Importer Ready.");
    }
}
