/**
 * File:   CForkerImpl.java
 * Author: Thomas Calmant
 * Date:   14 juin 2011
 */
package org.psem2m.isolates.forker.impl;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.psem2m.isolates.forker.IForker;

/**
 * @author Thomas Calmant
 * 
 */
public class CForkerImpl implements IForker {

	/** Isolates register */
	private Map<String, Process> pIsolates = new TreeMap<String, Process>();

	public CForkerImpl() {
		// Empty constructor for iPOJO
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.forker.IForkerMBean#dispose()
	 */
	@Override
	public void dispose() {
		// Empty the pool
	}

	public void execute(final String aIsolateID, final String aCommand) {
		execute(aIsolateID, aCommand, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.forker.IForkerMBean#execute(org.psem2m.isolates.forker
	 * .IIsolateConfiguration)
	 */
	@Override
	public void execute(final String aIsolateID, final String aCommand,
			final String[] aEnvironment) {

		// Kill old isolate instances
		if (pIsolates.containsKey(aIsolateID)) {
			kill(aIsolateID);
		}

		try {
			// Run the new one
			Process process = Runtime.getRuntime().exec(aCommand);
			pIsolates.put(aIsolateID, process);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.forker.IForkerMBean#initializeForker(int)
	 */
	@Override
	public boolean initializeForker(final int aPoolSize) {

		// Run the pool
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.forker.IForkerMBean#kill(java.lang.String)
	 */
	@Override
	public boolean kill(final String aIsolateId) {

		Process process = pIsolates.get(aIsolateId);
		if (process != null) {

			// Sends a SIGTERM on Unix
			process.destroy();

			try {
				process.waitFor();
				pIsolates.remove(aIsolateId);

			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}

			return true;
		}

		return false;
	}
}
