/**
 * File:   IIsolateComposer.java
 * Author: Thomas Calmant
 * Date:   18 oct. 2013
 */
package org.cohorte.composer.api;

import java.util.Set;

/**
 * Specification of an Isolate Composer service
 * 
 * @author Thomas Calmant
 */
public interface IIsolateComposer {

    /**
     * Returns a bean that describes this isolate
     * 
     * @return A bean that describes this isolate
     */
    Isolate get_isolate_info();

    /**
     * Instantiates the given components
     * 
     * @param aComponents
     *            A set of RawComponent beans
     */
    void instantiate(Set<RawComponent> aComponents);

    /**
     * Kills the components with the given names
     * 
     * @param aNames
     *            Names of the components to kill
     */
    void kill(Set<String> aNames);
}
