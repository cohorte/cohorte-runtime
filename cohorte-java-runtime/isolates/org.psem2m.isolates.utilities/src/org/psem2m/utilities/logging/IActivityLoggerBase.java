package org.psem2m.utilities.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.psem2m.utilities.IXDescriber;

/**
 * @author Adonix Grenoble
 * @version 140
 */
public interface IActivityLoggerBase extends IXDescriber {

    // Level.ALL => "ALL", Integer.MIN_VALUE
    public static final String ALL = CActivityLevel.ALL.getName();
    // Level.CONFIG => "CONFIG", 700,
    public static final String CONFIG = CActivityLevel.CONFIG.getName();
    // Same as Level.FINE
    public static final String DEBUG = CActivityLevel.DEBUG.getName();
    // Same as Level.SEVERE
    public static final String ERROR = CActivityLevel.ERROR.getName();
    // Level.FINE => "FINE", 500
    public static final String FINE = CActivityLevel.FINE.getName();
    // Level.FINER => "FINER", 400
    public static final String FINER = CActivityLevel.FINER.getName();
    // Level.FINEST => "FINEST", 300
    public static final String FINEST = CActivityLevel.FINEST.getName();
    // Level.INFO => "INFO", 800
    public static final String INFO = CActivityLevel.INFO.getName();
    // Level.OFF => "OFF", Integer.MAX_VALUE
    public static final String OFF = CActivityLevel.OFF.getName();
    // Level.SEVERE => "SEVERE", 1000
    public static final String SEVERE = CActivityLevel.SEVERE.getName();
    // Level.WARNING => "WARNING", 900
    public static final String WARNING = CActivityLevel.WARNING.getName();

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
