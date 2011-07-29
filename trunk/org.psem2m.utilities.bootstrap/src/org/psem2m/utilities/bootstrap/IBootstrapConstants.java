/**
 * File:   IBootstrapConstants.java
 * Author: Thomas Calmant
 * Date:   6 juil. 2011
 */
package org.psem2m.utilities.bootstrap;

/**
 * Common bootstrap constants
 * 
 * @author Thomas Calmant
 */
public interface IBootstrapConstants {

    /** Bootstrap properties prefix */
    String BOOTSTRAP_PROPERTY_PREFIX = IBootstrapConstants.PLATFORM_PROPERTY_PREFIX
	    + ".bootstrap.";

    /** Framework configuration key */
    String CONFIG_FRAMEWORK = IBootstrapConstants.BOOTSTRAP_PROPERTY_PREFIX
	    + "osgi.framework";

    /** Help command */
    String HELP_COMMAND = "help";

    /** Output human readable lines, not serialized ones */
    String HUMAN_OUTPUT_FLAG = "human";

    /** Platform property prefix */
    String PLATFORM_PROPERTY_PREFIX = "org.psem2m";

    /** Read lines from the given file */
    String READ_FROM_FILE = "file";

    /** Read lines from input */
    String READ_LINES_COMMAND = "lines";

    /** Test command (writes a serialized URL array in a file) */
    String TEST_COMMAND = "test";

    /** Read array object from input */
    String UNSERIALIZE_COMMAND = "serialized";
}
