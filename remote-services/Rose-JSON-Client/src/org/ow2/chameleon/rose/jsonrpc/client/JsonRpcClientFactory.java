package org.ow2.chameleon.rose.jsonrpc.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jabsorb.client.Client;
import org.jabsorb.client.HTTPSession;
import org.jabsorb.client.Session;
import org.ow2.chameleon.rose.client.RemoteProxyFactory;

/**
 * Provides a RemoteProxyFactory and a RpcStubFactory both allowing to access a
 * RemoteService through jsonrpc thanks to the jabsorb implementation. TODO
 * Improve the client management, only one client should be created for a given
 * uri.
 * 
 * @author Jonathan Bardin <jonathan.bardin@imag.fr>
 */
public class JsonRpcClientFactory implements RemoteProxyFactory {
    private static final String[] CONFIGS = { "jsonrpc", "org.jabsorb",
	    "json-rpc" };

    /**
     * Map which contains the proxies and theirs Client.
     */
    private HashMap<Object, Client> proxies;

    public JsonRpcClientFactory() {
	proxies = new HashMap<Object, Client>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ow2.chameleon.rose.client.RemoteProxyFactory#destroyRemoteProxy(java
     * .lang.Object)
     */
    @Override
    public void destroyRemoteProxy(final Object proxy)
	    throws IllegalArgumentException {

	if (proxies.containsKey(proxy)) {
	    Client client = proxies.remove(proxy);
	    // Close the proxy
	    client.closeProxy(proxy);
	} else {
	    throw new IllegalArgumentException(
		    "The given object has not been created through this factory");
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.chameleon.rose.client.RemoteClientFactory#getConfigs()
     */
    @Override
    public String[] getConfigs() {
	return CONFIGS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ow2.chameleon.rose.client.RemoteProxyFactory#getRemoteProxy(java.
     * util.Map, java.lang.Class)
     */
    @Override
    public Object getRemoteProxy(final Map<String, String> pEndpointProp,
	    final Class<?> pClass) throws IllegalArgumentException {
	Object proxy;
	Client client;

	// Get the endpoint properties
	final String uri = pEndpointProp.get(PROP_ENDPOINT_URI);
	final String name = pEndpointProp.get(PROP_ENDPOINT_NAME);

	if (name == null) {
	    throw new IllegalArgumentException("The property "
		    + PROP_ENDPOINT_NAME
		    + " must be set in the endpoint properties");
	}

	try {
	    Session session = new HTTPSession(new URI(uri));
	    client = new Client(session);

	} catch (URISyntaxException e) {
	    throw new IllegalArgumentException(
		    "The property"
			    + PROP_ENDPOINT_URI
			    + "must be set and a valid String form of the endpoint URL",
		    e);
	}

	// Create the proxy thanks to jabsorb
	proxy = client.openProxy(name, pClass);

	// Add the proxy to the proxy list
	proxies.put(proxy, client);

	return proxy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ow2.chameleon.rose.client.RemoteProxyFactory#getRemoteProxy(java.
     * util.Map, java.lang.ClassLoader, java.lang.Class)
     */
    @Override
    public Object getRemoteProxy(final Map<String, String> pEndpointProp,
	    final ClassLoader pLoader, final Class<?> pClass)
	    throws IllegalArgumentException {
	Object proxy;
	Client client;

	// Get the endpoint properties
	String uri = pEndpointProp.get(PROP_ENDPOINT_URI);
	String name = pEndpointProp.get(PROP_ENDPOINT_NAME);

	try {
	    Session session = new HTTPSession(new URI(uri));
	    client = new Client(session, pLoader);

	} catch (URISyntaxException e) {
	    throw new IllegalArgumentException(
		    "The property"
			    + PROP_ENDPOINT_URI
			    + "must be set and a valid String form of the endpoint URL",
		    e);
	}

	try {
	    // Create the proxy thanks to jabsorb
	    proxy = client.openProxy(name, pLoader.loadClass(pClass.getName()));
	} catch (ClassNotFoundException e) {
	    throw new IllegalArgumentException(
		    "The given classloader does not contain the proxy class", e);
	}

	// Add the proxy to the proxy list
	proxies.put(proxy, client);

	return proxy;
    }

    /*------------------------------------------*
     *  Component LifeCycle method              *
     *------------------------------------------*/

    /**
     * CallBack onInvalidate, called by iPOJO. Destroy all the created proxy.
     */
    private void stop() {
	// Close the proxy, clean the map
	for (Iterator iterator = proxies.entrySet().iterator(); iterator
		.hasNext();) {
	    Entry<Object, Client> entry = (Entry) iterator.next();
	    entry.getValue().closeProxy(entry.getKey());
	    iterator.remove();
	}
    }
}
