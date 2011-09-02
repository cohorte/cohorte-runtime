/**
 * File:   JsonConfigReader.java
 * Author: Thomas Calmant
 * Date:   2 sept. 2011
 */
package org.psem2m.isolates.config.json.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.psem2m.isolates.config.json.IBundleDescr;
import org.psem2m.isolates.config.json.IIsolateDescr;

/**
 * @author Thomas Calmant
 * 
 */
public class JsonConfigReader {

    public static final String CONFIG_APP_ID = "appId";

    public static final String CONFIG_BUNDLE_FILE = "file";

    public static final String CONFIG_BUNDLE_FROM = "from";

    public static final String CONFIG_BUNDLE_NAME = "symbolicName";

    public static final String CONFIG_BUNDLE_OPTIONAL = "optional";

    public static final String CONFIG_BUNDLE_VERSION = "version";

    public static final String CONFIG_ISOLATE_BUNDLES = "bundles";

    public static final String CONFIG_ISOLATE_FROM = "from";

    public static final String CONFIG_ISOLATE_ID = "id";

    public static final String CONFIG_ISOLATE_VMARGS = "vmArgs";

    public static final String CONFIG_ISOLATES_ARRAY = "isolates";

    /** Application ID (one application per file) */
    private String pApplicationId;

    /** Isolates ID -> Configuration map */
    private final Map<String, IIsolateDescr> pIsolates = new HashMap<String, IIsolateDescr>();

    public String getApplicationId() {
	return pApplicationId;
    }

    public IIsolateDescr getIsolate(final String aId) {
	return pIsolates.get(aId);
    }

    public void load(final String aFile) {

	pApplicationId = null;
	pIsolates.clear();

	try {
	    // Parse the configuration
	    final JSONObject configRoot = readJsonObjectFile(aFile);

	    // Throws JSONException if key is not found
	    pApplicationId = configRoot.getString(CONFIG_APP_ID);

	    // Throws JSONException if key is not found
	    parseIsolates(configRoot.getJSONArray(CONFIG_ISOLATES_ARRAY));

	} catch (JSONException ex) {
	    System.err.println("Error parsing a configuration file");
	    ex.printStackTrace();

	} catch (IOException e) {
	    System.err.println("Can't access a configuration file");
	    e.printStackTrace();
	}
    }

    /**
     * Parses a bundle entry
     * 
     * @param aJsonObject
     *            JSON bundle entry
     * @return The bundle description
     * @throws JSONException
     *             The bundle entry is invalid
     */
    protected IBundleDescr parseBundle(final JSONObject aJsonObject)
	    throws JSONException {

	// Get the symbolic name
	final String symbolicName = aJsonObject.getString(CONFIG_BUNDLE_NAME);

	// Get the version (optional)
	final String version = aJsonObject.optString(CONFIG_BUNDLE_VERSION);

	// Get the file name (optional)
	final String fileName = aJsonObject.optString(CONFIG_BUNDLE_FILE);

	// Bundle optional flag (optional)
	final boolean optional = aJsonObject.optBoolean(CONFIG_BUNDLE_OPTIONAL,
		false);

	// Create the description
	return new BundleDescription(symbolicName, version, fileName, optional);
    }

    /**
     * @param aIsolateDescription
     * @param aJsonArray
     * @throws JSONException
     * @throws FileNotFoundException
     */
    protected void parseBundles(final IsolateDescription aIsolateDescription,
	    final JSONArray aJsonArray) throws JSONException,
	    FileNotFoundException {

	final int bundlesCount = aJsonArray.length();
	for (int i = 0; i < bundlesCount; i++) {

	    final JSONObject bundleObject = aJsonArray.getJSONObject(i);

	    if (bundleObject.has(CONFIG_BUNDLE_FROM)) {
		// Read "distant" object
		parseBundles(aIsolateDescription,
			readJsonArrayFile(bundleObject
				.getString(CONFIG_BUNDLE_FROM)));

	    } else {
		// Parse local object
		aIsolateDescription.getBundles().add(parseBundle(bundleObject));
	    }
	}
    }

    protected IIsolateDescr parseIsolate(final JSONObject isolateObject)
	    throws JSONException, FileNotFoundException {

	// Isolate ID
	final String isolateId = isolateObject.getString(CONFIG_ISOLATE_ID);

	// Prepare the description object
	final IsolateDescription isolateDescription = new IsolateDescription(
		isolateId);

	// Isolate VM Args
	final JSONArray vmArgsArray = isolateObject
		.optJSONArray(CONFIG_ISOLATE_VMARGS);

	// Add the JSONArray content to the vmArgs
	if (vmArgsArray != null) {

	    final List<String> isolateVmArgsList = isolateDescription
		    .getVMArgs();

	    final int vmArgsCount = vmArgsArray.length();
	    for (int i = 0; i < vmArgsCount; i++) {
		// Add all string to the list
		isolateVmArgsList.add(vmArgsArray.getString(i));
	    }
	}

	// Isolate bundles
	parseBundles(isolateDescription,
		isolateObject.getJSONArray(CONFIG_ISOLATE_BUNDLES));

	return isolateDescription;
    }

    /**
     * @param aJsonArray
     * @throws JSONException
     * @throws FileNotFoundException
     */
    protected void parseIsolates(final JSONArray aJsonArray)
	    throws JSONException, FileNotFoundException {

	final int isolatesCount = aJsonArray.length();
	for (int i = 0; i < isolatesCount; i++) {

	    final JSONObject isolateObject = aJsonArray.getJSONObject(i);

	    if (isolateObject.has(CONFIG_ISOLATE_FROM)) {
		// Case 1 : the isolate is described in another file
		parseIsolates(readJsonArrayFile(isolateObject
			.getString(CONFIG_ISOLATE_FROM)));

	    } else {
		// Case 2 : everything is described here
		final IIsolateDescr isolateDescr = parseIsolate(isolateObject);

		// Store the isolate
		pIsolates.put(isolateDescr.getId(), isolateDescr);
	    }
	}
    }

    /**
     * Reads the given file content
     * 
     * @param aFile
     *            File to read
     * @return File content
     * @throws FileNotFoundException
     *             File not found
     */
    protected String readFile(final File aFile) throws FileNotFoundException {

	return new Scanner(aFile).useDelimiter("\\Z").next();
    }

    protected JSONArray readJsonArrayFile(final String aFile)
	    throws JSONException, FileNotFoundException {
	// TODO Test possible file paths (HOME, BASE, ...)
	final String fileContent = readFile(new File(aFile));

	return new JSONArray(fileContent);
    }

    protected JSONObject readJsonObjectFile(final String aFile)
	    throws JSONException, FileNotFoundException {
	// TODO Test possible file paths (HOME, BASE, ...)
	final String fileContent = readFile(new File(aFile));

	return new JSONObject(fileContent);
    }
}
