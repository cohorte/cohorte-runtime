/**
 * File:   CLogReaderServiceImpl.java
 * Author: "Thomas Calmant"
 * Date:   12 sept. 2011
 */
package org.psem2m.isolates.base.internal;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

/**
 * @author Thomas Calmant
 * 
 */
public class CLogReaderServiceImpl implements LogReaderService {

    /** Internal log handler */
    private CLogInternal pLogInternal;

    /** Log listeners */
    private final Set<LogListener> pLogListeners = new HashSet<LogListener>();

    /**
     * Prepares the log reader service
     * 
     * @param aLogInternal
     *            Internal log handler
     */
    public CLogReaderServiceImpl(final CLogInternal aLogInternal) {
	pLogInternal = aLogInternal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.service.log.LogReaderService#addLogListener(org.osgi.service
     * .log.LogListener)
     */
    @Override
    public void addLogListener(final LogListener aListener) {
	pLogListeners.add(aListener);
    }

    /**
     * Clears the listeners list
     */
    public synchronized void close() {
	pLogListeners.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.service.log.LogReaderService#getLog()
     */
    @Override
    public Enumeration<?> getLog() {
	return pLogInternal.getEntries();
    }

    /**
     * Notifies all listeners of the given entry
     * 
     * @param aEntry
     *            A log entry
     */
    public synchronized void notifyListeners(final LogEntry aEntry) {

	for (LogListener listener : pLogListeners) {
	    listener.logged(aEntry);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.service.log.LogReaderService#removeLogListener(org.osgi.service
     * .log.LogListener)
     */
    @Override
    public void removeLogListener(final LogListener aListener) {
	pLogListeners.remove(aListener);
    }
}
