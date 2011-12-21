/**
 * File:   CBundleFinderSvc.java
 * Author: Thomas Calmant
 * Date:   28 juil. 2011
 */
package org.psem2m.utilities.bootstrap.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.constants.boot.IBootstrapConstants;

/**
 * Implements the bundle finder, using the platform directories service
 * 
 * @author Thomas Calmant
 */
public class FileFinder {

    /** Bundle symbolic name information entry */
    public static final String INFO_SYMBOLIC_NAME = "name";

    /** Bundle symbolic name information time stamp */
    public static final String INFO_TIMESTAMP = "timestamp";

    /** Name of the ZIP entry of a JAR manifest */
    public static final String JAR_MANIFEST_ENTRY = "META-INF/MANIFEST.MF";

    /** Symbolic name -&gt; Jar file map */
    private final Map<String, String> pBundlesCache = new HashMap<String, String>();

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

                // Only work with not already read JAR files
                if (file.isFile()
                        && file.getName().toLowerCase().endsWith(".jar")
                        && !pBundlesCache.containsValue(file)) {
                    // Try to read it
                    final String symbolicName = getBundleSymbolicName(file
                            .getAbsolutePath());
                    if (symbolicName != null) {
                        // Store it
                        pBundlesCache.put(symbolicName, file.getAbsolutePath());
                    }

                } else if (file.isDirectory()) {
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

        return new File(pBundlesCache.get(aSymbolicName));
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
     * @param aFileName
     *            A bundle Jar file
     * @return The bundle symbolic name, null if unreadable
     */
    public String getBundleSymbolicName(final String aFileName) {

        final File jarFile = new File(aFileName);
        if (!jarFile.isFile()) {
            // Invalid file name
            return null;
        }

        // Try to read the associated informations
        final String fullFilePath = jarFile.getAbsolutePath();
        final int extIndex = fullFilePath.lastIndexOf('.');
        if (extIndex < 0) {
            // Invalid file name
            return null;
        }

        // Open the file
        final StringBuilder builder = new StringBuilder(fullFilePath.substring(
                0, extIndex + 1));
        builder.append("dat");
        final File infoFile = new File(builder.toString());

        // Erase builder content (re-use it)
        builder.setLength(0);

        System.out.println("Working on : " + infoFile + " / "
                + infoFile.exists());

        if (infoFile.exists() && readNameFromInfo(jarFile, infoFile, builder)) {
            // Info file up to date, read it
            return builder.toString();
        }

        final String bundleName = readNameFromJar(jarFile);
        if (bundleName != null) {
            try {
                writeInfoFile(infoFile, bundleName);
            } catch (final IOException e) {
                // Juste print it
                e.printStackTrace();
            }
        }

        return bundleName;
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

        return confDirs.toArray(new File[confDirs.size()]);
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
            baseRepo = new File(platformBase,
                    IBootstrapConstants.PLATFORM_REPOSITORY_DIR);
            if (baseRepo.exists() && baseRepo.isDirectory()) {
                repositories.add(baseRepo);
            }
        }

        // Home repository
        if (platformHome != null) {
            homeRepo = new File(platformHome,
                    IBootstrapConstants.PLATFORM_REPOSITORY_DIR);
            if (!homeRepo.equals(baseRepo) && homeRepo.exists()
                    && homeRepo.isDirectory()) {
                repositories.add(homeRepo);
            }
        }

        if (platformBase != null) {
            baseRepo = new File(platformBase);
            if (baseRepo.exists() && baseRepo.isDirectory()) {
                repositories.add(baseRepo);
            }
        }

        if (platformHome != null) {
            homeRepo = new File(platformHome);
            if (!homeRepo.equals(baseRepo) && homeRepo.exists()
                    && homeRepo.isDirectory()) {
                repositories.add(homeRepo);
            }
        }

        return repositories.toArray(new File[0]);
    }

    /**
     * Parses the Manifest content string
     * 
     * @param aManifestContent
     *            THe content of a Manifest.mf file
     * @return The manifest main attributes, null if invalid
     */
    public Map<String, String> readManifest(final String aManifestContent) {

        if (aManifestContent == null || aManifestContent.isEmpty()) {
            return null;
        }

        // Use a linked hash map to keep attributes order
        final Map<String, String> attributes = new LinkedHashMap<String, String>();

        final String[] lines = aManifestContent.split("\\r\\n|\\n|\\r");

        String currentEntry = null;
        StringBuilder currentLine = null;

        for (final String line : lines) {

            if (line.trim().isEmpty()) {
                // Empty line, end of main attributes. Store last entry
                if (currentEntry != null) {
                    attributes.put(currentEntry, currentLine.toString());
                }

                break;
            }

            if (line.charAt(0) == ' ') {
                // Continuation of previous line
                currentLine.append(line.trim());

            } else {

                // Store current line
                if (currentEntry != null) {
                    attributes.put(currentEntry, currentLine.toString());
                }

                // New line,
                final int separator = line.indexOf(":");

                if (separator == -1) {
                    // Invalid line
                    break;
                }

                currentEntry = line.substring(0, separator);
                // +2 : column and space
                currentLine = new StringBuilder(line.substring(separator + 2)
                        .trim());
            }
        }

        return attributes;
    }

