/**
 * 
 */
package org.psem2m.remote.endpoints.directory;

import org.osgi.framework.ServiceReference;
import org.psem2m.isolates.commons.remote.EndpointDescription;

/**
 * Describes an end point directory
 * 
 * @author Thomas Calmant
 */
public interface IEndpointDirectory {

	/**
	 * Adds the given end point to the directory
	 * 
	 * @param aServiceReference
	 *            Exported service reference
	 * @param aEndpointDescription
	 *            Description of the end point to be registered
	 */
	void addEndpoint(ServiceReference aServiceReference,
			EndpointDescription aEndpointDescription);

	/**
	 * Finds the end points exporting to the given interface. Excludes locally
	 * created end points (with same isolate ID)
	 * 
	 * @param aInterfaceName
	 *            Name of the interface to search for
	 * @return All corresponding end points
	 */
	EndpointDescription[] findEndpoints(String aInterfaceName);

	/**
	 * Removes the given end point of the directory
	 * 
	 * @param aEndpointDescription
	 *            Description of the end point to be removed
	 */
	void removeEndpoint(EndpointDescription aEndpointDescription);
}
