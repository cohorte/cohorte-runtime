/**
 * File:   EndpointDescription.java
 * Author: Thomas Calmant
 * Date:   27 juil. 2011
 */
package org.psem2m.isolates.services.remote.beans;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

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

    /** End point host */
    private String pHost;

    /** Port to join the end point */
    private final int pPort;

    /** End point access protocol (http, ...) */
    private final String pProtocol;

    /**
     * Sets up the end point description
     * 
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
    public EndpointDescription(final String aExportedConfig,
            final String aEndpointName, final String aProtocol,
            final String aEndpointUri, final int aPort) {

        pExportedConfig = aExportedConfig;
        pEndpointName = aEndpointName;
        pProtocol = aProtocol;
        pEndpointUri = aEndpointUri;
        pPort = aPort;

        // Set the default host name
        pHost = "localhost";
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object aObj) {

        if (!(aObj instanceof EndpointDescription)) {
            return false;
        }

        final EndpointDescription other = (EndpointDescription) aObj;

        // End point access
        if (!safeCompare(pHost, other.pHost)
                || !safeCompare(pPort, other.pPort)
                || !safeCompare(pProtocol, other.pProtocol)) {
            return false;
        }

        // End point properties
        if (!safeCompare(pEndpointName, other.pEndpointName)
                || !safeCompare(pEndpointUri, other.pEndpointUri)
                || !safeCompare(pExportedConfig, other.pExportedConfig)) {
            return false;
        }

        // Finally, test exported interfaces
        return true;
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
     * Safely compares two objects (with equals)
     * 
     * @param aObjectA
     *            Object A
     * @param aObjectB
     *            Object B
     * @return True if objects are equals (or both null)
     */
    protected boolean safeCompare(final Object aObjectA, final Object aObjectB) {

        if (aObjectA != null) {
            return aObjectA.equals(aObjectB);
        } else if (aObjectB != null) {
            return aObjectB.equals(aObjectA);
        } else {
            // Both null
            return true;
        }
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
