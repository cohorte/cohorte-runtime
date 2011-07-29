/**
 * File:   RemoteServiceAdapter.java
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
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.rose.RemoteConstants;
import org.ow2.chameleon.rose.client.RemoteProxyFactory;
import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.commons.remote.IRemoteServiceClientHandler;
import org.psem2m.isolates.commons.remote.IRemoteServiceEventListener;
import org.psem2m.isolates.commons.remote.RemoteServiceEvent;

/**
 * @author Thomas Calmant
 */
public class RemoteServiceAdapter extends CPojoBase implements
	IRemoteServiceEventListener {

    /** Service export properties prefix */
    public static final String SERVICE_EXPORTED_PREFIX = "service.exported.";

    /** The component bundle context */
    private BundleContext pBundleContext;

    /** Remote service proxy handlers */
    private IRemoteServiceClientHandler[] pClientHandlers;

    /** Local isolate ID */
    private final String pIsolateId;

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

	// TODO find it with configuration
	pIsolateId = System.getProperty("isolate.id");
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

    protected RemoteProxyFactory findAssociatedFactory(
	    final String aExportedConfig) {

	StringBuilder filter = new StringBuilder();
	filter.append("(");
	filter.append(RemoteConstants.REMOTE_CONFIGS_SUPPORTED);
	filter.append("=");
	filter.append(aExportedConfig);
	filter.append(")");

	ServiceReference[] serviceRef;
	try {
	    serviceRef = pBundleContext.getServiceReferences(
		    RemoteProxyFactory.class.getName(), filter.toString());

	} catch (InvalidSyntaxException e) {
	    // TODO Use a logger
	    e.printStackTrace();
	    return null;
	}

	if (serviceRef == null || serviceRef.length == 0) {
	    return null;
	}

	return null;
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

	if (aServiceEvent == null) {
	    return;
	}

	System.out.println("Handling event : " + aServiceEvent);

	// Ignore "local" calls (same isolate.id)
	if (pIsolateId.equals(aServiceEvent.getSourceIsolateId())) {
	    System.out.println("Ignoring service due to source=dest : "
		    + aServiceEvent);
	    return;
	}

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
    }
}
