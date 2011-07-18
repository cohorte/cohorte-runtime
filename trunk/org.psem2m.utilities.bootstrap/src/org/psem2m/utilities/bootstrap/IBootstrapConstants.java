/**
 * File:   IBootstrapConstants.java
 * Author: tcalmant
 * Date:   6 juil. 2011
 */
package org.psem2m.utilities.bootstrap;

/**
 * Common bootstrap constants
 * 
 * @author Thomas Calmant
 */
public interface IBootstrapConstants {

    /** Framework configuration key */
    String CONFIG_FRAMEWORK = IBootstrapConstants.PROPERTY_PREFIX
            + "osgi.framework";

    /** Help command */
    String HELP_COMMAND = "--help";

    /** Bootstrap properties prefix */
    String PROPERTY_PREFIX = "org.psem2m.bootstrap.";

    /** Read lines from input */
    String READ_LINES_COMMAND = "--read-input-lines";

    /** Read array object from input */
    String UNSERIALIZE_COMMAND = "--unserialize-input";

}
