/**
 * File:   RoseEndpointHandler.java
 * Author: Thomas Calmant
 * Date:   27 juil. 2011
 */
package org.psem2m.remote.endpoint.rose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
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

    /** Service -&gt; End points description mapping */
    private final Map<ServiceReference, List<EndpointDescription>> pEndpointDescriptionsMapping = new HashMap<ServiceReference, List<EndpointDescription>>();

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
    public synchronized EndpointDescription[] createEndpoint(
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

	// Set the exported interface name (first one found)
	final String[] interfaceName = (String[]) aServiceReference
		.getProperty(Constants.OBJECTCLASS);

	if (interfaceName == null || interfaceName.length == 0) {
	    pLogger.log(LogService.LOG_WARNING, "No " + Constants.OBJECTCLASS
		    + " service property. Endpoint creation aborted.");
	    return null;
	}

	// Set the service interface name (for Rose)
	serviceProperties.put(EndpointFactory.PROP_INTERFACE_NAME,
		interfaceName[0]);

	// Create end points
	pEndpointFactory.createEndpoint(serviceInstance, serviceProperties);

	// Store the end point description
	final String protocol = pEndpointFactory
		.getEndpointProtocol(endPointName);
	final String uri = pEndpointFactory.getEndpointBaseUri(endPointName);
	final int port = pEndpointFactory.getEndpointPort(endPointName);

	// FIXME Do it in a fancier way
	final String exportedConfig = pEndpointFactory.getConfigs()[0];

	// Prepare the list of results
	final List<EndpointDescription> newEndpoints = new ArrayList<EndpointDescription>();

	newEndpoints.add(new EndpointDescription(aServiceReference,
		exportedConfig, endPointName, protocol, uri, port));

	// Add it to the service reference existing entries
	final List<EndpointDescription> serviceEndpoints = pEndpointDescriptionsMapping
		.get(aServiceReference);
	if (serviceEndpoints != null) {
	    // Append new elements to the list
	    serviceEndpoints.addAll(newEndpoints);

	} else {
	    // Store the new entry
	    pEndpointDescriptionsMapping.put(aServiceReference, newEndpoints);
	}

	// Return an array of new elements
	return newEndpoints.toArray(new EndpointDescription[0]);
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
    public synchronized boolean destroyEndpoint(
	    final ServiceReference aServiceReference) {

	if (aServiceReference == null) {
	    return false;
	}

	// Remove the entry from the map
	final List<EndpointDescription> associatedDescriptions = pEndpointDescriptionsMapping
		.get(aServiceReference);
	if (associatedDescriptions != null) {

	    // Loop on end points descriptions
	    for (EndpointDescription description : associatedDescriptions) {

		try {
		    pEndpointFactory.destroyEndpoint(description
			    .getEndpointName());

		} catch (Exception ex) {
		    pLogger.log(
			    LogService.LOG_WARNING,
			    "Can't destroy end point '"
				    + description.getEndpointName() + "'", ex);
		}

	    }

	    // Clear the associated list content
	    associatedDescriptions.clear();
	}

	pEndpointDescriptionsMapping.remove(aServiceReference);
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.IEndpointHandler#getEndpoints(org
     * .osgi.framework.ServiceReference)
     */
    @Override
    public synchronized EndpointDescription[] getEndpoints(
	    final ServiceReference aServiceReference) {

	// Empty result array to avoid returning null
	final EndpointDescription[] emptyArray = new EndpointDescription[0];

	// Get all known end points
	final List<EndpointDescription> knownEndpoints = pEndpointDescriptionsMapping
		.get(aServiceReference);
	if (knownEndpoints == null) {
	    // Never return null
	    return emptyArray;
	}

	return knownEndpoints.toArray(emptyArray);
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