    /**
     * Reads the bundle symbolic name from the given information file
     * 
     * @param aJarFile
     *            Bundle file
     * @param aInfoFile
     *            Information file path
     * @param aBuilder
     *            Result string builder
     * @return True if data was read and up to date
     */
    protected boolean readNameFromInfo(final File aJarFile,
            final File aInfoFile, final StringBuilder aBuilder) {

        if (!aJarFile.isFile() || !aInfoFile.isFile() || aBuilder == null) {
            // Invalid parameters
            return false;
        }

        // Read information data
        final Properties properties = new Properties();
        try {
            final FileInputStream fis = new FileInputStream(aInfoFile);
            properties.load(fis);
            fis.close();

        } catch (final IOException e) {
            // Ignore
            return false;
        }

        // Test if JAR has been modified since the file was created
        final String timestamp = properties.getProperty(INFO_TIMESTAMP);
        if (timestamp == null) {
            // No time stamp
            return false;
        }

        try {
            if (Long.parseLong(timestamp) < aJarFile.lastModified()) {
                // Information too old
                return false;
            }

        } catch (final NumberFormatException e) {
            // Invalid time stamp
            return false;
        }

        // Copy the symbolic name
        final String symName = properties.getProperty(INFO_SYMBOLIC_NAME);
        if (symName != null) {
            // Found !
            aBuilder.append(symName);
            return true;
        }

        return false;
    }

    /**
     * Reads the bundle symbolic name from its JAR file
     * 
     * @param aFile
     *            A bundle Jar file path
     * @return The read name, null on error
     */
    protected String readNameFromJar(final File aFile) {

        ZipFile zip = null;
        Map<String, String> attributes = null;
        try {
            // Open the JAR file
            zip = new ZipFile(aFile);

            // Get the Manifest entry
            final ZipEntry manifestEntry = zip.getEntry(JAR_MANIFEST_ENTRY);
            if (manifestEntry == null) {
                // No manifest in the file
                return null;
            }

            // Read the manifest content
            final InputStream manifestStream = zip
                    .getInputStream(manifestEntry);
            final StringBuilder builder = new StringBuilder();
            readStream(manifestStream, builder);
            manifestStream.close();

            // Parse it
            attributes = readManifest(builder.toString());

            // Handle symbolic name format
            final String symbolicName = attributes
                    .get(IBootstrapConstants.BUNDLE_SYMBOLIC_NAME);
            if (symbolicName != null) {

                // Test if there is an extra information (version,
                // singleton, ...)
                final int endOfName = symbolicName.indexOf(';');
                if (endOfName == -1) {
                    // Return the whole name
                    return symbolicName;
                }

                // Only return the symbolic name part
                return symbolicName.substring(0, endOfName);
            }

        } catch (final IOException e) {
            // Ignore

        } finally {

            if (attributes != null) {
                // Clear the manifests map
                attributes.clear();
            }

            if (zip != null) {
                try {
                    // Be nice, close the Jar file
                    zip.close();

                } catch (final IOException e) {
                    // Ignore
                }
            }
        }

        return null;
    }

    /**
     * Reads the given input stream and fills the given string builder with it
     * 
     * @param aStream
     *            A manifest content input stream
     * @param aBuilder
     *            The filled builder
     * @return True on success, False on error
     */
    public boolean readStream(final InputStream aStream,
            final StringBuilder aBuilder) throws IOException {

        if (aStream == null || aBuilder == null) {
            return false;
        }

        // Clean up the builder
        aBuilder.setLength(0);

        final int bufferSize = 8192;
        final char[] buffer = new char[bufferSize];

        // The reader
        final Reader reader = new InputStreamReader(aStream, "UTF-8");
        int read = 0;

        do {
            read = reader.read(buffer);
            if (read > 0) {
                aBuilder.append(buffer, 0, read);
            }

        } while (read > 0);

        // Close the reader
        reader.close();
        return true;
    }

    /**
     * Writes the bundle information file
     * 
     * @param aInfoFile
     *            File to write into
     * @param aSymbolicName
     *            Bundle symbolic name
     * @throws IOException
     *             Error writing the file
     */
    protected void writeInfoFile(final File aInfoFile,
            final String aSymbolicName) throws IOException {

        if (!aInfoFile.exists()) {
            aInfoFile.createNewFile();
        }

        // Set up content
        final Properties properties = new Properties();
        properties.setProperty(INFO_TIMESTAMP,
                String.valueOf(System.currentTimeMillis()));
        properties.setProperty(INFO_SYMBOLIC_NAME, aSymbolicName);

        // Write the file
        final FileOutputStream fos = new FileOutputStream(aInfoFile);
        properties.store(fos, "PSEM2M Information file");
        fos.close();
    }
}
