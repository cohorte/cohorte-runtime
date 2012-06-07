/**
 * File:   Forker.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.forker.impl;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.LogRecord;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.base.isolates.IIsolateOutputListener;
import org.psem2m.isolates.base.isolates.boot.IsolateStatus;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.isolates.forker.IIsolateRunner;
import org.psem2m.isolates.forker.IProcessRef;
import org.psem2m.isolates.forker.impl.processes.ProcessRef;
import org.psem2m.isolates.services.conf.beans.IsolateDescription;
import org.psem2m.isolates.services.forker.IForker;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;

/**
 * Basic forker information behaviors
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-forker-factory", publicFactory = false, propagation = true)
@Provides(specifications = IForker.class)
public class CForkerSvc extends CPojoBase implements IForker,
        IIsolateOutputListener {

    /** Isolate runners, injected by iPOJO */
    @Requires
    private IIsolateRunner[] pIsolateRunners;

    /** The logger service, injected by iPOJO */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Isolate <-> Process association */
    private final Map<String, IProcessRef> pRunningIsolates = new TreeMap<String, IProcessRef>();

    /** Inter-isolates signal broadcaster */
    @Requires
    private ISignalBroadcaster pSignalBroadcaster;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.forker.IForker#getHostName()
     */
    @Override
    public String getHostName() {

        return Utilities.getHostName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.isolates.IIsolateOutputListener#
     * handleIsolateLogRecord(java.lang.String, java.util.logging.LogRecord)
     */
    @Override
    public void handleIsolateLogRecord(final String aSourceIsolateId,
            final LogRecord aLogRecord) {

        pLogger.log(aLogRecord);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.base.isolates.IIsolateOutputListener#handleIsolateStatus
     * (java.lang.String, org.psem2m.isolates.base.isolates.boot.IsolateStatus)
     */
    @Override
    public void handleIsolateStatus(final String aSourceIsolateId,
            final IsolateStatus aIsolateStatus) {

        if (aIsolateStatus == null) {
            // Contact lost with the isolate
            pRunningIsolates.remove(aSourceIsolateId);

            // Send the special signal and return
            pSignalBroadcaster.sendData(
                    ISignalBroadcaster.EEmitterTargets.MONITORS,
                    ISignalsConstants.ISOLATE_LOST_SIGNAL, aSourceIsolateId);

            pLogger.logInfo(this, "handleIsolateStatus", "Contact lost with",
                    aSourceIsolateId);
            return;
        }

        // Status read from the isolate itself
        final int isolateState = aIsolateStatus.getState();
        if (isolateState == IsolateStatus.STATE_FAILURE
                || isolateState == IsolateStatus.STATE_FRAMEWORK_STOPPED) {
            // Isolate stopped
            pRunningIsolates.remove(aSourceIsolateId);
        }

        // Send the signal
        pSignalBroadcaster.sendData(
                ISignalBroadcaster.EEmitterTargets.MONITORS,
                ISignalsConstants.ISOLATE_STATUS_SIGNAL, aIsolateStatus);

        pLogger.logInfo(this, "handleIsolateStatus", "Read from",
                aSourceIsolateId, ":", aIsolateStatus);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() {

        // logs in the bundle logger
        pLogger.logInfo(this, "invalidatePojo", "INVALIDATE", toDescription());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.forker.IForker#ping(java.lang.String)
     */
    @Override
    public int ping(final String aIsolateId) {

        final IProcessRef process = pRunningIsolates.get(aIsolateId);
        if (process == null) {
            return DEAD;
        }

        // TODO ping process

        return ALIVE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.forker.IForker#setPlatformStopping()
     */
    @Override
    public void setPlatformStopping() {

        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.forker.IForker#startIsolate(java.util.Map)
     */
    @Override
    public final int startIsolate(
            final Map<String, Object> aIsolateConfiguration) {

        final IsolateDescription isolateDesc = new IsolateDescription(
                aIsolateConfiguration);

        final String isolateId = isolateDesc.getId();
        pLogger.logInfo(this, "startIsolate", "Trying to launch", isolateId);

        if (isolateId == null || isolateId.isEmpty()) {
            pLogger.logSevere(this, "startIsolate",
                    "Can't start an isolate with an empty ID");
        }

        // Test if the isolate is already running
        if (pRunningIsolates.containsKey(isolateId)) {
            pLogger.logInfo(this, "startIsolate", "Already running =",
                    isolateId);
            return ALREADY_RUNNING;
        }

        // Find the runner for this isolate
        final String isolateKind = isolateDesc.getKind();

        IIsolateRunner isolateRunner = null;
        for (final IIsolateRunner availableRunner : pIsolateRunners) {
            if (availableRunner.canRun(isolateKind)) {
                isolateRunner = availableRunner;
                break;
            }
        }

        // Fail if no runner was found
        if (isolateRunner == null) {
            return UNKNOWN_KIND;
        }

        // Run it
        final IProcessRef isolateRef;

        try {
            isolateRef = isolateRunner.startIsolate(isolateDesc);

        } catch (final Exception e) {
            e.printStackTrace();
            return RUNNER_EXCEPTION;
        }

        if (isolateRef == null) {
            return NO_PROCESS_REF;
        }

        // Store it
        pRunningIsolates.put(isolateId, isolateRef);

        // Start the output reader
        try {
            final CProcessWatcherThread watcherThread = new CProcessWatcherThread(
                    this, isolateId, ((ProcessRef) isolateRef).getProcess());
            watcherThread.start();

        } catch (final IOException ex) {
            pLogger.logWarn(this, "startIsolate",
                    "Can't start the watcher for :", isolateId, ex);
            return NO_WATCHER;
        }

        return SUCCESS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.commons.forker.IForker#killProcess(java.lang.String)
     */
    @Override
    public void stopIsolate(final String aIsolateId) {

        pLogger.logInfo(this, "stopIsolate", "Trying to kill", aIsolateId);

        final IProcessRef process = pRunningIsolates.get(aIsolateId);
        if (process != null) {
            // TODO Do it a little more softly
            ((ProcessRef) process).getProcess().destroy();
        }

        pRunningIsolates.remove(aIsolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() {

        // logs in the bundle logger
        pLogger.logInfo(this, "validatePojo", "VALIDATE",

        toDescription());
    }
}
