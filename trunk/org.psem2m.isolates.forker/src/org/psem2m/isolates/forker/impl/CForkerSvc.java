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

import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.base.isolates.IIsolateOutputListener;
import org.psem2m.isolates.base.isolates.boot.IsolateStatus;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.isolates.forker.IIsolateRunner;
import org.psem2m.isolates.forker.IProcessRef;
import org.psem2m.isolates.forker.impl.processes.ProcessRef;
import org.psem2m.isolates.services.conf.IIsolateDescr;
import org.psem2m.isolates.services.forker.IForker;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;

/**
 * Basic forker information behaviors
 * 
 * @author Thomas Calmant
 */
public class CForkerSvc extends CPojoBase implements IForker,
        IIsolateOutputListener {

    /** The logger service, injected by iPOJO */
    private IIsolateLoggerSvc pIsolateLoggerSvc;

    /** Isolate runners, injected by iPOJO */
    private IIsolateRunner[] pIsolateRunners;

    /** Isolate <-> Process association */
    private final Map<String, IProcessRef> pRunningIsolates = new TreeMap<String, IProcessRef>();

    /** Inter-isolates signal broadcaster */
    private ISignalBroadcaster pSignalBroadcaster;

    /**
     * Default constructor
     */
    public CForkerSvc() {

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
     * @see org.psem2m.isolates.base.isolates.IIsolateOutputListener#
     * handleIsolateLogRecord(java.lang.String, java.util.logging.LogRecord)
     */
    @Override
    public void handleIsolateLogRecord(final String aSourceIsolateId,
            final LogRecord aLogRecord) {

        pIsolateLoggerSvc.log(aLogRecord);

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

        pIsolateLoggerSvc.logInfo(this, "", "Read from " + aSourceIsolateId
                + " : " + aIsolateStatus);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() {

        // logs in the bundle logger
        pIsolateLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
                toDescription());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.forker.IForker#ping(java.lang.String)
     */
    @Override
    public EProcessState ping(final String aIsolateId) {

        IProcessRef process = pRunningIsolates.get(aIsolateId);
        if (process == null) {
            return EProcessState.DEAD;
        }

        // TODO ping process

        return EProcessState.ALIVE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.commons.forker.IForker#startIsolate(org.psem2m.isolates
     * .base.conf.IIsolateDescr)
     */
    @Override
    public final EStartError startIsolate(
            final IIsolateDescr aIsolateConfiguration) {

        final String isolateId = aIsolateConfiguration.getId();
        pIsolateLoggerSvc.logInfo(this, "startIsolate", "Trying to launch =",
                isolateId);

        // Test if the isolate is already running
        if (pRunningIsolates.containsKey(isolateId)) {
            // throw new Exception("The isolate '" + isolateId
            // + "' is already running");

            pIsolateLoggerSvc.logInfo(this, "startIsolate",
                    "Already running =", isolateId);
            return EStartError.ALREADY_RUNNING;
        }

        // Find the runner for this isolate
        IIsolateRunner isolateRunner = null;

        final String isolateKind = aIsolateConfiguration.getKind();
        for (IIsolateRunner availableRunner : pIsolateRunners) {
            if (availableRunner.canRun(isolateKind)) {
                isolateRunner = availableRunner;
                break;
            }
        }

        // Fail if no runner was found
        if (isolateRunner == null) {
            // throw new Exception("No runner for : "
            // + aIsolateConfiguration.getKind());
            return EStartError.UNKNOWN_KIND;
        }

        // Run it
        final IProcessRef isolateRef;

        try {
            isolateRef = isolateRunner.startIsolate(aIsolateConfiguration);

        } catch (Exception e) {
            e.printStackTrace();
            return EStartError.RUNNER_EXCEPTION;
        }

        if (isolateRef == null) {
            // throw new
            // Exception("No reference to the isolate process. Abort.");
            return EStartError.NO_PROCESS_REF;
        }

        // Store it
        pRunningIsolates.put(isolateId, isolateRef);

        // Start the output reader
        try {
            final CProcessWatcherThread watcherThread = new CProcessWatcherThread(
                    this, isolateId, ((ProcessRef) isolateRef).getProcess());
            watcherThread.start();

        } catch (IOException ex) {
            pIsolateLoggerSvc.logWarn(this, "",

            "Can't start the watcher for :", isolateId, ex);
            return EStartError.NO_WATCHER;
        }

        return EStartError.SUCCESS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.commons.forker.IForker#killProcess(java.lang.String)
     */
    @Override
    public void stopIsolate(final String aIsolateId) {

        pIsolateLoggerSvc.logInfo(this, "stopIsolate", "Trying to kill =",

        aIsolateId);

        IProcessRef process = pRunningIsolates.get(aIsolateId);
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
    public void validatePojo() {

        // logs in the bundle logger
        pIsolateLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",

        toDescription());
    }
}
