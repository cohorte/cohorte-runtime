package org.psem2m.utilities.logging;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXJavaRunContext;
import org.psem2m.utilities.CXStringUtils;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CActivityLogger extends CActivityObject implements
		IActivityLogger {

	private final static String FORMAT_CLOSELOG = "Close logger [%s]";

	private final static String FORMAT_OPENLOG = "Open logger [%s]";

	public final static String LABEL_LEVEL = "Level";

	private final static String LIB_METHOD_CLOSE = "close";

	private final static String LIB_METHOD_OPEN = "open";

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

		CActivityLogger wLogger = new CActivityLogger(aLoggerName,
				aFilePathPattern, aLevel, aFileLimit, aFileCount);
		wLogger.initFileHandler();
		wLogger.open();
		return wLogger;
	}

	private final int pFileCount;

	private CActivityFileHandler pFileHandler = null;

	private final int pFileLimit;

	private final String pFilePathPattern;

	private final String pLevel;

	private Logger pLogger;

	private final CLogLineTextBuilder pLogLineTextBuilder = CLogLineTextBuilder
			.getInstance();

	/**
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
	protected CActivityLogger(final String aLoggerName,
			final String aFilePathPattern, final String aLevel,
			final int aFileLimit, final int aFileCount) throws Exception {
		super(null, aLoggerName);
		pFilePathPattern = aFilePathPattern;
		pLevel = aLevel;
		pFileLimit = aFileLimit;
		pFileCount = aFileCount;
	}

	@Override
	public Appendable addDescriptionInBuffer(final Appendable aBuffer) {
		super.addDescriptionInBuffer(aBuffer);
		CXStringUtils.appendKeyValInBuff(aBuffer, LABEL_LEVEL, pLevel);
		if (pFileHandler != null) {
			pFileHandler.addDescriptionInBuffer(aBuffer);
		}
		return aBuffer;
	}

	/**
	 * @param aLine
	 * @param e
	 * @return
	 */
	protected CharSequence buildLine(CharSequence aLine, final Throwable e) {
		if (e != null) {
			StringBuilder wSB = new StringBuilder();
			wSB.append(aLine);
			wSB.append(' ');
			wSB.append(CXException.eInString(e));
			aLine = wSB.toString();
		}
		return aLine;
	}

	/**
   * 
   */
	@Override
	public void close() {
		if (isOpened()) {
			// restart the logging in the parent logger
			pLogger.setUseParentHandlers(true);

			String wLine = String.format(FORMAT_CLOSELOG, getLoggerName());
			pLogger.logp(Level.INFO, getClass().getSimpleName(),
					LIB_METHOD_CLOSE, wLine);
			// close
			pLogger.setLevel(Level.OFF);
			pFileHandler.close();

			// disociates the file handler and the logger
			if (hasFileHandler()) {
				pLogger.removeHandler(pFileHandler);
			}

			// free the logger
			pLogger = null;
		}
	}

	@Override
	public void destroy() {
		// ...
	}

	/**
	 * @return
	 */
	protected int getFileCount() {
		return pFileCount;
	}

	/**
	 * @return
	 */
	protected CActivityFileHandler getFileHandler() {
		return pFileHandler;
	}

	/**
	 * @return
	 */
	protected int getFileLimit() {
		return pFileLimit;
	}

	/**
	 * @return
	 */
	protected String getFilePathPattern() {
		return pFilePathPattern;
	}

	/**
	 * @return
	 */
	protected String getLevel() {
		return pLevel;
	}

	/**
	 * @return
	 */
	public String getLoggerName() {
		return getIdentifier();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IActivityLogger#getRequester()
	 */
	@Override
	public IActivityRequester getRequester() {
		return new CActivityRequesterStd(getLoggerName(), getFileHandler());
	}

	/**
	 * @return
	 */
	private boolean hasFileHandler() {
		return pFileHandler != null;
	}

	/**
	 * @return
	 */
	private boolean hasLevel() {
		return pLevel != null;
	}

	/**
	 * @return
	 */
	private boolean hasLoggerName() {
		return getLoggerName() != null;
	}

	/**
   * 
   */
	protected void initFileHandler() throws Exception {
		CActivityFileHandler wFileHandler = new CActivityFileHandler(
				getFilePathPattern(), getFileLimit(), getFileCount());
		wFileHandler.setFormatter(new CActivityFormaterStd());
		setFileHandler(wFileHandler);
	}

	@Override
	public boolean isLogDebugOn() {
		return isLoggable(Level.FINE);
	}

	@Override
	public boolean isLoggable(final Level aLevel) {
		return isOpened() && pLogger.isLoggable(aLevel);
	}

	@Override
	public boolean isLogInfoOn() {
		return isLoggable(Level.INFO);
	}

	@Override
	public boolean isLogSevereOn() {
		return isLoggable(Level.SEVERE);
	}

	/**
	 * @return
	 */
	@Override
	public boolean isLogWarningOn() {
		return isLoggable(Level.WARNING);
	}

	/**
	 * @return
	 */
	protected boolean isOpened() {
		return pLogger != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#log(java.util.logging.Level,
	 * java.lang.Object, java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void log(final Level aLevel, final Object aWho,
			final CharSequence aWhat, final Object... aInfos) {

		String wLogLine = null;
		if (isOpened()) {
			wLogLine = pLogLineTextBuilder.buildLogLine(aInfos);
		}
		CharSequence wWhat = (aWhat != null) ? aWhat : CXJavaRunContext
				.getPreCallingMethod();

		// to diagnose the logging tool
		if (isTraceDebugOn()) {
			CLogLineBuffer wTB = new CLogLineBuffer();
			wTB.appendDescr("LOG", aLevel.getName());
			wTB.append(' ');
			wTB.append(wLogLine != null ? wLogLine : "no infos");
			traceDebug(this, wWhat, wTB);
		}

		if (wLogLine != null) {

			pLogger.logp(aLevel, pLogLineTextBuilder.buildWhoObjectId(aWho),
					wWhat.toString(), wLogLine);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLoggerBase#log(java.util.logging
	 * .LogRecord)
	 */
	@Override
	public void log(final LogRecord record) {
		pLogger.log(record);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#logDebug(java.lang.Object,
	 * java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logDebug(final Object aWho, final CharSequence aWhat,
			final Object... aInfos) {
		log(Level.FINE, aWho, aWhat, aInfos);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#logInfo(java.lang.Object,
	 * java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logInfo(final Object aWho, final CharSequence aWhat,
			final Object... aInfos) {
		log(Level.INFO, aWho, aWhat, aInfos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#logSevere(java.lang.Object,
	 * java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logSevere(final Object aWho, final CharSequence aWhat,
			final Object... aInfos) {
		log(Level.SEVERE, aWho, aWhat, aInfos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#logWarn(java.lang.Object,
	 * java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logWarn(final Object aWho, final CharSequence aWhat,
			final Object... aInfos) {
		log(Level.WARNING, aWho, aWhat, aInfos);
	}

	/**
   * 
   */
	protected void open() throws Exception {
		if (!hasLoggerName()) {
			throw new Exception(
					"No \"LoggerName\" available to configure Logger.");
		}
		if (!hasLevel()) {
			throw new Exception("No \"Level\" available to configure Logger.");
		}
		if (!hasFileHandler()) {
			throw new Exception(
					"No instance of FileHandler available to configure Logger.");
		}

		if (isOpened()) {
			throw new Exception("The Logger [" + pLogger.getName()
					+ "] is already opened.");
		}

		/*
		 * If a new logger is created its log level will be configured based on
		 * the LogManager configuration and it will configured to also send
		 * logging output to its parent's handlers. It will be registered in the
		 * LogManager global namespace.
		 */
		pLogger = Logger.getLogger(getLoggerName());
		// remove the current handler of the new logger
		removeHandlers(pLogger);
		// put in place the Filehandler in the logger
		if (hasFileHandler()) {
			pLogger.addHandler(pFileHandler);
			// use the same formater as the file handler one in the parent
			// logger
			if (pLogger.getUseParentHandlers()) {
				setFormater(pLogger.getParent(), pFileHandler.getFormatter());
			}
		}
		pLogger.setLevel(CActivityUtils.levelToLevel(pLevel));

		String wLine = String.format(FORMAT_OPENLOG, getLoggerName());
		// log in the current logger and in its parent
		logInfo(this, LIB_METHOD_OPEN, wLine);

		// stop the logging in the parent logger
		pLogger.setUseParentHandlers(false);
	}

	@Override
	public CLogLineBuffer popLogLineBuffer() {
		// ce serait plus econome si on utilisait un cache pour ne pas
		// instancier un CLogLineBuffer a
		// chaque fois
		return new CLogLineBuffer();
	}

	@Override
	public void pushLogLineBuffer(final CLogLineBuffer aLoggerLineBuffer) {
		// si cache : remettre l'instance de CLogLineBuffer dans le cache
	}

	/**
	 * @param aLogger
	 * @return
	 */
	private int removeHandlers(final Logger aLogger) {
		int wMax = -1;
		if (aLogger != null) {
			Handler[] wHandlers = aLogger.getHandlers();
			wMax = (wHandlers != null) ? wHandlers.length : -1;
			int wI = 0;
			while (wI < wMax) {
				aLogger.removeHandler(wHandlers[wI]);
				wI++;
			}
		}
		return wMax;
	}

	/**
	 * @return
	 */
	protected void setFileHandler(final CActivityFileHandler aFileHandler) {
		pFileHandler = aFileHandler;
	}

	/**
	 * @param aLogger
	 * @param aFormatter
	 * @return
	 */
	private int setFormater(final Logger aLogger, final Formatter aFormatter) {
		int wMax = -1;
		if (aLogger != null) {
			Handler[] wHandlers = aLogger.getHandlers();
			wMax = (wHandlers != null) ? wHandlers.length : -1;
			int wI = 0;
			while (wI < wMax) {
				wHandlers[wI].setFormatter(aFormatter);
				wI++;
			}
		}
		return wMax;
	}
}
