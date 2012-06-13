/**
 * File:   IIsolateDescr.java
 * Author: Thomas Calmant
 * Date:   2 sept. 2011
 */
package org.psem2m.isolates.services.conf;

import java.io.Serializable;

/**
 * Describes an isolate configuration
 * 
 * @author Thomas Calmant
 */
public interface IIsolateDescr extends Serializable {

    /**
     * Application arguments, mainly for non-OSGi Java isolates (name of the
     * Main class...) : List of strings (can be absent, null or empty)
     */
    String ISOLATE_APP_ARGS = "appArgs";

    /**
     * Multiple meanings for this one :
     * 
     * <ul>
     * <li>List of bundles to be installed in a Java OSGi isolate : List of
     * Strings (bundle symbolic names)</li>
     * <li>Name of the Python module to run (according to the PYTHONPATH
     * environment variable) : List of strings (module names). <strong>Only the
     * first entry is taken into account</string></li>
     * </ul>
     */
    String ISOLATE_BUNDLES = "bundles";

    /**
     * The isolate class path, mainly for Java isolates : List of string (can be
     * absent, null or empty)
     */
    String ISOLATE_CLASSPATH = "classpath";

    /**
     * Defines the environment variables to
     * <em>add or replace to the current variables</em> : Map String -&gt;
     * String (or null). Can be absent, null or empty.
     */
    String ISOLATE_ENVIRONMENT = "environment";

    /** Isolate ID : String (never null nor empty) */
    String ISOLATE_ID = "id";

    /** Isolate kind : String (never empty, can be null) */
    String ISOLATE_KIND = "kind";

    /** The node where to instanciate the isolate */
    String ISOLATE_NODE = "node";

    /**
     * The OSGi framework JAR file name, to be used in the Java isolate class
     * path : String (can be absent or null)
     * 
     * The OSGi framework resolution goes as follow :
     * <ol>
     * <li>File given in this entry (osgiFramework)</li>
     * <li>The default framework file name (internal constant) for the given
     * kind of isolate</li>
     * <li>The file stated in the platform.framework file, in the configuration
     * directory</li>
     * </ol>
     * 
     * If no framework JAR file can be found, the launch fails.
     */
    String ISOLATE_OSGI_FRAMEWORK = "osgiFramework";

    /** The socket port to access the signals receiver of the isolate */
    String ISOLATE_SIGNALS_PORT = "httpPort";

    /**
     * Virtual machine / interpreter arguments : List of strings (can be absent,
     * null or empty)
     */
    String ISOLATE_VM_ARGS = "vmArgs";
}
