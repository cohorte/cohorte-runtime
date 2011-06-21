/**
 * File:   Utilities.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.commons;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.psem2m.isolates.commons.impl.BundleRef;

/**
 * Various utility methods
 * 
 * @author Thomas Calmant
 */
public class Utilities {

    /**
     * Searches for a bundle according to the given possible names. It looks in
     * the local directory, then in platform repository if needed.
     * 
     * @param aPlatformConfiguration
     *            Platform configuration
     * @param aPossibleNames
     *            Possible file names for the bundle
     * @return A file reference to the bundle, null if not found
     */
    public static IBundleRef findBundle(
	    final IPlatformConfiguration aPlatformConfiguration,
	    final String... aPossibleNames) {

	if (aPlatformConfiguration == null || aPossibleNames == null
		|| aPossibleNames.length == 0) {
	    return null;
	}

	final String repository = aPlatformConfiguration
		.getRepositoryDirectory() + File.separator;

	for (String name : aPossibleNames) {

	    // Test 'local' File
	    File bundleFile = new File(name);
	    if (bundleFile.exists()) {
		return new BundleRef(name, bundleFile);
	    }

	    // Test 'repository' file
	    bundleFile = new File(repository + name);
	    if (bundleFile.exists()) {
		return new BundleRef(name, bundleFile);
	    }

	    // Test URI
	    try {
		URI bundleUri = new URI(name);
		URL bundleUrl = bundleUri.toURL();

		if (bundleUrl.getProtocol().equals("file")) {

		    bundleFile = new File(bundleUri.getPath());
		    if (bundleFile.exists()) {
			return new BundleRef(name, bundleFile);
		    }
		}

	    } catch (MalformedURLException e) {
		// Do nothing, we're determining the kind of element
	    } catch (URISyntaxException e) {
		// Do nothing, we're determining the kind of element
	    }
	}

	// Bundle not found
	return null;
    }

    /**
     * Objects joining operation, using the {@link String#valueOf(Object)}
     * method.
     * 
     * @param aJoinSequence
     *            Sequence between strings
     * @param aJoinedObjects
     *            Objects to be joined
     * @return Joined strings
     */
    public static String join(final String aJoinSequence,
	    final Object[] aJoinedObjects) {

	StringBuilder builder = new StringBuilder();
	boolean first = true;

	for (Object object : aJoinedObjects) {

	    if (!first) {
		builder.append(aJoinSequence);
	    } else {
		first = false;
	    }

	    builder.append(String.valueOf(object));
	}

	return builder.toString();
    }

    /**
     * String joining operation, like in Python str.join().
     * 
     * @param aJoinSequence
     *            Sequence between strings
     * @param aJoinedStrings
     *            Objects to be joined
     * @return Joined strings
     */
    public static String join(final String aJoinSequence,
	    final String... aJoinedStrings) {

	StringBuilder builder = new StringBuilder();
	boolean first = true;

	for (String string : aJoinedStrings) {

	    if (!first) {
		builder.append(aJoinSequence);
	    } else {
		first = false;
	    }

	    builder.append(string);
	}

	return builder.toString();
    }

    /**
     * Returns a valid directory from the given path (it may return the parent
     * directory).
     * 
     * @param aPath
     *            base directory path
     * @return A valid directory path
     * 
     * @throws IOException
     *             The directory already exists or can't be created
     */
    public static File makeDirectory(final CharSequence aPath)
	    throws IOException {

	File directory = new File(aPath.toString());

	if (!directory.exists()) {
	    // Create directory if needed
	    if (!directory.mkdirs()) {
		throw new IOException(
			"Directory not created. Already existing ?");
	    }

	} else if (!directory.isDirectory()) {
	    // A node already has this name
	    throw new IOException("'" + aPath + "' is not a valid directory.");
	}

	return directory;
    }
}
