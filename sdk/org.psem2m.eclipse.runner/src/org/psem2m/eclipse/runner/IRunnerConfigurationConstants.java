/**
 * File:   IRunnerConfigurationConstants.java
 * Author: Thomas Calmant
 * Date:   23 août 2011
 */
package org.psem2m.eclipse.runner;

/**
 * Constants of the PSEM2M Runner launch configuration
 * 
 * @author Thomas Calmant
 */
public interface IRunnerConfigurationConstants {

	/** Number of remote debug configurations to prepare */
	String DEBUG_COUNT = "org.psem2m.runner.debug.count";

	/** Base remote debug port */
	String DEBUG_PORT = "org.psem2m.runner.debug.port";

	/** Default number of debug configurations */
	int DEFAULT_DEBUG_COUNT = 3;

	/** Default remote debug port */
	int DEFAULT_DEBUG_PORT = 8000;

	/** Plug-ins export output folder */
	String EXPORT_OUTPUT_FOLDER = "org.psem2m.runner.output_folder";

	/** PSEM2M Home */
	String PSEM2M_BASE = "org.psem2m.base";

	/** PSEM2M Home */
	String PSEM2M_HOME = "org.psem2m.home";

	/** Runner working directory */
	String WORKING_DIRECTORY = "org.psem2m.runner.working_directory";
}
