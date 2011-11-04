/**
 * File:   RemoteServiceImporter.java
 * Author: Thomas Calmant
 * Date:   26 juil. 2011
 */
package org.psem2m.isolates.remote.importer;

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
import org.ow2.chameleon.rose.RemoteConstants;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.remote.IRemoteServiceBroadcaster;
import org.psem2m.isolates.services.remote.IRemoteServiceClientHandler;
import org.psem2m.isolates.services.remote.IRemoteServiceEventListener;
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

    /** Service export properties prefix */
    public static final String SERVICE_EXPORTED_PREFIX = "service.exported.";

    /** Remote service broadcaster (RSB) */
    @Requires
    private IRemoteServiceBroadcaster pBroadcaster;

    /** The component bundle context */
    private BundleContext pBundleContext;

    /** Remote service proxy handlers */
    @Requires
    private IRemoteServiceClientHandler[] pClientHandlers;

    /** The service interfaces exclude filters */
    private final Set<String> pExcludeFilters = new HashSet<String>();

    /** The service interfaces include filters */
    private final Set<String> pIncludeFilters = new HashSet<String>();

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

            if (pIncludeFilters.contains(aInterfaceName)) {
                // The interface full name is included
                return true;

            } else if (pExcludeFilters.contains(aInterfaceName)) {
                // The interface full name is excluded
                return false;

            } else {
                // The interface matches both filters : refuse it
                return false;
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
            if (!property.startsWith(SERVICE_EXPORTED_PREFIX)) {
                // Ignore export properties
                filteredProperties.put(property, entry.getValue());
            }

        }

        // Add "import" properties
        filteredProperties.put(RemoteConstants.SERVICE_IMPORTED, "true");

        final Object exportedProperty = aServiceProperties
                .get(RemoteConstants.SERVICE_EXPORTED_CONFIGS);
        if (exportedProperty != null) {
            filteredProperties.put(RemoteConstants.SERVICE_IMPORTED_CONFIGS,
                    exportedProperty);
        }

        return filteredProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.remote.IRemoteServiceEventListener#
     * handleRemoteEvent(org.psem2m.isolates.commons.remote.RemoteServiceEvent)
     */
    @Override
    public void handleRemoteEvent(final RemoteServiceEvent aServiceEvent) {

        pLogger.logInfo(this, "handleRemoteEvent",
                "Handling remote service event :", aServiceEvent);

        switch (aServiceEvent.getEventType()) {

        case REGISTERED: {
            registerService(aServiceEvent);
            break;
        }

        case UNREGISTERED: {
            unregisterService(aServiceEvent.getServiceRegistration()
                    .getServiceId());
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
            unregisterService(serviceId);
        }

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

            // Ignore already registered ids
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
                System.err.println("Class not found - " + e);
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

        // Filter properties, if any
        final Dictionary<String, Object> filteredProperties = filterProperties(registration
                .getServiceProperties());

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
        final ServiceRegistration serviceReg = pBundleContext.registerService(
                registration.getExportedInterfaces(), finalServiceProxy,
                filteredProperties);

        // Store the registration information
        if (serviceReg != null) {
            final ProxyServiceInfo serviceInfo = new ProxyServiceInfo(
                    serviceReg, finalServiceProxy);
            pRegisteredServices.put(serviceId, serviceInfo);
        }
        // }
        // }).start();
    }

    /**
     * Unregisters the given service and destroys its proxy
     * 
     * @param aServiceId
     *            The removed service ID
     */
    protected synchronized void unregisterService(final String aServiceId) {

        // Retrieve the service registration
        final ProxyServiceInfo serviceInfo = pRegisteredServices
                .get(aServiceId);
        if (serviceInfo == null) {
            // Unknown service
            return;
        }

        final ServiceRegistration serviceReg = serviceInfo
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
            }
        }

        pRegisteredServices.remove(aServiceId);
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
