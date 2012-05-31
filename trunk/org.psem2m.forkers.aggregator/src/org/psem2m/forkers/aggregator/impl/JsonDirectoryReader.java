/**
 * File:   JsonDirectoryReader.java
 * Author: Thomas Calmant
 * Date:   25 mai 2012
 */
package org.psem2m.forkers.aggregator.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;
import org.psem2m.isolates.base.IIsolateLoggerSvc;

/**
 * The JSON directory file reader
 * 
 * @author Thomas Calmant
 */
public class JsonDirectoryReader {

    /** Local host alias */
    public static final String LOCALHOST_NAME = "localhost";

    /** Aliases */
    private final Map<String, String> pAliases = new HashMap<String, String>();

    /** The directory : ID -&gt; Access */
    private final Map<String, String> pDirectory = new HashMap<String, String>();

    /** Host -&gt; IDs */
    private final Map<String, List<String>> pHosts = new HashMap<String, List<String>>();

    /** The logger (can be null) */
    private IIsolateLoggerSvc pLogger;

    /**
     * Retrieves a copy of the current directory
     * 
     * @return a copy of the current directory
     */
    public Map<String, String> getDirectoryCopy() {

        synchronized (pDirectory) {
            return new HashMap<String, String>(pDirectory);
        }
    }

    /**
     * Returns the real host name for the given one, dereferencing aliases.
     * 
     * @param aHostName
     *            A host name (alias or not)
     * @return The unaliased host name
     */
    protected String getHostName(final String aHostName) {

        synchronized (pAliases) {
            if (pAliases.containsKey(aHostName)) {
                return pAliases.get(aHostName);
            }
        }

        return aHostName;
    }

    /**
     * Retrieves the access URL of the given isolate ID
     * 
     * @param aIsolateId
     *            An isolate ID
     * @return The access URL, or null
     */
    public String getIsolateAccess(final String aIsolateId) {

        synchronized (pDirectory) {
            return pDirectory.get(aIsolateId);
        }
    }

    /**
     * Retrieves the known isolates IDs
     * 
     * @return the known isolates IDs
     */
    public String[] getIsolates() {

        synchronized (pDirectory) {
            return pDirectory.keySet().toArray(new String[0]);
        }
    }

    /**
     * Retrieves the IDs associated to the given host
     * 
     * @param aHostName
     *            A host name
     * @return The isolates of the host, or null
     */
    public String[] getIsolatesForHost(final String aHostName) {

        final List<String> isolates = pHosts.get(getHostName(aHostName));
        if (isolates == null) {
            return null;
        }

        return isolates.toArray(new String[0]);
    }

    /**
     * Loads the given file
     * 
     * @param aFile
     *            A file object
     * @return True on success
     * @throws FileNotFoundException
     *             The given file doesn't exist
     */
    public boolean loadFile(final File aFile) throws FileNotFoundException {

        // Get the file
        if (!aFile.exists()) {
            throw new FileNotFoundException(aFile.getAbsolutePath());
        }

        // Read its content at once
        final String fileContent = new Scanner(aFile).useDelimiter("\\Z")
                .next();

        synchronized (pDirectory) {
            synchronized (pAliases) {
                try {
                    final JSONObject conf = new JSONObject(fileContent);

                    @SuppressWarnings("unchecked")
                    final Iterator<String> hosts = conf.keys();
                    while (hosts.hasNext()) {

                        // Get the host name
                        final String host = hosts.next();

                        // Get the raw object
                        final Object hostContent = conf.get(host);
                        if (hostContent instanceof String) {
                            // It's an alias
                            pAliases.put(host, (String) hostContent);

                        } else if (hostContent instanceof JSONObject) {

                            // Get the directory for the host
                            final JSONObject directory = (JSONObject) hostContent;

                            final List<String> hostIsolates = new ArrayList<String>();

                            @SuppressWarnings("unchecked")
                            final Iterator<String> isolates = directory.keys();
                            while (isolates.hasNext()) {
                                final String isolate = isolates.next();

                                // Get the access port
                                final int port = directory.getInt(isolate);

                                final StringBuilder access = new StringBuilder();
                                access.append("http://");
                                access.append(host);
                                access.append(":");
                                access.append(port);

                                pDirectory.put(isolate, access.toString());
                                hostIsolates.add(isolate);
                            }

                            if (!hostIsolates.isEmpty()) {
                                pHosts.put(host, hostIsolates);
                            }

                        } else {
                            // Invalid content
                            final StringBuilder builder = new StringBuilder();
                            builder.append("Invalid content type for '");
                            builder.append(host);
                            builder.append(": ");

                            if (hostContent == null) {
                                builder.append("<null>");

                            } else {
                                builder.append(hostContent.getClass()
                                        .getSimpleName());
                            }

                            throw new JSONException(builder.toString());
                        }
                    }

                } catch (final JSONException ex) {
                    if (pLogger != null) {
                        pLogger.logSevere(this, "loadFile", "Error:", ex);
                    }
                    return false;
                }
            }
        }

        // File read
        return true;
    }

    /**
     * Loads the given directory file
     * 
     * @param aFileName
     *            The directory file
     * @return True on success
     * @throws FileNotFoundException
     *             The given file doesn't exist
     */
    public boolean loadFile(final String aFileName)
            throws FileNotFoundException {

        return loadFile(new File(aFileName));
    }

    /**
     * Resets the directory (should be used before {@link #loadFile(String)})
     */
    public void reset() {

        synchronized (pHosts) {
            pHosts.clear();
        }

        synchronized (pDirectory) {
            pDirectory.clear();
        }

        synchronized (pAliases) {
            pAliases.clear();
        }
    }

    /**
     * Sets the logger to be used by this directory reader
     * 
     * @param aLogger
     *            A logger (can be null)
     */
    public void setLogger(final IIsolateLoggerSvc aLogger) {

        pLogger = aLogger;
    }
}
