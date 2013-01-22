/**
 * File:   JsonRpcClient.java
 * Author: Thomas Calmant
 * Date:   19 déc. 2011
 */
package org.psem2m.remote.jsonrpc;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.jabsorb.ng.client.Client;
import org.jabsorb.ng.client.ISession;
import org.jabsorb.ng.client.TransportRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.BundlesClassLoader;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.remote.IRemoteServiceClientHandler;
import org.psem2m.isolates.services.remote.beans.EndpointDescription;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent;
import org.psem2m.isolates.services.remote.beans.RemoteServiceRegistration;

/**
 * @author Thomas Calmant
 * 
 */
@Component(name = "psem2m-remote-client-jsonrpc-factory", publicFactory = false)
@Provides(specifications = IRemoteServiceClientHandler.class)
public class JsonRpcClient extends CPojoBase implements
        IRemoteServiceClientHandler {

    /** The bundle context */
    private final BundleContext pBundleContext;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Proxy -&gt; Jabsorb Client map */
    private final Map<Object, Client> pProxies = new HashMap<Object, Client>();

    /**
     * Sets up the Jabsorb client
     * 
     * @param aBundleContext
     *            The bundle context
     */
    public JsonRpcClient(final BundleContext aBundleContext) {

        super();
        pBundleContext = aBundleContext;
    }

    /**
     * Creates the proxy for the given interface at the given end point
     * 
     * @param aEndpoint
     *            An end point description
     * @param aClass
     *            The interface exported at the end point
     * @return A proxy to that end point
     */
    protected Object createProxy(final EndpointDescription aEndpoint,
            final Class<?> aClass) {

        // Prepare a bundle class loader
        final BundlesClassLoader classLoader = new BundlesClassLoader(
                pBundleContext);

        // Create the Jabsorb client
        final ISession session = TransportRegistry.i().createSession(
                aEndpoint.computeURI());
        final Client client = new Client(session, classLoader);

        // Create the proxy
        final Object proxy = client.openProxy(aEndpoint.getEndpointName(),
                aClass);

        // Store it
        pProxies.put(proxy, client);
        return proxy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.IRemoteServiceClientHandler#destroyProxy
     * (java.lang.Object)
     */
    @Override
    public void destroyProxy(final Object aProxy) {

        synchronized (pProxies) {

            if (pProxies.containsKey(aProxy)) {

                // Close the proxy
                final Client client = pProxies.get(aProxy);
                if (client != null) {
                    pProxies.get(aProxy).closeProxy(aProxy);
                }

                // Remove its reference
                pProxies.remove(aProxy);
            }
        }
    }

    /**
     * Looks for a known configuration in the exported ones
     * 
     * @param aEndpoints
     *            End points descriptions
     * @return The first handled description, null if none found
     */
    protected EndpointDescription getEndpointDescription(
            final EndpointDescription[] aEndpoints) {

        pLogger.logInfo(this, "EXPORT END POINTS", "endpoints=", aEndpoints);

        for (final EndpointDescription endpoint : aEndpoints) {

            // Test if the service has a JSON-RPC export configuration
            final String exportedConfig = endpoint.getExportedConfig();

            pLogger.logInfo(this, "EXPORTED CONFIG", "config=", exportedConfig);

            for (final String config : IJsonRpcConstants.EXPORT_CONFIGS) {

                if (config.equals(exportedConfig)) {
                    return endpoint;
                }
            }
        }

        return null;
    }

    /**
     * Tries to get the last exported interface of the given ones
     * 
     * @param aInterfacesNames
     *            Exported interfaces names
     * @return The loaded interface class, null if not described
     * @throws ClassNotFoundException
     *             The interface is unknown
     */
    protected Class<?> getExportedInterface(final String[] aInterfacesNames)
            throws ClassNotFoundException {

        // Invalid parameter
        if (aInterfacesNames == null || aInterfacesNames.length == 0) {
            pLogger.logSevere(this, "getRemoteProxy",
                    "No/Empty interface array");
            return null;
        }

        // Get the last name
        final String interfaceName = aInterfacesNames[aInterfacesNames.length - 1];
        if (interfaceName == null) {
            pLogger.logSevere(this, "getRemoteProxy", "No interface");
            return null;
        }

        // Find the class
        // Class.forName(interfaceName) won't work...
        final Class<?> interfaceClass = Utilities.findClassInBundles(
                pBundleContext.getBundles(), interfaceName);
        if (interfaceClass == null) {
            pLogger.logSevere(this, "getRemoteProxy", "Interface not found=",
                    interfaceName);
            throw new ClassNotFoundException(interfaceName);
        }

        return interfaceClass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.IRemoteServiceClientHandler#
     * getRemoteProxy
     * (org.psem2m.isolates.services.remote.beans.RemoteServiceEvent)
     */
    @Override
    public Object getRemoteProxy(final RemoteServiceEvent aServiceEvent)
            throws ClassNotFoundException {

        if (aServiceEvent == null) {
            pLogger.logSevere(this, "getRemoteProxy", "Invalid service event");
            return null;
        }

        // Get the remote service registration
        final RemoteServiceRegistration serviceRegistration = aServiceEvent
                .getServiceRegistration();
        if (serviceRegistration == null) {
            pLogger.logSevere(this, "getRemoteProxy",
                    "No service registration in the remove service event");
            return null;
        }

        // Get available end points
        final EndpointDescription foundEndpoint = getEndpointDescription(serviceRegistration
                .getEndpoints());
        if (foundEndpoint == null) {
            // Not exported with JSON-RPC
            pLogger.logSevere(this, "getRemoteProxy",
                    "Service is not exported with JSON-RPC");
            return null;
        }

        // Get the interface class
        final Class<?> interfaceClass = getExportedInterface(serviceRegistration
                .getExportedInterfaces());

        // Store it
        return createProxy(foundEndpoint, interfaceClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        // Clean up all proxies
        synchronized (pProxies) {

            for (final Entry<Object, Client> entry : pProxies.entrySet()) {

                // Close the proxy
                final Client client = entry.getValue();
                if (client != null) {
                    client.closeProxy(entry.getKey());
                }
            }

            // Clear the list
            pProxies.clear();
        }

        pLogger.logInfo(this, "invalidatePojo",
                "PSEM2M JSON-RPC Remote-Services proxy Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        // Be sure to start from nothing...
        pProxies.clear();

        pLogger.logInfo(this, "validatePojo",
                "PSEM2M JSON-RPC Remote-Services proxy Ready");
    }
}
