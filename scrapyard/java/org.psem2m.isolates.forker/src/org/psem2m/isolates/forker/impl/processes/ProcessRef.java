/**
 * File:   ProcessRef.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.forker.impl.processes;

import java.io.Serializable;

import org.psem2m.isolates.forker.IProcessRef;

/**
 * Represents a reference to an OS process
 * 
 * @author Thomas Calmant
 */
public class ProcessRef implements IProcessRef, Serializable {

    /** Serializable version UID */
    private static final long serialVersionUID = 1L;

    /** The process ID */
    private int pPid;

    /** The process object */
    private Process pProcess;

    /**
     * Sets up the reference
     * 
     * @param aPid
     *            The process ID (-1 if unknown)
     * @param aProcess
     *            The Runtime executed process (null if non sense)
     */
    public ProcessRef(final int aPid, final Process aProcess) {
	pPid = aPid;
	pProcess = aProcess;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.forker.IProcessRef#getPid()
     */
    @Override
    public int getPid() {
	return pPid;
    }

    /**
     * Returns the Java Process object, if any
     * 
     * @return the Java Process object
     */
    public Process getProcess() {
	return pProcess;
    }
}
