package org.psem2m.isolates.remote.importer;

import org.osgi.framework.ServiceRegistration;

/**
 * Stored information about proxied services
 * 
 * @author Thomas Calmant
 */
public class ProxyServiceInfo {

    /** The proxy object */
    private final Object pProxy;

    /** The service registration information */
    private final ServiceRegistration pServiceRegistration;

    /**
     * Sets up the bean
     * 
     * @param aRegistration
     *            Service registration information
     * @param aProxy
     *            Service proxy object
     */
    public ProxyServiceInfo(final ServiceRegistration aRegistration,
            final Object aProxy) {

        pServiceRegistration = aRegistration;
        pProxy = aProxy;
    }

    /**
     * Retrieves the proxy object
     * 
     * @return the proxy object
     */
    public Object getProxy() {

        return pProxy;
    }

    /**
     * Retrieves the service registration information
     * 
     * @return the service registration information
     */
    public ServiceRegistration getServiceRegistration() {

        return pServiceRegistration;
    }
}
