/**
 * File:   IWaitingSignalListener.java
 * Author: Thomas Calmant
 * Date:   19 juin 2012
 */
package org.psem2m.signals;

/**
 * Interface to implement to be called back when the state of a stacked signal
 * changes
 * 
 * @author Thomas Calmant
 */
public interface IWaitingSignalListener {

    /**
     * The given waiting signal has been received by at least one isolate
     * 
     * @param aSignal
     *            The sent signal
     */
    void waitingSignalSent(IWaitingSignal aSignal);

    /**
     * The given waiting signal time to live expired before it has been sent
     * 
     * @param aSignal
     *            The waiting signal
     */
    void waitingSignalTimeout(IWaitingSignal aSignal);
}
