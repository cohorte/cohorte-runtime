/**
 * File:   IPlatformConfigurationConstants.java
 * Author: Thomas Calmant
 * Date:   21 juil. 2011
 */
package org.psem2m.isolates.config;

/**
 * PSEM2M configuration constants
 * 
 * @author Thomas Calmant
 */
public interface IPlatformConfigurationConstants {

    /**
     * Extra "standard" configuration folders, in descending priority : the
     * first one contains default values, overridden by the next one and so on.
     */
    String EXTRA_CONF_FOLDERS[] = { "/etc/default/psem2m", "/etc/psem2m" };

    /** Main configuration file */
    String FILE_MAIN_CONF = "platform.xml";

    /** Configuration sub-directory */
    String SUBDIR_CONF = "conf";

    /** PSEM2M base system property */
    String SYSTEM_PSEM2M_BASE = "psem2m.base";

    /** PSEM2M home system property */
    String SYSTEM_PSEM2M_HOME = "psem2m.home";
}
