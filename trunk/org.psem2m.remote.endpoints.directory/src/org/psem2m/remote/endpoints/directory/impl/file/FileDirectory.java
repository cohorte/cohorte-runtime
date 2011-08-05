/**
 * 
 */
package org.psem2m.remote.endpoints.directory.impl.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.base.IPlatformDirsSvc;
import org.psem2m.isolates.commons.remote.EndpointDescription;
import org.psem2m.remote.endpoints.directory.IDirectoryContentHandler;
import org.psem2m.remote.endpoints.directory.IEndpointDirectory;
import org.psem2m.remote.endpoints.directory.IEndpointDirectoryListener;
import org.psem2m.utilities.files.CXFile;
import org.psem2m.utilities.files.CXFileDir;

/**
 * Watches and works on the common file containing service end point
 * informations
 * 
 * @author Thomas Calmant
 */
public class FileDirectory extends CPojoBase implements IEndpointDirectory,
		IEndpointDirectoryListener {

	/** End points directory file name, relative to $PSEM2M_BASE */
	public static final String DIRECTORY_FILE_NAME = "var" + File.separator
			+ "services.repository";

	/** Directory content handler */
	private IDirectoryContentHandler pContentHandler;

	/** Directory file watcher thread */
	private FilePoller pFilePoller;

	/** Last known end points list content */
	private List<EndpointDescription> pLastKnownState;

	/** Platform directories service, injected by iPOJO */
	private IPlatformDirsSvc platformDirsSvc;

	/**
	 * Default constructor
	 */
	public FileDirectory() {
		super();
	}

	@Override
	public synchronized void addEndpoint(
			final ServiceReference aServiceReference,
			final EndpointDescription aEndpointDescription) {

		if (aEndpointDescription == null) {
			return;
		}

		try {
			pContentHandler.addEndpoint(aEndpointDescription);

		} catch (IOException e) {
			// TODO Log it
			System.err.println("Error adding endpoint - "
					+ aEndpointDescription);
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		// ...
	}

	@Override
	public synchronized void directoryModified() {

		List<EndpointDescription> newContent;
		try {
			newContent = pContentHandler.getEndpoints();

		} catch (IOException e) {
			// Error reading end points list : file deleted ?
			return;
		}

		// Look for new end points
		List<EndpointDescription> addedEndpoints = new ArrayList<EndpointDescription>();
		for (EndpointDescription endpoint : newContent) {
			if (!pLastKnownState.contains(endpoint)) {
				addedEndpoints.add(endpoint);
			}
		}

		// Look for removed end points
		List<EndpointDescription> removedEndpoints = new ArrayList<EndpointDescription>();
		for (EndpointDescription endpoint : pLastKnownState) {
			if (!newContent.contains(endpoint)) {
				removedEndpoints.add(endpoint);
			}
		}

		// Update last known state
		pLastKnownState = newContent;

		System.out.println("The directory has been modified - "
				+ addedEndpoints.size() + " new end points, "
				+ removedEndpoints.size() + " removed end points");

		// TODO propagate the modifications
	}

	@Override
	public synchronized EndpointDescription[] findEndpoints(
			final String aInterfaceName) {

		if (aInterfaceName == null) {
			return new EndpointDescription[0];
		}

		// Read the end points list
		final List<EndpointDescription> endpointsList;
		try {
			endpointsList = pContentHandler.getEndpoints();

		} catch (IOException e) {
			// TODO log error
			System.err.println("Error reading endpoints list to find "
					+ aInterfaceName);
			e.printStackTrace();

			// Do not return null
			return new EndpointDescription[0];
		}

		// Find the end points exporting the interface
		final List<EndpointDescription> matchingEndpoints = new ArrayList<EndpointDescription>();
		for (EndpointDescription endpoint : endpointsList) {

			final String[] exportedInterfaces = endpoint
					.getExportedInterfaces();
			for (String exportedInterface : exportedInterfaces) {

				if (aInterfaceName.equals(exportedInterface)) {
					matchingEndpoints.add(endpoint);
					break;
				}
			}
		}

		// Convert the list into an array
		return matchingEndpoints.toArray(new EndpointDescription[0]);
	}

	@Override
	public void invalidatePojo() throws BundleException {
		// do nothing...
		pFilePoller.interrupt();
		System.out.println("RSR Gone");
	}

	@Override
	public synchronized void removeEndpoint(
			final EndpointDescription aEndpointDescription) {

		if (aEndpointDescription == null) {
			return;
		}

		try {
			pContentHandler.removeEndpoint(aEndpointDescription);

		} catch (IOException e) {
			// TODO log error
			System.err.println("Error removing endpoint - "
					+ aEndpointDescription);
			e.printStackTrace();
		}
	}

	@Override
	public void validatePojo() throws BundleException {

		CXFileDir baseDir = platformDirsSvc.getPlatformBaseDir();
		CXFile repositoryFile = new CXFile(baseDir, DIRECTORY_FILE_NAME);

		pContentHandler = new FileContentHandler(repositoryFile);

		// Try to store the initial state
		try {
			pLastKnownState = pContentHandler.getEndpoints();

		} catch (IOException e) {
			// No file yet ?
			pLastKnownState = new ArrayList<EndpointDescription>();
		}

		pFilePoller = new FilePoller(repositoryFile, 1000);
		pFilePoller.addDirectoryListener(this);
		pFilePoller.start();

		System.out.println("RSR Created with file : " + repositoryFile);
	}
}
