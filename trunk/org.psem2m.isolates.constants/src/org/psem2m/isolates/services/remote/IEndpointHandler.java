/**
 * File:   IEndpointHandler.java
 * Author: Thomas Calmant
 * Date:   27 juil. 2011
 */
package org.psem2m.isolates.services.remote;

import org.osgi.framework.ServiceReference;
import org.psem2m.isolates.services.remote.beans.EndpointDescription;

/**
 * Represents an end point handler
 * 
 * @author Thomas Calmant
 */
public interface IEndpointHandler {

    /**
     * Create all end points needed for the specified service
     * 
     * @param aExportedInterface
     *            Interface to export from the given service
     * @param aServiceReference
     *            A reference to the service to be exported
     * @return A description of all created end points, null on error
     */
    EndpointDescription[] createEndpoint(String aExportedInterface,
            ServiceReference aServiceReference);

    /**
     * Destroys the end point(s) associated to the given service
     * 
     * @param aServiceReference
     *            A service reference
     * @return True on success
     */
    boolean destroyEndpoint(ServiceReference aServiceReference);

    /**
     * Retrieves all end points associated to the given service reference
     * 
     * @param aServiceReference
     *            A reference to the exported service
     * @return All associated end points, or an empty array (never null)
     */
    EndpointDescription[] getEndpoints(ServiceReference aServiceReference);
}
