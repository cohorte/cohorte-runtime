/**
 * Copyright 2014 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.psem2m.isolates.base.dirs.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.files.CXFileDir;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 *
 */
public class CPlatformDirsSvc implements IPlatformDirsSvc {

	/**
	 * MOD_OG_20150625 prefixes
	 */
	private static final String NAMEPREFIX_ISOLATE = "Isolate";
	private static final String NAMEPREFIX_NODE = "Node";

	/**
	 * MOD_OG_20150625 Warning message prefixes
	 */
	private static final String PREFIX_CREATE_WARNING = "CPlatformDirsSvc:tryCreateDirectory() >>";
	private static final String PREFIX_VALID_DIR_WARNING = "CPlatformDirsSvc:validDirectory() >>";
	private static final String PREFIX_VALID_USERDIR_WARNING = "CPlatformDirsSvc:validIsolateUserDir() >>";

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
	private String pIsolateName;

	private final File pIsolateStorageDir;

	/**
	 * The UUID of the isolate , given in "-Dcohorte.isolate.uid" or calculated
	 */
	private String pIsolateUID;

	/**
	 * The initial working directory of the current isolate (user.dir of the
	 * jvm)
	 */
	private final File pIsolateUserDir;

	/**
	 * The node name of the isolate , given in "-Dcohorte.node.name" or
	 * calculated
	 */
	private String pNodeName;

	/**
	 * The node UUID of the isolate , given in "-Dcohorte.node.uid" or
	 * calculated
	 */
	private String pNodeUID;

	private File pPlatformBaseDir = null;

	private File pPlatformHomeDir = null;

