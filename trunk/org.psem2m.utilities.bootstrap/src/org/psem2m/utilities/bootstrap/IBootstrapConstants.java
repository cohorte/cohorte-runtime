/**
 * File:   IBootstrapConstants.java
 * Author: tcalmant
 * Date:   6 juil. 2011
 */
package org.psem2m.utilities.bootstrap;

/**
 * @author tcalmant
 * 
 */
public interface IBootstrapConstants {

    /** Bootstrap properties prefix */
    String PROPERTY_PREFIX = "org.psem2m.bootstrap.";
    /** Help command */
    public static final String HELP_COMMAND = "--help";
    /** Read lines from input */
    public static final String READ_LINES_COMMAND = "--read-input-lines";
    /** Read array object from input */
    public static final String UNSERIALIZE_COMMAND = "--unserialize-input";
    /** Framework configuration key */
    public static final String CONFIG_FRAMEWORK = PROPERTY_PREFIX
            + "osgi.framework";

}
