/**
 * File:   EndpointDescription.java
 * Author: Thomas Calmant
 * Date:   27 juil. 2011
 */
package org.psem2m.isolates.services.remote.beans;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import org.osgi.framework.ServiceReference;

/**
 * Description of an end point description
 * 
 * @author Thomas Calmant
 */
public class EndpointDescription implements Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** End point name, as in service properties (can be null) */
    private final String pEndpointName;

    /** Complete end point URI, if needed (e.g. /JSON-RPC/endpointname) */
    private final String pEndpointUri;

    /** Associated "service.exported.configs" value */
    private final String pExportedConfig;

    /** Interfaces exported by this end point */
    private final String[] pExportedInterfaces;

    /** End point host */
    private String pHost;

    /** Port to join the end point */
    private final int pPort;

    /** End point access protocol (http, ...) */
    private final String pProtocol;

    public EndpointDescription(final ServiceReference aServiceReference,
	    final String aExportedConfig, final String aEndpointName,
	    final String aProtocol, final String aEndpointUri, final int aPort) {

	// Default assignments
	this(new String[0], aExportedConfig, aEndpointName, aProtocol,
		aEndpointUri, aPort);

    }

    /**
     * Sets up the end point description
     * 
     * @param aExportedInterfaces
     *            Interfaces exported by this end point
     * @param aExportedConfig
     *            Remote Services exported configurations
     * @param aEndpointName
     *            End point name, as in service properties (can be null)
     * @param aProtocol
     *            Protocol to be used to connect the end point
     * @param aEndpointUri
     *            Complete end point URI, if needed (e.g.
     *            /JSON-RPC/endpointname)
     * @param aPort
     *            Port to join the end point
     */
    public EndpointDescription(final String[] aExportedInterfaces,
	    final String aExportedConfig, final String aEndpointName,
	    final String aProtocol, final String aEndpointUri, final int aPort) {

	pExportedInterfaces = aExportedInterfaces;
	pExportedConfig = aExportedConfig;
	pEndpointName = aEndpointName;
	pProtocol = aProtocol;
	pEndpointUri = aEndpointUri;
	pPort = aPort;
    }

    /**
     * Returns a URI according to the given informations
     * 
     * @return A URI to contact the end point, null on error
     */
    public String computeURI() {

	try {
	    URI uri = new URI(pProtocol, null, pHost, pPort, pEndpointUri,
		    null, null);
	    return uri.toString();

	} catch (URISyntaxException ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    /**
     * Retrieves the end point name, as in service properties (can be null)
     * 
     * @return the end point name
     */
    public String getEndpointName() {
	return pEndpointName;
    }

    /**
     * Retrieves the complete end point URI, if needed (e.g.
     * /JSON-RPC/endpointname)
     * 
     * @return the end point URI
     */
    public String getEndpointUri() {
	return pEndpointUri;
    }

    /**
     * Retrieves the associated "service.exported.configs" value
     * 
     * @return the associated "service.exported.configs" value
     */
    public String getExportedConfig() {
	return pExportedConfig;
    }

    /**
     * Retrieves the array of the interfaces exported by this end point
     * 
     * @return The interfaces exported by this end point
     */
    public String[] getExportedInterfaces() {
	return pExportedInterfaces;
    }

    /**
     * Retrieves the end point host
     * 
     * @return The end point host
     */
    public String getHost() {
	return pHost;
    }

    /**
     * Retrieves the port where to join the end point
     * 
     * @return the end point port
     */
    public int getPort() {
	return pPort;
    }

    /**
     * Retrieves the end point access protocol (http, ...)
     * 
     * @return The end point access protocol
     */
    public String getProtocol() {
	return pProtocol;
    }

    /**
     * Sets the end point host. Only modifiable value : it may be updated when
     * received by a discoverer
     * 
     * @param aHost
     *            The end point host
     */
    public void setHost(final String aHost) {
	pHost = aHost;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

	StringBuilder builder = new StringBuilder();
	builder.append("EndpointDescription(");
	builder.append("protocol=").append(pProtocol);
	builder.append(", port=").append(pPort);
	builder.append(", uri=").append(pEndpointUri);
	builder.append(", name=").append(pEndpointName);
	builder.append(")");

	return builder.toString();
    }
}
