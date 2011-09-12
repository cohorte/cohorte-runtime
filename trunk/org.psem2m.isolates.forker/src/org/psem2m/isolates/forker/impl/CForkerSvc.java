/**
 * File:   Forker.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.forker.impl;

import java.util.Map;
import java.util.TreeMap;

import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.forker.IBundleForkerLoggerSvc;
import org.psem2m.isolates.forker.IIsolateRunner;
import org.psem2m.isolates.forker.IProcessRef;
import org.psem2m.isolates.services.conf.IIsolateDescr;
import org.psem2m.isolates.services.forker.IForker;

/**
 * Basic forker information behaviors
 * 
 * @author Thomas Calmant
 */
public class CForkerSvc extends CPojoBase implements IForker {

    /** Service reference managed by iPojo (see metadata.xml) **/
    private IBundleForkerLoggerSvc pBundleForkerLoggerSvc;

    /** Isolate runners, injected by iPOJO */
    private IIsolateRunner[] pIsolateRunners;

    /** Isolate <-> Process association */
    private final Map<String, IProcessRef> pRunningIsolates = new TreeMap<String, IProcessRef>();

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
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() {
	// logs in the bundle logger
	pBundleForkerLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
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
    public final void startIsolate(final IIsolateDescr aIsolateConfiguration)
	    throws Exception {

	final String isolateId = aIsolateConfiguration.getId();

	pBundleForkerLoggerSvc.logInfo(this, "startIsolate",
		"Trying to launch =", isolateId);

	// Test if the isolate is already running
	if (pRunningIsolates.containsKey(isolateId)) {
	    throw new Exception("The isolate '" + isolateId
		    + "' is already running");
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
	    throw new Exception("No runner for : "
		    + aIsolateConfiguration.getKind());
	}

	// Run it
	IProcessRef isolateRef = isolateRunner
		.startIsolate(aIsolateConfiguration);

	if (isolateRef == null) {
	    throw new Exception("No reference to the isolate process. Abort.");
	}

	// Store it
	pRunningIsolates.put(isolateId, isolateRef);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.commons.forker.IForker#killProcess(java.lang.String)
     */
    @Override
    public void stopIsolate(final String aIsolateId) {

	pBundleForkerLoggerSvc.logInfo(this, "stopIsolate", "Trying to kill =",
		aIsolateId);

	IProcessRef process = pRunningIsolates.get(aIsolateId);
	if (process != null) {
	    // TODO
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
	pBundleForkerLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",
		toDescription());
    }
}
