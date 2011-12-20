/**
 * File:   IsolateLogger.java
 * Author: Thomas Calmant
 * Date:   20 déc. 2011
 */
package org.psem2m.isolates.foundations;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.utilities.logging.CLogLineTextBuilder;

/**
 * Simple logger implementation
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-foundations-logger-factory", publicFactory = false)
@Provides(specifications = IIsolateLoggerSvc.class)
@Instantiate(name = "psem2m-foundations-logger")
public class IsolateLogger extends CPojoBase implements IIsolateLoggerSvc {

    /** The log line buider */
    private CLogLineTextBuilder pLineBuilder;

    /** The standard log service */
    @Requires(optional = true)
    private LogService pLogger;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLineBuilder = null;

        pLogger.log(LogService.LOG_DEBUG, "PSEM2M Logger Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogDebugOn()
     */
    @Override
    public boolean isLogDebugOn() {

        return isLoggable(Level.FINEST);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.utilities.logging.IActivityLoggerBase#isLoggable(java.util
     * .logging.Level)
     */
    @Override
    public boolean isLoggable(final Level aLevel) {

        return aLevel.intValue() >= Level.INFO.intValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogInfoOn()
     */
    @Override
    public boolean isLogInfoOn() {

        return isLoggable(Level.INFO);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogSevereOn()
     */
    @Override
    public boolean isLogSevereOn() {

        return isLoggable(Level.SEVERE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogWarningOn()
     */
    @Override
    public boolean isLogWarningOn() {

        return isLoggable(Level.WARNING);
    }

    /**
     * Converts a Java logging level to a OSGi LogService one
     * 
     * @param aLevel
     *            A Java logging level
     * @return The corresponding LogService level
     */
    private int levelToLogService(final Level aLevel) {

        if (aLevel == null) {
            // Info by default
            return LogService.LOG_INFO;
        }

        if (aLevel.equals(Level.SEVERE)) {
            return LogService.LOG_ERROR;

        } else if (aLevel.equals(Level.WARNING)) {
            return LogService.LOG_WARNING;

        } else if (aLevel.equals(Level.INFO)) {
            return LogService.LOG_INFO;
        }

        // Any other cases : DEBUG
        return LogService.LOG_DEBUG;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.utilities.logging.IActivityLoggerBase#log(java.util.logging
     * .Level, java.lang.Object, java.lang.CharSequence, java.lang.Object[])
     */
    @Override
    public void log(final Level aLevel, final Object aWho,
            final CharSequence aWhat, final Object... aInfos) {

        if (!isLoggable(aLevel)) {
            return;
        }

        // Prepare the line
        final StringBuilder logLine = new StringBuilder();

        // Object
        logLine.append("[Object=");
        logLine.append(pLineBuilder.formatWhoObjectId(aWho)).append("] ");

        // What
        logLine.append("[").append(String.valueOf(aWhat)).append("] ");

        // Information
        logLine.append(pLineBuilder.formatLogLine(aInfos));

        System.out.println(logLine);

        // Log it...
        pLogger.log(levelToLogService(aLevel), logLine.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.utilities.logging.IActivityLoggerBase#log(java.util.logging
     * .LogRecord)
     */
    @Override
    public void log(final LogRecord aRecord) {

        if (aRecord == null || !isLoggable(aRecord.getLevel())) {
            return;
        }

        // Prepare the line
        final StringBuilder logLine = new StringBuilder();

        // Object / logger
        logLine.append("[Logger=").append(aRecord.getLoggerName()).append("]");

        // What / source method
        logLine.append("[");

        final String sourceClass = aRecord.getSourceClassName();
        final String sourceMethod = aRecord.getSourceMethodName();

        if (sourceClass != null) {
            logLine.append(sourceClass);
        }

        if (sourceMethod != null) {
            if (sourceClass != null) {
                logLine.append(".");
            }

            logLine.append(sourceMethod);
        }

        if (sourceClass == null && sourceMethod == null) {
            logLine.append("<unknown method>");
        }

        // Information
        logLine.append(pLineBuilder.formatLogLine(aRecord.getMessage(),
                aRecord.getThrown()));

        // Log it
        pLogger.log(levelToLogService(aRecord.getLevel()), logLine.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.utilities.logging.IActivityLoggerBase#logDebug(java.lang.Object
     * , java.lang.CharSequence, java.lang.Object[])
     */
    @Override
    public void logDebug(final Object aWho, final CharSequence aWhat,
            final Object... aInfos) {

        log(Level.FINEST, aWho, aWhat, aInfos);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.utilities.logging.IActivityLoggerBase#logInfo(java.lang.Object
     * , java.lang.CharSequence, java.lang.Object[])
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
     * org.psem2m.utilities.logging.IActivityLoggerBase#logSevere(java.lang.
     * Object, java.lang.CharSequence, java.lang.Object[])
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
     * org.psem2m.utilities.logging.IActivityLoggerBase#logWarn(java.lang.Object
     * , java.lang.CharSequence, java.lang.Object[])
     */
    @Override
    public void logWarn(final Object aWho, final CharSequence aWhat,
            final Object... aInfos) {

        log(Level.WARNING, aWho, aWhat, aInfos);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLineBuilder = CLogLineTextBuilder.getInstance();

        pLogger.log(LogService.LOG_DEBUG, "PSEM2M Logger Ready");
    }
}
