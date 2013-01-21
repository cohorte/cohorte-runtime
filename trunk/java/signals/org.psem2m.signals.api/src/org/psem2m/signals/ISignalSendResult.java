/**
 * File:   ISignalSendResult.java
 * Author: Thomas Calmant
 * Date:   12 juin 2012
 */
package org.psem2m.signals;

import java.util.Map;

/**
 * Represents the result of a signal
 * 
 * @author Thomas Calmant
 */
public interface ISignalSendResult {

    /**
     * Retrieves the IDs of the isolates that failed
     * 
     * @return the IDs of the isolates that failed
     */
    String[] getFailed();

    /**
     * Retrieves the results of each listener of each isolate
     * 
     * @return the isolates results (UID -&gt; Result)
     */
    Map<String, Object[]> getResults();
}
