/**
 * File:   IPyBridge.java
 * Author: Thomas Calmant
 * Date:   17 janv. 2013
 */
package org.cohorte.pyboot.api;

import java.util.List;

/**
 * Represents a Python-side boot orders service
 * 
 * @author Thomas Calmant
 */
public interface IPyBridge {

    /**
     * Logs the given message at debug level
     * 
     * @param aMessage
     *            A message
     * @param aValues
     *            Values to inject in the message
     */
    void debug(String aMessage, String... aValues);

    /**
     * Logs the given message at error level
     * 
     * @param aMessage
     *            A message
     * @param aValues
     *            Values to inject in the message
     */
    void error(String aMessage, String... aValues);

    /**
     * Retrieves the components to instantiate
     * 
     * @return An array of components, or null
     */
    List<ComponentBean> getComponents();

    /**
     * Called when a component has been started
     * 
     * @param aName
     *            Name of the component
     */
    void onComponentStarted(String aName);

    /**
     * Called when an error has occurred
     * 
     * @param aError
     *            An error message
     */
    void onError(String aError);
}
