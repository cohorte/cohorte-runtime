/**
 * 
 */
package org.psem2m.remote.endpoints.directory.impl.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.psem2m.remote.endpoints.directory.IEndpointDirectoryListener;

/**
 * Thread that watches for file modifications
 * 
 * @author Thomas Calmant
 */
public class FilePoller extends Thread {

	/** File to poll */
	private final File pFile;

	/** List of event listeners */
	private final List<IEndpointDirectoryListener> pListeners = new ArrayList<IEndpointDirectoryListener>();

	/** Poll interval */
	private final long pPollInterval;

	/**
	 * Sets up the file poller
	 * 
	 * @param aFile
	 *            File to poll
	 * @param aPollInterval
	 *            Poll interval (in milliseconds)
	 */
	public FilePoller(final File aFile, final long aPollInterval) {

		super();
		pFile = aFile;
		pPollInterval = aPollInterval;
	}

	/**
	 * Subscribes the given listener to directory events
	 * 
	 * @param aListener
	 *            The listener to be added
	 */
	public void addDirectoryListener(final IEndpointDirectoryListener aListener) {

		if (aListener == null) {
			return;
		}

		synchronized (pListeners) {
			pListeners.add(aListener);
		}
	}

	/**
	 * Notifies all registered listeners that an event occurred
	 */
	protected void notifyListeners() {

		synchronized (pListeners) {
			for (IEndpointDirectoryListener listener : pListeners) {
				listener.directoryModified();
			}
		}
	}

	/**
	 * Subscribes the given listener to directory events
	 * 
	 * @param aListener
	 *            The listener to be added
	 */
	public void removeDirectoryListener(
			final IEndpointDirectoryListener aListener) {

		if (aListener == null) {
			return;
		}

		synchronized (pListeners) {
			pListeners.remove(aListener);
		}
	}

	@Override
	public void run() {

		long lastModification = pFile.lastModified();

		while (!isInterrupted()) {

			try {
				Thread.sleep(pPollInterval);

			} catch (InterruptedException e) {
				// Let the last test be done, then the loop will be stopped
			}

			long newModification = pFile.lastModified();
			if (newModification > lastModification) {
				// Update info and notify listeners
				lastModification = newModification;
				notifyListeners();
			}
		}

		// Clear the listeners list (no more usage of this thread)
		pListeners.clear();
	}
}
