/**
 * File:   ProcessMonitorThread.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.monitor.impl;

import java.security.InvalidParameterException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.psem2m.isolates.monitor.IBundleMonitorLoggerService;
import org.psem2m.isolates.monitor.IIsolateListener;

/**
 * Monitor a process, signals the process death
 * 
 * @author Thomas Calmant
 */
@Component(name = "isolates-monitor-ProcessMonitorThread", immediate = true)
public class ProcessMonitorThread extends Thread {

	/** Service reference managed by iPojo (see metadata.xml) **/
	@Requires
	private IBundleMonitorLoggerService pBundleMonitorLoggerService;

	/** Monitored isolate ID */
	private final String pIsolateId;

	/** Isolate listener */
	private final IIsolateListener pListener;

	/** Monitored isolate process */
	private final Process pProcess;

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

			pBundleMonitorLoggerService.logSevere(this, "Monitor process",
					"Interrupted : ", e);

			pListener.monitorStopped(pIsolateId);
		}
	}
}
