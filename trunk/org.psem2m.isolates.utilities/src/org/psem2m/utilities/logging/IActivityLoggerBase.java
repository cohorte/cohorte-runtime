package org.psem2m.utilities.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.psem2m.utilities.IXDescriber;

/**
 * @author Adonix Grenoble
 * @version 140
 */
public interface IActivityLoggerBase extends IXDescriber {

	public static final String ALL = Level.ALL.getName();

	public static final String CONFIG = Level.CONFIG.getName();

	public static final String FINE = Level.FINE.getName();

	public static final String FINER = Level.FINER.getName();

	public static final String FINEST = Level.FINEST.getName();

	public static final String INFO = Level.INFO.getName();

	public static final String OFF = Level.OFF.getName();

	public static final String SEVERE = Level.SEVERE.getName();

	public static final String WARNING = Level.WARNING.getName();

	/**
	 * @return
	 */
	public boolean isLogDebugOn();

	/**
	 * @param aLevel
	 * @return
	 */
	public boolean isLoggable(Level aLevel);

	/**
	 * @return
	 */
	public boolean isLogInfoOn();

	/**
	 * @return
	 */
	public boolean isLogSevereOn();

	/**
	 * @return
	 */
	public boolean isLogWarningOn();

	/**
	 * @param aLevel
	 * @param aWho
	 * @param aWhat
	 * @param aLine
	 */
	public void log(Level aLevel, Object aWho, CharSequence aWhat,
			Object... aInfos);

	/**
	 * @param record
	 */
	public void log(LogRecord record);

	/**
	 * @param aWho
	 * @param aWhat
	 * @param aInfos
	 */
	public void logDebug(Object aWho, CharSequence aWhat, Object... aInfos);

	/**
	 * @param aWho
	 * @param aWhat
	 * @param aInfos
	 */
	public void logInfo(Object aWho, CharSequence aWhat, Object... aInfos);

	/**
	 * @param aWho
	 * @param aWhat
	 * @param aInfos
	 */
	public void logSevere(Object aWho, CharSequence aWhat, Object... aInfos);

	/**
	 * @param aWho
	 * @param aWhat
	 * @param aInfos
	 */
	public void logWarn(Object aWho, CharSequence aWhat, Object... aInfos);

}
