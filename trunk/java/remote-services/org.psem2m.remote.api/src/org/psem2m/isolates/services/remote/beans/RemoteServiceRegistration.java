/**
 * File:   RemoteServiceRegistration.java
 * Author: Thomas Calmant
 * Date:   21 sept. 2011
 */
package org.psem2m.isolates.services.remote.beans;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Constants;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.remote.IRemoteServicesConstants;

/**
 * Contains information about a remote service registration
 * 
 * @author Thomas Calmant
 */
public class RemoteServiceRegistration implements Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** Remote service end points */
    private final Set<EndpointDescription> pEndpoints = new HashSet<EndpointDescription>();

    /** Exported interfaces */
    private String[] pExportedInterfaces;

    /** The host isolate ID */
    private String pHostIsolate;

    /** An ID representing the service */
    private String pServiceId;

    /** Service properties copy */
    private final Map<String, Object> pServiceProperties = new HashMap<String, Object>();

    /**
     * Default constructor
     */
    public RemoteServiceRegistration() {

        // Do nothing
    }

    /**
     * Stores a remote service registration description.
     * 
     * @param aExportedInterface
     *            Interface exported
     * @param aServiceProperties
     *            Service properties (will be copied)
     * @param aEndpoints
     *            End points to access to the service
     */
    public RemoteServiceRegistration(final String aExportedInterface,
            final Map<String, Object> aServiceProperties,
            final EndpointDescription[] aEndpoints) {

        this(new String[] { aExportedInterface }, aServiceProperties,
                aEndpoints);
    }

    /**
     * Stores a remote service registration description.
     * 
     * @param aExportedInterfaces
     *            Interfaces exported
     * @param aServiceProperties
     *            Service properties (will be copied)
     * @param aEndpoints
     *            End points to access to the service
     */
    public RemoteServiceRegistration(final String[] aExportedInterfaces,
            final Map<String, Object> aServiceProperties,
            final Collection<EndpointDescription> aEndpoints) {

        pExportedInterfaces = aExportedInterfaces;
        pServiceProperties.putAll(aServiceProperties);
        pEndpoints.addAll(aEndpoints);

        // Get the current isolate ID
        final String sourceIsolate = System.getProperty(
                IPlatformProperties.PROP_PLATFORM_ISOLATE_ID,
                IRemoteServicesConstants.UNKNOWN_ISOLATE_ID);

        pHostIsolate = sourceIsolate;

        // Generate a service ID
        final StringBuilder builder = new StringBuilder(sourceIsolate);
        builder.append(".");
        builder.append(pServiceProperties.get(Constants.SERVICE_ID));

        pServiceId = builder.toString();
    }

    /**
     * Stores a remote service registration description.
     * 
     * @param aExportedInterfaces
     *            Interfaces exported
     * @param aServiceProperties
     *            Service properties (will be copied)
     * @param aEndpoints
     *            End points to access to the service
     */
    public RemoteServiceRegistration(final String[] aExportedInterfaces,
            final Map<String, Object> aServiceProperties,
            final EndpointDescription[] aEndpoints) {

        this(aExportedInterfaces, aServiceProperties, Arrays.asList(aEndpoints));
    }

    /**
     * Retrieves the end points to access to the service
     * 
     * @return the end points
     */
    public EndpointDescription[] getEndpoints() {

        synchronized (pEndpoints) {
            return pEndpoints
                    .toArray(new EndpointDescription[pEndpoints.size()]);
        }
    }

    /**
     * Retrieves the interfaces exported by the end points
     * 
     * @return the exported interfaces
     */
    public String[] getExportedInterfaces() {

        return pExportedInterfaces;
    }

    /**
     * Retrieves the ID of the isolate exporting the service
     * 
     * @return The ID of the host isolate
     */
    public String getHostIsolate() {

        return pHostIsolate;
    }

    /**
     * Retrieves the service ID, based on the source isolate
     * 
     * @return the service id
     */
    public String getServiceId() {

        return pServiceId;
    }

    /**
     * Retrieves a copy of the service properties at the time of the end points
     * creation.
     * 
     * @return the service properties
     */
    public Map<String, Object> getServiceProperties() {

        return pServiceProperties;
    }

    /**
     * Removes the given end points from the registration.
     * 
     * <b>This method should only be called by a Remote Service Repository</b>.
     * 
     * @param aEndpoints
     *            End points to be removed
     */
    public void removeEndpoints(final EndpointDescription... aEndpoints) {

        if (aEndpoints != null) {

            synchronized (pEndpoints) {
                pEndpoints.removeAll(Arrays.asList(aEndpoints));
            }
        }
    }

    /**
     * @param aEndpoints
     *            the endpoints to set
     */
    public void setEndpoints(final EndpointDescription[] aEndpoints) {

        pEndpoints.clear();

        if (aEndpoints == null) {
            return;
        }

        for (final EndpointDescription endpoint : aEndpoints) {
            pEndpoints.add(endpoint);
        }
    }

    /**
     * @param aExportedInterfaces
     *            the exportedInterfaces to set
     */
    public void setExportedInterfaces(final String[] aExportedInterfaces) {

        pExportedInterfaces = aExportedInterfaces;
    }

    /**
     * @param aHostIsolate
     *            the hostIsolate to set
     */
    public void setHostIsolate(final String aHostIsolate) {

        pHostIsolate = aHostIsolate;

        if (!pServiceProperties.isEmpty()) {
            final StringBuilder builder = new StringBuilder(pHostIsolate);
            builder.append(".");
            builder.append(pServiceProperties.get(Constants.SERVICE_ID));

            pServiceId = builder.toString();
        }
    }

    /**
     * @param aServiceId
     *            the serviceId to set
     */
    public void setServiceId(final String aServiceId) {

        pServiceId = aServiceId;
    }

    /**
     * @param aServiceProperties
     *            the serviceProperties to set
     */
    public void setServiceProperties(
            final Map<String, Object> aServiceProperties) {

        pServiceProperties.clear();
        pServiceProperties.putAll(aServiceProperties);

        if (pHostIsolate != null) {
            final StringBuilder builder = new StringBuilder(pHostIsolate);
            builder.append(".");
            builder.append(pServiceProperties.get(Constants.SERVICE_ID));

            pServiceId = builder.toString();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder(
                "RemoteServiceRegistration(");
        builder.append("serviceId = ").append(pServiceId);
        builder.append(", Endpoints : [");
        for (final EndpointDescription endpoint : pEndpoints) {
            builder.append(endpoint);
        }
        builder.append("])");

        return builder.toString();
    }
}
