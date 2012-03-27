/**
 * File:   JsonConfigReader.java
 * Author: Thomas Calmant
 * Date:   2 sept. 2011
 */
package org.psem2m.isolates.config.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.Stack;

import org.psem2m.isolates.config.IPlatformConfigurationConstants;
import org.psem2m.isolates.services.conf.IConfigurationReader;
import org.psem2m.isolates.services.conf.beans.ApplicationDescription;
import org.psem2m.isolates.services.conf.beans.BundleDescription;
import org.psem2m.isolates.services.conf.beans.IsolateDescription;
import org.psem2m.isolates.services.dirs.IFileFinderSvc;
import org.psem2m.utilities.json.JSONArray;
import org.psem2m.utilities.json.JSONException;
import org.psem2m.utilities.json.JSONObject;

/**
 * PSEM2M configuration reader from JSON files
 * 
 * @author Thomas Calmant
 */
public class JsonConfigReader implements IConfigurationReader {

    /** The described application */
    private ApplicationDescription pApplication;

    /** A file finder */
    private IFileFinderSvc pFileFinder;

    /** The file inclusion stack, for relative paths */
    private final Stack<File> pIncludeStack = new Stack<File>();

    /**
     * Parses the given properties object and overrides it with the given
     * properties
     * 
     * @param aPropertiesJsonObject
     *            A properties JSON object (can't be null)
     * @param aOverridingProperties
     *            Overriding properties (can be null)
     * 
     * @return The overridden properties
     */
    protected Properties computeOverriddenProperties(
            final JSONObject aPropertiesJsonObject,
            final Properties aOverridingProperties) {

        final Properties overriddenProperties = parseProperties(aPropertiesJsonObject
                .optJSONObject(IJsonConfigKeys.CONFIG_OVERRIDDEN_PROPERTIES));

        if (overriddenProperties == null) {
            // No properties in the JSON object, return overriding ones
            return aOverridingProperties;
        }

        if (aOverridingProperties != null) {
            // Override found properties
            overriddenProperties.putAll(aOverridingProperties);
        }

        return overriddenProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.config.IConfigurationReader#getApplication(java.lang
     * .String)
     */
    @Override
    public ApplicationDescription getApplication(final String aApplicationId) {

        if (pApplication.getApplicationId().equals(aApplicationId)) {
            return pApplication;
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.config.IConfigurationReader#getApplicationIds()
     */
    @Override
    public String[] getApplicationIds() {

        return new String[] { pApplication.getApplicationId() };
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.base.conf.IConfigurationReader#load(java.lang.String,
     * org.psem2m.isolates.base.dirs.IFileFinderSvc)
     */
    @Override
    public boolean load(final String aFile, final IFileFinderSvc aFileFinder) {

        pFileFinder = aFileFinder;

        try {
            // Parse the configuration
            final JSONObject configRoot = readJsonObjectFile(aFile);

            // Throws JSONException if key is not found
            final String applicationId = configRoot
                    .getString(IJsonConfigKeys.CONFIG_APP_ID);

            pApplication = new ApplicationDescription(applicationId);

            // Throws JSONException if key is not found
            parseIsolates(configRoot
                    .getJSONArray(IJsonConfigKeys.CONFIG_ISOLATES_ARRAY));

            return true;

        } catch (final JSONException ex) {
            System.err.println("Error parsing a configuration file");
            ex.printStackTrace();

        } catch (final IOException e) {
            System.err.println("Can't access a configuration file");
            e.printStackTrace();

        } finally {
            // Don't reference the finder anymore
            pFileFinder = null;

            // Clear the stack
            pIncludeStack.clear();
        }

        return false;
    }

    /**
     * Parses a bundle entry
     * 
     * @param aBundleObject
     *            JSON bundle entry
     * @return The bundle description
     * @throws JSONException
     *             The bundle entry is invalid
     */
    protected BundleDescription parseBundle(final JSONObject aBundleObject,
            final Properties aOverridenProperties) throws JSONException {

        // Get the symbolic name
        final String symbolicName = aBundleObject
                .getString(IJsonConfigKeys.CONFIG_BUNDLE_NAME);

        // Get the version (optional)
        final String version = aBundleObject
                .optString(IJsonConfigKeys.CONFIG_BUNDLE_VERSION);

        // Get the file name (optional)
        final String fileName = aBundleObject
                .optString(IJsonConfigKeys.CONFIG_BUNDLE_FILE);

        // Bundle optional flag (optional)
        final boolean optional = aBundleObject.optBoolean(
                IJsonConfigKeys.CONFIG_BUNDLE_OPTIONAL, false);

        // Create the description
        final BundleDescription wBundleDescription = new BundleDescription(
                symbolicName, version, fileName, optional);

        // Bundle properties (optional)
        Properties bundleProperties = parseProperties(aBundleObject
                .optJSONObject(IJsonConfigKeys.CONFIG_BUNDLE_PROPERTIES));

        if (bundleProperties == null) {
            // Set properties
            bundleProperties = aOverridenProperties;

        } else if (aOverridenProperties != null) {
            // Override found properties with upper level ones
            bundleProperties.putAll(aOverridenProperties);

        }

        // Update the bundle properties
        wBundleDescription.setProperties(bundleProperties);

        return wBundleDescription;
    }

    /**
     * Parses an array of bundles
     * 
     * @param aIsolateDescription
     *            Isolate currently described
     * @param aJsonArray
     *            Bundle array
     * @param aOverridingProperties
     *            Overriding properties (can be null)
     * 
     * @throws JSONException
     *             An error occurred while reading the array
     * @throws FileNotFoundException
     *             An imported file wasn't found
     */
    protected void parseBundles(final IsolateDescription aIsolateDescription,
            final JSONArray aJsonArray, final Properties aOverridingProperties)
            throws JSONException, FileNotFoundException {

        final int bundlesCount = aJsonArray.length();
        for (int i = 0; i < bundlesCount; i++) {

            final JSONObject bundleObject = aJsonArray.getJSONObject(i);
            if (bundleObject.has(IJsonConfigKeys.CONFIG_FROM)) {
                // Overridden properties
                final Properties overridingProperties = computeOverriddenProperties(
                        bundleObject, aOverridingProperties);

                // Read "distant" object
                parseBundles(aIsolateDescription,
                        readJsonArrayFile(bundleObject
                                .getString(IJsonConfigKeys.CONFIG_FROM)),
                        overridingProperties);

                // Remove the included file from the stack
                if (!pIncludeStack.isEmpty()) {
                    pIncludeStack.pop();
                }

            } else {
                // Parse local object
                aIsolateDescription.getBundles().add(
                        parseBundle(bundleObject, aOverridingProperties));
            }
        }
    }

    /**
     * Parses an isolate entry
     * 
     * @param aIsolateObject
     *            A JSON object describing an isolate
     * @param aOverridingProperties
     *            Overriding properties (overrides isolate's ones), can be null
     * 
     * @return The description of the isolate
     * 
     * @throws JSONException
     *             An error occurred while reading the object
     * @throws FileNotFoundException
     *             An imported file wasn't found
     */
    protected IsolateDescription parseIsolate(final JSONObject aIsolateObject,
            final Properties aOverridingProperties) throws JSONException,
            FileNotFoundException {

        // Isolate ID
        final String isolateId = aIsolateObject
                .getString(IJsonConfigKeys.CONFIG_ISOLATE_ID);

        // Prepare the description object
        final IsolateDescription isolateDescription = new IsolateDescription(
                isolateId);

        // Isolate kind
        isolateDescription.setKind(aIsolateObject
                .optString(IJsonConfigKeys.CONFIG_ISOLATE_KIND));

        // Isolate VM Args
        final JSONArray vmArgsArray = aIsolateObject
                .optJSONArray(IJsonConfigKeys.CONFIG_ISOLATE_VMARGS);

        // Add the JSONArray content to the vmArgs
        if (vmArgsArray != null) {

            final List<String> isolateVmArgsList = isolateDescription
                    .getVmArgs();

            final int vmArgsCount = vmArgsArray.length();
            for (int i = 0; i < vmArgsCount; i++) {
                // Add all string to the list
                isolateVmArgsList.add(vmArgsArray.getString(i));
            }
        }

        // Isolate host name (also HTTP communication host name)
        String isolateHost = aIsolateObject
                .optString(IJsonConfigKeys.CONFIG_ISOLATE_HOST);
        if (isolateHost != null) {
            isolateHost = isolateHost.trim();
        }

        if (isolateHost == null || isolateHost.isEmpty()) {
            // Invalid name
            isolateDescription.setHostName("localhost");

        } else {
            isolateDescription.setHostName(isolateHost);
        }

        // Isolate HTTP communication port
        String isolatePort = aIsolateObject
                .optString(IJsonConfigKeys.CONFIG_ISOLATE_PORT);
        if (isolatePort == null) {
            // Default port : 8080
            isolatePort = "8080";
        }

        // Compute the isolate access URL
        final StringBuilder accessUrl = new StringBuilder();
        // FIXME considers that the communication is HTTP only
        accessUrl.append("http://");
        accessUrl.append(isolateDescription.getHostName());
        accessUrl.append(":");
        accessUrl.append(isolatePort);
        isolateDescription.setAccessUrl(accessUrl.toString());

        // Parse overridden system properties
        final Properties overridingProperties = computeOverriddenProperties(
                aIsolateObject, aOverridingProperties);

        // Isolate bundles
        parseBundles(isolateDescription,
                aIsolateObject
                        .getJSONArray(IJsonConfigKeys.CONFIG_ISOLATE_BUNDLES),
                overridingProperties);

        return isolateDescription;
    }

    /**
     * Parses an array of isolates
     * 
     * @param aJsonArray
     *            Bundle array
     * @throws JSONException
     *             An error occurred while reading the array
     * @throws FileNotFoundException
     *             An imported file wasn't found
     */
    protected void parseIsolates(final JSONArray aJsonArray)
            throws JSONException, FileNotFoundException {

        final int isolatesCount = aJsonArray.length();
        for (int i = 0; i < isolatesCount; i++) {

            final IsolateDescription isolateDescription;
            final JSONObject isolateObject = aJsonArray.getJSONObject(i);

            // Compute overriding properties
            final Properties overridingProperties = computeOverriddenProperties(
                    isolateObject, null);

            if (isolateObject.has(IJsonConfigKeys.CONFIG_FROM)) {
                // Case 1 : the isolate is described in another file
                isolateDescription = parseIsolate(
                        readJsonObjectFile(isolateObject
                                .getString(IJsonConfigKeys.CONFIG_FROM)),
                        overridingProperties);

                // Remove the included file from the stack
                if (!pIncludeStack.isEmpty()) {
                    pIncludeStack.pop();
                }

            } else {
                // Case 2 : everything is described here
                isolateDescription = parseIsolate(isolateObject,
                        overridingProperties);
            }

            // Store it
            pApplication.addIsolate(isolateDescription);
        }
    }

    /**
     * Parses a set of properties from the JSON file
     * 
     * @param aSetOfProperties
     *            A JSON object representing the properties
     * @return A Properties object
     */
    protected Properties parseProperties(final JSONObject aSetOfProperties) {

        if (aSetOfProperties != null) {

            final Properties wProperties = new Properties();
            final Iterator<Entry<String, Object>> wEntries = aSetOfProperties
                    .entries();

            Entry<String, Object> wEntry;
            while (wEntries.hasNext()) {
                wEntry = wEntries.next();
                wProperties.put(wEntry.getKey(), wEntry.getValue());
            }

            return wProperties;
        }

        return null;
    }

    /**
     * Reads the given file content
     * 
     * @param aFileName
     *            Path to the file to read
     * @return The file content
     * @throws FileNotFoundException
     *             File not found
     */
    protected String readFile(final String aFileName)
            throws FileNotFoundException {

        final File confFile;
        final File baseFile;

        if (!pIncludeStack.isEmpty()) {
            // Use a base file, if possible
            baseFile = pIncludeStack.peek();

        } else {
            // Use the configuration directory
            baseFile = new File(IPlatformConfigurationConstants.SUBDIR_CONF);
        }

        final File[] foundFiles = pFileFinder.find(baseFile, aFileName);
        if (foundFiles == null || foundFiles.length == 0) {
            throw new FileNotFoundException(aFileName);
        }

        // Use the first corresponding file
        confFile = foundFiles[0];

        // Add it to the stack (it will be the next read file)
        pIncludeStack.push(confFile.getAbsoluteFile());

        // Read its content at once
        return new Scanner(confFile).useDelimiter("\\Z").next();
    }

    /**
     * Reads the given file and parses it as a JSON array
     * 
     * @param aFile
     *            File to read
     * @return The parsed JSON array
     * @throws JSONException
     *             The file is not a JSON array
     * @throws FileNotFoundException
     *             The given file wasn't found
     */
    protected JSONArray readJsonArrayFile(final String aFile)
            throws JSONException, FileNotFoundException {

        return new JSONArray(readFile(aFile));
    }

    /**
     * Reads the given file and parses it as a JSON object
     * 
     * @param aFile
     *            File to read
     * @return The parsed JSON array
     * @throws JSONException
     *             The file is not a JSON object
     * @throws FileNotFoundException
     *             The given file wasn't found
     */
    protected JSONObject readJsonObjectFile(final String aFile)
            throws JSONException, FileNotFoundException {

        return new JSONObject(readFile(aFile));
    }
}
