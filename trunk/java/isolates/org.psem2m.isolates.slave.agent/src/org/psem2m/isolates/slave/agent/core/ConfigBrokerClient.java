/**
 * File:   ConfigBrokerClient.java
 * Author: Thomas Calmant
 * Date:   8 août 2012
 */
package org.psem2m.isolates.slave.agent.core;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.psem2m.isolates.base.Utilities;

/**
 * HTTP Configuration Broker client
 * 
 * @author Thomas Calmant
 */
public class ConfigBrokerClient {

    /** Default content encoding */
    private static final String DEFAULT_CONTENT_ENCODING = "UTF-8";

    /** Default HTTP server port */
    private final static int DEFAULT_HTTP_PORT = 80;

    /** The URL to the broker */
    private final URL pBrokerBaseUrl;

    /**
     * Sets up the broker client
     * 
     * The given URL must point to an HTTP server. If the port is omitted, the
     * default HTTP port 80 will be used. Query string is ignored.
     * 
     * @param aBrokerUrlStr
     *            Base URL to the broker
     * @throws BrokerException
     *             Invalid value found in the broker URL
     * @throws MalformedURLException
     *             Error reading or normalizing the broker URL
     */
    public ConfigBrokerClient(final String aBrokerUrlStr)
            throws MalformedURLException, BrokerException {

        this(new URL(aBrokerUrlStr));
    }

    /**
     * Sets up the broker client.
     * 
     * The given URL must point to an HTTP server. If the port is omitted, the
     * default HTTP port 80 will be used. Query string is ignored.
     * 
     * @param aBrokerUrl
     *            Base URL to the broker
     * @throws BrokerException
     *             Invalid value found in the broker URL
     * @throws MalformedURLException
     *             Error normalizing the broker URL
     */
    public ConfigBrokerClient(final URL aBrokerUrl) throws BrokerException,
            MalformedURLException {

        // Test the protocol
        final String protocol = aBrokerUrl.getProtocol();
        if (!"http".equals(protocol)) {
            // Only HTTP is handled
            throw new BrokerException("Unknown protocol: " + protocol);
        }

        // Normalize the port
        int port = aBrokerUrl.getPort();
        if (port < 0) {
            port = DEFAULT_HTTP_PORT;
        }

        // Prepare a normalized URL
        pBrokerBaseUrl = new URL(protocol, aBrokerUrl.getHost(), port,
                aBrokerUrl.getPath());
    }

    /**
     * Deletes the configuration of the given isolate from the broker
     * 
     * @param aIsolateId
     *            An isolate ID
     * @throws IOException
     *             An error occurred during the request
     * @throws BrokerException
     *             The broker returned an error
     */
    public void deleteConfiguration(final String aIsolateId)
            throws IOException, BrokerException {

        // Get the configuration URL
        final URL isolateConfigUrl = getConfigurationUrl(aIsolateId);

        // Connect to the broker
        final HttpURLConnection connection = (HttpURLConnection) isolateConfigUrl
                .openConnection();

        // Change the request method
        connection.setRequestMethod("DELETE");

        // Send the request
        final int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            // Error during the request
            throw new BrokerException(
                    "Configuration broker returned an error code: {0}",
                    responseCode);
        }
    }

    /**
     * Retrieves the URL used by this client to access the broker
     * 
     * @return The broker URL
     */
    public URL getBrokerUrl() {

        return pBrokerBaseUrl;
    }

    /**
     * Retrieves the configuration of the given isolate from the broker
     * 
     * @param aIsolateId
     *            An isolate ID
     * @return The configuration of the given isolate
     * @throws IOException
     *             An error occurred during the request
     * @throws BrokerException
     *             The broker returned an error
     */
    public String getConfiguration(final String aIsolateId) throws IOException,
            BrokerException {

        // Get the configuration URL
        final URL isolateConfigUrl = getConfigurationUrl(aIsolateId);

        // Connect to the broker
        final HttpURLConnection connection = (HttpURLConnection) isolateConfigUrl
                .openConnection();

        // Send the request
        final int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            // Error during the request
            throw new BrokerException(
                    "Configuration broker returned an error code: {0}",
                    responseCode);
        }

        // Get the content encoding
        String encoding = connection.getHeaderField("Content-Encoding");
        if (encoding == null) {
            encoding = DEFAULT_CONTENT_ENCODING;
        }

        // Get the response content
        final byte[] rawContent = Utilities.inputStreamToBytes(connection
                .getInputStream());

        // Return the corresponding string
        return new String(rawContent, encoding);
    }

    /**
     * Retrieves the configuration broker URL for the given isolate
     * 
     * @param aIsolateId
     *            An isolate ID
     * @return The URL to the configuration of the isolate
     * @throws MalformedURLException
     *             Error preparing the URL
     */
    private URL getConfigurationUrl(final String aIsolateId)
            throws MalformedURLException {

        // Prepare the URI
        final StringBuilder uriBuilder = new StringBuilder(
                pBrokerBaseUrl.getPath());
        uriBuilder.append("/configuration/");
        uriBuilder.append(aIsolateId);

        return new URL(pBrokerBaseUrl.getProtocol(), pBrokerBaseUrl.getHost(),
                pBrokerBaseUrl.getPort(), uriBuilder.toString());
    }
}
