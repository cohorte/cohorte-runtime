/**
 * File:   FelixForker.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.forker.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.psem2m.isolates.commons.PlatformConfiguration;
import org.psem2m.isolates.commons.forker.ProcessConfiguration;
import org.psem2m.isolates.forker.Activator;

/**
 * Starts Felix frameworks
 * 
 * @author Thomas Calmant
 */
public class FelixForker extends AbstractForker {

    /** Felix profile configuration directory */
    public static final String FELIX_CONF_DIRECTORY = File.separator + "conf";

    /** Felix configuration file name */
    public static final String FELIX_CONF_FILE = "config.properties";

    /** Felix Jar name */
    public static final String FELIX_JAR = "org.apache.felix.main-3.2.2.jar";

    /**
     * Initiates the forker
     */
    public FelixForker() {
	// Do nothing (here for iPOJO)
    }

    /**
     * Copies a file with Java NIO.
     * 
     * @param aSourceFile
     *            Source file
     * @param aDestFile
     *            Destination file (created if needed)
     * @throws IOException
     *             An error occurred while copying
     */
    public void copyFile(final File aSourceFile, final File aDestFile)
	    throws IOException {

	if (!aDestFile.exists()) {
	    aDestFile.createNewFile();
	}

	FileChannel source = null;
	FileChannel destination = null;
	try {
	    source = new FileInputStream(aSourceFile).getChannel();
	    destination = new FileOutputStream(aDestFile).getChannel();
	    destination.transferFrom(source, 0, source.size());

	} finally {
	    if (source != null) {
		source.close();
	    }

	    if (destination != null) {
		destination.close();
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.forker.impl.AbstractForker#doRunProcess(org.psem2m
     * .isolates.commons.IPlatformConfiguration,
     * org.psem2m.isolates.commons.forker.IProcessConfiguration)
     */
    @Override
    protected Process doRunProcess(
	    final PlatformConfiguration aPlatformConfiguration,
	    final ProcessConfiguration aProcessConfiguration)
	    throws IOException {

	Activator.getLogger().logDebug(this, "runProcess", "Felix to run in ",
		aProcessConfiguration.getWorkingDirectory());

	// Prepare the Felix profile
	try {
	    prepareProfile(aPlatformConfiguration, aProcessConfiguration);

	} catch (IOException e) {
	    Activator.getLogger().logSevere(this, "runFelix",
		    "Error preparing Felix profile : ", e);
	    return null;
	}

	// Get names
	final String java = PlatformConfiguration.getJavaPath();

	// Try in the platform directory
	File felixFile = new File(aPlatformConfiguration.getPlatformDirectory()
		+ File.separator + FELIX_JAR);

	// Try in the repository directory
	if (!felixFile.exists()) {
	    felixFile = new File(
		    aPlatformConfiguration.getRepositoryDirectory()
			    + File.separator + FELIX_JAR);
	}

	if (!felixFile.exists()) {
	    throw new FileNotFoundException("Can't find Felix Jar at : "
		    + felixFile);
	}

	// Prepare the process builder
	ProcessBuilder processBuilder = new ProcessBuilder(java, "-jar",
		felixFile.getAbsolutePath());

	Activator.getLogger().logDebug(this, "runFelix", "Command : ",
		processBuilder.command());

	// Set the working directory
	final String workingDir = aProcessConfiguration.getWorkingDirectory();
	if (workingDir != null) {
	    processBuilder.directory(makeDirectory(workingDir));
	}

	Activator.getLogger().logDebug(this, "runFelix", "Working dir : ",
		processBuilder.directory());

	// Set the environment
	Map<String, String> environmentMap = processBuilder.environment();
	environmentMap.putAll(aProcessConfiguration.getEnvironment());

	// Run
	return processBuilder.start();
    }

    /**
     * String joining operation, like in Python str.join().
     * 
     * @param aJoinSequence
     *            Sequence between strings
     * @param aJoinedStrings
     *            Strings to be joined
     * @return Joined strings
     */
    public String join(final String aJoinSequence,
	    final String... aJoinedStrings) {

	StringBuilder builder = new StringBuilder();
	boolean first = true;

	for (String str : aJoinedStrings) {

	    if (!first) {
		builder.append(aJoinSequence);
	    } else {
		first = false;
	    }

	    builder.append("file://" + str);
	}

	return builder.toString();
    }

    /**
     * Writes the Felix configuration file
     * 
     * @param aWorkingDirectory
     *            The process working directory
     * @param aPlatformBundles
     *            Standard bundles for isolates
     * @param aIsolateBundles
     *            Bundles to be installed for the given isolate
     * @throws IOException
     *             An error occurred while writing the property file
     */
    private void makeConf(final String aWorkingDirectory,
	    final String[] aPlatformBundles, final String[] aIsolateBundles)
	    throws IOException {

	final String felixConfigurationFolder = aWorkingDirectory
		+ FELIX_CONF_DIRECTORY;

	final String felixConfigurationFile = felixConfigurationFolder
		+ File.separator + FELIX_CONF_FILE;

	// Make the configuration folder
	makeDirectory(felixConfigurationFolder);

	// Make the configuration file
	Properties felixProperties = new Properties();

	// Level 2 bundles = platform ones
	felixProperties.put("felix.auto.start.2", join(" ", aPlatformBundles));

	// Level 4 bundles = isolate bundles
	felixProperties.put("felix.auto.start.4", join(" ", aIsolateBundles));

	// Start level 4
	felixProperties.put("org.osgi.framework.startlevel.beginning", "4");

	// Store the file
	felixProperties.store(new FileOutputStream(felixConfigurationFile), "");
    }

    /**
     * Prepares the Felix profile folder
     * 
     * @param aPlatformConfiguration
     *            Platform configuration
     * @param aProcessConfiguration
     *            Process configuration
     * @throws FileNotFoundException
     *             An isolate bundle is not available
     * @throws IOException
     *             An error occurred while writing the configuration file
     */
    private void prepareProfile(
	    final PlatformConfiguration aPlatformConfiguration,
	    final ProcessConfiguration aProcessConfiguration)
	    throws IOException {

	// Store constant data
	final String repository = aPlatformConfiguration
		.getRepositoryDirectory();

	// Verify that needed bundles are in the repository
	final Set<String> repoBundlePaths = new HashSet<String>();

	for (String bundle : aProcessConfiguration.getIsolateConfiguration()
		.getBundles()) {

	    File repoBundleFile = new File(bundle);

	    if (!repoBundleFile.exists()) {
		repoBundleFile = new File(repository, bundle);
	    }

	    if (!repoBundleFile.exists()) {
		throw new FileNotFoundException("Can't find bundle '" + bundle
			+ "'");
	    }

	    repoBundlePaths.add(repoBundleFile.getAbsolutePath());
	}

	// Make the working directory
	makeDirectory(aProcessConfiguration.getWorkingDirectory());

	// Make the configuration
	makeConf(aProcessConfiguration.getWorkingDirectory(),
		aPlatformConfiguration.getCommonBundles(),
		repoBundlePaths.toArray(new String[0]));
    }
}
