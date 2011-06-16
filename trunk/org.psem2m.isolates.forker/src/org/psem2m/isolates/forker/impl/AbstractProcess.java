/**
 *
 */
package org.psem2m.isolates.forker.impl;

import org.psem2m.isolates.forker.IProcess;

/**
 * Common process methods
 */
public abstract class AbstractProcess implements IProcess {

    /** The associated isolate ID */
    private String pIsolateId;

    /** The system process ID */
    private int pProcessId;

    /**
     * Retrieves the associated isolate ID
     * 
     * @return the isolate ID
     */
    @Override
    public String getIsolateId() {
	return pIsolateId;
    }

    /**
     * Retrieves the system process ID
     * 
     * @return the system process ID
     */
    @Override
    public int getProcessId() {
	return pProcessId;
    }

    /**
     * Sets the system process ID
     * 
     * @param aProcessId
     *            The system process ID
     */
    protected void setProcessId(final int aProcessId) {
	pProcessId = aProcessId;
    }
}