/**
 * File:   ISignalDirectoryConstants.java
 * Author: Thomas Calmant
 * Date:   21 juin 2012
 */
package org.psem2m.signals;

/**
 * Defines constants for the Signals Directory and Directory Updater
 * 
 * @author Thomas Calmant
 */
public interface ISignalDirectoryConstants {

    /** The dumper port system property */
    String PROP_DUMPER_PORT = "psem2m.directory.dumper.port";

    /**
     * Special case for early starting forkers, when a monitor signals its dump
     * port
     */
    String SIGNAL_CONTACT = ISignalDirectoryConstants.SIGNAL_PREFIX + "/contact";

    /** Directory dump request */
    String SIGNAL_DUMP = ISignalDirectoryConstants.SIGNAL_PREFIX + "/dump";

    /** Prefix of all directory updater signals */
    String SIGNAL_PREFIX = "/psem2m-directory-updater";

    /** Pattern to match all directory updater signals */
    String SIGNAL_PREFIX_MATCH_ALL = ISignalDirectoryConstants.SIGNAL_PREFIX
            + "/*";

    /** Isolate registration notification */
    String SIGNAL_REGISTER = ISignalDirectoryConstants.SIGNAL_PREFIX
            + "/register";
}
