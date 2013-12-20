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
import org.psem2m.utilities.IXDescriber;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CPlatformDirsSvc implements IPlatformDirsSvc, IXDescriber {

	/**
	 * standards sub dirs
	 */
	private static final String DIRNAME_LOG = "log";
	private static final String DIRNAME_REPOSITORY = "repo";
	private static final String DIRNAME_STORAGE = "storage";
	private static final String DIRPREFIX_ISOLATE = "isolate_";
	private static final String NAMEPREFIX_ISOLATE = "Isolate";
	private static final String NAMEPREFIX_NODE = "Node";

	/**
	 * The bundle context
	 */
	private final BundleContext pContext;

	private final File pIsolateDir;

	private final File pIsolateLogDir;

	/**
	 * The Name of the isolate , given in "-Dcohorte.isolate.name" or calculated
	 * (eg. "Isolate258769")
	 */
	private String pIsolateName = null;
	private final File pIsolateStorageDir;

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
	 * The node name of the isolate , given in "-Dcohorte.node.name" or
	 * calculated
	 */
	private String pNodeName = null;
	/**
	 * The node UUID of the isolate , given in "-Dcohorte.node.uid" or
	 * calculated
	 */
	private String pNodeUID = null;

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

		// init
		getIsolateName();
		getIsolateUID();
		getNodeName();
		pIsolateDir = initIsolateDir();
		pIsolateLogDir = initIsolateLogDir();
		pIsolateStorageDir = initIsolateStorageDir();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.IXDescriber#addDescriptionInBuffer(java.lang.Appendable
	 * )
	 */
	@Override
	public Appendable addDescriptionInBuffer(Appendable aBuffer) {

		appendKeyValInBuff(aBuffer, IPlatformProperties.PROP_ISOLATE_UID,
				getIsolateUID());
		appendKeyValInBuff(aBuffer, IPlatformProperties.PROP_ISOLATE_NAME,
				getIsolateName());
		appendKeyValInBuff(aBuffer, IPlatformProperties.PROP_NODE_UID,
				getNodeUID());
		appendKeyValInBuff(aBuffer, IPlatformProperties.PROP_NODE_NAME,
				getNodeName());

		appendKeyValInBuff(aBuffer, IPlatformProperties.PROP_PLATFORM_HOME,
				getPlatformHome());
		appendKeyValInBuff(aBuffer, IPlatformProperties.PROP_PLATFORM_BASE,
				getPlatformBase());

		appendKeyValInBuff(aBuffer, IPlatformProperties.PROP_PLATFORM_BASE
				+ ".log.dir", getPlatformBaseLogDir());
		appendKeyValInBuff(aBuffer, IPlatformProperties.PROP_PLATFORM_BASE
				+ ".storage.dir", getPlatformBaseStorageDir());

		appendKeyValInBuff(aBuffer, "IsolateDir", getIsolateDir());
		appendKeyValInBuff(aBuffer, "IsolateLogDir", getIsolateLogDir());
		appendKeyValInBuff(aBuffer, "IsolateStorageDir", getIsolateStorageDir());

		appendKeyValInBuff(aBuffer, "NbFoundRepositories",
				getRepositories().length);

		return aBuffer;
	}

	/**
	 * @param aBuffer
	 * @param aKey
	 * @param aValue
	 * @return
	 */
	private Appendable appendKeyValInBuff(Appendable aBuffer,
			final String aKey, final Object aValue) {
		aBuffer = CXStringUtils.appendKeyValInBuff(aBuffer,
				CXStringUtils.strAdjustRight(aKey, 25, ' '), aValue.toString());
		CXStringUtils.appendCharInBuff(aBuffer, '\n');
		return aBuffer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getIsolateDir()
	 */
	@Override
	public File getIsolateDir() {

		return pIsolateDir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.osgi.IPlatformDirs#getIsolateLogDir()
	 */
	@Override
	public File getIsolateLogDir() {
		return pIsolateLogDir;
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
				pIsolateName = NAMEPREFIX_ISOLATE
						+ CXStringUtils.strAdjustRight(
								System.currentTimeMillis(), 6);
			}
		}
		return pIsolateName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getIsolateStorageDir()
	 */
	@Override
	public File getIsolateStorageDir() {
		return pIsolateStorageDir;
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
		return getIsolateStorageDir();
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
	 * @see org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getIsolateNode()
	 */
	@Override
	public String getNodeName() {
		if (pNodeName == null) {
			pNodeName = pContext
					.getProperty(IPlatformProperties.PROP_NODE_NAME);
			if (pNodeName == null || pNodeName.isEmpty()) {
				pNodeName = NAMEPREFIX_NODE
						+ CXStringUtils.strAdjustRight(
								System.currentTimeMillis(), 6);
			}
		}
		return pNodeName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getIsolateNode()
	 */
	@Override
	public String getNodeUID() {
		if (pNodeUID == null) {
			pNodeUID = pContext.getProperty(IPlatformProperties.PROP_NODE_UID);
			if (pNodeUID == null || pNodeUID.isEmpty()) {
				pNodeUID = UUID.randomUUID().toString();
			}
		}
		return pNodeUID;
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
		return tryCreateDirectory(new File(getPlatformBase(), DIRNAME_LOG),
				getIsolateUserDir());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.services.dirs.IPlatformDirsSvc#getPlatformBaseStorageDir
	 * ()
	 */
	@Override
	public File getPlatformBaseStorageDir() {
		// Valid storage or create directory
		return tryCreateDirectory(new File(getPlatformBase(), DIRNAME_STORAGE),
				getIsolateUserDir());
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
		final File baseRepo = new File(getPlatformBase(), DIRNAME_REPOSITORY);
		if (baseRepo.exists()) {
			repositories.add(baseRepo);
		}

		// Home repository
		final File homeRepo = new File(getPlatformHome(), DIRNAME_REPOSITORY);
		if (!homeRepo.equals(baseRepo) && homeRepo.exists()) {
			repositories.add(homeRepo);
		}

		// Add other repositories here, from higher to lower priority

		return repositories.toArray(new File[0]);
	}

	/**
	 * @return
	 */
	private File initIsolateDir() {

		// Valid root directory of the isolate
		return tryCreateDirectory(new File(getPlatformBase(), DIRPREFIX_ISOLATE
				+ getIsolateName()), pIsolateUserDir);

	}

	/**
	 * @return
	 */
	public File initIsolateLogDir() {

		// Valid log directory of the isolate
		return tryCreateDirectory(new File(getIsolateDir(), DIRNAME_LOG),
				pIsolateUserDir);
	}

	/**
	 * @return
	 */
	public File initIsolateStorageDir() {

		return tryCreateDirectory(new File(getIsolateDir(), DIRNAME_STORAGE),
				pIsolateUserDir);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IXDescriber#toDescription()
	 */
	@Override
	public String toDescription() {
		return addDescriptionInBuffer(new StringBuilder()).toString();
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
