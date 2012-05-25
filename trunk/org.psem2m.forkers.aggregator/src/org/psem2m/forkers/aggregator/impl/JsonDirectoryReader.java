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
 * The JSON directory reader
 * 
 * @author Thomas Calmant
 */
public class JsonDirectoryReader {

    /** The directory : ID -&gt; Access */
    private final Map<String, String> pDirectory = new HashMap<String, String>();

    /** Host -&gt; IDs */
    private final Map<String, List<String>> pHosts = new HashMap<String, List<String>>();

    /** FIXME */
    public IIsolateLoggerSvc pLogger;

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
     * Retrieves the access URL of the given isolate ID
     * 
     * @param aIsolateId
     *            An isolate ID
     * @return The access URL, or null
     */
    public String getIsolateAccess(final String aIsolateId) {

        return pDirectory.get(aIsolateId);
    }

    /**
     * Retrieves the known isolates IDs
     * 
     * @return the known isolates IDs
     */
    public String[] getIsolates() {

        return pDirectory.keySet().toArray(new String[0]);
    }

    /**
     * Retrieves the IDs associated to the given host
     * 
     * @param aHostName
     *            A host name
     * @return The isolates of the host, or null
     */
    public String[] getIsolatesForHost(final String aHostName) {

        final List<String> isolates = pHosts.get(aHostName);
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

        pLogger.logDebug(this, "loadFile", "Read :\n", fileContent);

        synchronized (pDirectory) {
            try {
                final JSONObject conf = new JSONObject(fileContent);

                @SuppressWarnings("unchecked")
                final Iterator<String> hosts = conf.keys();
                while (hosts.hasNext()) {
                    // Get the host name
                    final String host = hosts.next();
                    final List<String> hostIsolates = new ArrayList<String>();

                    pLogger.logDebug(this, "loadFile", "host=", host);

                    // Get the directory for the host
                    final JSONObject directory = conf.getJSONObject(host);

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

                        pLogger.logDebug(this, "loadFile", "host=", host,
                                ", access=", access);

                        pDirectory.put(isolate, access.toString());
                        hostIsolates.add(isolate);
                    }

                    if (!hostIsolates.isEmpty()) {
                        pHosts.put(host, hostIsolates);
                    }
                }

            } catch (final JSONException ex) {
                pLogger.logSevere(this, "loadFile", "Error:", ex);
                return false;
            }
        }

        pLogger.logDebug(this, "loadFile", "RESULT=\n", pHosts, "\n",
                pDirectory);

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

        final File file = new File(aFileName);
        return loadFile(file);
    }

    /**
     * Resets the directory (should be used before {@link #loadFile(String)})
     */
    public void reset() {

        synchronized (pDirectory) {
            pDirectory.clear();
        }
    }
}
