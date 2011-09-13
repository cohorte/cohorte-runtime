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

import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.monitor.IBundleMonitorLoggerService;
import org.psem2m.isolates.monitor.IIsolateListener;
import org.psem2m.isolates.monitor.IIsolateManager;
import org.psem2m.isolates.services.conf.IIsolateDescr;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.forker.IForker;
import org.psem2m.utilities.CXTimer;

/**
 * Isolate manager : starts/restarts/stops isolates according to the
 * configuration
 */
public class CIsolateManagerSvc extends CPojoBase implements IIsolateManager,
	IIsolateListener {

    /** Logger, injected by iPOJO **/
    private IBundleMonitorLoggerService pBundleMonitorLoggerSvc;

    /** Configuration service, injected by iPOJO */
    private ISvcConfig pConfigurationSvc;

    /** Forker service, injected by iPOJO */
    private IForker pForkerSvc;

    /** Isolate monitors */
    private final Map<String, ProcessMonitorThread> pMonitors = new TreeMap<String, ProcessMonitorThread>();

    /** Platform running */
    private boolean pRunning = false;

    /**
     * Initiates the manager
     */
    public CIsolateManagerSvc() {
	super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.CXObjectBase#destroy()
     */
    @Override
    public void destroy() {
	// ...
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.monitor.IIsolateManager#getRunningIsolates()
     */
    @Override
    public Collection<String> getRunningIsolates() {
	return pMonitors.keySet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() {
	// logs in the bundle logger
	pBundleMonitorLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
		toDescription());

	stopPlatform();
    }

    /**
     * Tests if the given isolate ID is an internal one, therefore if it should
     * be ignored while starting the platform.
     * 
     * @param aIsolateId
     *            ID to be tested
     * @return True if the ID should be ignored
     */
    protected boolean isInternalIsolate(final String aIsolateId) {

	if (aIsolateId == null) {
	    // Invalid ID, don't use it
	    return true;
	}

	if (aIsolateId
		.startsWith(IPlatformProperties.SPECIAL_INTERNAL_ISOLATES_PREFIX)) {
	    // The ID begins with the internal prefix
	    return true;
	}

	final String currentId = System
		.getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_ID);
	if (aIsolateId.equals(currentId)) {
	    // Current isolate ID is an internal one
	    return true;
	}

	// Play with it
	return false;
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

	pBundleMonitorLoggerSvc.logInfo(this, "IsolateMonitor",
		"Isolate stopped : ", aIsolateId);

	if (pMonitors.containsKey(aIsolateId)) {
	    // Stop the monitor
	    Thread monitor = pMonitors.get(aIsolateId);
	    monitor.interrupt();

	    // Remove it from the map
	    pMonitors.remove(aIsolateId);
	}

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

	pBundleMonitorLoggerSvc.logWarn(this, "IsolateMonitor",
		"Monitor stopped before the isolate : ", aIsolateId);

	pMonitors.remove(aIsolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.monitor.IIsolateManager#restartPlatform(boolean)
     */
    @Override
    public boolean restartPlatform(final boolean aForce) {

	if (aForce) {
	    stopPlatform();
	    startPlatform();
	    return true;
	}

	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.monitor.IIsolateManager#startIsolate(java.lang.String
     * , boolean)
     */
    @Override
    public boolean startIsolate(final String aIsolateId,
	    final boolean aForceRestart) {

	// Performance counter (for DEBUG)
	final CXTimer timer = new CXTimer(true);

	// Get the isolate configuration
	final IIsolateDescr isolateConfig = pConfigurationSvc.getApplication()
		.getIsolate(aIsolateId);
	if (isolateConfig == null) {
	    return false;
	}

	try {
	    // Start the process
	    pForkerSvc.startIsolate(isolateConfig);

	    // TODO add a monitor

	} catch (InvalidParameterException e) {
	    pBundleMonitorLoggerSvc.logSevere(this, "startPlatform",
		    "Error in isolate configuration : ", e);
	    return false;

	} catch (IOException e) {
	    pBundleMonitorLoggerSvc.logSevere(this, "startPlatform",
		    "Error starting isolate : ", e);
	    return false;

	} catch (Exception e) {
	    pBundleMonitorLoggerSvc.logSevere(this, "startPlatform",
		    "Error preparing or starting isolate : ", e);
	}

	timer.stop();
	pBundleMonitorLoggerSvc.logDebug(this, "startPlatform", "Result : ",
		timer.getDurationStrMicroSec());

	return true;
    }

    /**
     * Starts all isolates from the configuration that are not the current one
     * neither internal ones
     */
    protected void startPlatform() {

	for (String isolateId : pConfigurationSvc.getApplication()
		.getIsolateIds()) {

	    if (!isInternalIsolate(isolateId)) {
		startIsolate(isolateId, true);
	    }
	}

	pRunning = true;
	pBundleMonitorLoggerSvc.logInfo(this, "startPlatform",
		"Nb monitored isolates=[%d]", pMonitors.size());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.monitor.IIsolateManager#stopIsolate(java.lang.String)
     */
    @Override
    public boolean stopIsolate(final String aIsolateId) {

	pForkerSvc.stopIsolate(aIsolateId);
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.monitor.IIsolateManager#stopPlatform()
     */
    @Override
    public synchronized void stopPlatform() {

	pRunning = false;
	int nbStopped = 0;

	// Stop every isolate
	for (String isolateId : pMonitors.keySet()) {
	    pForkerSvc.stopIsolate(isolateId);
	    nbStopped++;
	}

	// Clear the map
	pMonitors.clear();

	pBundleMonitorLoggerSvc.logInfo(this, "stopPlatform",
		"Nb stopped isolates=[%d] ", nbStopped);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() {
	// logs in the bundle logger
	pBundleMonitorLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",
		toDescription());

	// Start the whole platform
	startPlatform();
    }
}
