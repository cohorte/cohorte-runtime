/**
 * File:   JsonRpcEndpoint.java
 * Author: Thomas Calmant
 * Date:   19 déc. 2011
 */
package org.psem2m.remote.jsonrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.jabsorb.ng.JSONRPCBridge;
import org.jabsorb.ng.JSONRPCServlet;
import org.jabsorb.ng.client.HTTPSessionFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.psem2m.isolates.base.BundlesClassLoader;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.remote.IEndpointHandler;
import org.psem2m.isolates.services.remote.beans.EndpointDescription;

/**
 * Implementation of the PSEM2M JSON-RPC end point handler. Uses Jabsorb.
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-remote-endpoint-handler-jsonrpc-factory", publicFactory = false)
@Provides(specifications = IEndpointHandler.class)
@Instantiate(name = "psem2m-remote-endpoint-handler-jsonrpc")
public class JsonRpcEndpoint extends CPojoBase implements IEndpointHandler {

    /** Default HTTP port */
    private static final int DEFAULT_HTTP_PORT = 80;

    /** The bundle context */
    private final BundleContext pBundleContext;

    /** Service -&gt; End point description mapping */
    private final Map<ServiceReference, EndpointDescription[]> pEndpointsDescriptions = new HashMap<ServiceReference, EndpointDescription[]>();

    /** HTTP Service, to host Jabsorb servlet */
    @Requires
    private HttpService pHttpService;

    /** The JSON-RPC bridge (Jabsorb) */
    private JSONRPCBridge pJsonRpcBridge;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The registered end points names list */
    private final List<String> pRegisteredEndpoints = new ArrayList<String>();

    /** Name of the Jabsorb servlet */
    @Property(name = "endpoint.servlet.name", value = IJsonRpcConstants.DEFAULT_SERVLET_NAME)
    private String pServletName;

    /**
     * Prepares the component
     * 
     * @param aBundleContext
     *            The bundle context
     */
    public JsonRpcEndpoint(final BundleContext aBundleContext) {

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
    public EndpointDescription[] createEndpoint(
            final String aExportedInterface,
            final ServiceReference aServiceReference) {

        if (aServiceReference == null) {
            return null;
        }

        // Copy service properties in a map
        final Map<String, String> serviceProperties = getServiceProperiesMap(aServiceReference);

        // Compute a end point name
        final String endPointName = generateEndpointName(serviceProperties);

        // Get a reference to the service
        final Object serviceInstance = pBundleContext
                .getService(aServiceReference);
        if (serviceInstance == null) {
            pLogger.logSevere(this, "createEndpoint",
                    "The service reference to export as no associated instance.");
            return null;
        }

        // Get the service interfaces
        final String[] interfacesNames = (String[]) aServiceReference
                .getProperty(Constants.OBJECTCLASS);
        if (!isInterfaceExported(aExportedInterface, interfacesNames)) {
            // Interface not exported, already logged.
            return null;
        }

        // Create the end point
        if (aExportedInterface == null) {
            // Export all public methods
            pJsonRpcBridge.registerObject(endPointName, serviceInstance);

        } else {
            // Export only methods declared in the exported interface
            try {
                final Class<?> interfaceClass = serviceInstance.getClass()
                        .getClassLoader().loadClass(aExportedInterface);

                pJsonRpcBridge.registerObject(endPointName, serviceInstance,
                        interfaceClass);

            } catch (final ClassNotFoundException ex) {
                // Log the error
                pLogger.logSevere(this, "createEndpoint",
                        "Error loading the exported interface :", ex);

                // No end point created.
                return null;
            }
        }

        // Keep a track of the end point
        pRegisteredEndpoints.add(endPointName);

        // Prepare the end point description to return
        final EndpointDescription endpointDescription = new EndpointDescription(
                IJsonRpcConstants.EXPORT_CONFIGS[0], endPointName,
                IJsonRpcConstants.EXPORT_PROTOCOL,
                makeEndpointUri(endPointName), getHttpPort());

        // Make an array
        final EndpointDescription[] result = new EndpointDescription[] { endpointDescription };

        // Store the information
        pEndpointsDescriptions.put(aServiceReference, result);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.IEndpointHandler#destroyEndpoint(
     * org.osgi.framework.ServiceReference)
     */
    @Override
    public boolean destroyEndpoint(final ServiceReference aServiceReference) {

        final String endpointName = (String) aServiceReference
                .getProperty(PROP_ENDPOINT_NAME);
        if (!pRegisteredEndpoints.contains(endpointName)) {
            // Unknown service
            return false;
        }

        // Destroy the end point
        pJsonRpcBridge.unregisterObject(endpointName);
        pEndpointsDescriptions.remove(aServiceReference);
        pRegisteredEndpoints.remove(endpointName);
        return true;
    }

    /**
     * Prepares an end point name, based on service properties
     * 
     * @param aServiceProperties
     *            Properties of the exported service
     * @return An end point name, never null
     */
    protected String generateEndpointName(
            final Map<String, String> aServiceProperties) {

        // Compute a end point name
        String endpointName = aServiceProperties.get(PROP_ENDPOINT_NAME);
        if (endpointName == null) {
            endpointName = "service"
                    + aServiceProperties.get(Constants.SERVICE_ID);
        }

        return endpointName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.IEndpointHandler#getEndpoints(org
     * .osgi.framework.ServiceReference)
     */
    @Override
    public EndpointDescription[] getEndpoints(
            final ServiceReference aServiceReference) {

        final EndpointDescription[] result = pEndpointsDescriptions
                .get(aServiceReference);

        if (result == null) {
            // Never return null
            return new EndpointDescription[0];
        }

        return result;
    }

    /**
     * Tries to retrieve the HTTP port to access the generated end points. First
     * tries reading the <em>org.osgi.service.http.port</em> system property. If
     * the property content is invalid, returns 80.
     * 
     * @return The found HTTP port.
     */
    protected int getHttpPort() {

        final String portStr = System.getProperty("org.osgi.service.http.port");
        int port = DEFAULT_HTTP_PORT;

        try {
            port = Integer.parseInt(portStr);

        } catch (final NumberFormatException ex) {
            port = DEFAULT_HTTP_PORT;
        }

        return port;
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

        for (final String key : propertyKeys) {
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
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        // Clean up the bridge
        stopJabsorbBridge();

        // Clear structures
        pEndpointsDescriptions.clear();
        pRegisteredEndpoints.clear();

        pLogger.logInfo(this, "invalidatePojo",
                "PSEM2M JSON-RPC Remote-Services endpoint Gone");
    }

    /**
     * Tests if the exported interface is in the implemented ones. The
     * implemented interfaces can be found by reading the
     * {@link Constants#OBJECTCLASS} service property.
     * 
     * 
     * @param aExportedInterface
     *            Exported interface
     * @param aImplementedInterfaces
     *            Implemented interfaces
     * @return True if the exported interface is in the implemented ones
     */
    protected boolean isInterfaceExported(final String aExportedInterface,
            final String[] aImplementedInterfaces) {

        if (aImplementedInterfaces == null
                || aImplementedInterfaces.length == 0) {
            pLogger.logWarn(this, "createEndpoint",
                    "No interface implememted by the service.");
            return false;
        }

        // Test if the exported interface is available
        for (final String interfaceName : aImplementedInterfaces) {

            if (interfaceName.equals(aExportedInterface)) {
                return true;
            }
        }

        // Interface not found, abandon.
        pLogger.logSevere(
                this,
                "createEndpoint",
                "The service to export doesn't provide the specified interface=",
                aExportedInterface);
        return false;
    }

    /**
     * Generates the URI to access the given end point
     * 
     * @param aEndpointName
     *            A end point name
     * @return The URI to access the end point
     */
    protected String makeEndpointUri(final String aEndpointName) {

        final StringBuilder builder = new StringBuilder(pServletName);
        builder.append("/").append(aEndpointName);

        return builder.toString();
    }

    /**
     * Sets up the Jabsorb bridge
     */
    protected void startJabsorbBridge() {

        // Register the Jabsorb servlet
        try {
            pHttpService.registerServlet(pServletName, new JSONRPCServlet(),
                    null, null);

        } catch (final Exception ex) {
            pLogger.logSevere(this, "startJabsorbBridge",
                    "Error registering the JSON-RPC servlet (Jabsorb) :", ex);
        }

        // Set the bridge
        pJsonRpcBridge = JSONRPCBridge.getGlobalBridge();

        // Set the serializer class loader
        final BundlesClassLoader classLoader = new BundlesClassLoader(
                pBundleContext);
        JSONRPCBridge.getSerializer().setClassLoader(classLoader);

        // Set the HTTP session provider
        HTTPSessionFactory
                .setHTTPSessionProvider(new JabsorbHttpSessionProvider());
    }

    /**
     * Cleans up the Jabsorb bridge references.
     */
    protected void stopJabsorbBridge() {

        // Unregister the servlet
        pHttpService.unregister(pServletName);

        // Unregister end points
        final String[] endpoints = pRegisteredEndpoints
                .toArray(new String[pRegisteredEndpoints.size()]);
        for (final String endpoint : endpoints) {
            pJsonRpcBridge.unregisterObject(endpoint);
        }

        // Clean up references
        HTTPSessionFactory.setHTTPSessionProvider(null);
        JSONRPCBridge.getSerializer().setClassLoader(null);
        pJsonRpcBridge = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        // Be sure to have clean members
        pEndpointsDescriptions.clear();
        pRegisteredEndpoints.clear();

        // Start the bridge
        startJabsorbBridge();

        pLogger.logInfo(this, "validatePojo",
                "PSEM2M JSON-RPC Remote-Services endpoint Ready");
    }
}
