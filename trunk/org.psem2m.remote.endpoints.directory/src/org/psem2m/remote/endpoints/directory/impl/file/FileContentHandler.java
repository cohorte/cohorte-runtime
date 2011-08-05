/**
 * 
 */
package org.psem2m.remote.endpoints.directory.impl.file;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.psem2m.isolates.commons.remote.EndpointDescription;
import org.psem2m.remote.endpoints.directory.IDirectoryContentHandler;

/**
 * Abstract the file directory content format.
 * 
 * TODO: transform the internal format in something better (JSON, ...)
 * 
 * @author Thomas Calmant
 */
public class FileContentHandler implements IDirectoryContentHandler {

	/** The file to be used */
	private final File pFile;

	/**
	 * Creates the file content handler
	 * 
	 * @param aFile
	 *            File to be used
	 */
	public FileContentHandler(final File aFile) {
		pFile = aFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.remote.endpoints.directory.impl.file.IDirectoryContentHandler
	 * #addEndpoint(org.psem2m.isolates.commons.remote.EndpointDescription)
	 */
	@Override
	public void addEndpoint(final EndpointDescription aEndpointDescription)
			throws IOException {

		if (aEndpointDescription == null) {
			// Avoid useless locks, etc
			return;
		}

		List<EndpointDescription> endpointsList = getEndpoints();
		endpointsList.add(aEndpointDescription);

		writeContent(endpointsList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.remote.endpoints.directory.impl.file.IDirectoryContentHandler
	 * #getEndpoints()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public synchronized List<EndpointDescription> getEndpoints()
			throws IOException {

		List<EndpointDescription> resultList = new ArrayList<EndpointDescription>();

		if (pFile.exists()) {

			InputStream fileStream = null;
			ObjectInputStream objectStream = null;

			// Lock it
			FileChannel fileChannel = new RandomAccessFile(pFile, "r")
					.getChannel();

			// Lock is automatically released when stream is closed
			/* FileLock lock = */
			fileChannel.lock();

			try {
				fileStream = Channels.newInputStream(fileChannel);
				objectStream = new ObjectInputStream(fileStream);

				try {
					Object readObject = objectStream.readObject();

					if (readObject instanceof List<?>) {
						resultList
								.addAll((Collection<? extends EndpointDescription>) readObject);
					}

				} catch (ClassNotFoundException e) {
					// TODO log error / re-throw the exception ?
					System.err.println("Can't deserialize data");
					e.printStackTrace();
				}

			} finally {
				// Close streams
				safeClose(objectStream);

				// Closing this stream will close the associated channel,
				// therefore
				// the associated lock (see the javadocs...)
				safeClose(fileStream);
			}
		}

		return resultList;
	}

	@Override
	public void removeEndpoint(final EndpointDescription aEndpointDescription)
			throws IOException {

		if (aEndpointDescription == null) {
			// Avoid useless locks, etc
			return;
		}

		List<EndpointDescription> endpointsList = getEndpoints();

		if (endpointsList.remove(aEndpointDescription)) {
			// Only write content if the list was modified
			writeContent(endpointsList);
		}
	}

	/**
	 * Safely closes the given stream
	 * 
	 * @param aCloseable
	 *            Stream to close
	 */
	protected void safeClose(final Closeable aCloseable) {

		if (aCloseable == null) {
			return;
		}

		try {
			aCloseable.close();
		} catch (IOException e) {
			// Do nothing...
		}
	}

	/**
	 * Writes the given list to the directory content file. Sets up locking,
	 * etc.
	 * 
	 * @param aEndpointsList
	 *            List to be stored
	 * @throws IOException
	 *             An error occurred while writing into the file
	 */
	protected synchronized void writeContent(
			final List<EndpointDescription> aEndpointsList) throws IOException {

		// Prepare the file
		pFile.createNewFile();

		// Lock it
		FileChannel fileChannel = new RandomAccessFile(pFile, "w").getChannel();

		// Lock is automatically released when stream is closed
		/* FileLock lock = */
		fileChannel.lock();

		// Store the new list
		OutputStream fileStream = null;
		ObjectOutputStream objectStream = null;

		try {
			// Open stream
			fileStream = Channels.newOutputStream(fileChannel);
			objectStream = new ObjectOutputStream(fileStream);

			// Write the list
			objectStream.writeObject(aEndpointsList);

		} finally {
			// Close streams
			safeClose(objectStream);

			// Closing this stream will close the associated channel, therefore
			// the associated lock (see the javadocs...)
			safeClose(fileStream);
		}
	}
}
