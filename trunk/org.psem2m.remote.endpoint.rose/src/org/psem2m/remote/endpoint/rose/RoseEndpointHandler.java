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
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.remote.IEndpointHandler;
import org.psem2m.isolates.services.remote.beans.EndpointDescription;

/**
 * Implementation of end point handler, using Rose EndpointFactory services
 * 
 * @author Thomas Calmant
 */
public class RoseEndpointHandler extends CPojoBase implements IEndpointHandler {

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
     * org.psem2m.isolates.services.remote.IEndpointHandler#createEndpoint(java
     * .lang.String, org.osgi.framework.ServiceReference)
     */
    @Override
    public synchronized EndpointDescription[] createEndpoint(
            final String aExportedInterface,
            final ServiceReference aServiceReference) {

        if (aServiceReference == null) {
            return null;
        }

        // Copy service properties in a map
        final Map<String, String> serviceProperties = getServiceProperiesMap(aServiceReference);

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
        final Object serviceInstance = pBundleContext
                .getService(aServiceReference);
        if (serviceInstance == null) {
            pLogger.log(LogService.LOG_ERROR,
                    "The service reference to export as no associated instance.");
            return null;
        }

        // Get the service interfaces
        final String[] interfacesNames = (String[]) aServiceReference
                .getProperty(Constants.OBJECTCLASS);

        if (interfacesNames == null || interfacesNames.length == 0) {
            pLogger.log(LogService.LOG_WARNING, "No " + Constants.OBJECTCLASS
                    + " service property. Endpoint creation aborted.");
            return null;
        }

        // Test if the exported interface is available
        boolean interfaceFound = false;
        for (String interfaceName : interfacesNames) {

            if (interfaceName.equals(aExportedInterface)) {
                interfaceFound = true;
                break;
            }
        }

        if (!interfaceFound) {
            // Interface not found, abandon.
            pLogger.log(LogService.LOG_ERROR,
                    "The service to export doesn't provide the '"
                            + aExportedInterface + "' interface.");
            return null;
        }

        // Set the service interface name (for Rose)
        serviceProperties.put(EndpointFactory.PROP_INTERFACE_NAME,
                aExportedInterface);

        // Create end points
        try {
            pEndpointFactory.createEndpoint(serviceInstance, serviceProperties);

        } catch (IllegalArgumentException ex) {
            // Return null if no end point has been created
            pLogger.log(LogService.LOG_ERROR,
                    "Can't create an endpoint for ref: '" + aServiceReference
                            + "', instance: '" + serviceInstance + "'");
            return null;
        }

        // Store the end point description
        final String protocol = pEndpointFactory
                .getEndpointProtocol(endPointName);
        final String uri = pEndpointFactory.getEndpointBaseUri(endPointName);
        final int port = pEndpointFactory.getEndpointPort(endPointName);

        // FIXME Do it in a fancier way
        final String exportedConfig = pEndpointFactory.getConfigs()[0];

        // Prepare the list of results
        final List<EndpointDescription> newEndpoints = new ArrayList<EndpointDescription>();

        newEndpoints.add(new EndpointDescription(exportedConfig, endPointName,
                protocol, uri, port));

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

    /**
     * Retrieves the service properties as a String -&gt; String map
     * 
     * @param aServiceReference
     *            Service reference
     * @return The service properties, as a map
     */
    protected Map<String, String> getServiceProperiesMap(
            final ServiceReference aServiceReference) {

        // Get the service properties keys
        final String[] propertyKeys = aServiceReference.getPropertyKeys();

        if (propertyKeys == null) {
            // Very unlikely case - return an empty map
            return new HashMap<String, String>();
        }

        // Prepare the result
        final Map<String, String> result = new HashMap<String, String>(
                propertyKeys.length);

        for (String key : propertyKeys) {
            final Object value = aServiceReference.getProperty(key);

            if (value == null) {
                // Keep null values
                result.put(key, null);

            } else {
                // Convert others
                result.put(key, String.valueOf(value));
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() throws BundleException {

        pLogger.log(LogService.LOG_INFO, "RoseEndpointHandler Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() throws BundleException {

        pLogger.log(LogService.LOG_INFO, "RoseEndpointHandler Ready");
    }
}
