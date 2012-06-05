/**
 * File:   JsonDirectoryReader.java
 * Author: Thomas Calmant
 * Date:   25 mai 2012
 */
package org.psem2m.forkers.aggregator.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
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

    /** The internal directory to store data */
    private final IInternalSignalsDirectory pDirectory;

    /** The logger */
    private final IIsolateLoggerSvc pLogger;

    /**
     * Constructor
     * 
     * @param aDirectory
     *            The directory to store data
     * @param aLogger
     *            A logger (can be null)
     */
    public JsonDirectoryReader(final IInternalSignalsDirectory aDirectory,
            final IIsolateLoggerSvc aLogger) {

        pDirectory = aDirectory;
        pLogger = aLogger;
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
                    // host is an alias to hostContent
                    pDirectory.setHostAlias((String) hostContent, host);

                } else if (hostContent instanceof JSONObject) {

                    // Get the directory for the host
                    final JSONObject directory = (JSONObject) hostContent;

                    @SuppressWarnings("unchecked")
                    final Iterator<String> isolates = directory.keys();
                    while (isolates.hasNext()) {
                        // Get the next isolate access
                        final String isolate = isolates.next();

                        // Get the access port
                        final int port = directory.getInt(isolate);
                        pDirectory.addIsolate(isolate, host, port);
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
                        builder.append(hostContent.getClass().getSimpleName());
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
}
