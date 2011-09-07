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

    /** Bundle version (string, optional) */
    String CONFIG_BUNDLE_VERSION = "version";

    /** Inclusion keyword */
    String CONFIG_FROM = "from";

    /** Isolate bundles array (array, mandatory) */
    String CONFIG_ISOLATE_BUNDLES = "bundles";

    /** Isolate ID (string, mandatory) */
    String CONFIG_ISOLATE_ID = "id";

    /** Isolate kind (to be handled by the forker and the bootstrap) */
    String CONFIG_ISOLATE_KIND = "kind";

    /** Isolate Java virtual machine arguments (string array, optional) */
    String CONFIG_ISOLATE_VMARGS = "vmArgs";

    /** Application isolates array (array, mandatory) */
    String CONFIG_ISOLATES_ARRAY = "isolates";
}
