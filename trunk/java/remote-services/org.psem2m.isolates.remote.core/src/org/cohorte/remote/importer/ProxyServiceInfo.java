package org.cohorte.remote.importer;

import org.cohorte.remote.beans.RemoteServiceRegistration;
import org.osgi.framework.ServiceRegistration;

/**
 * Stored information about proxied services
 * 
 * @author Thomas Calmant
 */
public class ProxyServiceInfo {

    /** The proxy object */
    private final Object pProxy;

    /** The remote service registration bean */
    private final RemoteServiceRegistration pRemoteRegistration;

    /** The service registration information */
    private final ServiceRegistration<?> pServiceRegistration;

    /**
     * Sets up the bean
     * 
     * @param aRemoteRegistration
     *            Remote service registration
     * @param aLocalRegistration
     *            Local service registration information
     * @param aProxy
     *            Service proxy object
     */
    public ProxyServiceInfo(
            final RemoteServiceRegistration aRemoteRegistration,
            final ServiceRegistration<?> aLocalRegistration, final Object aProxy) {

        pRemoteRegistration = aRemoteRegistration;
        pServiceRegistration = aLocalRegistration;
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
     * Retrieves the remote service registration
     * 
     * @return the remote service registration
     */
    public RemoteServiceRegistration getRemoteRegistration() {

        return pRemoteRegistration;
    }

    /**
     * Retrieves the service registration information
     * 
     * @return the service registration information
     */
    public ServiceRegistration<?> getServiceRegistration() {

        return pServiceRegistration;
    }
}
