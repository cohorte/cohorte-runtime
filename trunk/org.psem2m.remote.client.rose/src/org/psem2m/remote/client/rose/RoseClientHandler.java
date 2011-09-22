/**
 * File:   RoseClientHandler.java
 * Author: Thomas Calmant
 * Date:   27 juil. 2011
 */
package org.psem2m.remote.client.rose;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.log.LogService;
import org.ow2.chameleon.rose.client.RemoteClientFactory;
import org.ow2.chameleon.rose.client.RemoteProxyFactory;
import org.psem2m.isolates.base.BundlesClassLoader;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.remote.IRemoteServiceClientHandler;
import org.psem2m.isolates.services.remote.beans.EndpointDescription;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent;
import org.psem2m.isolates.services.remote.beans.RemoteServiceRegistration;

/**
 * Implementation of a remote service client handler, using Rose ClientFactory
 * services
 * 
 * @author Thomas Calmant
 */
@Component(name = "remote-client-handler-rose-factory", publicFactory = false)
@Provides(specifications = IRemoteServiceClientHandler.class)
@Instantiate(name = "remote-client-handler-rose")
public class RoseClientHandler extends CPojoBase implements
        IRemoteServiceClientHandler {

    /** The component bundle context */
    private BundleContext pBundleContext;

    /** Log service */
    @Requires
    private LogService pLogger;

    /** Rose remote proxy factories */
    @Requires
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
            pLogger.log(LogService.LOG_ERROR, "[RoseClientHandler] No proxies");
            return null;
        }

        // Get the remote service registration
        final RemoteServiceRegistration serviceRegistration = aServiceEvent
                .getServiceRegistration();
        if (serviceRegistration == null) {
            pLogger.log(LogService.LOG_ERROR,
                    "[RoseClientHandler] No service registration in event");
            return null;
        }

        // Compute the interface name
        String[] interfaceNames = serviceRegistration.getExportedInterfaces();
        if (interfaceNames == null || interfaceNames.length == 0) {
            pLogger.log(LogService.LOG_ERROR,
                    "[RoseClientHandler] No/Empty interface array");
            return null;
        }

        final String interfaceName = interfaceNames[interfaceNames.length - 1];
        if (interfaceName == null) {
            pLogger.log(LogService.LOG_ERROR,
                    "[RoseClientHandler] No interface");
            return null;
        }

        // Find the class
        // Class.forName(interfaceName) won't work...
        final Class<?> interfaceClass = Utilities.findClassInBundles(
                pBundleContext.getBundles(), interfaceName);
        if (interfaceClass == null) {
            pLogger.log(LogService.LOG_ERROR, "[RoseClientHandler] Interface '"
                    + interfaceName + "' no found");
            throw new ClassNotFoundException(interfaceName);
        }

        // Get available end points
        final EndpointDescription[] endpoints = serviceRegistration
                .getEndpoints();

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
            pLogger.log(LogService.LOG_ERROR,
                    "[RoseClientHandler] No factory/endpoint couple found");
            return null;
        }

        final String endpointUri = selectedEndpoint.computeURI();
        if (endpointUri == null) {
            pLogger.log(LogService.LOG_ERROR,
                    "[RoseClientHandler] Invalid endpoint URI");
            return null;
        }

        // Convert end point properties to a map
        Map<String, String> endpointProperties = new HashMap<String, String>();
        endpointProperties.put(RemoteClientFactory.PROP_ENDPOINT_NAME,
                selectedEndpoint.getEndpointName());

        endpointProperties.put(RemoteClientFactory.PROP_ENDPOINT_URI,
                endpointUri);

        // Prepare a framework wide class loader
        // FIXME CLASS LOADER
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
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.log(LogService.LOG_INFO, "RoserHandlerClient Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.log(LogService.LOG_INFO, "RoserHandlerClient Ready");
    }
}
