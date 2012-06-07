/**
 * File:   IMasterManagerConfig.java
 * Author: Thomas Calmant
 * Date:   21 juil. 2011
 */
package org.psem2m.isolates.master.manager;

/**
 * Constants and default values for the Master.Manager bundle configuration
 * 
 * @author Thomas Calmant
 */
public interface IMasterManagerConfig {

    /** Isolate JVM arguments (in isolate name space) */
    String ISOLATE_ARGS = "isolate.arguments";

    /** Isolate bundles list (in isolate name space) */
    String ISOLATE_BUNDLES_LIST = "bundles";

    /** Isolate framework bundle (in isolate name space) */
    String ISOLATE_FRAMEWORK = "framework";

    /** Isolate IDs list (in manager name space) */
    String ISOLATE_ID_LIST = "isolates.list";

    /** Prefix in configuration keys */
    String MANAGER_NAMESPACE = "psem2m.master.manager";
}
