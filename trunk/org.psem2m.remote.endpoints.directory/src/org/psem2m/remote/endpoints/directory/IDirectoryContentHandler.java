package org.psem2m.remote.endpoints.directory;

import java.io.IOException;
import java.util.List;

import org.psem2m.isolates.commons.remote.EndpointDescription;

public interface IDirectoryContentHandler {

	/**
	 * Adds an end point to the file
	 * 
	 * @param aEndpointDescription
	 *            End point to be added
	 * @throws IOException
	 *             An error occurred while writing the directory
	 */
	void addEndpoint(EndpointDescription aEndpointDescription)
			throws IOException;

	/**
	 * Retrieves all end points stored in the directory
	 * 
	 * @return All end points stored in the directory, never null
	 * @throws IOException
	 *             An error occurred while reading the directory
	 */
	List<EndpointDescription> getEndpoints() throws IOException;

	/**
	 * Removes an end point to the file
	 * 
	 * @param aEndpointDescription
	 *            End point to be removed
	 * @throws IOException
	 *             An error occurred while writing the directory
	 */
	void removeEndpoint(EndpointDescription aEndpointDescription)
			throws IOException;
}