/**
 * Copyright 2014 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.psem2m.isolates.base.bundles.impl;

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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.osgi.framework.Constants;
import org.psem2m.isolates.base.bundles.BundleRef;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;

/**
 * Implements the bundle finder, using the platform directories service
 *
 * @author Thomas Calmant
 */
public class CBundleFinderSvc implements IBundleFinderSvc {

    /** Bundle symbolic name information entry */
    public static final String INFO_SYMBOLIC_NAME = "name";

    /** Bundle symbolic name information time stamp */
    public static final String INFO_TIMESTAMP = "timestamp";

    /** Name of the ZIP entry of a JAR manifest */
    public static final String JAR_MANIFEST_ENTRY = "META-INF/MANIFEST.MF";

    /** Bundle files cache : symbolic name -&gt; file */
    private final Map<String, String> pBundlesCache = new HashMap<String, String>();

    /** Platform directories service */
    private final IPlatformDirsSvc pPlatformDirsSvc;

    /**
     * Constructor for non-injected usage
     *
     * @param aPlatformDirsSvc
     *            A platform service instance
     */
    public CBundleFinderSvc(final IPlatformDirsSvc aPlatformDirsSvc) {

        pPlatformDirsSvc = aPlatformDirsSvc;
    }

    /**
     * Finds all bundles in the known repositories and stores their path and
     * symbolic names
     */
    protected synchronized void findAllBundles() {

        // Clear existing cache
        pBundlesCache.clear();

        // Do the job
        for (final File repository : pPlatformDirsSvc.getRepositories()) {
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
                        && !pBundlesCache.containsValue(file.getAbsolutePath())) {

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

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.isolates.base.bundles.IBundleFinderSvc#findBundle(java.lang
     * .String[])
     */
    @Override
    public BundleRef findBundle(final String... aBundlePossibleNames) {

        // Fill the cache, if needed
        if (pBundlesCache.isEmpty()) {
            findAllBundles();
        }

        // Try with cache first
        for (final String bundleName : aBundlePossibleNames) {
            if (pBundlesCache.containsKey(bundleName)) {
                return new BundleRef(bundleName, new File(
                        pBundlesCache.get(bundleName)));
            }
        }

        // Look in each repository
        for (final File repository : pPlatformDirsSvc.getRepositories()) {

            if (!repository.exists() || !repository.isDirectory()) {
                continue;
            }

            // Look for each possible name
            for (final String bundleName : aBundlePossibleNames) {

                final File bundleFile = new File(repository, bundleName);
                if (bundleFile.exists()) {
                    // Stop on first bundle found
                    return new BundleRef(bundleName, bundleFile);
                }
            }
        }

        // Bundle not found in repositories, tries the names as full ones
        for (final String bundleName : aBundlePossibleNames) {

            // Try 'local' file
            File bundleFile = new File(bundleName);
            if (bundleFile.exists()) {
                // Stop on first bundle found
                return new BundleRef(bundleName, bundleFile);
            }

            // Try as a URI
            try {
                final URI bundleUri = new URI(bundleName);
                final URL bundleUrl = bundleUri.toURL();

                if (bundleUrl.getProtocol().equals("file")) {

                    bundleFile = new File(bundleUri.getPath());
                    if (bundleFile.exists()) {
                        return new BundleRef(bundleName, bundleFile);
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

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.base.bundles.IBundleFinderSvc#getBootstrap()
     */
    @Override
    public File getBootstrap() {

        final BundleRef bootRef = findBundle(BOOTSTRAP_SYMBOLIC_NAME);
        if (bootRef == null) {
            // Bootstrap not found
            return null;
        }

        return bootRef.getFile();
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
                    .get(Constants.BUNDLE_SYMBOLICNAME);
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
