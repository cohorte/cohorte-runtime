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
import java.util.Map.Entry;
import java.util.TreeMap;

import org.psem2m.isolates.commons.IReconfigurable;
import org.psem2m.isolates.commons.PlatformConfiguration;
import org.psem2m.isolates.commons.forker.IForker;
import org.psem2m.isolates.commons.forker.IsolateConfiguration;
import org.psem2m.isolates.commons.forker.ProcessConfiguration;
import org.psem2m.isolates.monitor.Activator;
import org.psem2m.isolates.monitor.IIsolateListener;
import org.psem2m.isolates.monitor.IIsolateManager;

/**
 * Isolate manager : starts/restarts/stops isolates according to the
 * configuration
 */
public class IsolateManager implements IReconfigurable, IIsolateManager,
	IIsolateListener {

    /** Forker service */
    private IForker pForker;

    /** Isolate monitors */
    private Map<String, ProcessMonitorThread> pMonitors = new TreeMap<String, ProcessMonitorThread>();

    /** Platform configuration */
    private PlatformConfiguration pPlatformConfiguration;

    /** Possible isolates */
    private Map<String, ProcessConfiguration> pPossibleIsolates = new TreeMap<String, ProcessConfiguration>();

    /** Platform running */
    private boolean pRunning = false;

    /** Running isolates */
    private Map<String, ProcessConfiguration> pRunningIsolates = new TreeMap<String, ProcessConfiguration>();

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
    public Collection<ProcessConfiguration> getPossibleIsolates() {
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

	// Global platform configuration
	pPlatformConfiguration = new PlatformConfiguration(
		System.getProperty("user.home"),
		"/home/tcalmant/programmation/workspaces/psem2m/platforms/felix");

	pPlatformConfiguration
		.addCommonBundle("org.apache.felix.ipojo-1.8.0.jar");

	// Isolates definition
	String[] ids = new String[] { "htop", "python" };
	String[][] cmds = new String[][] {
		{ "/usr/bin/gnome-terminal", "-x", "htop" },
		{ "/usr/bin/gnome-terminal", "-x", "/usr/bin/python" } };

	for (int i = 0; i < ids.length; i++) {
	    IsolateConfiguration isolateConfig = new IsolateConfiguration(
		    ids[i]);

	    ProcessConfiguration processConfig = new ProcessConfiguration(
		    cmds[i], isolateConfig);

	    pPossibleIsolates.put(ids[i], processConfig);
	}

	Activator.getLogger().logDebug(this, "reloadConfiguration", "Found ",
		pPossibleIsolates.size(), " isolates");
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
    public Process startIsolate(final String aIsolateId,
	    final boolean aForceRestart) {

	Process process = null;

	ProcessConfiguration config = pPossibleIsolates.get(aIsolateId);
	if (config == null) {
	    return null;
	}

	try {
	    // Start the process
	    process = pForker.runProcess(pPlatformConfiguration, config);

	    // Start the monitor
	    ProcessMonitorThread monitor = new ProcessMonitorThread(this,
		    aIsolateId, process);

	    monitor.start();
	    pMonitors.put(aIsolateId, monitor);

	} catch (InvalidParameterException e) {
	    Activator.getLogger().logSevere(this, "startPlatform",
		    "Error in isolate configuration : ", e);

	} catch (IOException e) {
	    Activator.getLogger().logSevere(this, "startPlatform",
		    "Error starting isolate : ", e);
	}

	return process;
    }

    /**
     * Loads the configuration for the first time
     */
    protected void startPlatform() {

	Activator.getLogger().logDebug(this, "startPlatform", "Starting ",
		pPossibleIsolates.size(), " isolates");

	for (Entry<String, ProcessConfiguration> entry : pPossibleIsolates
		.entrySet()) {

	    try {
		String isolateId = entry.getKey();

		Process process = pForker.runProcess(pPlatformConfiguration,
			entry.getValue());

		ProcessMonitorThread monitor = new ProcessMonitorThread(this,
			isolateId, process);
		monitor.start();
		pMonitors.put(isolateId, monitor);

	    } catch (InvalidParameterException e) {
		Activator.getLogger().logSevere(this, "startPlatform",
			"Error in isolate configuration : ", e);

	    } catch (IOException e) {
		Activator.getLogger().logSevere(this, "startPlatform",
			"Error starting isolate : ", e);
	    }
	}

	pRunning = true;
    }

    @Override
    public boolean stopIsolate(final String aIsolateId) {

	pForker.killProcess(aIsolateId);
	return true;
    }

    @Override
    public void stopPlatform() {

	pRunning = false;

	for (String isolateId : pRunningIsolates.keySet()) {
	    pForker.killProcess(isolateId);
	}
    }
}
