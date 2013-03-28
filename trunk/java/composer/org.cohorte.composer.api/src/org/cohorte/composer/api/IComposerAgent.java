/**
 * File:   IComposerAgent.java
 * Author: Thomas Calmant
 * Date:   28 mars 2013
 */
package org.cohorte.composer.api;

import java.util.Collection;
import java.util.Map;

/**
 * Specification of a Composer Agent
 * 
 * @author Thomas Calmant
 */
public interface IComposerAgent {

    /**
     * Retrieves an array of all factories accessible by this agent
     * 
     * @return All factories accessible by this agent
     */
    String[] get_factories();

    /**
     * Retrieves the UID and the name of the isolate hosting this agent
     * 
     * @return An array: [isolate UID, isolate name]
     */
    String[] get_isolate();

    /**
     * Instantiates the given components. The result lists only contains
     * components names, without compositions names.
     * 
     * @param aComponents
     *            A list of component descriptions
     * @param aUntilPossible
     *            If True, every component whom factory is missing will be kept
     *            until its instantiation can be done; else, those components
     *            are considered failing.
     */
    void instantiate(Collection<Map<String, Object>> aComponents,
            boolean aUntilPossible);

    /**
     * Tests if the given component is running and returns 0 if the component is
     * unknown, 1 if it running, and -1 if a component with the same name but a
     * different factory is running. The latter only works if the given
     * parameter is a component description.
     * 
     * @param aComponent
     *            A component description
     * @return One of -1, 0, 1
     */
    int is_running(Map<String, Object> aComponent);

    /**
     * Tests if the given component is running and returns 0 if the component is
     * unknown, 1 if it running, and -1 if a component with the same name but a
     * different factory is running. The latter only works if the given
     * parameter is a component description.
     * 
     * @param aName
     *            A component instance name
     * @return One of -1, 0, 1
     */
    int is_running(String aName);

    /**
     * Kills the given components
     * 
     * @param aComponentNames
     *            A list of component names
     * @return A 2D array: [killed components names[], unknown components[]]
     */
    String[][] kill(Collection<String> aComponentNames);
}
