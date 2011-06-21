/**
 * File:   IIsolateConfiguration.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.commons;

import java.util.Map;

/**
 * Description of an isolate
 * 
 * @author Thomas Calmant
 */
public interface IIsolateConfiguration {

    /**
     * Kind of isolated process
     * 
     * @author Thomas Calmant
     */
    enum IsolateKind {
	/** Equinox framework isolate */
	EQUINOX,
	/** Felix framework isolate */
	FELIX,
	/** Standard Java process */
	JAVA,
    }

    /**
     * Retrieves the arguments to be added when calling the process
     * 
     * @return The isolate process arguments
     */
    String[] getArguments();

    /**
     * Retrieves the bundles needed in the isolate
     * 
     * @return the bundles needed in the isolate
     */
    IBundleRef[] getBundles();

    /**
     * Retrieves the environment variables of the isolated process
     * 
     * @return the environment of the isolated process
     */
    Map<String, String> getEnvironment();

    /**
     * Retrieves the isolate ID
     * 
     * @return The isolate ID
     */
    String getId();

    /**
     * Retrieves the isolated process kind
     * 
     * @return The isolate kind
     */
    IsolateKind getKind();
}
