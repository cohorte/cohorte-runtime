/**
 * File:   RoseEndpointHandler.java
 * Author: Thomas Calmant
 * Date:   27 juil. 2011
 */
package org.psem2m.remote.endpoint.rose;

import java.util.Arrays;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.ow2.chameleon.rose.RemoteConstants;
import org.ow2.chameleon.rose.server.EndpointFactory;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.remote.IEndpointHandler;
import org.psem2m.isolates.services.remote.beans.EndpointDescription;

/**
 * Implementation of end point handler, using Rose EndpointFactory services
 * 
 * @author Thomas Calmant
 */
public class RoseEndpointHandler extends CPojoBase implements IEndpointHandler {

    /** Default end point port */
    public static int DEFAULT_ENDPOINT_PORT = 8080;

    /** The bundle context */
    private final BundleContext pBundleContext;

    /** Rose end point factory, injected by iPOJO */
    private EndpointFactory pEndpointFactory;

    /** Log service */
    private LogService pLogger;

    /**
     * Prepares the component
     * 
     * @param aBundleContext
     *            The bundle context
     */
    public RoseEndpointHandler(final BundleContext aBundleContext) {

	super();
	pBundleContext = aBundleContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.remote.endpoint.rose.IEndpointHandler#createEndpoint(org.osgi
     * .framework.ServiceReference)
     */
    @Override
    public EndpointDescription[] createEndpoint(
	    final ServiceReference aServiceReference) {

	if (aServiceReference == null) {
	    return null;
	}

	// Copy service properties in a map
	final Map<String, String> serviceProperties = Utilities
		.getServiceProperties(aServiceReference);

	// Compute a end point name
	String endPointName = serviceProperties
		.get(EndpointFactory.PROP_ENDPOINT_NAME);
	if (endPointName == null) {
	    endPointName = "service"
		    + serviceProperties.get(Constants.SERVICE_ID);
	    serviceProperties.put(EndpointFactory.PROP_ENDPOINT_NAME,
		    endPointName);
	}

	// Get a reference to the service
	Object serviceInstance = pBundleContext.getService(aServiceReference);
	if (serviceInstance == null) {
	    pLogger.log(LogService.LOG_ERROR,
		    "The service reference to export as no associated instance.");
	    return null;
	}

	// Create end points
	pEndpointFactory.createEndpoint(serviceInstance, serviceProperties);

	// Store the end point description
	final String protocol = pEndpointFactory
		.getEndpointProtocol(endPointName);
	final String uri = pEndpointFactory.getEndpointBaseUri(endPointName);
	final int port = pEndpointFactory.getEndpointPort(endPointName);

	// FIXME Do it in a fancier way
	final String exportedConfig = pEndpointFactory.getConfigs()[0];

	return new EndpointDescription[] { new EndpointDescription(
		aServiceReference, exportedConfig, endPointName, protocol, uri,
		port) };
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
     * org.psem2m.remote.endpoint.rose.IEndpointHandler#destroyEndpoint(org.
     * osgi.framework.ServiceReference)
     */
    @Override
    public boolean destroyEndpoint(final ServiceReference aServiceReference) {

	if (aServiceReference == null) {
	    return false;
	}

	// Copy service properties in a map
	final Map<String, String> serviceProperties = Utilities
		.getServiceProperties(aServiceReference);

	// Find the end point name
	final String endpointName = serviceProperties
		.get(EndpointFactory.PROP_ENDPOINT_NAME);

	// Abandon if non found
	if (endpointName == null || endpointName.isEmpty()) {
	    return false;
	}

	// Destroy end point
	pEndpointFactory.destroyEndpoint(endpointName);

	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() throws BundleException {
	// ...
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() throws BundleException {
	// ...
    }
}
