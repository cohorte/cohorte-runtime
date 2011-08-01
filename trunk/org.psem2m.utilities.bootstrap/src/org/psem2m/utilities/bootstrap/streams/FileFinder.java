/**
 * File:   CBundleFinderSvc.java
 * Author: Thomas Calmant
 * Date:   28 juil. 2011
 */
package org.psem2m.utilities.bootstrap.streams;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Implements the bundle finder, using the platform directories service
 * 
 * @author Thomas Calmant
 */
public class FileFinder {

	/** Bundle symbolic name Manifest attribute name */
	public static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";

	/**
	 * contains the absolute path of the BASE folder of the current application
	 */
	public final static String PROP_PLATFORM_BASE = "org.psem2m.platform.base";

	/**
	 * contains the absolute path of the HOME folder of the current application
	 */
	public final static String PROP_PLATFORM_HOME = "org.psem2m.platform.home";

	/** Platform base and home repository directory name */
	public static final String REPOSITORY_NAME = "repo";

	/** Jar files list */
	private final Map<String, File> pBundlesCache = new HashMap<String, File>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.base.bundles.IBundleFinderSvc#findBundle(java.lang
	 * .String[])
	 */
	public File find(final String... aFilePossibleNames) {

		// Look in each repository
		for (File repository : getRepositories()) {

			if (!repository.exists() || !repository.isDirectory()) {
				continue;
			}

			// Look for each possible name
			for (String bundleName : aFilePossibleNames) {

				File bundleFile = new File(repository, bundleName);
				if (bundleFile.exists()) {
					// Stop on first bundle found
					return bundleFile;
				}
			}
		}

		// Bundle not found in repositories, tries the names as full ones
		for (String fileName : aFilePossibleNames) {

			// Try 'local' file
			File bundleFile = new File(fileName);
			if (bundleFile.exists()) {
				// Stop on first bundle found
				return bundleFile;
			}

			// Try as a URI
			try {
				URI bundleUri = new URI(fileName);
				URL bundleUrl = bundleUri.toURL();

				if (bundleUrl.getProtocol().equals("file")) {

					bundleFile = new File(bundleUri.getPath());
					if (bundleFile.exists()) {
						return bundleFile;
					}
				}

			} catch (MalformedURLException e) {
				// Do nothing, we're determining the kind of element
			} catch (URISyntaxException e) {
				// Do nothing, we're determining the kind of element
			} catch (IllegalArgumentException e) {
				// Do nothing, the URI is not absolute
			}
		}

		return null;
	}

	/**
	 * Finds all bundles in the known repositories and stores their path and
	 * symbolic names
	 */
	protected synchronized void findAllBundles() {

		// Clear existing cache
		pBundlesCache.clear();

		// Do the job
		for (File repository : getRepositories()) {
			findAllBundles(repository);
		}
	}

	/**
	 * Completes the given list with all files having the extension ".jar"
	 * 
	 * @param aRootDirectory
	 *            Root search directory
	 */
	protected void findAllBundles(final File aRootDirectory) {

		File[] repoFiles = aRootDirectory.listFiles();
		if (repoFiles != null) {

			for (File file : repoFiles) {

				if (file.isFile()
						&& file.getName().toLowerCase().endsWith(".jar")) {
					// Try to read it
					String symbolicName = getBundleSymbolicName(file);
					if (symbolicName != null) {
						// Store it
						pBundlesCache.put(symbolicName, file);
					}

				} else {
					// Recursive search
					findAllBundles(file);
				}
			}
		}
	}

	/**
	 * Tries to find the first bundle with the given symbolic name
	 * 
	 * @param aSymbolicName
	 *            A bundle symbolic name
	 * @return The first corresponding file found, null if none found
	 */
	public File findBundle(final String aSymbolicName) {

		if (aSymbolicName == null) {
			return null;
		}

		// Be sure we have some files to look into
		if (pBundlesCache.isEmpty()) {
			findAllBundles();
		}

		return pBundlesCache.get(aSymbolicName);
	}

	/**
	 * Retrieves the bundle symbolic name from the manifest of the given file,
	 * null if unreadable
	 * 
	 * @param file
	 *            A bundle Jar file
	 * @return The bundle symbolic name, null if unreadable
	 */
	public String getBundleSymbolicName(final File file) {

		if (!file.exists() || !file.isFile()) {
			// Ignore invalid files
			return null;
		}

		try {
			JarFile jarFile = new JarFile(file);
			Manifest jarManifest = jarFile.getManifest();

			if (jarManifest == null) {
				// Ignore files without Manifest
				return null;
			}

			Attributes attributes = jarManifest.getMainAttributes();
			if (attributes != null) {

				// Handle symbolic name format
				String symbolicName = attributes.getValue(BUNDLE_SYMBOLIC_NAME);
				if (symbolicName != null) {

					// Test if there is an extra information (version,
					// singleton, ...)
					int endOfName = symbolicName.indexOf(';');
					if (endOfName == -1) {
						return symbolicName;
					}

					// Only return the symbolic name part
					return symbolicName.substring(0, endOfName);
				}
			}

		} catch (IOException ex) {
			// Ignore
		}

		return null;
	}

	/**
	 * <pre>
	 * org.psem2m.platform.base=[/Users/ogattaz/workspaces/psem2m/psem2m/platforms/felix.user.dir]
	 * </pre>
	 * 
	 * @return
	 */
	public String getPlatformBase() {
		return System.getProperty(PROP_PLATFORM_BASE);
	}

	/**
	 * <pre>
	 * org.psem2m.platform.home=[/usr/share/psem2m]
	 * </pre>
	 * 
	 * @return
	 */
	public String getPlatformHome() {
		return System.getProperty(PROP_PLATFORM_HOME);
	}

	public File[] getRepositories() {

		List<File> repositories = new ArrayList<File>();

		final String platformBase = getPlatformBase();
		final String platformHome = getPlatformHome();

		File baseRepo = null;
		File homeRepo = null;

		// Current instance repository
		if (platformBase != null) {
			baseRepo = new File(getPlatformBase(), REPOSITORY_NAME);
			if (baseRepo.exists() && baseRepo.isDirectory()) {
				repositories.add(baseRepo);
			}
		}

		// Home repository
		if (platformHome != null) {
			homeRepo = new File(getPlatformHome(), REPOSITORY_NAME);
			if (!homeRepo.equals(baseRepo) && homeRepo.exists()
					&& homeRepo.isDirectory()) {
				repositories.add(homeRepo);
			}
		}

		if (platformBase != null) {
			baseRepo = new File(getPlatformBase());
			if (baseRepo.exists() && baseRepo.isDirectory()) {
				repositories.add(baseRepo);
			}
		}

		if (platformHome != null) {
			homeRepo = new File(getPlatformHome());
			if (!homeRepo.equals(baseRepo) && homeRepo.exists()
					&& homeRepo.isDirectory()) {
				repositories.add(homeRepo);
			}
		}

		return repositories.toArray(new File[0]);
	}
}
