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

    /** Extra "standard" configuration folders */
    String EXTRA_CONF_FOLDERS[] = { "/etc/psem2m", "/etc/default/psem2m" };

    /** Main configuration file */
    String FILE_MAIN_CONF = "platform.xml";

    /** Configuration sub-directory */
    String SUBDIR_CONF = "conf";

    /** PSEM2M base system property */
    String SYSTEM_PSEM2M_BASE = "psem2m.base";

    /** PSEM2M home system property */
    String SYSTEM_PSEM2M_HOME = "psem2m.home";
}
