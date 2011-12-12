/**
 * File:   IBootstrapConstants.java
 * Author: Thomas Calmant
 * Date:   6 juil. 2011
 */
package org.psem2m.isolates.constants.boot;

/**
 * Common bootstrap constants
 * 
 * @author Thomas Calmant
 */
public interface IBootstrapConstants {

    /** Bootstrap properties prefix */
    String BOOTSTRAP_PROPERTY_PREFIX = IBootstrapConstants.PLATFORM_PROPERTY_PREFIX
	    + ".bootstrap.";

    /** Bundle symbolic name Manifest attribute name */
    String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";

    /** Framework configuration key */
    String CONFIG_FRAMEWORK = IBootstrapConstants.BOOTSTRAP_PROPERTY_PREFIX
	    + "osgi.framework";

    /** Platform bundles list file */
    String FILE_PLATFORM_BUNDLES = "psem2m.bundles";

    /** Help command */
    String HELP_COMMAND = "help";

    /** Output human readable lines, not serialized ones */
    String HUMAN_OUTPUT_FLAG = "human";

    /** PSEM2M Configuration directory */
    String PLATFORM_CONFIGURATION_DIR = "conf";

    /** Platform property prefix */
    String PLATFORM_PROPERTY_PREFIX = "org.psem2m";

    /** Platform base and home repository directory name */
    String PLATFORM_REPOSITORY_DIR = "repo";
}
