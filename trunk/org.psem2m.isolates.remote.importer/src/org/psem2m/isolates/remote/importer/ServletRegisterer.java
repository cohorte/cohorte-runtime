/**
 * File:   ServletRegisterer.java
 * Author: Thomas Calmant
 * Date:   25 juil. 2011
 */
package org.psem2m.isolates.remote.importer;

import javax.servlet.ServletException;

import org.osgi.framework.BundleException;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.remote.IRemoteServiceEventListener;
import org.psem2m.isolates.services.remote.beans.RemoteServiceEvent;

/**
 * Remote Service Importer main component
 * 
 * @author Thomas Calmant
 */
public class ServletRegisterer extends CPojoBase implements
	IRemoteServiceEventListener {

    /** The servlet alias constant */
    public static String SERVLET_ALIAS = "/remote-service-importer";

    /** OSGi HTTP Service, injected by iPOJO */
    private HttpService pHttpService;

    /** OSGi log service, injected by iPOJO */
    private LogService pLogger;

    /** Remote service listeners, injected by iPOJO */
    private IRemoteServiceEventListener[] pRemoteServiceListeners;

    /**
     * Default constructor
     */
    public ServletRegisterer() {
	super();
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

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.remote.IRemoteServiceEventListener#
     * handleRemoteEvent(org.psem2m.isolates.commons.remote.RemoteServiceEvent)
     */
    @Override
    public void handleRemoteEvent(final RemoteServiceEvent aServiceEvent) {

	for (IRemoteServiceEventListener listener : pRemoteServiceListeners) {
	    try {
		listener.handleRemoteEvent(aServiceEvent);

	    } catch (Throwable t) {
		// Just log errors
		pLogger.log(LogService.LOG_WARNING,
			"Error notifying a remote service listener", t);
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() throws BundleException {
	// May be a dummy call...
	pHttpService.unregister(SERVLET_ALIAS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() throws BundleException {

	try {
	    // Register the servlet
	    pHttpService.registerServlet(SERVLET_ALIAS, new ListeningServlet(
		    this), null, null);

	} catch (ServletException e) {
	    pLogger.log(LogService.LOG_ERROR,
		    "Error registering the RSI servlet", e);

	    throw new BundleException("Error registering the RSI servlet", e);

	} catch (NamespaceException e) {
	    pLogger.log(LogService.LOG_ERROR,
		    "Error registering the RSI servlet", e);

	    throw new BundleException("Error registering the RSI servlet", e);
	}
    }
}
