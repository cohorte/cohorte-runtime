/**
 * File:   CBundleFinderSvc.java
 * Author: Thomas Calmant
 * Date:   28 juil. 2011
 */
package org.psem2m.utilities.bootstrap.config;

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

import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.constants.boot.IBootstrapConstants;

/**
 * Implements the bundle finder, using the platform directories service
 * 
 * @author Thomas Calmant
 */
public class FileFinder {

    /** Jar files list */
    private final Map<String, File> pBundlesCache = new HashMap<String, File>();

    /**
     * Finds the first file matching one of the possible names in the given
     * directories
     * 
     * @param aLookDirs
     *            Directories to search in
     * @param aFilePossibleNames
     *            Possible file names
     * @return The first matching file, or null
     */
    public File find(final File[] aLookDirs, final String... aFilePossibleNames) {

        // Look in each given directories
        for (final File lookDir : aLookDirs) {

            if (!lookDir.exists() || !lookDir.isDirectory()) {
                continue;
            }

            // Look for each possible name
            for (final String bundleName : aFilePossibleNames) {

                final File bundleFile = new File(lookDir, bundleName);
                if (bundleFile.exists()) {
                    // Stop on first bundle found
                    return bundleFile;
                }
            }
        }

        // Bundle not found in repositories, tries the names as full ones
        for (final String fileName : aFilePossibleNames) {

            // Try 'local' file
            File bundleFile = new File(fileName);
            if (bundleFile.exists()) {
                // Stop on first bundle found
                return bundleFile;
            }

            // Try as a URI
            try {
                final URI bundleUri = new URI(fileName);
                final URL bundleUrl = bundleUri.toURL();

                if (bundleUrl.getProtocol().equals("file")) {

                    bundleFile = new File(bundleUri.getPath());
                    if (bundleFile.exists()) {
                        return bundleFile;
                    }
                }

            } catch (final MalformedURLException e) {
                // Do nothing, we're determining the kind of element
            } catch (final URISyntaxException e) {
                // Do nothing, we're determining the kind of element
            } catch (final IllegalArgumentException e) {
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
        for (final File repository : getRepositories()) {
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

        final File[] repoFiles = aRootDirectory.listFiles();
        if (repoFiles != null) {

            for (final File file : repoFiles) {

                if (file.isFile()
                        && file.getName().toLowerCase().endsWith(".jar")) {
                    // Try to read it
                    final String symbolicName = getBundleSymbolicName(file);
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
     * Finds the given file in configuration directories or working directory
     * 
     * @param aFilePossibleNames
     *            Possible file names
     * @return The first found file, or null
     */
    public File findInConfiguration(final String... aFilePossibleNames) {

        return find(getConfigurationDirectories(), aFilePossibleNames);
    }

    /**
     * Finds the given file in repositories or working directory
     * 
     * @param aFilePossibleNames
     *            Possible file names
     * @return The first found file, or null
     */
    public File findInRepositories(final String... aFilePossibleNames) {

        return find(getRepositories(), aFilePossibleNames);
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
            final JarFile jarFile = new JarFile(file);
            final Manifest jarManifest = jarFile.getManifest();

            if (jarManifest == null) {
                // Ignore files without Manifest
                return null;
            }

            final Attributes attributes = jarManifest.getMainAttributes();
            if (attributes != null) {

                // Handle symbolic name format
                final String symbolicName = attributes
                        .getValue(IBootstrapConstants.BUNDLE_SYMBOLIC_NAME);
                if (symbolicName != null) {

                    // Test if there is an extra information (version,
                    // singleton, ...)
                    final int endOfName = symbolicName.indexOf(';');
                    if (endOfName == -1) {
                        return symbolicName;
                    }

                    // Only return the symbolic name part
                    return symbolicName.substring(0, endOfName);
                }
            }

        } catch (final IOException ex) {
            // Ignore
        }

        return null;
    }

    /**
     * Retrieves the list of all local platform repositories (base and home)
     * 
     * @return The list of platform local repositories
     */
    public File[] getConfigurationDirectories() {

        final List<File> confDirs = new ArrayList<File>();

        final String platformBase = getPlatformBase();
        final String platformHome = getPlatformHome();

        File baseRepo = null;
        File homeRepo = null;

        // Current instance configuration directory
        if (platformBase != null) {
            baseRepo = new File(getPlatformBase(),
                    IBootstrapConstants.PLATFORM_CONFIGURATION_DIR);
            if (baseRepo.exists() && baseRepo.isDirectory()) {
                confDirs.add(baseRepo);
            }
        }

        // Home configuration directory
        if (platformHome != null) {
            homeRepo = new File(getPlatformHome(),
                    IBootstrapConstants.PLATFORM_CONFIGURATION_DIR);
            if (!homeRepo.equals(baseRepo) && homeRepo.exists()
                    && homeRepo.isDirectory()) {
                confDirs.add(homeRepo);
            }
        }

        // Base directory
        if (platformBase != null) {
            baseRepo = new File(getPlatformBase());
            if (baseRepo.exists() && baseRepo.isDirectory()) {
                confDirs.add(baseRepo);
            }
        }

        // Home directory
        if (platformHome != null) {
            homeRepo = new File(getPlatformHome());
            if (!homeRepo.equals(baseRepo) && homeRepo.exists()
                    && homeRepo.isDirectory()) {
                confDirs.add(homeRepo);
            }
        }

        return confDirs.toArray(new File[0]);
    }

    /**
     * <pre>
     * org.psem2m.platform.base=[/Users/ogattaz/workspaces/psem2m/psem2m/platforms/felix.user.dir]
     * </pre>
     * 
     * @return The platform base system property value (can be null)
     */
    public String getPlatformBase() {

        return System.getProperty(IPlatformProperties.PROP_PLATFORM_BASE);
    }

    /**
     * <pre>
     * org.psem2m.platform.home=[/usr/share/psem2m]
     * </pre>
     * 
     * @return The platform home system property value (can be null)
     */
    public String getPlatformHome() {

        return System.getProperty(IPlatformProperties.PROP_PLATFORM_HOME);
    }

    /**
     * Retrieves the list of all local platform repositories (base and home)
     * 
     * @return The list of platform local repositories
     */
    public File[] getRepositories() {

        final List<File> repositories = new ArrayList<File>();

        final String platformBase = getPlatformBase();
        final String platformHome = getPlatformHome();

        File baseRepo = null;
        File homeRepo = null;

        // Current instance repository
        if (platformBase != null) {
            baseRepo = new File(getPlatformBase(),
                    IBootstrapConstants.PLATFORM_REPOSITORY_DIR);
            if (baseRepo.exists() && baseRepo.isDirectory()) {
                repositories.add(baseRepo);
            }
        }

        // Home repository
        if (platformHome != null) {
            homeRepo = new File(getPlatformHome(),
                    IBootstrapConstants.PLATFORM_REPOSITORY_DIR);
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
