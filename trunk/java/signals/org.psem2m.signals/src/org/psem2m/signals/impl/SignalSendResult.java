/**
 * File:   SignalSendResult.java
 * Author: Thomas Calmant
 * Date:   12 juin 2012
 */
package org.psem2m.signals.impl;

import java.util.Map;

import org.psem2m.signals.ISignalSendResult;

/**
 * Represents the result of a sending loop
 * 
 * @author Thomas Calmant
 */
public class SignalSendResult implements ISignalSendResult {

    /** Isolates that failed */
    private final String[] pFailed;

    /** Results of each listener of each isolate */
    private final Map<String, Object[]> pResults;

    /**
     * Sets up the bean
     * 
     * @param aResults
     *            Results of each listener of each isolate
     * @param aFailed
     *            Isolates that failed
     */
    public SignalSendResult(final Map<String, Object[]> aResults,
            final String[] aFailed) {

        pFailed = aFailed;
        pResults = aResults;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.impl.ISignalSendResult#getFailed()
     */
    @Override
    public String[] getFailed() {

        return pFailed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.impl.ISignalSendResult#getResults()
     */
    @Override
    public Map<String, Object[]> getResults() {

        return pResults;
    }
}
