/**
 * File:   ProcessRef.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.forker;

import java.io.Serializable;



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

    /**
     * Sets up the reference
     * 
     * @param aPid
     */
    public ProcessRef(final int aPid) {
	pPid = aPid;
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
}
