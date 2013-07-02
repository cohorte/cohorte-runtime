/**
 * File:   StorageServlet.java
 * Author: Thomas Calmant
 * Date:   28 juin 2013
 */
package org.cohorte.remote.multicast;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cohorte.remote.IRemoteServiceRepository;
import org.cohorte.remote.beans.EndpointDescription;
import org.cohorte.remote.beans.RemoteServiceRegistration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Constants;

/**
 * The servlet that publishes the content of the exported services registry
 * 
 * @author Thomas Calmant
 */
public class RegistryServlet extends HttpServlet {

    /** JSON mime-type */
    private static final String JSON_TYPE = "application/json";

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** The isolate UID */
    private final String pIsolateUID;

    /** The exported services repository */
    private final IRemoteServiceRepository pRepository;

    /**
     * Sets up members
     */
    public RegistryServlet(final String aIsolateUID,
            final IRemoteServiceRepository aRepository) {

        pIsolateUID = aIsolateUID;
        pRepository = aRepository;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
     * , javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(final HttpServletRequest aReq,
            final HttpServletResponse aResp) throws ServletException,
            IOException {

        // Get the path given after the servlet path
        final String extra = aReq.getPathInfo();
        if (extra == null) {
            // No order given
            aResp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid request path");
            return;
        }

        // Split it (extra will start with a '/')
        final String[] parts = extra.substring(1).split("/");
        if (parts[0].equals("endpoint")) {
            // /endpoint/<uid>
            if (parts.length < 2) {
                // Missing the UID
                aResp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Endpoint UID is missing");
                return;

            } else {
                // Send the response
                sendEndpointDict(aResp, parts[1]);
            }

        } else if (parts[0].equals("endpoints")) {
            // /endpoints
            sendEndpoints(aResp);

        } else {
            // Unknown path
            aResp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
    }

    /**
     * Converts a Cohorte Remote Services registration bean to a Pelix remote
     * services end point
     * 
     * @param aRegistration
     *            A remote service registration
     * @return The corresponding Pelix representation
     */
    private Map<String, Object> registrationToMap(
            final RemoteServiceRegistration aRegistration) {

        // Find a JSON-RPC end point
        EndpointDescription foundEndpoint = null;
        for (final EndpointDescription endpoint : aRegistration.getEndpoints()) {
            if (endpoint.getExportedConfig().contains("json")) {
                foundEndpoint = endpoint;
                break;
            }
        }

        if (foundEndpoint == null) {
            // No JSON end point, try the first one
            foundEndpoint = aRegistration.getEndpoints()[0];
        }

        // Filter the properties (remove the specifications)
        final Map<String, Object> properties = new LinkedHashMap<String, Object>(
                aRegistration.getServiceProperties());
        properties.remove(Constants.OBJECTCLASS);

        // Prepare the end point map
        final Map<String, Object> endpoint = new LinkedHashMap<String, Object>();

        // Found in the registration...
        endpoint.put("sender", pIsolateUID);
        endpoint.put("uid", aRegistration.getServiceId());
        endpoint.put("specifications", aRegistration.getExportedInterfaces());
        endpoint.put("properties", properties);

        // Found in the end point
        endpoint.put("kind", foundEndpoint.getExportedConfig());
        endpoint.put("name", foundEndpoint.getEndpointName());

        // Forge the URL
        final String url = String.format("%s://{server}:%d%s",
                foundEndpoint.getProtocol(), foundEndpoint.getPort(),
                foundEndpoint.getEndpointUri());
        endpoint.put("url", url);

        return endpoint;
    }

    /**
     * Sends the representation of the end point matching the given ID
     * 
     * @param aResp
     *            Servlet response
     * @param aRegistrationUID
     *            The UID of the registration (the service ID)
     * @throws IOException
     *             Error writing to the client
     */
    private void sendEndpointDict(final HttpServletResponse aResp,
            final String aRegistrationUID) throws IOException {

        // The requested one
        RemoteServiceRegistration requested = null;

        // Get all registrations
        final RemoteServiceRegistration[] regBeans = pRepository
                .getLocalRegistrations();
        for (final RemoteServiceRegistration registration : regBeans) {
            if (registration.getServiceId().equals(aRegistrationUID)) {
                // Found it !
                requested = registration;
                break;
            }
        }

        if (requested == null) {
            // Unknown ID
            aResp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Unknown end point ID: " + aRegistrationUID);
            return;
        }

        // Convert the object to a map
        final Map<String, Object> regMap = registrationToMap(requested);

        // Convert to JSON
        final JSONObject jsonContent = new JSONObject(regMap);

        // Send
        sendJson(aResp, jsonContent.toString());
    }

    /**
     * Sends the whole content of the repository
     * 
     * @param aResp
     *            Servlet response
     * @throws IOException
     *             Error writing to the client
     */
    private void sendEndpoints(final HttpServletResponse aResp)
            throws IOException {

        // Get our registrations
        final RemoteServiceRegistration[] regBeans = pRepository
                .getLocalRegistrations();

        // Convert the objects to maps
        final List<Object> regMaps = new LinkedList<Object>();
        for (final RemoteServiceRegistration registration : regBeans) {
            regMaps.add(registrationToMap(registration));
        }

        // Convert to JSON
        final JSONArray jsonContent = new JSONArray(regMaps);

        // Send
        sendJson(aResp, jsonContent.toString());
    }

    /**
     * Writes a JSON response
     * 
     * @param aResp
     *            Servlet response
     * @param aJsonString
     *            JSON string
     * @throws IOException
     *             Error writing to the client
     */
    private void sendJson(final HttpServletResponse aResp,
            final String aJsonString) throws IOException {

        // Setup headers
        aResp.setStatus(HttpServletResponse.SC_OK);
        aResp.setContentType(JSON_TYPE);
        aResp.setContentLength(aJsonString.length());

        // Write the content
        final PrintWriter writer = aResp.getWriter();
        writer.print(aJsonString);
        writer.flush();
    }
}