	/**
	 * Sets up the platform informations service. Stores the working directory
	 * at instantiation time.
	 *
	 * @param aBundleContext
	 *            The bundle context
	 */
	public CPlatformDirsSvc(final BundleContext aBundleContext) {

		pContext = aBundleContext;

		// Store the working directory fixed by the launcher of the isolate :
		// The "user.dir" path is the isolateDir path
		// eg.
		// ${project_loc:/fr.agilium.ng.base}/var/serverldap/0000-SERV-ERLD-AP38-1010
		pIsolateUserDir = validIsolateUserDir(new File(
				System.getProperty("user.dir")));

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
	public Appendable addDescriptionInBuffer(final Appendable aBuffer) {

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

		appendKeyValInBuff(aBuffer, "IsolateDir", getIsolateDir());
		final boolean wIsolateDirRespectsFormat = testIsolateUserDir();
		appendKeyValInBuff(aBuffer, "IsolateDirRespectsFormat",
				wIsolateDirRespectsFormat);
		if (!wIsolateDirRespectsFormat) {
			appendKeyValInBuff(aBuffer, "CalculatedIsolateDir",
					calculateIsolateDir().getAbsolutePath());
		}
		appendKeyValInBuff(aBuffer, "IsolateLogDir", getIsolateLogDir());
		appendKeyValInBuff(aBuffer, "IsolateStorageDir", getIsolateStorageDir());

		// MOD_OG_20150625
		final File[] wRepositories = getRepositories();
		appendKeyValInBuff(aBuffer, "Repositories", wRepositories.length);
		int wIdx = 0;
		for (final File wRepository : wRepositories) {
			appendKeyValInBuff(aBuffer, String.format("Repository(%s)", wIdx),
					wRepository.getAbsolutePath());
			wIdx++;
		}

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

		CXStringUtils.appendCharInBuff(aBuffer, '\n');
		aBuffer = CXStringUtils.appendKeyValInBuff(aBuffer,
				CXStringUtils.strAdjustRight(aKey, 25, ' '), aValue.toString());

		return aBuffer;
	}

	/**
	 * @return
	 */
	private File calculateIsolateDir() {

		// eg. ...base/var/myIsolate/OIUE-HGD8-JUSC-8VS
		return new CXFileDir(getPlatformBase(), DIRNAME_VAR, getIsolateName(),
				getIsolateUID());
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

		// init if null with Valid directory
		if (pPlatformBaseDir == null) {

			// eg. -Dcohorte.base=${project_loc:/fr.agilium.ng.base}
			final File wBaseDir = new File(
					System.getProperty(IPlatformProperties.PROP_PLATFORM_BASE));
			pPlatformBaseDir = validDirectory(wBaseDir, pIsolateUserDir);
		}

		return pPlatformBaseDir;

	}

	/**
	 * @return
	 */
	public File getPlatformBaseVar() {
		return validDirectory(new File(getPlatformBase(), DIRNAME_VAR),
				pIsolateUserDir);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.isolates.base.IPlatformDirsSvc#getPlatformHomeDir()
	 */
	@Override
	public File getPlatformHome() {

		// init if null with Valid directory
		if (pPlatformHomeDir == null) {
			pPlatformHomeDir = validDirectory(
					new File(
							pContext.getProperty(IPlatformProperties.PROP_PLATFORM_HOME)),
					pIsolateUserDir);
		}
		return pPlatformHomeDir;
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
	 * The isolateDir path is fixed by the launcher of the isolate : look at
	 * "user.dir"
	 *
	 * @return the dir the isolate dir ( eg.
	 *         ...BASE/var/myIsolate/OIUE-HGD8-JUSC-8VS7 )
	 *
	 * @see the pIsolateUserDir initialisation
	 */
	private File initIsolateDir() {

		return pIsolateUserDir;
	}

	/**
	 * create the directory if doesn't exist
	 *
	 * @return the subdir "log" of the isolate dir ( eg.
	 *         ...BASE/var/myIsolate/OIUE-HGD8-JUSC-8VS7/log )
	 *
	 * @see the pIsolateDir initialisation
	 */
	public File initIsolateLogDir() {

		// Valid log directory of the isolate
		return tryCreateDirectory(new File(pIsolateDir, DIRNAME_LOG),
				pIsolateUserDir);
	}

	/**
	 * create the directory if doesn't exist
	 *
	 * @return the subdir "log" of the isolate dir ( eg.
	 *         ...BASE/var/myIsolate/OIUE-HGD8-JUSC-8VS7/storage )
	 *
	 * @see the pIsolateDir initialisation
	 */
	public File initIsolateStorageDir() {

		return tryCreateDirectory(new File(pIsolateDir, DIRNAME_STORAGE),
				pIsolateUserDir);
	}

	/**
	 * MOD_OG_20150625 add warning messages
	 *
	 * @param aFormat
	 * @param aArgs
	 */
	private void logWarn(final String aFormat, final Object... aArgs) {
		System.err.println(String.format(aFormat, aArgs));
	}

	/**
	 * MOD_OG_20150625
	 *
	 * @return
	 */
	private boolean testIsolateUserDir() {
		return testIsolateUserDir(getIsolateUserDir());
	}

	/**
	 * MOD_OG_20150625
	 *
	 * @return
	 */
	private boolean testIsolateUserDir(final File aIsolateUserDir) {
		return aIsolateUserDir != null
				&& aIsolateUserDir.getAbsolutePath().equalsIgnoreCase(
						calculateIsolateDir().getAbsolutePath());
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
	 * MOD_OG_20150625 add warning messages
	 *
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
			logWarn("%s Can't create a null directory : returns default [%s]",
					PREFIX_CREATE_WARNING, aDefault);
			return aDefault;
		}

		if (!aDirectory.exists()) {
			try {
				// mkdirs returns true if and only if the directory was created,
				// along with all necessary parent directories; false otherwise
				if (!aDirectory.mkdirs()) {
					// Error creating directories
					logWarn("%s Can't create directory [%s] throwing no Exception ...",
							PREFIX_CREATE_WARNING, aDirectory);

					return aDefault;
				}

			} catch (final SecurityException e) {
				// Security error
				logWarn("%s Can't create directory [%s] : %s",
						PREFIX_CREATE_WARNING, aDirectory,
						CXException.eUserMessagesInString(e));

				return aDefault;
			}
		}

		// returns existing directory
		return aDirectory;
	}

	/**
	 * MOD_OG_20150625 add warning messages
	 *
	 * @param aDirectory
	 *            to valid
	 * @return the passed aDirectory
	 */
	private File validDirectory(final File aDirectory) {
		return validDirectory(aDirectory, aDirectory);
	}

	/**
	 * MOD_OG_20150625 add warning messages
	 *
	 * @param aDirectory
	 *            to valid
	 * @param aDefault
	 *            a default directory
	 * @return aDirectory or aDefault if aDirectory doesn't exist
	 */
	private File validDirectory(final File aDirectory, final File aDefault) {

		if (aDirectory == null) {
			logWarn("%s Can't valid a null directory : returns default [%s]",
					PREFIX_VALID_DIR_WARNING, aDefault);
			return aDefault;
		}

		if (!aDirectory.exists()) {
			logWarn("%s Can't valid a non existing directory [%s]: returns default [%s]",
					PREFIX_VALID_DIR_WARNING, aDirectory, aDefault);
			return aDefault;
		}

		// return the valid directory
		return aDirectory;
	}

	/**
	 *
	 * MOD_OG_20150625
	 *
	 * This method logs warning in the stdout if there's an error
	 *
	 * @param aIsolateUserDir
	 *            the IsolateUserDir to valid
	 * @return the validated IsolateUserDir
	 */
	private File validIsolateUserDir(final File aIsolateUserDir) {

		// to log wargning if needed
		validDirectory(aIsolateUserDir);

		if (aIsolateUserDir != null && !testIsolateUserDir(aIsolateUserDir)) {
			logWarn("%s\nUserDir system prop.=[%s] doesn't respect the format [...base/var/[ISOLATE_NAME]/[ISOLATE_UUID]/]"
					+ "\nCalculatedIsolateDir=[%s]",
					PREFIX_VALID_USERDIR_WARNING, aIsolateUserDir,
					calculateIsolateDir());

		}
		return aIsolateUserDir;
	}
}
