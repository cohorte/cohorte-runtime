/**
 * File:   RemoteServiceImporter.java
 * Author: Thomas Calmant
 * Date:   26 juil. 2011
 */
package org.psem2m.isolates.remote.importer;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.ow2.chameleon.rose.RemoteConstants;
import org.psem2m.isolates.base.activators.CPojoBase;
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
@Component(name = "remote-service-importer-factory", publicFactory = false)
@Provides(specifications = IRemoteServiceEventListener.class)
@Instantiate(name = "remote-service-importer")
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

    /** Log service */
    @Requires
    private LogService pLogger;

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
        for (Entry<String, Object> entry : aServiceProperties.entrySet()) {

            final String property = entry.getKey();
            if (!property.startsWith(SERVICE_EXPORTED_PREFIX)) {
                // Ignore export properties
                filteredProperties.put(property, entry.getValue());
            }

        }

        // Add "import" properties
        filteredProperties.put(RemoteConstants.SERVICE_IMPORTED, "true");

        Object exportedProperty = aServiceProperties
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
    public synchronized void handleRemoteEvent(
            final RemoteServiceEvent aServiceEvent) {

        pLogger.log(LogService.LOG_INFO,
                "[RemoteServiceAdapter] Handling event : " + aServiceEvent);

        // Store the remote service ID
        final String serviceId = aServiceEvent.getServiceRegistration()
                .getServiceId();

        switch (aServiceEvent.getEventType()) {

        case REGISTERED: {
            registerService(aServiceEvent);
            break;
        }

        case UNREGISTERED: {
            unregisterService(serviceId);
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

        // Unregister all exported services
        for (String serviceId : pRegisteredServices.keySet()) {
            unregisterService(serviceId);
        }

        pLogger.log(LogService.LOG_INFO, "RemoteServiceAdapter Gone");
    }

    /**
     * Registers the service described in the given event
     * 
     * @param aServiceEvent
     *            A service registration event
     */
    protected void registerService(final RemoteServiceEvent aServiceEvent) {

        // Get the service registration object
        final RemoteServiceRegistration registration = aServiceEvent
                .getServiceRegistration();

        // Store the remote service ID
        final String serviceId = registration.getServiceId();

        if (pRegisteredServices.containsKey(serviceId)) {
            // Ignore already registered ids
            pLogger.log(LogService.LOG_WARNING,
                    "[RemoteServiceAdapter] Already registered service : "
                            + serviceId);
            return;
        }

        // Create a proxy
        Object serviceProxy = null;
        for (IRemoteServiceClientHandler clientHandler : pClientHandlers) {
            try {
                serviceProxy = clientHandler.getRemoteProxy(aServiceEvent);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                pLogger.log(LogService.LOG_ERROR,
                        "Error looking for proxyfied class", e);
                return;
            }

            if (serviceProxy != null) {
                break;
            }
        }

        if (serviceProxy == null) {
            pLogger.log(LogService.LOG_ERROR,
                    "[RemoteServiceAdapter] No proxy created for service : "
                            + serviceId);
            return;
        }

        // Filter properties, if any
        final Dictionary<String, Object> filteredProperties = filterProperties(registration
                .getServiceProperties());

        // Used in the thread
        final Object finalServiceProxy = serviceProxy;

        // Register the service
        new Thread(new Runnable() {

            @Override
            public void run() {

                // This call is synchronous and may take a while
                // -> use a thread
                ServiceRegistration serviceReg = pBundleContext
                        .registerService(registration.getExportedInterfaces(),
                                finalServiceProxy, filteredProperties);

                // Store the registration information
                if (serviceReg != null) {
                    ProxyServiceInfo serviceInfo = new ProxyServiceInfo(
                            serviceReg, finalServiceProxy);
                    pRegisteredServices.put(serviceId, serviceInfo);
                }
            }
        }).start();
    }

    /**
     * Unregisters the given service and destroys its proxy
     * 
     * @param aServiceId
     *            The removed service ID
     */
    protected void unregisterService(final String aServiceId) {

        // Retrieve the service registration
        ProxyServiceInfo serviceInfo = pRegisteredServices.get(aServiceId);

        ServiceRegistration serviceReg = serviceInfo.getServiceRegistration();
        if (serviceReg != null) {
            // Unregister it
            serviceReg.unregister();
        }

        Object proxy = serviceInfo.getProxy();
        for (IRemoteServiceClientHandler handler : pClientHandlers) {
            try {
                handler.destroyProxy(proxy);
            } catch (Throwable t) {
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

        // Request other isolates state with the RSB
        pBroadcaster.requestAllEndpoints();

        pLogger.log(LogService.LOG_INFO, "RemoteServiceAdapter Ready");
    }
}
