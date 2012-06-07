package org.psem2m.utilities.logging;

import java.util.logging.XMLFormatter;

public class CActivityLoggerXml extends CActivityLogger implements
		IActivityLogger {

	/**
	 * @param aTracer
	 * @param aLoggerName
	 * @param aFilePathPattern
	 *            the pattern for naming the output file
	 * @param aLevel
	 *            the value for the log level (may be null)
	 * @param aFileLimit
	 *            the maximum number of bytes to write to any one file
	 * @param aFileCount
	 *            the number of files to use
	 * @return
	 * @throws Exception
	 */
	public static IActivityLogger newLogger(String aLoggerName,
			String aFilePathPattern, String aLevel, int aFileLimit,
			int aFileCount) throws Exception {
		CActivityLoggerXml wLogger = new CActivityLoggerXml(aLoggerName,
				aFilePathPattern, aLevel, aFileLimit, aFileCount);
		wLogger.initFileHandler();
		wLogger.open();
		return wLogger;
	}

	/**
	 * @param aTracer
	 * @param aLoggerName
	 * @param aFilePathPattern
	 *            the pattern for naming the output file
	 * @param aLevel
	 *            the value for the log level (may be null)
	 * @param aFileLimit
	 *            the maximum number of bytes to write to any one file
	 * @param aFileCount
	 *            the number of files to use
	 * @throws Exception
	 */
	protected CActivityLoggerXml(String aLoggerName, String aFilePathPattern,
			String aLevel, int aFileLimit, int aFileCount) throws Exception {
		super(aLoggerName, aFilePathPattern, aLevel, aFileLimit, aFileCount);
	}

	/**
   * 
   */
	@Override
	protected void initFileHandler() throws Exception {
		CActivityFileHandler wFileHandler = new CActivityFileHandler(
				getFilePathPattern(), getFileLimit(), getFileCount());
		wFileHandler.setFormatter(new XMLFormatter());
		setFileHandler(wFileHandler);
	}
}
