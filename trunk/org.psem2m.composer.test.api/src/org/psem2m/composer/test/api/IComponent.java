/**
 * File:   IComponent.java
 * Author: Thomas Calmant
 * Date:   4 nov. 2011
 */
package org.psem2m.composer.test.api;

import java.util.Map;

/**
 * Represents a member of a treatment chain
 * 
 * @author Thomas Calmant
 */
public interface IComponent {

    /** Queued channel */
    String CHANNEL_TYPE_DEQUEUE = "queue";

    /** Map channel */
    String CHANNEL_TYPE_MAP = "map";

    /**
     * Data key to represent a error
     * 
     * Value type : Throwable or String (depends if the caught exception was
     * serializable or not)
     */
    String KEY_ERROR = "error";

    /**
     * Data key to represent a result
     * 
     * Value type : implementation dependent
     */
    String KEY_RESULT = "result";

    /** ID of the next component field */
    String NEXT_FIELD_ID = "next";

    /** The instance name component property */
    String PROPERTY_INSTANCE_NAME = "instance.name";

    /**
     * The component does its job and updates the data map, according to
     * standard keys.
     * 
     * @param aData
     *            The transferred data. The value should be serializable.
     * @return The treated data map (must never be null)
     * @throws Exception
     *             Something wrong occurred during the treatment
     */
    Map<String, Object> computeResult(Map<String, Object> aData)
            throws Exception;
}
