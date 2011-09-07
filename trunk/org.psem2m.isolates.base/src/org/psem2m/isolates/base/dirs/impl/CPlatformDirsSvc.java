/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz (isandlaTech) - initial API and implementation
 *******************************************************************************/
package org.psem2m.isolates.base.dirs.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.psem2m.isolates.base.dirs.IPlatformDirsSvc;
import org.psem2m.utilities.CXJvmUtils;
import org.psem2m.utilities.CXOSUtils;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CPlatformDirsSvc implements IPlatformDirsSvc {

    /** Forker start script name */
    public static final String FORKER_SCRIPT_BASE_NAME = "run_forker";

    /** Isolate working directory parent */
    public static final String ISOLATE_WORKING_DIR_BASE = CPlatformDirsSvc.VAR_DIRECTORY
	    + File.separator + "work" + File.separator;

    public static String LIB_ISOLATE_ID = "IsolateId";

    /** Platform instance log directory */
    public static final String LOGGING_DIR_BASE = CPlatformDirsSvc.VAR_DIRECTORY
	    + File.separator + "log" + File.separator;

    /** Platform base and home repository directory name */
    public static final String REPOSITORY_NAME = "repo";

    /** Script executor on Linux */
    public static final String[] SCRIPT_LINUX_COMMAND = { "/bin/sh" };

    /** Script executor on Windows */
    public static final String[] SCRIPT_WINDOWS_COMMAND = { "cmd.exe", "/C" };

    /** Base working directory */
    public static final String VAR_DIRECTORY = "var";

    /**
     * <pre>
     * org.psem2m.platform.isolate.id=[development]
     * </pre>
     * 
     * @return
     */
    private static String getCurrentIsolateId() {
	return System.getProperty(PROP_PLATFORM_ISOLATE_ID);
    }

    /**
     * <pre>
     * org.psem2m.platform.base=[/Users/ogattaz/workspaces/psem2m/psem2m/platforms/felix.user.dir]
     * </pre>
     * 
     * @return
     */
    static String getCurrentPlatformBase() {
	return System.getProperty(PROP_PLATFORM_BASE);
    }

    /**
     * <pre>
     * org.psem2m.platform.home=[/usr/share/psem2m]
     * </pre>
     * 
     * @return
     */
    static String getCurrentPlatformHome() {
	return System.getProperty(PROP_PLATFORM_HOME);
    }

    /**
     * Explicit default constructor
     */
    public CPlatformDirsSvc() {
	super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.IPlatformDirsSvc#getForkerStartScript()
     */
    @Override
    public List<String> getForkerStartCommand() {

	List<String> command = new ArrayList<String>();

	// Prepare the final script name
	final StringBuilder scriptFileNameBuilder = new StringBuilder(
		FORKER_SCRIPT_BASE_NAME);

	// Interpreter and script extension depends on the system
	if (isOsWindowsFamily()) {
	    command.addAll(Arrays.asList(SCRIPT_WINDOWS_COMMAND));
	    scriptFileNameBuilder.append(".bat");

	} else {
	    command.addAll(Arrays.asList(SCRIPT_LINUX_COMMAND));
	    scriptFileNameBuilder.append(".sh");
	}

	// Finish with the script file name
	final String scriptFileName = scriptFileNameBuilder.toString();
	final String[] possibleDirectories = { System.getProperty("user.dir"),
		getCurrentPlatformBase(), getCurrentPlatformHome() };
	boolean scriptFound = false;

	for (String platformDirectory : possibleDirectories) {

	    File varDirectory = new File(platformDirectory, VAR_DIRECTORY);
	    File scriptFile = new File(varDirectory, scriptFileName);

	    if (scriptFile.exists()) {
		command.add(scriptFile.getAbsolutePath());
		scriptFound = true;
		break;
	    }
	}

	if (!scriptFound) {
	    return null;
	}

	return command;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.osgi.IPlatformDirs#getCurrentIsolateId()
     */
    @Override
    public String getIsolateId() {
	return getCurrentIsolateId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.osgi.IPlatformDirs#getIsolateLogDir()
     */
    @Override
    public File getIsolateLogDir() throws Exception {
	return getIsolateLogDir(getCurrentIsolateId());

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.osgi.IPlatformDirs#getIsolateLogDir(java.lang.String)
     */
    @Override
    public File getIsolateLogDir(final String aIsolateId) throws Exception {
	File wLogDir = new File(getPlatformLogDir(), aIsolateId);
	if (!wLogDir.exists()) {
	    wLogDir.mkdirs();
	}
	return wLogDir;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.base.IPlatformDirsSvc#getIsolateWorkingDir(java.lang
     * .String)
     */
    @Override
    public File getIsolateWorkingDir(final String aIsolateId) {
	return new File(getPlatformBaseDir(), ISOLATE_WORKING_DIR_BASE
		+ aIsolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.dirs.IPlatformDirsSvc#getJavaExecutable()
     */
    @Override
    public File getJavaExecutable() {

	final StringBuilder javaExecutablePath = new StringBuilder();

	javaExecutablePath.append(System
		.getProperty(CXJvmUtils.SYSPROP_JAVA_HOME));
	javaExecutablePath.append(File.separator);
	javaExecutablePath.append("bin");
	javaExecutablePath.append(File.separator);
	javaExecutablePath.append("java");

	if (CXOSUtils.isOsWindowsFamily()) {
	    javaExecutablePath.append(".exe");
	}

	return new File(javaExecutablePath.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.osgi.IPlatformDirs#getPlatformBaseDir()
     */
    @Override
    public File getPlatformBaseDir() {
	return new File(getCurrentPlatformBase());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.IPlatformDirsSvc#getPlatformHomeDir()
     */
    @Override
    public File getPlatformHomeDir() {
	return new File(getCurrentPlatformHome());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.osgi.IPlatformDirs#getPlatformLogDir()
     */
    @Override
    public File getPlatformLogDir() throws Exception {
	File wLogDir = new File(getPlatformBaseDir(), LOGGING_DIR_BASE
		+ "psem2m");
	if (!wLogDir.exists()) {
	    wLogDir.mkdirs();
	}
	return wLogDir;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.dirs.IPlatformDirsSvc#getPlatformRootDirs()
     */
    @Override
    public File[] getPlatformRootDirs() {

	return new File[] { getPlatformHomeDir(), getPlatformBaseDir(),
		new File(System.getProperty("user.dir")) };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.IPlatformDirsSvc#getRepositories()
     */
    @Override
    public File[] getRepositories() {

	List<File> repositories = new ArrayList<File>();

	// Current instance repository
	File baseRepo = new File(getPlatformBaseDir(), REPOSITORY_NAME);
	if (baseRepo.exists()) {
	    repositories.add(baseRepo);
	}

	// Home repository
	File homeRepo = new File(getPlatformHomeDir(), REPOSITORY_NAME);
	if (!homeRepo.equals(baseRepo) && homeRepo.exists()) {
	    repositories.add(homeRepo);
	}

	// Add other repositories here, from higher to lower priority

	return repositories.toArray(new File[0]);
    }

    /**
     * Tests if we're running on Windows
     * 
     * @return True if we're running on Windows
     */
    protected boolean isOsWindowsFamily() {

	String osName = System.getProperty("os.name");
	if (osName == null) {
	    // What ?
	    return false;
	}

	osName = osName.toLowerCase();
	return osName.startsWith("windows");
    }
}
