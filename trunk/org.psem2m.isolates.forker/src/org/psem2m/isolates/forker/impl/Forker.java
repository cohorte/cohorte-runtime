/**
 * File:   Forker.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.forker.impl;

import java.util.Map;
import java.util.TreeMap;

import org.psem2m.isolates.commons.IIsolateConfiguration;
import org.psem2m.isolates.commons.IPlatformConfiguration;
import org.psem2m.isolates.commons.forker.IForker;
import org.psem2m.isolates.forker.Activator;
import org.psem2m.isolates.forker.IIsolateRunner;
import org.psem2m.isolates.forker.IProcessRef;
import org.psem2m.isolates.forker.impl.runners.FelixRunner;
import org.psem2m.isolates.forker.impl.runners.JavaRunner;

/**
 * Basic forker information behaviors
 * 
 * @author Thomas Calmant
 */
public class Forker implements IForker {

    /** The known platform configuration */
    private IPlatformConfiguration pPlatformConfiguration;

    /** Isolate <-> Process association */
    private Map<String, IProcessRef> pRunningIsolates = new TreeMap<String, IProcessRef>();

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
     * org.psem2m.isolates.commons.forker.IForker#setConfiguration(org.psem2m
     * .isolates.commons.IPlatformConfiguration)
     */
    @Override
    public void setConfiguration(
	    final IPlatformConfiguration aPlatformConfiguration) {
	pPlatformConfiguration = aPlatformConfiguration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.commons.forker.IForker#startIsolate(org.psem2m.isolates
     * .commons.IIsolateConfiguration)
     */
    @Override
    public final void startIsolate(
	    final IIsolateConfiguration aIsolateConfiguration) throws Exception {

	final String isolateId = aIsolateConfiguration.getId();

	// Test if the isolate is already running
	if (pRunningIsolates.containsKey(isolateId)) {
	    throw new Exception("The isolate '" + isolateId
		    + "' is already running");
	}

	// Find the runner for this isolate
	// TODO maybe use services ?
	IIsolateRunner isolateRunner;

	switch (aIsolateConfiguration.getKind()) {
	case JAVA:
	    isolateRunner = new JavaRunner();
	    break;

	case FELIX:
	    isolateRunner = new FelixRunner();

	default:
	    throw new UnsupportedOperationException("Unknown runner : "
		    + aIsolateConfiguration.getKind());
	}

	// Prepare the runner
	isolateRunner.setConfiguration(pPlatformConfiguration);

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

	Activator.getLogger().logDebug(this, "killProcess",
		"Trying to kill : ", aIsolateId);

	IProcessRef process = pRunningIsolates.get(aIsolateId);
	if (process != null) {

	}

	pRunningIsolates.remove(aIsolateId);
    }
}
