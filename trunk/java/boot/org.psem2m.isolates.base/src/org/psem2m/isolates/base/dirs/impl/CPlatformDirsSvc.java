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
import java.util.List;
import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.utilities.CXStringUtils;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CPlatformDirsSvc implements IPlatformDirsSvc {

	/**
	 * Platform base and home repository directory name
	 */
	private static final String REPOSITORY_NAME = "repo";

	/**
	 * The bundle context
	 */
	private final BundleContext pContext;

	/**
	 * The Name of the isolate , given in "-Dcohorte.isolate.name" or calculated
	 * (eg. "Isolate258769")
	 */
	private String pIsolateName = null;

	/**
	 * The node of the isolate , given in "-Dcohorte.node" or calculated
	 */
	private String pIsolateNode = null;

	/**
	 * The UUID of the isolate , given in "-Dcohorte.isolate.uid" or calculated
	 */
	private String pIsolateUID = null;

	/**
	 * The initial working directory of the current isolate (user.dir of the
	 * jvm)
	 */
	private final File pIsolateUserDir;

	/**
	 * Sets up the platform informations service. Stores the working directory
	 * at instantiation time.
	 * 
	 * @param aBundleContext
	 *            The bundle context
	 */
	public CPlatformDirsSvc(final BundleContext aBundleContext) {

		pContext = aBundleContext;

		// Store the working directory
		pIsolateUserDir = new File(System.getProperty("user.dir"));

		getIsolateName();
		getIsolateUID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.IPlatformDirs#getIsolateLogDir()
	 */
	@Override
	public File getIsolateLogDir() {

		// Valid log directory
		return tryCreateDirectory(new File(getPlatformBaseLogDir(), "log"),
				pIsolateUserDir);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getIsolateName()
	 */
	@Override
	public String getIsolateName() {
		if (pIsolateName == null) {
			pIsolateName = pContext
					.getProperty(IPlatformProperties.PROP_ISOLATE_NAME);
			if (pIsolateName == null || pIsolateName.isEmpty()) {
				pIsolateName = "Isolate"
						+ CXStringUtils.strAdjustRight(
								System.currentTimeMillis(), 6);
			}
		}
		return pIsolateName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getIsolateNode()
	 */
	@Override
	public String getIsolateNode() {
		if (pIsolateNode == null) {
			pIsolateNode = pContext
					.getProperty(IPlatformProperties.PROP_ISOLATE_NODE);
			if (pIsolateNode == null || pIsolateNode.isEmpty()) {
				pIsolateNode = "Node"
						+ CXStringUtils.strAdjustRight(
								System.currentTimeMillis(), 6);
			}
		}
		return pIsolateNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getIsolateStorageDirectory
	 * ()
	 */
	@Override
	public File getIsolateStorageDirectory() {

		return tryCreateDirectory(new File(pIsolateUserDir, "storage"),
				pIsolateUserDir);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getIsolateUID()
	 */
	@Override
	public String getIsolateUID() {

		if (pIsolateUID == null) {
			pIsolateUID = pContext
					.getProperty(IPlatformProperties.PROP_ISOLATE_UID);
			if (pIsolateUID == null || pIsolateUID.isEmpty()) {
				pIsolateUID = UUID.randomUUID().toString();
			}
		}
		return pIsolateUID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getIsolateUserDir()
	 */
	@Override
	public File getIsolateUserDir() {

		return pIsolateUserDir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getIsolateWorkingDirectory
	 * ()
	 */
	@Override
	public File getIsolateWorkingDirectory() {

		return getIsolateUserDir();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.IPlatformDirs#getPlatformBaseDir()
	 */
	@Override
	public File getPlatformBase() {
		// Valid directory
		return validDirectory(
				new File(pContext
						.getProperty(IPlatformProperties.PROP_PLATFORM_BASE)),
				getIsolateWorkingDirectory());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getPlatformBaseLogDir
	 * ()
	 */
	@Override
	public File getPlatformBaseLogDir() {
		// Valid log or create directory
		return tryCreateDirectory(new File(getPlatformBase(), "log"),
				getIsolateWorkingDirectory());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.base.IPlatformDirsSvc#getPlatformHomeDir()
	 */
	@Override
	public File getPlatformHome() {

		return validDirectory(
				new File(pContext
						.getProperty(IPlatformProperties.PROP_PLATFORM_HOME)),
				getIsolateWorkingDirectory());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.base.dirs.IPlatformDirsSvc#getPlatformRootDirs()
	 */
	@Override
	public File[] getPlatformRootDirs() {

		return new File[] { getPlatformBase(), getPlatformHome(),
				new File(System.getProperty("user.dir")) };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.base.IPlatformDirsSvc#getRepositories()
	 */
	@Override
	public File[] getRepositories() {

		final List<File> repositories = new ArrayList<File>();

		// Current instance repository
		final File baseRepo = new File(getPlatformBase(), REPOSITORY_NAME);
		if (baseRepo.exists()) {
			repositories.add(baseRepo);
		}

		// Home repository
		final File homeRepo = new File(getPlatformHome(), REPOSITORY_NAME);
		if (!homeRepo.equals(baseRepo) && homeRepo.exists()) {
			repositories.add(homeRepo);
		}

		// Add other repositories here, from higher to lower priority

		return repositories.toArray(new File[0]);
	}

	/**
	 * Tries to create the given directory, return the default one on failure
	 * 
	 * @param aDirectory
	 *            Directory to create via {@link File#mkdirs()}
	 * @param aDefault
	 *            Directory to return on failure
	 * @return The created directory or the default one
	 */
	private File tryCreateDirectory(final File aDirectory, final File aDefault) {

		if (aDirectory == null) {
			return aDefault;
		}

		if (!aDirectory.exists()) {
			try {
				if (!aDirectory.mkdirs()) {
					// Error creating directories
					return aDefault;
				}

			} catch (final SecurityException ex) {
				// Security error
				return aDefault;
			}
		}

		// Valid log directory
		return aDirectory;
	}

	/**
	 * 
	 * @param aDirectory
	 *            to valid
	 * @param aDefault
	 *            a defaut dir
	 * @return aDirectory or aDefault if aDirectory doesn't exist
	 */
	private File validDirectory(final File aDirectory, final File aDefault) {

		if (aDirectory == null) {
			return aDefault;
		}

		if (!aDirectory.exists()) {

			return aDefault;
		}

		// Valid log directory
		return aDirectory;
	}
}
