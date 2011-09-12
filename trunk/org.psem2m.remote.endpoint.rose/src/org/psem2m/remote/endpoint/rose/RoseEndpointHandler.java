/**
 * File:   RoseEndpointHandler.java
 * Author: Thomas Calmant
 * Date:   27 juil. 2011
 */
package org.psem2m.remote.endpoint.rose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
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
	Map<String, String> serviceProperties = Utilities
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

	// Find indicated factories
	ServiceReference endpointFactoriesRefs[] = findExportFactories(serviceProperties);
	if (endpointFactoriesRefs == null) {
	    System.out.println("No end point factory");
	    return null;
	}

	// Get a reference to the service
	Object serviceInstance = pBundleContext.getService(aServiceReference);
	if (serviceInstance == null) {
	    // TODO: log
	    System.out.println("No service instance available");
	    return null;
	}

	// Create end points
	List<EndpointDescription> endpointDescriptions = new ArrayList<EndpointDescription>();

	for (ServiceReference factoryRef : endpointFactoriesRefs) {

	    EndpointFactory factory = (EndpointFactory) pBundleContext
		    .getService(factoryRef);

	    if (factory != null) {
		try {
		    factory.createEndpoint(serviceInstance, serviceProperties);

		    // Store the end point description
		    String protocol = factory.getEndpointProtocol(endPointName);
		    String uri = factory.getEndpointBaseUri(endPointName);
		    int port = factory.getEndpointPort(endPointName);

		    // FIXME To it better
		    final String exportedConfig = factory.getConfigs()[0];

		    endpointDescriptions.add(new EndpointDescription(
			    aServiceReference, exportedConfig, endPointName,
			    protocol, uri, port));

		} finally {
		    // Release the service reference
		    pBundleContext.ungetService(factoryRef);
		}
	    }
	}

	return endpointDescriptions.toArray(new EndpointDescription[0]);
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
	Map<String, String> serviceProperties = Utilities
		.getServiceProperties(aServiceReference);

	// Find the end point name
	String endpointName = serviceProperties
		.get(EndpointFactory.PROP_ENDPOINT_NAME);

	// Abandon if non found
	if (endpointName == null || endpointName.isEmpty()) {
	    return false;
	}

	// Find indicated factories
	ServiceReference endpointFactoriesRefs[] = findExportFactories(serviceProperties);
	if (endpointFactoriesRefs == null) {
	    return false;
	}

	// Destroy end points
	for (ServiceReference factoryRef : endpointFactoriesRefs) {

	    EndpointFactory factory = (EndpointFactory) pBundleContext
		    .getService(factoryRef);

	    if (factory != null) {
		factory.destroyEndpoint(endpointName);
		pBundleContext.ungetService(factoryRef);
	    }
	}

	return true;
    }

    /**
     * Finds all factories references corresponding to the specified
     * service.exported.configs
     * 
     * @param aServiceProperties
     *            Service properties
     * @return Factories services references, null if none found
     */
    protected ServiceReference[] findExportFactories(
	    final Map<String, String> aServiceProperties) {

	// Get the export configurations
	Object exportConfigsRaw = aServiceProperties
		.get(RemoteConstants.SERVICE_EXPORTED_CONFIGS);
	String[] exportConfigsArray = null;

	if (exportConfigsRaw instanceof String[]) {
	    // Multiple configurations
	    exportConfigsArray = (String[]) exportConfigsRaw;

	} else if (exportConfigsRaw instanceof String) {
	    // Single configuration
	    exportConfigsArray = new String[] { (String) exportConfigsRaw };

	}

	// Prepare a filter, if needed
	String factoryFilter = null;

	if (exportConfigsArray != null && exportConfigsArray.length != 0) {

	    StringBuilder builder = new StringBuilder();
	    builder.append("(|");

	    for (String exportConfig : exportConfigsArray) {
		builder.append("(");
		builder.append(RemoteConstants.REMOTE_CONFIGS_SUPPORTED);
		builder.append("=");
		builder.append(exportConfig);
		builder.append(")");
	    }

	    builder.append(")");
	}

	// Get all factories found
	try {
	    ServiceReference[] references = pBundleContext
		    .getServiceReferences(EndpointFactory.class.getName(),
			    factoryFilter);

	    System.out.println("Found : " + Arrays.toString(references));

	    return references;

	} catch (InvalidSyntaxException e) {
	    e.printStackTrace();
	    // Act as nothing found on error
	    return null;
	}
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
