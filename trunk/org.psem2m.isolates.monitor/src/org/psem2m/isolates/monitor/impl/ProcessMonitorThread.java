/**
 * File:   ProcessMonitorThread.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.monitor.impl;

import java.security.InvalidParameterException;

import org.psem2m.isolates.monitor.Activator;
import org.psem2m.isolates.monitor.IIsolateListener;

/**
 * Monitor a process, signals the process death
 * 
 * @author Thomas Calmant
 */
public class ProcessMonitorThread extends Thread {

    /** Monitored isolate ID */
    private String pIsolateId;

    /** Isolate listener */
    private IIsolateListener pListener;

    /** Monitored isolate process */
    private Process pProcess;

    /**
     * Prepares a monitor thread
     * 
     * @param aListener
     *            The isolate listener
     * @param aIsolateId
     *            The isolate ID
     * @param aProcess
     *            The isolate process
     * @throws InvalidParameterException
     *             One of the parameters is null.
     */
    public ProcessMonitorThread(final IIsolateListener aListener,
	    final String aIsolateId, final Process aProcess)
	    throws InvalidParameterException {

	super();
	pListener = aListener;
	pIsolateId = aIsolateId;
	pProcess = aProcess;

	if (pListener == null || pIsolateId == null || pProcess == null) {
	    throw new InvalidParameterException(
		    "Isolate ID, process and listener can't be null");
	}

	setName("monitor-" + pIsolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

	try {
	    // Use a IForker.ping() loop instead of a blocking waitFor()
	    pProcess.waitFor();
	    // pListener.isolateStopped(pIsolateId);

	} catch (InterruptedException e) {

	    Activator.getLogger().logSevere(this, "Monitor process",
		    "Interrupted : ", e);

	    pListener.monitorStopped(pIsolateId);
	}
    }
}
