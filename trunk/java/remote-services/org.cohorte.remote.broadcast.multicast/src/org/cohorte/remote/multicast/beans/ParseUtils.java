/**
 * File:   ParseUtils.java
 * Author: Thomas Calmant
 * Date:   1 juil. 2013
 */
package org.cohorte.remote.multicast.beans;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * JSON parsing utility class
 * 
 * @author Thomas Calmant
 */
public class ParseUtils {

    /**
     * Converts a JSON array into a list
     * 
     * @param aJSONArray
     *            A JSON array or null
     * @return A list, or null
     * @throws JSONException
     *             Error parsing the JSON object
     */
    public static List<Object> jsonToList(final JSONArray aJSONArray)
            throws JSONException {

        if (aJSONArray == null) {
            // Nothing to do
            return null;
        }

        // Prepare the list
        final List<Object> content = new LinkedList<Object>();

        for (int i = 0; i < aJSONArray.length(); i++) {
            // Get the next object
            final Object rawObject = aJSONArray.get(i);

            if (rawObject instanceof JSONObject) {
                // Got a child map
                content.add(jsonToMap((JSONObject) rawObject));

            } else if (rawObject instanceof JSONArray) {
                // Got a child collection
                content.add(jsonToList((JSONArray) rawObject));

            } else {
                // Use the read value
                content.add(rawObject);
            }
        }

        return content;
    }

    /**
     * Converts a JSON object to a map
     * 
     * @param aJSONObject
     *            A JSON object or null
     * @return A map, or null
     * @throws JSONException
     *             Error parsing the JSON object
     */
    public static Map<String, Object> jsonToMap(final JSONObject aJSONObject)
            throws JSONException {

        if (aJSONObject == null) {
            // Nothing to do
            return null;
        }

        // Prepare the map
        final Map<String, Object> content = new LinkedHashMap<String, Object>();

        @SuppressWarnings("unchecked")
        final Iterator<String> iterator = aJSONObject.keys();
        while (iterator.hasNext()) {
            // Read the next entry
            final String key = iterator.next();
            final Object rawObject = aJSONObject.get(key);

            if (rawObject instanceof JSONObject) {
                // Got a child map
                content.put(key, jsonToMap((JSONObject) rawObject));

            } else if (rawObject instanceof JSONArray) {
                // Got a child collection
                content.put(key, jsonToList((JSONArray) rawObject));

            } else {
                // Use the read value
                content.put(key, rawObject);
            }
        }

        return content;
    }

    /**
     * Hidden constructor
     */
    private ParseUtils() {

        // Hidden constructor
    }
}
