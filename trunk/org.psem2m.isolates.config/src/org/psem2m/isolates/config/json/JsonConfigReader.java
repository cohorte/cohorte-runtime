/**
 * File:   JsonConfigReader.java
 * Author: Thomas Calmant
 * Date:   2 sept. 2011
 */
package org.psem2m.isolates.config.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.psem2m.isolates.base.conf.IApplicationDescr;
import org.psem2m.isolates.base.conf.IBundleDescr;
import org.psem2m.isolates.base.conf.IConfigurationReader;
import org.psem2m.isolates.base.conf.IIsolateDescr;
import org.psem2m.isolates.base.conf.beans.ApplicationDescription;
import org.psem2m.isolates.base.conf.beans.BundleDescription;
import org.psem2m.isolates.base.conf.beans.IsolateDescription;
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.config.IConfigurationReader#getApplication(java.lang
     * .String)
     */
    @Override
    public IApplicationDescr getApplication(final String aApplicationId) {

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

    /**
     * Loads the given configuration file
     * 
     * @param aFile
     *            JSON configuration file
     * 
     * @return True on a successful read, else false
     */
    @Override
    public boolean load(final String aFile) {

	try {
	    // Parse the configuration
	    final JSONObject configRoot = readJsonObjectFile(aFile);

	    // Throws JSONException if key is not found
	    String applicationId = configRoot
		    .getString(IJsonConfigKeys.CONFIG_APP_ID);

	    pApplication = new ApplicationDescription(applicationId);

	    // Throws JSONException if key is not found
	    parseIsolates(configRoot
		    .getJSONArray(IJsonConfigKeys.CONFIG_ISOLATES_ARRAY));

	    return true;

	} catch (JSONException ex) {
	    System.err.println("Error parsing a configuration file");
	    ex.printStackTrace();

	} catch (IOException e) {
	    System.err.println("Can't access a configuration file");
	    e.printStackTrace();
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
    protected IBundleDescr parseBundle(final JSONObject aBundleObject)
	    throws JSONException {

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
	return new BundleDescription(symbolicName, version, fileName, optional);
    }

    /**
     * Parses an array of bundles
     * 
     * @param aIsolateDescription
     *            Isolate currently described
     * @param aJsonArray
     *            Bundle array
     * @throws JSONException
     *             An error occurred while reading the array
     * @throws FileNotFoundException
     *             An imported file wasn't found
     */
    protected void parseBundles(final IsolateDescription aIsolateDescription,
	    final JSONArray aJsonArray) throws JSONException,
	    FileNotFoundException {

	final int bundlesCount = aJsonArray.length();
	for (int i = 0; i < bundlesCount; i++) {

	    final JSONObject bundleObject = aJsonArray.getJSONObject(i);

	    if (bundleObject.has(IJsonConfigKeys.CONFIG_FROM)) {
		// Read "distant" object
		parseBundles(aIsolateDescription,
			readJsonArrayFile(bundleObject
				.getString(IJsonConfigKeys.CONFIG_FROM)));

	    } else {
		// Parse local object
		aIsolateDescription.getBundles().add(parseBundle(bundleObject));
	    }
	}
    }

    /**
     * Parses an isolate entry
     * 
     * @param aIsolateObject
     *            A JSON object describing an isolate
     * @return The description of the isolate
     * @throws JSONException
     *             An error occurred while reading the object
     * @throws FileNotFoundException
     *             An imported file wasn't found
     */
    protected IIsolateDescr parseIsolate(final JSONObject aIsolateObject)
	    throws JSONException, FileNotFoundException {

	// Isolate ID
	final String isolateId = aIsolateObject
		.getString(IJsonConfigKeys.CONFIG_ISOLATE_ID);

	// Prepare the description object
	final IsolateDescription isolateDescription = new IsolateDescription(
		isolateId);

	// Isolate VM Args
	final JSONArray vmArgsArray = aIsolateObject
		.optJSONArray(IJsonConfigKeys.CONFIG_ISOLATE_VMARGS);

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
		aIsolateObject
			.getJSONArray(IJsonConfigKeys.CONFIG_ISOLATE_BUNDLES));

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

	    final IIsolateDescr isolateDescription;
	    final JSONObject isolateObject = aJsonArray.getJSONObject(i);

	    if (isolateObject.has(IJsonConfigKeys.CONFIG_FROM)) {
		// Case 1 : the isolate is described in another file
		isolateDescription = parseIsolate(readJsonObjectFile(isolateObject
			.getString(IJsonConfigKeys.CONFIG_FROM)));

	    } else {
		// Case 2 : everything is described here
		isolateDescription = parseIsolate(isolateObject);
	    }

	    // Store it
	    pApplication.addIsolate(isolateDescription);
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

	// TODO try multiple files paths (use FileFinder service)
	return new Scanner(aFile).useDelimiter("\\Z").next();
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

	return new JSONArray(readFile(new File(aFile)));
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

	return new JSONObject(readFile(new File(aFile)));
    }
}
