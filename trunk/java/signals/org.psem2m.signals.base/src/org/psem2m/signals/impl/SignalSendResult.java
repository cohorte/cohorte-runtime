/**
 * File:   SignalSendResult.java
 * Author: Thomas Calmant
 * Date:   12 juin 2012
 */
package org.psem2m.signals.impl;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();
        builder.append("SignalSendResults(Results=");

        if (pResults == null) {
            builder.append("null");

        } else {
            builder.append("{");
            for (final Entry<String, Object[]> entry : pResults.entrySet()) {
                builder.append(entry.getKey());
                builder.append("=");
                builder.append(Arrays.toString(entry.getValue()));
            }
            builder.append("}");
        }

        builder.append("; Failed=");
        builder.append(Arrays.toString(pFailed));
        builder.append(")");

        return builder.toString();
    }
}
