/**
 * File:   IAgent.java
 * Author: Thomas Calmant
 * Date:   18 oct. 2013
 */
package org.cohorte.composer.api;

import java.util.Set;

/**
 * Specification of a Composer Agent
 * 
 * @author Thomas Calmant
 */
public interface IAgent {

    /**
     * Tries to instantiate the given components immediately and stores the
     * remaining ones to instantiate them as soon as possible
     * 
     * @param aComponents
     *            A set of RawComponent beans
     * @return The immediately instantiated components
     */
    Set<RawComponent> handle(Set<RawComponent> aComponents);

    /**
     * Kills the component with the given name
     * 
     * @param aName
     *            Name of the component to kill
     */
    void kill(String aName);
}
