/**
 * File:   IJsonConfigKeys.java
 * Author: Thomas Calmant
 * Date:   6 sept. 2011
 */
package org.psem2m.isolates.config.json;

/**
 * Defines the JSON configuration file keys
 * 
 * @author Thomas Calmant
 */
public interface IJsonConfigKeys {

    /** The application ID (string, mandatory) */
    String CONFIG_APP_ID = "appId";

    /** Bundle file path (string, optional) */
    String CONFIG_BUNDLE_FILE = "file";

    /** Bundle symbolic name (string, mandatory) */
    String CONFIG_BUNDLE_NAME = "symbolicName";

    /** Bundle is optional (boolean, optional) */
    String CONFIG_BUNDLE_OPTIONAL = "optional";

    /** Bundle associated system properties (string, optional) */
    String CONFIG_BUNDLE_PROPERTIES = "properties";

    /** Bundle version (string, optional) */
    String CONFIG_BUNDLE_VERSION = "version";

    /** Inclusion keyword */
    String CONFIG_FROM = "from";

    /** Application arguments (string array, optional) */
    String CONFIG_ISOLATE_APP_ARGS = "appArgs";

    /** Isolate bundles array (array, mandatory) */
    String CONFIG_ISOLATE_BUNDLES = "bundles";

    /** Isolate class path (array, optional) */
    String CONFIG_ISOLATE_CLASSPATH = "classpath";

    /** The isolate process environment variables (map, optional) */
    String CONFIG_ISOLATE_ENVIRONMENT = "environment";

    /** The HTTP server host */
    String CONFIG_ISOLATE_HOST = "host";

    /** Isolate ID (string, mandatory) */
    String CONFIG_ISOLATE_ID = "id";

    /** Isolate kind (to be handled by the forker and the bootstrap) */
    String CONFIG_ISOLATE_KIND = "kind";

    /** The OSGi framework JAR file (string, optional) */
    String CONFIG_ISOLATE_OSGI_FRAMEWORK = "osgiFramework";

    /** The HTTP server port (used for Jetty and Signals-HTTP */
    String CONFIG_ISOLATE_PORT = "httpPort";

    /** Isolate Java virtual machine arguments (string array, optional) */
    String CONFIG_ISOLATE_VMARGS = "vmArgs";

    /** Application isolates array (array, mandatory) */
    String CONFIG_ISOLATES_ARRAY = "isolates";

    /** Bundle "from" associated overridden system properties (string, optional) */
    String CONFIG_OVERRIDDEN_PROPERTIES = "overriddenProperties";
}
