package org.ow2.chameleon.rose.jsonrpc.endpoint;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCServlet;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.log.LogService;
import org.ow2.chameleon.rose.server.EndpointFactory;

/**
 * This component provides a JsonRpc, jabsorb.org based implementation of an
 * EndpointFactory service provider.
 * 
 * @author Jonathan Bardin <jonathan.bardin@imag.fr>
 */
public class JsonRpcEndpointFactory implements EndpointFactory {

    public static final String[] CONFIGS = { "jsonrpc", "org.jabsorb",
	    "json-rpc" };

    /**
     * Name of the Property needed by JSONRPCServlet, the gzip threshold.
     */
    private static final String PROP_GZIP_THRESHOLD = "gzip_threshold";

    /**
     * Set which contains the names of the endpoint created by this factory.
     */
    private Set<String> endpointNames = new HashSet<String>();

    /**
     * Values of the Property needed by JSONRPCServlet, the gzip threshold.
     */
    private String gzip_threshold = "200";

    /**
     * The HTTPService available on this gateway, injected by iPOJO.
     */
    private HttpService httpservice;

    /**
     * This class implements a bridge that unmarshalls JSON objects in JSON-RPC
     * request format, invokes a method on the exported object, and then
     * marshalls the resulting Java objects to JSON objects in JSON-RPC result
     * format.
     */
    private JSONRPCBridge jsonbridge;

    /**
     * The LogService, injected by iPOJO.
     */
    private LogService logger;

    /**
     * The Servlet name of the JSON-RPC bridge.
     */
    private String servletname;

    /**
     * Default constructor, for iPOJO
     */
    public JsonRpcEndpointFactory() {
	super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ow2.chameleon.rose.server.EndpointFactory#createEndpoint(java.lang
     * .Object, java.lang.ClassLoader, java.util.Map)
     */
    @Override
    public void createEndpoint(final Object pService,
	    final ClassLoader pLoader, final Map<String, String> properties)
	    throws IllegalArgumentException {
	String name = properties.get(PROP_ENDPOINT_NAME);

	// Check if the name is valid
	if (endpointNames.contains(name)) {
	    throw new IllegalArgumentException("An endpoint of name: " + name
		    + " has already been created.");
	}

	String itf = properties.get(PROP_INTERFACE_NAME);

	try {
	    jsonbridge.registerObject(name, pService, pLoader.loadClass(itf));
	} catch (NullPointerException e) {
	    throw new IllegalArgumentException("The properties "
		    + PROP_ENDPOINT_NAME + " and " + PROP_INTERFACE_NAME
		    + " must be set.", e);
	} catch (ClassNotFoundException e) {
	    throw new IllegalArgumentException(
		    "Cannot load the service interface " + itf
			    + " from the given classloader.", e);
	}

	// OK, add the endpoint names to the set
	endpointNames.add(name);
    }

    /*------------------------------------*
     *  Component Life-cycle methods      *
     *------------------------------------*/

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ow2.chameleon.rose.server.EndpointFactory#createEndpoint(java.lang
     * .Object, java.util.Map)
     */
    @Override
    public void createEndpoint(final Object pService,
	    final Map<String, String> properties)
	    throws IllegalArgumentException {
	String name = properties.get(PROP_ENDPOINT_NAME);

	// Check if the name is valid
	if (endpointNames.contains(name)) {
	    throw new IllegalArgumentException("An endpoint of name: " + name
		    + " has already been created.");
	}

	if (!properties.containsKey(PROP_INTERFACE_NAME)) {
	    // Export all !
	    jsonbridge.registerObject(name, pService);
	}

	// only the specified interface
	else {
	    String itf = properties.get(PROP_INTERFACE_NAME);

	    try {
		jsonbridge.registerObject(name, pService, pService.getClass()
			.getClassLoader().loadClass(itf));
	    } catch (NullPointerException e) {
		throw new IllegalArgumentException("The properties "
			+ PROP_ENDPOINT_NAME + ", " + PROP_INTERFACE_NAME
			+ " must be set.", e);
	    } catch (ClassNotFoundException e) {
		throw new IllegalArgumentException(
			"Cannot load the service interface " + itf
				+ " from the service classloader.", e);
	    }
	}

	// OK, add the endpoint names to the set
	endpointNames.add(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ow2.chameleon.rose.server.EndpointFactory#destroyEndpoint(java.lang
     * .String)
     */
    @Override
    public void destroyEndpoint(final String pService)
	    throws IllegalArgumentException, NullPointerException {
	// Check if the name is valid
	if (!endpointNames.contains(pService)) {
	    throw new IllegalArgumentException("There is no endpoint of name: "
		    + pService);
	}

	// Destroy the endpoint
	jsonbridge.unregisterObject(pService);

	endpointNames.remove(pService);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.chameleon.rose.server.EndpointFactory#getConfigs()
     */
    @Override
    public String[] getConfigs() {
	return CONFIGS;
    }

    /*------------------------------------*
     *  EndpointFactory methods           *
     *------------------------------------*/

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ow2.chameleon.rose.server.EndpointFactory#getEndpointBaseUri(java
     * .lang.String)
     */
    @Override
    public String getEndpointBaseUri(final String aPEndpointName) {
	return servletname + "/";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ow2.chameleon.rose.server.EndpointFactory#getEndpointPort(java.lang
     * .String)
     */
    @Override
    public int getEndpointPort(final String pEndpointName) {

	String portStr = System.getProperty("org.osgi.service.http.port");
	int port = 80;

	try {
	    port = Integer.parseInt(portStr);
	} catch (NumberFormatException ex) {
	    port = 80;
	}

	return port;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ow2.chameleon.rose.server.EndpointFactory#getEndpointProtocol(java
     * .lang.String)
     */
    @Override
    public String getEndpointProtocol(final String pEndpointName) {
	return "http";
    }

    /**
     * Execute while this instance is starting. Call by iPOJO.
     */
    @SuppressWarnings("unused")
    private void start() {

	Dictionary<String, String> properties = new Hashtable<String, String>();
	properties.put(PROP_GZIP_THRESHOLD, gzip_threshold);

	try {
	    // Registered the JSONRPCServlet
	    httpservice.registerServlet(servletname, new JSONRPCServlet(),
		    properties, null);
	} catch (NamespaceException e) {
	    logger.log(LogService.LOG_ERROR, e.getMessage(), e);
	} catch (Exception e) {
	    logger.log(LogService.LOG_ERROR, e.getMessage(), e);
	}

	// Set the bridge to a global bridge. (HttpSession are not managed here)
	// TODO support the HttpSession
	jsonbridge = JSONRPCBridge.getGlobalBridge();
    }

    /**
     * Execute while this instance is stopping. Call by iPOJO.
     */
    @SuppressWarnings("unused")
    private void stop() {
	try {
	    if (httpservice != null) {
		httpservice.unregister(servletname);
	    }
	} catch (RuntimeException re) {
	    logger.log(LogService.LOG_ERROR, re.getMessage(), re);
	}
    }
}
