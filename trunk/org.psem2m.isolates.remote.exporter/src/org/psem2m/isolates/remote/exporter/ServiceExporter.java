/**
 * File:   ServiceExporter.java
 * Author: Thomas Calmant
 * Date:   26 juil. 2011
 */
package org.psem2m.isolates.remote.exporter;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.commons.remote.EndpointDescription;
import org.psem2m.isolates.commons.remote.IEndpointHandler;
import org.psem2m.isolates.commons.remote.RemoteServiceEvent;
import org.psem2m.isolates.commons.remote.RemoteServiceEvent.ServiceEventType;

/**
 * Tracks services to be exported and uses active handlers to create associated
 * end points
 * 
 * @author Thomas Calmant
 */
public class ServiceExporter extends CPojoBase implements ServiceListener {

    /**
     * The filter to detect exported services only. Test the existence of the
     * service.exported.interfaces and service.exported.configs properties.
     */
    public static final String EXPORTED_SERVICE_FILTER = "(|(service.exported.interfaces=*)(service.exported.configs=*))";

    /** The bundle context */
    private BundleContext pBundleContext;

    /** End point handlers */
    private IEndpointHandler[] pEndpointHandlers;

    /**
     * Base constructor
     * 
     * @param aBundleContext
     *            The bundle context
     */
    public ServiceExporter(final BundleContext aBundleContext) {
	super();
	pBundleContext = aBundleContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.CXObjectBase#destroy()
     */
    @Override
    public void destroy() {
	// ...
    }

    /**
     * Creates all possible end points according to the service properties
     * 
     * @param aServiceReference
     *            Service to be exported
     * @return A list of created end points
     */
    protected List<EndpointDescription> exportService(
	    final ServiceReference aServiceReference) {

	List<EndpointDescription> endpointDescriptions = new ArrayList<EndpointDescription>();

	for (IEndpointHandler handler : pEndpointHandlers) {

	    try {
		EndpointDescription[] endpoints = handler
			.createEndpoint(aServiceReference);

		// Store end points if they are valid
		if (endpoints != null && endpoints.length != 0) {
		    endpointDescriptions.addAll(Arrays.asList(endpoints));
		}

	    } catch (Throwable t) {
		// Log errors
		t.printStackTrace();
	    }
	}

	return endpointDescriptions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() throws BundleException {

	pBundleContext.removeServiceListener(this);
    }

    /**
     * Sends the service notification
     * 
     * @param aServiceReference
     *            A reference to the modified service
     * @param aNewEndpoints
     *            Added end points, in case of registration of a new service
     */
    protected void sendNotification(final ServiceReference aServiceReference,
	    final ServiceEventType aEventType,
	    final List<EndpointDescription> aNewEndpoints) {

	// Safe conversion
	EndpointDescription[] endpointsArray = null;
	if (aNewEndpoints != null) {
	    endpointsArray = aNewEndpoints.toArray(new EndpointDescription[0]);
	}

	// Prepare the transmitted information
	final RemoteServiceEvent serviceEvent = new RemoteServiceEvent(
		aServiceReference, aEventType, endpointsArray);

	// TODO To be replaced by a look into a directory
	String[] test = new String[] { "http://localhost:9000/remote-service-importer" };

	// For each isolate, send a signal
	for (String isolateImporter : test) {

	    try {
		// Try to parse the URL and open a connection
		URL isolateImporterUrl = new URL(isolateImporter);
		URLConnection urlConnection = isolateImporterUrl
			.openConnection();

		if (urlConnection instanceof HttpURLConnection) {

		    // Only handle HTTP streams
		    HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;

		    // POST message
		    httpConnection.setRequestMethod("POST");
		    httpConnection.setUseCaches(false);
		    httpConnection.setDoInput(true);
		    httpConnection.setDoOutput(true);

		    // Raw content-type
		    httpConnection.setRequestProperty("Content-Type",
			    "application/octet-stream");

		    // After fields, before content
		    httpConnection.connect();

		    // Write the event in the request body
		    ObjectOutputStream objectStream = new ObjectOutputStream(
			    httpConnection.getOutputStream());

		    objectStream.writeObject(serviceEvent);
		    objectStream.flush();
		    objectStream.close();

		    // Flush the request
		    httpConnection.getResponseCode();
		    httpConnection.disconnect();
		}

	    } catch (MalformedURLException e) {
		e.printStackTrace();

	    } catch (IOException e) {

		e.printStackTrace();
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.
     * ServiceEvent)
     */
    @Override
    public void serviceChanged(final ServiceEvent aServiceEvent) {

	// Event type, if recognized
	ServiceEventType eventType = null;

	// New export end points
	List<EndpointDescription> newEndpoints = null;

	switch (aServiceEvent.getType()) {
	case ServiceEvent.REGISTERED:

	    // Export service
	    newEndpoints = exportService(aServiceEvent.getServiceReference());
	    if (newEndpoints == null || newEndpoints.isEmpty()) {
		// Error while exporting service
		return;
	    }

	    // Exported service registered
	    eventType = ServiceEventType.REGISTERED;
	    break;

	case ServiceEvent.UNREGISTERING:
	    // Unregistering exported service
	    eventType = ServiceEventType.UNREGISTERED;
	    break;

	case ServiceEvent.MODIFIED_ENDMATCH:
	    // Local service is no longer exported (so unregister it)
	    eventType = ServiceEventType.UNREGISTERED;
	    break;

	case ServiceEvent.MODIFIED:
	    // Service properties have been modified
	    eventType = ServiceEventType.MODIFIED;
	    break;
	}

	if (eventType != null) {
	    // Send the notification if the event is recognized
	    sendNotification(aServiceEvent.getServiceReference(), eventType,
		    newEndpoints);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() throws BundleException {

	// Handle already registered services
	try {
	    ServiceReference[] exportedServices = pBundleContext
		    .getAllServiceReferences(null, EXPORTED_SERVICE_FILTER);

	    for (ServiceReference serviceRef : exportedServices) {
		// Fake event to have the same behavior
		ServiceEvent serviceEvent = new ServiceEvent(
			ServiceEvent.REGISTERED, serviceRef);

		serviceChanged(serviceEvent);
	    }

	} catch (InvalidSyntaxException ex) {
	    ex.printStackTrace();
	}

	// Register a listener for future exported services
	try {
	    pBundleContext.addServiceListener(this, EXPORTED_SERVICE_FILTER);

	} catch (InvalidSyntaxException e) {

	    throw new BundleException(
		    "Error creating the service listener filter", e);
	}
    }
}
