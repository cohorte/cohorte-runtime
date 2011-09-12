/**
 * File:   RemoteServiceEvent.java
 * Author: Thomas Calmant
 * Date:   26 juil. 2011
 */
package org.psem2m.isolates.services.remote.beans;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.psem2m.isolates.constants.IPlatformProperties;

/**
 * @author Thomas Calmant
 */
public class RemoteServiceEvent implements Serializable {

	/**
	 * Kind of service event
	 * 
	 * @author Thomas Calmant
	 */
	public enum ServiceEventType {

		/** The service properties have been modified */
		MODIFIED,

		/** The service have just been registered */
		REGISTERED,

		/** The service have just been unregistered */
		UNREGISTERED,
	}

	/** The serializable version UID */
	private static final long serialVersionUID = 1L;

	/** Unknown isolate ID constant */
	public static final String UNKNOWN_ISOLATE_ID = "unknown";

	/** End points available to access this service */
	private final EndpointDescription[] pEndpoints;

	/** Service event type */
	private final ServiceEventType pEventType;

	/** Service interfaces names */
	private final String[] pInterfacesNames;

	/** An ID representing the service */
	private final String pServiceId;

	/** Service properties */
	private final Dictionary<String, Object> pServiceProperties;

	/** Source isolate ID */
	private final String pSourceIsolate;

	/**
	 * Sets up the event
	 * 
	 * @param aClass
	 *            Service interface class (can't be null)
	 * @param aProperties
	 *            Service properties (can be null)
	 * @param aEventType
	 *            The service event type
	 */
	public RemoteServiceEvent(final Class<?> aClass,
			final Dictionary<String, Object> aProperties,
			final ServiceEventType aEventType) {

		this(new String[] { aClass.getName() }, aProperties, aEventType, null);
	}

	/**
	 * Sets up the event
	 * 
	 * @param aClass
	 *            Service interface class (can't be null)
	 * @param aEventType
	 *            The service event type
	 */
	public RemoteServiceEvent(final Class<?> aClass,
			final ServiceEventType aEventType) {

		this(aClass.getName(), aEventType);
	}

	/**
	 * Sets up the event
	 * 
	 * @param aServiceReference
	 *            A service reference
	 * @param aEventType
	 *            The service event type
	 */
	public RemoteServiceEvent(final ServiceReference aServiceReference,
			final ServiceEventType aEventType,
			final EndpointDescription[] aEndpoints) {

		// String array, as defined in OSGi specs
		pInterfacesNames = (String[]) aServiceReference
				.getProperty(Constants.OBJECTCLASS);
		pEventType = aEventType;
		pSourceIsolate = System.getProperty(
				IPlatformProperties.PROP_PLATFORM_ISOLATE_ID,
				UNKNOWN_ISOLATE_ID);
		pEndpoints = aEndpoints;

		// Look after service properties
		pServiceProperties = new Hashtable<String, Object>();
		for (String propertyKey : aServiceReference.getPropertyKeys()) {
			pServiceProperties.put(propertyKey,
					aServiceReference.getProperty(propertyKey));
		}

		// Generate an ID
		StringBuilder builder = new StringBuilder();
		builder.append(pSourceIsolate);
		builder.append(".");
		builder.append(pServiceProperties.get(Constants.SERVICE_ID));

		pServiceId = builder.toString();
	}

	/**
	 * Sets up the event
	 * 
	 * @param aServiceRegistration
	 *            A service registration reference
	 * @param aEventType
	 *            The service event type
	 */
	public RemoteServiceEvent(final ServiceRegistration aServiceRegistration,
			final ServiceEventType aEventType) {

		this(aServiceRegistration.getReference(), aEventType, null);
	}

	/**
	 * Sets up the event
	 * 
	 * @param aClassName
	 *            Service interface name (can't be null)
	 * @param aEventType
	 *            The service event type
	 */
	public RemoteServiceEvent(final String aClassName,
			final ServiceEventType aEventType) {

		this(new String[] { aClassName }, null, aEventType, null);
	}

	/**
	 * Sets up the event
	 * 
	 * @param aInterfaces
	 *            Service interfaces names (can't be null nor empty)
	 * @param aProperties
	 *            Service properties (can be null)
	 * @param aEventType
	 *            The service event type
	 */
	public RemoteServiceEvent(final String[] aInterfaces,
			final Dictionary<String, Object> aProperties,
			final ServiceEventType aEventType,
			final EndpointDescription[] aEndpoints) {

		pInterfacesNames = aInterfaces;
		pServiceProperties = aProperties;
		pEventType = aEventType;
		pSourceIsolate = System.getProperty(
				IPlatformProperties.PROP_PLATFORM_ISOLATE_ID,
				UNKNOWN_ISOLATE_ID);
		pEndpoints = aEndpoints;

		String serviceId = null;

		// Try to use service properties
		if (aProperties != null) {
			// Try to find the service ID, if any
			Long rawServiceId = (Long) aProperties.get(Constants.SERVICE_ID);
			if (rawServiceId != null) {
				serviceId = rawServiceId.toString();
			}
		}

		// If the service ID is not found, use the exported interfaces
		if (serviceId == null) {
			serviceId = Arrays.toString(aInterfaces);
		}

		pServiceId = pSourceIsolate + "." + serviceId;
	}

	/**
	 * Sets up the event
	 * 
	 * @param aClassName
	 *            Service interfaces names (can't be null nor empty)
	 * @param aEventType
	 *            The service event type
	 */
	public RemoteServiceEvent(final String[] aInterfaces,
			final ServiceEventType aEventType) {

		this(aInterfaces, null, aEventType, null);
	}

	/**
	 * Retrieves available end points to access this service
	 * 
	 * @return end points to access this service
	 */
	public EndpointDescription[] getEndpoints() {
		return pEndpoints;
	}

	/**
	 * Retrieves the kind of event
	 * 
	 * @return the kind of event
	 */
	public ServiceEventType getEventType() {
		return pEventType;
	}

	/**
	 * Retrieves the service interfaces names
	 * 
	 * @return the service interfaces names
	 */
	public String[] getInterfacesNames() {
		return pInterfacesNames;
	}

	/**
	 * Retrieves the service identifier, based on the source isolate and the
	 * 'local' service ID (or exported interfaces).
	 * 
	 * @return The service identifier
	 */
	public String getServiceId() {
		return pServiceId;
	}

	/**
	 * Retrieves the service properties, can return null.
	 * 
	 * @return the properties of the associated service
	 */
	public Dictionary<String, Object> getServiceProperties() {
		return pServiceProperties;
	}

	/**
	 * Retrieves the ID of the source isolate or "unknown"
	 * 
	 * @return The ID of the source isolate
	 */
	public String getSourceIsolateId() {
		return pSourceIsolate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append("RemoteServiceEvent(");
		builder.append("Source=").append(pSourceIsolate);
		builder.append(", ID=").append(pServiceId);
		builder.append(", Event=").append(pEventType);
		builder.append(", Endpoint.count=");

		if (pEndpoints == null) {
			builder.append("<null>");
		} else {
			builder.append(pEndpoints.length);
		}

		builder.append(")");

		return builder.toString();
	}
}
