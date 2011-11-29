/**
 * File:   IComponent.java
 * Author: Thomas Calmant
 * Date:   4 nov. 2011
 */
package org.psem2m.composer.demo;

/**
 * Represents a member of a treatment chain
 * 
 * @author Thomas Calmant
 */
public interface IComponent {

    /**
     * Data key to represent a error
     * 
     * Value type : Throwable or String (depends if the caught exception was
     * serializable or not)
     */
    String KEY_ERROR = "error";

    /**
     * Data key to represent a treatment request
     * 
     * Value type : implementation dependent (should be a map)
     */
    String KEY_REQUEST = "request";

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
     * The component does its job and returns a new context or a modified one.
     * Implementations must never return null.
     * 
     * @param aContext
     *            The transferred data. The value should be serializable.
     * @return The treated data map (must never be null)
     * @throws Exception
     *             Something wrong occurred during the treatment
     */
    IComponentContext computeResult(IComponentContext aContext)
            throws Exception;
}
