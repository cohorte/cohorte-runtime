/**
 * File:   IMasterManagerConfig.java
 * Author: Thomas Calmant
 * Date:   21 juil. 2011
 */
package org.psem2m.isolates.master.manager;

/**
 * @author Thomas Calmant
 * 
 */
public interface IMasterManagerConfig {

    /** Isolate bundles list (in isolate name space) */
    String ISOLATE_BUNDLES_LIST = "bundles";

    /** Isolate framework bundle (in isolate name space) */
    String ISOLATE_FRAMEWORK = "framework";

    /** Isolate IDs list (in manager name space) */
    String ISOLATE_ID_LIST = "isolate.list";

    /** Prefix in configuration keys */
    String MANAGER_NAMESPACE = "psem2m.master.manager";
}
