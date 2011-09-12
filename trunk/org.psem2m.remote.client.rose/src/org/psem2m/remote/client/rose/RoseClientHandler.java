/**
 * File:   RoseClientHandler.java
 * Author: Thomas Calmant
 * Date:   27 juil. 2011
 */
package org.psem2m.remote.client.rose;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.ow2.chameleon.rose.client.RemoteClientFactory;
import org.ow2.chameleon.rose.client.RemoteProxyFactory;
import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.services.remote.IRemoteServiceClientHandler;
import org.psem2m.isolates.services.remote.beans.EndpointDescription;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent;

/**
 * Implementation of a remote service client handler, using Rose ClientFactory
 * services
 * 
 * @author Thomas Calmant
 */
public class RoseClientHandler extends CPojoBase implements
	IRemoteServiceClientHandler {

    /** The component bundle context */
    private BundleContext pBundleContext;

    /** Rose remote proxy factories */
    private RemoteProxyFactory[] pProxyFactories;

    /**
     * Prepares the component
     */
    public RoseClientHandler(final BundleContext aBundleContext) {

	super();
	pBundleContext = aBundleContext;
    }

    /**
     * Tests if the array contains the given string, returns false if one is
     * null
     * 
     * @param aArray
     *            Array to look into
     * @param aString
     *            String to look for
     * @return True if the string is in the array
     */
    protected boolean arrayContains(final String[] aArray, final String aString) {

	if (aArray == null) {
	    return false;
	}

	for (String value : aArray) {
	    if (value != null && value.equals(aString)) {
		return true;
	    }
	}

	return false;
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
     * @see
     * org.psem2m.remote.client.rose.IRemoteServiceClientHandler#destroyProxy
     * (java.lang.Object)
     */
    @Override
    public void destroyProxy(final Object aProxy) {

	for (RemoteProxyFactory factory : pProxyFactories) {
	    try {
		factory.destroyRemoteProxy(aProxy);
	    } catch (IllegalArgumentException ex) {
		// Ignore errors : the proxy hasn't been created with this
		// factory
	    }
	}
    }

    /**
     * Finds the first factory matching with the export configuration
     * 
     * @param aExportConfig
     *            A service.exported.configs value
     * @return The first matching factory, null if not found
     */
    protected RemoteProxyFactory findAssociatedFactory(
	    final String aExportConfig) {

	for (RemoteProxyFactory factory : pProxyFactories) {

	    if (factory != null
		    && arrayContains(factory.getConfigs(), aExportConfig)) {
		return factory;
	    }
	}

	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.remote.client.rose.IRemoteServiceClientHandler#getRemoteProxy
     * (org.psem2m.isolates.commons.remote.RemoteServiceEvent)
     */
    @Override
    public Object getRemoteProxy(final RemoteServiceEvent aServiceEvent)
	    throws ClassNotFoundException {

	// No factory, no proxy
	if (aServiceEvent == null || pProxyFactories.length == 0) {
	    System.out.println("No proxies");
	    return null;
	}

	// Compute the interface name
	String[] interfaceNames = aServiceEvent.getInterfacesNames();
	if (interfaceNames == null || interfaceNames.length == 0) {
	    System.out.println("No interface array");
	    return null;
	}

	final String interfaceName = interfaceNames[interfaceNames.length - 1];
	if (interfaceName == null) {
	    System.out.println("No interface");
	    return null;
	}

	// Find the class
	// Class.forName(interfaceName) won't work...
	final Class<?> interfaceClass = Utilities.findClassInBundles(
		pBundleContext.getBundles(), interfaceName);

	if (interfaceClass == null) {
	    throw new ClassNotFoundException(interfaceName);
	}

	// Get available end points
	EndpointDescription[] endpoints = aServiceEvent.getEndpoints();

	// Find a factory
	RemoteProxyFactory preferredFactory = null;
	EndpointDescription selectedEndpoint = null;

	for (EndpointDescription endpoint : endpoints) {
	    preferredFactory = findAssociatedFactory(endpoint
		    .getExportedConfig());

	    if (preferredFactory != null) {
		selectedEndpoint = endpoint;
		break;
	    }
	}

	// Nothing found : abandon.
	if (preferredFactory == null || selectedEndpoint == null) {
	    System.out.println("No factory/endpoint couple found");
	    return null;
	}

	final String endpointUri = selectedEndpoint.computeURI();
	if (endpointUri == null) {
	    System.out.println("Invalid URI");
	    return null;
	}

	// Convert end point properties to a map
	Map<String, String> endpointProperties = new HashMap<String, String>();
	endpointProperties.put(RemoteClientFactory.PROP_ENDPOINT_NAME,
		selectedEndpoint.getEndpointName());

	endpointProperties.put(RemoteClientFactory.PROP_ENDPOINT_URI,
		endpointUri);

	// Prepare a framework wide class loader
	final BundlesClassLoader classLoader = new BundlesClassLoader(
		pBundleContext);

	Object serviceProxy = preferredFactory.getRemoteProxy(
		endpointProperties, classLoader, interfaceClass);
	return serviceProxy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() throws BundleException {
	// ...
	System.out.println("RoserHandlerClient Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() throws BundleException {
	// ...
	System.out.println("RoserHandlerClient alive");
    }
}
