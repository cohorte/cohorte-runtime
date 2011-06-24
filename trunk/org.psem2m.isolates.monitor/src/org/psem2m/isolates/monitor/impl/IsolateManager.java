/**
 * File:   IsolateManager.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.monitor.impl;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.psem2m.isolates.commons.IIsolateConfiguration;
import org.psem2m.isolates.commons.IPlatformConfiguration;
import org.psem2m.isolates.commons.IReconfigurable;
import org.psem2m.isolates.commons.forker.IForker;
import org.psem2m.isolates.commons.forker.ProcessConfiguration;
import org.psem2m.isolates.commons.impl.PlatformConfiguration;
import org.psem2m.isolates.monitor.Activator;
import org.psem2m.isolates.monitor.IIsolateListener;
import org.psem2m.isolates.monitor.IIsolateManager;
import org.psem2m.utilities.CXTimer;
import org.psem2m.utilities.files.CXFileDir;

/**
 * Isolate manager : starts/restarts/stops isolates according to the
 * configuration
 */
public class IsolateManager implements IReconfigurable, IIsolateManager,
		IIsolateListener {

	/** Forker service */
	private IForker pForker;

	/** Isolate monitors */
	private final Map<String, ProcessMonitorThread> pMonitors = new TreeMap<String, ProcessMonitorThread>();

	/** Platform configuration */
	private IPlatformConfiguration pPlatformConfiguration;

	/** Possible isolates */
	private final Map<String, IIsolateConfiguration> pPossibleIsolates = new TreeMap<String, IIsolateConfiguration>();

	/** Platform running */
	private boolean pRunning = false;

	/** Running isolates */
	private final Map<String, ProcessConfiguration> pRunningIsolates = new TreeMap<String, ProcessConfiguration>();

	/**
	 * Initiates the manager
	 */
	public IsolateManager() {
		reloadConfiguration(null, true);
	}

	/**
	 * Return pPossibleIsolates.
	 * 
	 * @return pPossibleIsolates
	 */
	@Override
	public Collection<IIsolateConfiguration> getPossibleIsolates() {
		return pPossibleIsolates.values();
	}

	/**
	 * Return pRunningIsolates.
	 * 
	 * @return pRunningIsolates
	 */
	@Override
	public Collection<ProcessConfiguration> getRunningIsolates() {
		return pRunningIsolates.values();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.monitor.IIsolateListener#isolateStopped(java.lang
	 * .String)
	 */
	@Override
	public void isolateStopped(final String aIsolateId) {

		Activator.getLogger().logInfo(this, "IsolateMonitor",
				"Isolate stopped : ", aIsolateId);

		pRunningIsolates.remove(aIsolateId);
		pMonitors.remove(aIsolateId);

		if (pRunning) {
			// Restart isolate
			startIsolate(aIsolateId, true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.monitor.IIsolateListener#monitorStopped(java.lang
	 * .String)
	 */
	@Override
	public void monitorStopped(final String aIsolateId) {
		Activator.getLogger().logWarn(this, "IsolateMonitor",
				"Monitor stopped before the isolate : ", aIsolateId);

		pMonitors.remove(aIsolateId);
	}

	@Override
	public void reloadConfiguration(final String aPath, final boolean aForce) {

		// TODO call the configuration service
		// -Dorg.psem2m.platform.base=${workspace_loc}/psem2m/platforms/felix.user.dir
		CXFileDir wBaseDir = new CXFileDir(
				System.getProperty("org.psem2m.platform.base"));
		// -Dorg.psem2m.platform.target=${workspace_loc}/psem2m/platforms/felix
		CXFileDir wTargetDir = new CXFileDir(
				System.getProperty("org.psem2m.platform.target"));

		// Global platform configuration
		pPlatformConfiguration = new PlatformConfiguration(
				wBaseDir.getAbsolutePath(), wTargetDir.getAbsolutePath());

		pPlatformConfiguration
				.addCommonBundle("org.apache.felix.ipojo-1.8.0.jar");

		Activator.getLogger().logDebug(this, "reloadConfiguration", "Found ",
				pPossibleIsolates.size(), " isolates");

		// Update the forker
		pForker.setConfiguration(pPlatformConfiguration);
	}

	@Override
	public boolean restartPlatform(final boolean aForce) {

		if (aForce) {
			stopPlatform();
			startPlatform();
			return true;
		}

		return false;
	}

	@Override
	public boolean startIsolate(final String aIsolateId,
			final boolean aForceRestart) {

		CXTimer timer = new CXTimer(true);

		IIsolateConfiguration isolateConfig = pPossibleIsolates.get(aIsolateId);
		if (isolateConfig == null) {
			return false;
		}

		try {
			// Start the process
			pForker.startIsolate(isolateConfig);

		} catch (InvalidParameterException e) {
			Activator.getLogger().logSevere(this, "startPlatform",
					"Error in isolate configuration : ", e);
			return false;

		} catch (IOException e) {
			Activator.getLogger().logSevere(this, "startPlatform",
					"Error starting isolate : ", e);
			return false;

		} catch (Exception e) {
			Activator.getLogger().logSevere(this, "startPlatform",
					"Error preparing or starting isolate : ", e);
		}

		timer.stop();
		Activator.getLogger().logDebug(this, "startPlatform", "Result : ",
				timer.getDurationStrMicroSec());

		return true;
	}

	/**
	 * Loads the configuration for the first time
	 */
	protected void startPlatform() {

		Activator.getLogger().logDebug(this, "startPlatform", "Starting ",
				pPossibleIsolates.size(), " isolates");

		for (String isolateId : pPossibleIsolates.keySet()) {
			startIsolate(isolateId, true);
		}

		pRunning = true;
	}

	@Override
	public boolean stopIsolate(final String aIsolateId) {

		pForker.stopIsolate(aIsolateId);
		return true;
	}

	@Override
	public void stopPlatform() {

		pRunning = false;

		for (String isolateId : pRunningIsolates.keySet()) {
			pForker.stopIsolate(isolateId);
		}
	}
}
