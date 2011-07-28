package org.psem2m.utilities.logging;

import java.util.logging.Formatter;

import org.psem2m.utilities.CXException;

/**
 * @author ogattaz
 * 
 */
public class CActivityLoggerBasic extends CActivityLogger implements
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
	public static IActivityLogger newLogger(final String aLoggerName,
			final String aFilePathPattern, final String aLevel,
			final int aFileLimit, final int aFileCount) throws Exception {
		CActivityLoggerBasic wLogger = new CActivityLoggerBasic(aLoggerName,
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
	protected CActivityLoggerBasic(final String aLoggerName,
			final String aFilePathPattern, final String aLevel,
			final int aFileLimit, final int aFileCount) throws Exception {
		super(aLoggerName, aFilePathPattern, aLevel, aFileLimit, aFileCount);
	}

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.CActivityLogger#buildLine(java.lang.String,
	 * java.lang.Throwable)
	 */
	protected String buildLine(String aLine, final Throwable e) {
		if (e != null) {
			StringBuffer wSB = new StringBuffer();
			wSB.append(aLine);
			wSB.append(' ');
			wSB.append(CXException.eInString(e).replace('\n', '|'));
			aLine = wSB.toString();
		}
		return aLine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IActivityLogger#getRequester()
	 */
	@Override
	public IActivityRequester getRequester() {
		return new CActivityRequesterBasic(getLoggerName(), getFileHandler());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.CActivityLoggerStd#initFileHandler()
	 */
	@Override
	protected void initFileHandler() throws Exception {
		CActivityFileHandler wFileHandler = new CActivityFileHandler(
				getFilePathPattern(), getFileLimit(), getFileCount());
		wFileHandler.setFormatter((Formatter) CActivityFormaterBasic
				.getInstance());
		super.setFileHandler(wFileHandler);
	}
}
