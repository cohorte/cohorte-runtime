/**
 * File:   SignalResult.java
 * Author: Thomas Calmant
 * Date:   13 juin 2012
 */
package org.psem2m.signals;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains the result of the treatment of a signal by a signal receiver.
 * 
 * @author Thomas Calmant
 */
public class SignalResult {

    /** Result map key : result code (int) */
    public static final String KEY_CODE = "code";

    /** Result map key : message (String) */
    public static final String KEY_MESSAGE = "message";

    /** Result map key : results (Object[]) */
    public static final String KEY_RESULTS = "results";

    /** The result code */
    private final int pCode;

    /** The map result */
    private final Map<String, Object> pResult = new HashMap<String, Object>();

    /**
     * Sets up the result
     * 
     * @param aCode
     *            The result code
     * @param aResults
     *            The listeners results
     */
    public SignalResult(final int aCode, final Collection<Object> aResults) {

        pCode = aCode;

        if (aResults != null) {
            makeMap("", aResults.toArray());

        } else {
            makeMap("", null);
        }
    }

    /**
     * Sets up the result
     * 
     * @param aCode
     *            The result code
     * @param aResults
     *            The listeners results
     */
    public SignalResult(final int aCode, final Object[] aResults) {

        pCode = aCode;
        makeMap("", aResults);
    }

    /**
     * Sets up the result
     * 
     * @param aCode
     *            The result code
     * @param aMessage
     *            A message
     */
    public SignalResult(final int aCode, final String aMessage) {

        pCode = aCode;
        makeMap(aMessage, null);
    }

    /**
     * Construct the result from a map
     * 
     * @param aMap
     *            A result map
     */
    public SignalResult(final Map<String, Object> aMap) {

        pCode = (Integer) aMap.get("code");
        pResult.putAll(aMap);
    }

    /**
     * Retrieves the result code
     * 
     * @return the code
     */
    public int getCode() {

        return pCode;
    }

    /**
     * Retrieves the result message
     * 
     * @return the result message
     */
    public String getMessage() {

        return (String) pResult.get("message");
    }

    /**
     * Returns the result map
     * 
     * @return The result map
     */
    public Map<String, Object> getResult() {

        return pResult;
    }

    /**
     * Retrieves the listeners results
     * 
     * @return The listeners results (can be null)
     */
    public Object[] getResults() {

        return (Object[]) pResult.get("results");
    }

    /**
     * Prepares the result map
     * 
     * @param aMessage
     *            The result message
     * @param aResults
     *            The listeners results
     */
    private void makeMap(final String aMessage, final Object[] aResults) {

        pResult.put(KEY_CODE, pCode);
        pResult.put(KEY_MESSAGE, aMessage);
        pResult.put(KEY_RESULTS, aResults);
    }
}
