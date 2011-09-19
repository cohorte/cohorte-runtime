/**
 * File:   RemoteServiceImporter.java
 * Author: Thomas Calmant
 * Date:   26 juil. 2011
 */
package org.psem2m.isolates.remote.importer;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.rose.RemoteConstants;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.remote.IRemoteServiceClientHandler;
import org.psem2m.isolates.services.remote.IRemoteServiceEventListener;
import org.psem2m.isolates.services.remote.IRemoteServiceRepository;
import org.psem2m.isolates.services.remote.IRemoveServiceBroadcaster;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent;

/**
 * Core the Remote Service Importer (RSI). Responds to RSR events to create and
 * delete remote services proxies.
 * 
 * @author Thomas Calmant
 */
public class RemoteServiceAdapter extends CPojoBase implements
        IRemoteServiceEventListener {

    /** Service export properties prefix */
    public static final String SERVICE_EXPORTED_PREFIX = "service.exported.";

    /** Remote service broadcaster (RSB) */
    private IRemoveServiceBroadcaster pBroadcaster;

    /** The component bundle context */
    private BundleContext pBundleContext;

    /** Remote service proxy handlers */
    private IRemoteServiceClientHandler[] pClientHandlers;

    /** Registered services */
    private final Map<String, ProxyServiceInfo> pRegisteredServices = new HashMap<String, ProxyServiceInfo>();

    /** Remote service repository (RSR) */
    private IRemoteServiceRepository pRepository;

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
    protected Dictionary<String, ?> filterProperties(
            final Dictionary<String, ?> aServiceProperties) {

        if (aServiceProperties == null) {
            return null;
        }

        Dictionary<String, Object> filteredProperties = new Hashtable<String, Object>();

        // Add "import" properties
        filteredProperties.put(RemoteConstants.SERVICE_IMPORTED, "true");

        Object exportedProperty = aServiceProperties
                .get(RemoteConstants.SERVICE_EXPORTED_CONFIGS);
        if (exportedProperty != null) {
            filteredProperties.put(RemoteConstants.SERVICE_IMPORTED_CONFIGS,
                    exportedProperty);
        }

        // Remove "export" properties
        Enumeration<String> propertiesKeys = aServiceProperties.keys();
        while (propertiesKeys.hasMoreElements()) {
            // Test all keys
            String nextElement = propertiesKeys.nextElement();
            if (nextElement.startsWith(SERVICE_EXPORTED_PREFIX)) {
                aServiceProperties.remove(nextElement);
            }
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

        System.out.println("Handling event : " + aServiceEvent);

        // Store the remote service ID
        final String serviceId = aServiceEvent.getServiceId();

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
    public void invalidatePojo() throws BundleException {

        // Stop listening to the RSR
        pRepository.removeListener(this);

        // Unregister all exported services
        for (String serviceId : pRegisteredServices.keySet()) {
            unregisterService(serviceId);
        }
    }

    /**
     * Registers the service described in the given event
     * 
     * @param aServiceEvent
     *            A service registration event
     */
    protected void registerService(final RemoteServiceEvent aServiceEvent) {

        // Store the remote service ID
        final String serviceId = aServiceEvent.getServiceId();

        if (pRegisteredServices.containsKey(serviceId)) {
            // Ignore already registered ids
            System.out.println("Already registered service : " + serviceId);
            return;
        }

        // Create a proxy
        Object serviceProxy = null;
        for (IRemoteServiceClientHandler clientHandler : pClientHandlers) {
            try {
                serviceProxy = clientHandler.getRemoteProxy(aServiceEvent);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            }

            if (serviceProxy != null) {
                break;
            }
        }

        if (serviceProxy == null) {
            System.out.println("No proxy created");
            return;
        }

        // Filter properties, if any
        Dictionary<String, ?> filteredProperties = filterProperties(aServiceEvent
                .getServiceProperties());

        // Register the service
        ServiceRegistration serviceReg = pBundleContext.registerService(
                aServiceEvent.getInterfacesNames(), serviceProxy,
                filteredProperties);

        // Store the registration information
        if (serviceReg != null) {
            ProxyServiceInfo serviceInfo = new ProxyServiceInfo(serviceReg,
                    serviceProxy);
            pRegisteredServices.put(aServiceEvent.getServiceId(), serviceInfo);
        }
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
    public void validatePojo() throws BundleException {

        // Register to RSR events
        pRepository.addListener(this);

        // Request other isolates state with the RSB
        pBroadcaster.requestAllEndpoints();
    }
}
