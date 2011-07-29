/**
 * File:   CBundleFinderSvc.java
 * Author: Thomas Calmant
 * Date:   28 juil. 2011
 */
package org.psem2m.utilities.bootstrap.streams;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.psem2m.utilities.bootstrap.IMessageSender;

/**
 * Implements the bundle finder, using the platform directories service
 * 
 * @author Thomas Calmant
 */
public class FileFinder {

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

    /** Message sender */
    private IMessageSender pMessageSender;

    /**
     * Prepares the finder
     * 
     * @param aMessageSender
     *            A message sender
     */
    public FileFinder(final IMessageSender aMessageSender) {
	pMessageSender = aMessageSender;
    }

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

	pMessageSender.sendMessage(Level.WARNING, "FileFinder", "find",
		"File not found : " + Arrays.toString(aFilePossibleNames));

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
