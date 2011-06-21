/**
 * File:   FelixRunner.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.forker.impl.runners;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.psem2m.isolates.commons.IBundleRef;
import org.psem2m.isolates.commons.Utilities;

/**
 * @author Thomas Calmant
 * 
 */
public class FelixRunner extends AbstractOSGiRunner {

    /** Felix profile configuration directory */
    public static final String FELIX_CONF_DIRECTORY = "conf";

    /** Felix configuration file name */
    public static final String FELIX_CONF_FILE = "config.properties";

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.forker.impl.runners.AbstractOSGiRunner#getArguments()
     */
    @Override
    protected List<String> getArguments() {
	// No specific arguments added
	return Arrays.asList(getIsolateConfiguration().getArguments());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.forker.impl.runners.AbstractOSGiRunner#getEnvironment
     * ()
     */
    @Override
    protected Map<String, String> getEnvironment() {
	// No specific environment
	return getIsolateConfiguration().getEnvironment();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.forker.impl.runners.AbstractOSGiRunner#getMainBundle
     * ()
     */
    @Override
    protected IBundleRef getMainBundle() {

	List<String> possibleNames = new ArrayList<String>();
	possibleNames.add("org.apache.felix.main-3.2.2.jar");
	possibleNames.add("org.felix.main.jar");
	possibleNames.add("felix.jar");

	return Utilities.findBundle(getPlatformConfiguration(),
		possibleNames.toArray(new String[0]));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.forker.impl.runners.AbstractOSGiRunner#prepareProfile
     * ()
     */
    @Override
    protected void prepareProfile(final File aWorkingDirectory)
	    throws Exception {

	// Make the configuration file
	final String felixConfigurationFolder = aWorkingDirectory
		+ File.separator + FELIX_CONF_DIRECTORY;

	final String felixConfigurationFile = felixConfigurationFolder
		+ File.separator + FELIX_CONF_FILE;

	// Make the configuration folder
	Utilities.makeDirectory(felixConfigurationFolder);

	// Make the configuration file
	Properties felixProperties = new Properties();

	// Level 2 bundles = platform ones
	felixProperties.put("felix.auto.start.2", Utilities.join(" ",
		getPlatformConfiguration().getCommonBundles()));

	// Level 4 bundles = isolate bundles
	felixProperties.put("felix.auto.start.4",
		Utilities.join(" ", getIsolateConfiguration().getBundles()));

	// Start level 4
	felixProperties.put("org.osgi.framework.startlevel.beginning", "4");

	// Store the file
	felixProperties.store(new FileOutputStream(felixConfigurationFile), "");
    }
}
