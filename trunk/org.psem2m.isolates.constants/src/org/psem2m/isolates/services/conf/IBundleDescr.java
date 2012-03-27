/**
 * File:   IBundleDescr.java
 * Author: Thomas Calmant
 * Date:   2 sept. 2011
 */
package org.psem2m.isolates.services.conf;

import java.io.Serializable;

/**
 * Describes a bundle
 * 
 * @author Thomas Calmant
 */
public interface IBundleDescr extends Serializable {

    /**
     * The file path as indicated in the configuration file : String. Can be
     * null.
     */
    String BUNDLE_FILE = "file";

    /**
     * The bundle symbolic name : String
     */
    String BUNDLE_NAME = "symbolicName";

    /**
     * If true, the isolate state is valid even if this bundle is not present :
     * Boolean False by default.
     */
    String BUNDLE_OPTIONAL = "optional";

    /**
     * The set of properties declared in the bundle description. (Map String
     * -&gt; String)
     */
    String BUNDLE_PROPERTIES = "properties";

    /**
     * The bundle version as indicated in the configuration file : String. Can
     * be null.
     */
    String BUNDLE_VERSION = "version";
}
