/**
 * File:   IComponentContext.java
 * Author: Thomas Calmant
 * Date:   16 nov. 2011
 */
package org.psem2m.composer.test.api;

import java.util.List;
import java.util.Map;

/**
 * @author Thomas Calmant
 * 
 */
public interface IComponentContext {

    /**
     * RAW identifier : contains the RAW data sent by the caller
     **/
    String META_REQUEST_RAW = "META_REQUEST_RAW";

    /**
     * RAW identifier : contains the RAW data returned by the server
     **/
    String META_RESPONSE_RAW = "META_RESPONSE_RAW";

    /**
     * CRITERIAS identifier : allows the key builder to retrieve the map which
     * contains the criteria values in the Request map
     */
    String REQUEST_CRITERIA = "REQUEST_CRITERIA";

    /**
     * KEY identifier : allows the key builder to retrieve the map which
     * contains key parts in the Request map
     **/
    String REQUEST_KEY = "REQUEST_KEY";

    /**
     * KEYS identifier : allows the key builder to retrieve the list which
     * contains key map in the Request map
     **/
    String REQUEST_KEYS = "REQUEST_KEYS";

    /**
     * Add an error to the context
     * 
     * @param aComponentName
     *            The name of the component storing the error
     * @param aErrorMessage
     *            An error description
     */
    void addError(CharSequence aComponentName, CharSequence aErrorMessage);

    /**
     * Formats an error description, containing the given message and the string
     * representation of the given throwable, if any
     * 
     * @param aComponentName
     *            The name of the component storing the error
     * @param aErrorMessage
     *            An error description
     * @param aThrowable
     *            The source throwable
     */
    void addError(CharSequence aComponentName, CharSequence aErrorMessage,
            Throwable aThrowable);

    /**
     * Appends a result to the list.
     * 
     * Does nothing if aResult is null.
     * 
     * @param aResult
     *            A result to be added, ignored if null.
     */
    void addResult(Map<String, Object> aResult);

    /**
     * Retrieve the list of errors (in the insertion order)
     * 
     * @return The list of errors
     */
    List<String> getErrors();

    /**
     * Retrieves the meta data associated to the treatment, i.e. any data that
     * may be needed by further components but that is not part of the request
     * nor of the result.
     * 
     * @return The meta data associated to the treatment
     */
    Map<String, Object> getMetadata();

    /**
     * Retrieves the treatment entry request
     * 
     * @return The treatment entry request
     */
    Map<String, Object> getRequest();

    /**
     * Retrieves the treatment result data
     * 
     * @return The current treatment result
     */
    List<Map<String, Object>> getResults();

    /**
     * Simply tests if the context contains at least one error
     * 
     * @return True if at least one error has been stored in the context
     */
    boolean hasError();

    /**
     * Simply tests if the context contains at least one result in the result
     * list
     * 
     * @return True if at least one result has been stored in the context
     */
    boolean hasResult();

    /**
     * Empties the result list and adds the given result.
     * 
     * Does nothing if aResult is null
     * 
     * @param aResult
     *            The only result to set, ignore if null
     */
    void setResult(Map<String, Object> aResult);
}
