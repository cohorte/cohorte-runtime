/**
 * File:   IEndpointHandler.java
 * Author: Thomas Calmant
 * Date:   27 juil. 2011
 */
package org.psem2m.isolates.commons.remote;

import org.osgi.framework.ServiceReference;

/**
 * Represents a end point handler
 * 
 * @author Thomas Calmant
 */
public interface IEndpointHandler {

    /**
     * Create all end points needed for the specified service
     * 
     * @param aServiceReference
     *            A reference to the service to be exported
     * @return A description of all created end points, null on error
     */
    public abstract EndpointDescription[] createEndpoint(
	    final ServiceReference aServiceReference);

    /**
     * Destroys the end point(s) associated to the given service
     * 
     * @param aServiceReference
     *            A service reference
     * @return
     */
    public abstract boolean destroyEndpoint(
	    final ServiceReference aServiceReference);

}