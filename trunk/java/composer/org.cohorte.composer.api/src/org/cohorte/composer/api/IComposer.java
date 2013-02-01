/**
 * File:   IComposer.java
 * Author: "Thomas Calmant"
 * Date:   29 janv. 2013
 */
package org.cohorte.composer.api;

import java.util.Map;

/**
 * @author "Thomas Calmant"
 * 
 */
public interface IComposer {

    Map<String, Object> getComposition(String aCompositionUID);

    String load(Map<String, Object> aCompositionMap);

    boolean unload(String aCompositionUID);
}
