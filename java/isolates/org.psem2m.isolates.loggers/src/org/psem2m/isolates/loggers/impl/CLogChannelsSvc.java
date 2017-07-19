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

package org.psem2m.isolates.loggers.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.loggers.CLogChannelException;
import org.psem2m.isolates.loggers.ILogChannelSvc;
import org.psem2m.isolates.loggers.ILogChannelsSvc;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.utilities.CXException;
import org.psem2m.utilities.files.CXFileDir;
import org.psem2m.utilities.logging.CActivityLoggerBasic;
import org.psem2m.utilities.logging.IActivityFormater;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 *
 * This service is a Service factory.
 *
 *
 * @author isandlatech (www.isandlatech.com) - ogattaz
 *
 */
public class CLogChannelsSvc extends CPojoBase implements ILogChannelsSvc {

	/**
	 * @author isandlatech (www.isandlatech.com) - ogattaz
	 *
	 */
	class CLogChannelLogger extends CActivityLoggerBasic implements
			ILogChannelSvc {

		private final String pFilePatternPath;
		private final CXFileDir pLogChannelDir;

		/**
		 * @param aLoggerName
		 * @param aFilePathPattern
		 * @param aLevel
		 * @param aFileLimit
		 * @param aFileCount
		 * @param aMultiline
		 *            the flag to control the format of the logger: monline or
		 *            multilines
		 * @see IActivityFormater.MULTILINES_TEXT)
		 * @throws Exception
		 */
		CLogChannelLogger(final String aLoggerName, final File aFilePattern,
				final String aLevel, final int aFileLimit,
				final int aFileCount, final boolean aMultiline)
				throws Exception {

			super(aLoggerName, aFilePattern.getAbsolutePath(), aLevel,
					aFileLimit, aFileCount, IActivityFormater.LINE_SHORT,
					aMultiline);

			// something like ".../logsdir/aLoggerName/aLoggerName_%g.txt"
			pFilePatternPath = aFilePattern.getAbsolutePath();

			// something like ".../logsdir/aLoggerName/"
			pLogChannelDir = new CXFileDir(aFilePattern.getParentFile());

			initFileHandler();

			open();
		}

		/**
		 * @return the number of deleted file
		 * @throws IOException
		 */
		int cleanChannelFiles() throws IOException {
			close();
			return getLogChannelDir().remove();
		}

		/**
		 * @param aFileIdx
		 * @return
		 * @throws IOException
		 */
		File getChannelFile(final int aFileIdx) throws IOException {
			final String wFilePath = pFilePatternPath.replace("%g",
					String.valueOf(aFileIdx));
			final File wFile = new File(wFilePath);
			if (wFile.isFile()) {
				throw new IOException(String.format(
						"LogChannel File idx [%s] doesn't exist. path=[%s]",
						wFile.getAbsolutePath()));
			}
			return wFile;
		}

		/**
		 * @return
		 * @throws IOException
		 */
		List<File> getChannelFiles() throws IOException {
			return getLogChannelDir().getMySortedFiles(null,
					!CXFileDir.WITH_DIR, CXFileDir.WITH_TEXTFILE);
		}

		/**
		 * @return
		 */
		CXFileDir getLogChannelDir() {
			return pLogChannelDir;
		}

	}

	private final String LOG_FILE_SUFFIX = "_%g.log";
	private final String LOG_FOLDER_PREFIX = "channel_";

	/**
	 * Service reference managed by iPojo (see metadata.xml)
	 *
	 * This service is the logger of the current bundle
	 **/
	private IIsolateLoggerSvc pIsolateLoggerSvc;

	/** the "repository" of the opened logging channels **/
	TreeMap<String, ILogChannelSvc> pLoggers = new TreeMap<String, ILogChannelSvc>();

	/** Service reference managed by iPojo (see metadata.xml) **/
	private IPlatformDirsSvc pPlatformDirsSvc;

