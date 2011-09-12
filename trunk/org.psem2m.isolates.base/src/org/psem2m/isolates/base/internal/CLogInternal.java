/**
 * File:   CLogInternal.java
 * Author: Thomas Calmant
 * Date:   12 sept. 2011
 */
package org.psem2m.isolates.base.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogService;
import org.psem2m.utilities.CXJavaCallerContext;
import org.psem2m.utilities.CXJavaRunContext;
import org.psem2m.utilities.logging.IActivityLogger;

/**
 * Internal log handler
 * 
 * @author Thomas Calmant
 */
public class CLogInternal {

    /** Maximum stored entries */
    public static final int MAX_ENTRIES = 100;

    /** Log line formatter */
    private IActivityLogger pActivityLogger;

    /** Log entries */
    private final LinkedList<LogEntry> pLogEntries = new LinkedList<LogEntry>();

    /** Active log readers */
    private final List<CLogReaderServiceImpl> pLogReaders = new ArrayList<CLogReaderServiceImpl>();

    /**
     * Prepares the internal log
     * 
     * @param aLogger
     *            The underlying logger
     */
    public CLogInternal(final IActivityLogger aLogger) {
	pActivityLogger = aLogger;
    }

    /**
     * Adds the given log entry to the log
     * 
     * @param aEntry
     *            A log entry
     */
    public synchronized void addEntry(final LogEntry aEntry) {

	// Convert log level
	final Level logLevel = osgiToJavaLogLevel(aEntry.getLevel());

	/*
	 * Find log caller (0-3: CXJavaCallerContext, 4: CLogInternal, 5:
	 * CLogServiceImpl, 6: Logger)
	 */
	final Class<?> who = CXJavaCallerContext.getCaller(6);

	// Find what
	final String what = CXJavaRunContext.getPreCallingMethod();

	// Log the entry
	final Throwable throwable = aEntry.getException();

	if (throwable != null) {
	    // Full log
	    pActivityLogger.log(logLevel, who, what, aEntry.getMessage(),
		    throwable);

	} else {
	    // Ignore the throwable if it's null
	    pActivityLogger.log(logLevel, who, what, aEntry.getMessage());
	}

	// Remove the oldest entry if needed
	if (pLogEntries.size() == MAX_ENTRIES) {
	    pLogEntries.removeLast();
	}

	// Add the entry to the list
	pLogEntries.addFirst(aEntry);

	// Notify readers
	for (CLogReaderServiceImpl reader : pLogReaders) {
	    reader.notifyListeners(aEntry);
	}
    }

    /**
     * Adds the given log reader to the handler, for log notifications
     * 
     * @param aReader
     *            LogReader to be added
     */
    protected synchronized void addLogReader(final CLogReaderServiceImpl aReader) {
	pLogReaders.add(aReader);
    }

    /**
     * Retrieves all stored entries as an enumeration
     * 
     * @return All stored entries
     */
    public synchronized Enumeration<?> getEntries() {

	return Collections.enumeration(pLogEntries);
    }

    /**
     * Converts OSGi log levels to Java ones. Unknown levels are considered as
     * INFO.
     * 
     * @param aOsgiLevel
     *            A OSGi log level (see {@link LogService})
     * @return The corresponding Java log level
     */
    protected Level osgiToJavaLogLevel(final int aOsgiLevel) {

	switch (aOsgiLevel) {

	case LogService.LOG_ERROR:
	    return Level.SEVERE;

	case LogService.LOG_WARNING:
	    return Level.WARNING;

	case LogService.LOG_DEBUG:
	    return Level.FINEST;

	case LogService.LOG_INFO:
	default:
	    return Level.INFO;
	}
    }

    /**
     * Removes the given log reader from the internal log handler
     * 
     * @param aReader
     *            Log reader to remove
     */
    public synchronized void removeLogReader(final CLogReaderServiceImpl aReader) {
	pLogReaders.remove(aReader);
    }
}
