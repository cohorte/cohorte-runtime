/**
 * File:   JsonComposerConfigReader.java
 * Author: Thomas Calmant
 * Date:   3 nov. 2011
 */
package org.psem2m.composer.config.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.config.IComposerConfigReader;
import org.psem2m.composer.model.ComponentBean;
import org.psem2m.composer.model.ComponentsSetBean;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.config.IPlatformConfigurationConstants;
import org.psem2m.isolates.services.dirs.IFileFinderSvc;
import org.psem2m.utilities.json.JSONArray;
import org.psem2m.utilities.json.JSONException;
import org.psem2m.utilities.json.JSONObject;

/**
 * Reads the JSON configuration file of the PSEM2M Composer
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-composer-config-json-factory", publicFactory = false)
@Provides(specifications = IComposerConfigReader.class)
@Instantiate(name = "psem2m-composer-config-json")
public class JsonComposerConfigReader extends CPojoBase implements
        IComposerConfigReader {

    /** The file finder */
    @Requires
    private IFileFinderSvc pFileFinder;

    /** The file inclusion stack, for relative paths */
    private final Stack<File> pIncludeStack = new Stack<File>();

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /**
     * Default constructor
     */
    public JsonComposerConfigReader() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo",
                "JSON Composer Configuration Reader Gone");
    }

    /**
     * Converts the given JSON Object into a String -&gt; String map
     * 
     * @param aJsonObject
     *            JSON Object to convert
     * @return The JSON Object as a map
     */
    protected Map<String, String> jsonObjectToMap(final JSONObject aJsonObject) {

        // Prepare the result
        final Map<String, String> resultMap = new HashMap<String, String>(
                aJsonObject.length());

        for (final String key : aJsonObject.keySet()) {
            // Store all valid values
            final String value = aJsonObject.optString(key);

            if (key != null && value != null) {
                // Key and value are valid
                resultMap.put(key, value);
            }
        }

        return resultMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.config.impl.IComposerConfigReader#load(java.lang.
     * String)
     */
    @Override
    public ComponentsSetBean load(final String aFileName) {

        // Parse the configuration
        try {
            pLogger.logInfo(this, "load", "CompositionFileName=[%s]", aFileName);

            final JSONObject configRoot = readJsonObjectFile(aFileName);
            return parseComponentSet(null, configRoot);

        } catch (final IOException e) {
            pLogger.logSevere(this, "", "Error accessing configuration file '",
                    aFileName, "' :", e);

        } catch (final JSONException e) {
            pLogger.logSevere(this, "", "Error parsing configuration file '",
                    aFileName, "' :", e);
        }

        return null;
    }

    /**
     * Parses the given component JSON object
     * 
     * @param aParentName
     *            Name of the parent component set
     * @param aJsonObject
     *            A component representation
     * 
     * @return The parsed component bean
     * @throws JSONException
     *             Error while parsing the file
     */
    protected ComponentBean parseComponent(final String aParentName,
            final JSONObject aJsonObject) throws JSONException {

        String wComponentName = aJsonObject
                .getString(IJsonComposerConfigConstants.COMPONENT_NAME);

        pLogger.logInfo(this, "parseComponent", "Name=[%s]", wComponentName);

        // Set up the bean
        final ComponentBean resultBean = new ComponentBean();
        resultBean.setParentName(aParentName);

        // Get the name
        resultBean.setName(wComponentName);

        // Get the type
        resultBean.setType(aJsonObject
                .getString(IJsonComposerConfigConstants.COMPONENT_TYPE));

        // Get the host isolate (optional)
        resultBean.setIsolate(aJsonObject
                .optString(IJsonComposerConfigConstants.COMPONENT_ISOLATE));

        // Get the properties (optional)
        final JSONObject jsonProperties = aJsonObject
                .optJSONObject(IJsonComposerConfigConstants.COMPONENT_PROPERTIES);
        if (jsonProperties != null) {
            resultBean.setProperties(jsonObjectToMap(jsonProperties));
        }

        // Get the filters
        final JSONObject jsonFilters = aJsonObject
                .optJSONObject(IJsonComposerConfigConstants.COMPONENT_FIELDS_FILTERS);
        if (jsonFilters != null) {
            resultBean.setFieldsFilters(jsonObjectToMap(jsonFilters));
        }

        // Get the wires
        final JSONObject jsonWires = aJsonObject
                .optJSONObject(IJsonComposerConfigConstants.COMPONENT_WIRES);
        if (jsonWires != null) {
            resultBean.setWires(jsonObjectToMap(jsonWires));
        }

        return resultBean;
    }

    /**
     * Parses multiple components
     * 
     * @param aParentName
     *            Name of the parent component set
     * @param aJsonArray
     *            A JSON array of components
     * 
     * @return The parsed components
     * @throws JSONException
     *             Error while parsing the file
     */
    protected Collection<ComponentBean> parseComponents(
            final String aParentName, final JSONArray aJsonArray)
            throws JSONException {

        // Result list
        final List<ComponentBean> resultList = new ArrayList<ComponentBean>();

        // The length is constant
        final int arrayLen = aJsonArray.length();
        for (int i = 0; i < arrayLen; i++) {

            final ComponentBean component = parseComponent(aParentName,
                    aJsonArray.getJSONObject(i));
            if (component != null) {
                // Successfully parsed component set
                resultList.add(component);
            }
        }

        return resultList;
    }

    /**
     * Parses the given component set JSON object
     * 
     * @param aParentName
     *            Name of the parent component set
     * @param aComponentSetNode
     *            A component set representation
     * @return The ComponentsSetBean Java bean
     * @throws JSONException
     *             An error occurred while parsing the file
     */
    protected ComponentsSetBean parseComponentSet(
            final ComponentsSetBean aParent, final JSONObject aComponentSetNode)
            throws JSONException, FileNotFoundException {

        // Get the name
        final String compoSetName = aComponentSetNode
                .getString(IJsonComposerConfigConstants.COMPOSET_NAME);

        pLogger.logInfo(this, "parseComponentSet", "Name=[%s]", compoSetName);

        // Prepare the resulting component set
        final ComponentsSetBean resultSet = new ComponentsSetBean();
        resultSet.setName(compoSetName);
        resultSet.setParent(aParent);

        // Gets the "from"
        String wCompoSetFrom = null;
        if (aComponentSetNode.has(IJsonComposerConfigConstants.COMPOSET_FROM)) {
            wCompoSetFrom = aComponentSetNode
                    .getString(IJsonComposerConfigConstants.COMPOSET_FROM);
        }

        if (wCompoSetFrom != null) {

            // Read "distant" composet
            ComponentsSetBean wComposet = parseComponentSet(aParent,
                    readJsonObjectFile(wCompoSetFrom));

            resultSet.setComponents(wComposet.getAllComponents());
            resultSet.setComponentSets(wComposet.getComponentSets());

        } else {

            // Get the components
            final JSONArray components = aComponentSetNode
                    .optJSONArray(IJsonComposerConfigConstants.COMPOSET_COMPONENTS);
            if (components != null) {
                resultSet.setComponents(parseComponents(compoSetName,
                        components));
            }

            // Get the sub-sets
            final JSONArray subsets = aComponentSetNode
                    .optJSONArray(IJsonComposerConfigConstants.COMPOSET_COMPOSETS);
            if (subsets != null) {
                resultSet.setComponentSets(parseComponentSets(resultSet,
                        subsets));
            }
        }

        if (!resultSet.isEmpty()) {
            // The set contains something...
            return resultSet;
        }

        return null;
    }

    /**
     * Parses multiple component sets
     * 
     * @param aParentName
     *            Name of the parent component set
     * @param aJsonArray
     *            A JSON array of component sets
     * 
     * @return The parsed component sets
     * @throws JSONException
     *             Error while parsing the file
     */
    protected Collection<ComponentsSetBean> parseComponentSets(
            final ComponentsSetBean aParent, final JSONArray aJsonArray)
            throws JSONException, FileNotFoundException {

        // Result list
        final List<ComponentsSetBean> resultList = new ArrayList<ComponentsSetBean>();

        // The length is constant
        final int arrayLen = aJsonArray.length();
        for (int i = 0; i < arrayLen; i++) {

            final ComponentsSetBean compoSet = parseComponentSet(aParent,
                    aJsonArray.getJSONObject(i));
            if (compoSet != null) {
                // Successfully parsed component set
                resultList.add(compoSet);
            }
        }

        return resultList;
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

        pLogger.logInfo(this, "readFile", "File=[%s]",
                confFile.getAbsolutePath());

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

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo",
                "JSON Composer Configuration Reader Ready");
    }
}