	/**
	 * Explicit default constructor
	 */
	public CLogChannelsSvc() {

		super();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.psem2m.isolates.loggers.ILogChannelsSvc#cleanLogChannelFiles(java
	 * .lang.String)
	 */
	@Override
	public int cleanLogChannelFiles(String aChannelId)
			throws CLogChannelException {

		final CLogChannelLogger wLogChannelLogger = findLogChannelLogger(aChannelId);
		try {
			if (wLogChannelLogger == null) {
				throw new CLogChannelException(
						"The channel [%s], doesn't exist", aChannelId);
			}

			return wLogChannelLogger.cleanChannelFiles();

		} catch (final IOException e) {
			throw new CLogChannelException(
					"Unable to clean the files of the channel [%s] : ",
					aChannelId, CXException.eUserMessagesInString(e));
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.utilities.CXObjectBase#destroy()
	 */
	@Override
	public void destroy() {

		for (final Entry<String, ILogChannelSvc> wLoggerEntry : pLoggers
				.entrySet()) {
			wLoggerEntry.getValue().close();
		}
		pLoggers.clear();
	}

	/**
	 * @param aChannelId
	 * @return
	 */
	private ILogChannelSvc findLogChannel(final String aChannelId) {
		return pLoggers.get(aChannelId);
	}

	/**
	 * @param aChannelId
	 * @return
	 */
	private CLogChannelLogger findLogChannelLogger(final String aChannelId) {
		return (CLogChannelLogger) findLogChannel(aChannelId);
	}

	/**
	 * @param aChannelId
	 * @param aLogDir
	 * @return
	 * @throws IOException
	 */
	private File getChannelDir(final File aLogDir, final String aChannelId)
			throws IOException {

		final CXFileDir wChannelDir = new CXFileDir(aLogDir, LOG_FOLDER_PREFIX
				+ aChannelId);

		if (!wChannelDir.isDirectory()) {
			wChannelDir.createHierarchy();
		}

		return wChannelDir;
	}

	/**
	 * @param aLogDir
	 * @param aChannelId
	 * @return
	 * @throws IOException
	 */
	private File getChannelFilePattern(final File aLogDir,
			final String aChannelId) throws IOException {

		final File wChannelDir = getChannelDir(aLogDir, aChannelId);

		return new File(wChannelDir, aChannelId + LOG_FILE_SUFFIX);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.isolates.loggers.ILogChannelsSvc#getChannels()
	 */
	@Override
	public List<ILogChannelSvc> getChannels() {

		final List<ILogChannelSvc> wLoggers = new ArrayList<ILogChannelSvc>();
		for (final Entry<String, ILogChannelSvc> wLoggerEntry : pLoggers
				.entrySet()) {
			wLoggers.add(wLoggerEntry.getValue());
		}

		return wLoggers;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.isolates.loggers.ILogChannelsSvc#getChannelsIds()
	 */
	@Override
	public List<String> getChannelsIds() {

		final List<String> wIds = new ArrayList<String>();
		for (final Entry<String, ILogChannelSvc> wLoggerEntry : pLoggers
				.entrySet()) {
			wIds.add(wLoggerEntry.getKey());
		}
		return wIds;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.psem2m.isolates.loggers.ILogChannelsSvc#getLogChannel(java.lang.String
	 * )
	 */
	@Override
	public ILogChannelSvc getLogChannel(final String aChannelId)
			throws CLogChannelException {

		final ILogChannelSvc wLogger = findLogChannel(aChannelId);
		if (wLogger != null) {
			return wLogger;
		}

		return newLogChannel(aChannelId, LOGCHANNEL_FILE_SIZE,
				LOGCHANNEL_FILE_COUNT_MAX, IActivityFormater.MULTILINES_TEXT);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.psem2m.isolates.loggers.ILogChannelsSvc#getLogChannel(java.lang.String
	 * , int, int, boolean)
	 */
	@Override
	public ILogChannelSvc getLogChannel(String aChannelId, final int aFileSize,
			final int aFileCount, boolean aMultiline)
			throws CLogChannelException {

		final ILogChannelSvc wLogger = pLoggers.get(aChannelId);
		if (wLogger != null) {
			return wLogger;
		}

		return newLogChannel(aChannelId, aFileSize, aFileCount, aMultiline);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.loggers.ILogChannelsSvc#getLogChannelFile(java.lang
	 * .String, int)
	 */
	@Override
	public File getLogChannelFile(String aChannelId, int aFileIdx)
			throws CLogChannelException {

		final CLogChannelLogger wLogChannelLogger = findLogChannelLogger(aChannelId);
		try {
			if (wLogChannelLogger == null) {
				throw new CLogChannelException(
						"The channel [%s], doesn't exist", aChannelId);
			}

			return wLogChannelLogger.getChannelFile(aFileIdx);

		} catch (final IOException e) {
			throw new CLogChannelException(
					"Unable to get the files idx [%s] of the channel [%s] : ",
					aFileIdx, aChannelId, CXException.eUserMessagesInString(e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.loggers.ILogChannelsSvc#getLogChannelFiles(java.lang
	 * .String)
	 */
	@Override
	public List<File> getLogChannelFiles(String aChannelId)
			throws CLogChannelException {
		final CLogChannelLogger wLogChannelLogger = findLogChannelLogger(aChannelId);
		try {
			if (wLogChannelLogger == null) {
				throw new CLogChannelException(
						"The channel [%s], doesn't exist", aChannelId);
			}

			return wLogChannelLogger.getChannelFiles();

		} catch (final IOException e) {
			throw new CLogChannelException(
					"Unable to get the list of the files of the channel [%s] : ",
					aChannelId, CXException.eUserMessagesInString(e));
		}
	}

	/**
	 * @param aChannelId
	 * @param aLogDir
	 * @param aFileSize
	 * @param aFileCount
	 * @param aMultiline
	 * @return
	 * @throws CLogChannelException
	 */
	private ILogChannelSvc instanciateLogChannel(final String aChannelId,
			final File aLogDir, final int aFileSize, final int aFileCount,
			final boolean aMultiline) throws CLogChannelException {

		try {
			final File wLogFilePattern = getChannelFilePattern(aLogDir,
					aChannelId);

			return new CLogChannelLogger(aChannelId, wLogFilePattern,
					IActivityLoggerBase.ALL, aFileSize, aFileCount, aMultiline);

		} catch (final Exception e) {
			throw new CLogChannelException(e,
					"Unable to instanciate the log channel [%s]", aChannelId);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
	 */
	@Override
	public void invalidatePojo() {

		// logs in the bundle output
		pIsolateLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
				toDescription());
		destroy();
	}

	/**
	 * @param aChannelId
	 * @param aFileSize
	 * @param aFileCount
	 * @param aMultiline
	 * @return
	 * @throws CLogChannelException
	 */
	public ILogChannelSvc newLogChannel(final String aChannelId,
			final int aFileSize, final int aFileCount, final boolean aMultiline)
			throws CLogChannelException {

		final ILogChannelSvc wLogger = instanciateLogChannel(aChannelId,
				pPlatformDirsSvc.getIsolateLogDir(), aFileSize, aFileCount,
				aMultiline);

		pLoggers.put(aChannelId, wLogger);

		return wLogger;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.psem2m.isolates.loggers.ILogChannelsSvc#removeLogChannel(java.lang
	 * .String)
	 */
	@Override
	public boolean removeLogChannel(String aChannelId)
			throws CLogChannelException {

		final ILogChannelSvc wLogChannel = pLoggers.remove(aChannelId);
		if (wLogChannel == null) {
			throw new CLogChannelException("The channel [%s] doesn't exist",
					aChannelId);
		}

		// close the channel removed from the map
		wLogChannel.close();

		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
	 */
	@Override
	public void validatePojo() {

		// logs in the isolate logger
		pIsolateLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",
				toDescription());

		try {

			// ...

		} catch (final Exception e) {
			pIsolateLoggerSvc.logSevere(this, "validatePojo", e);
		}
	}
}
