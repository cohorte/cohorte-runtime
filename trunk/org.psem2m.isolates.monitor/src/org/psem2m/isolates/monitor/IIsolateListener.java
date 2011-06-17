/**
 * File:   IIsolateListener.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.monitor;

/**
 * Description of an isolate life cycle listener
 */
public interface IIsolateListener {

    /**
     * The specified isolate has stopped.
     * 
     * @param aIsolateId
     *            The stopped isolate
     */
    public void isolateStopped(String aIsolateId);

    /**
     * The monitor of the given isolate has stopped. The isolate state is
     * unknown.
     * 
     * @param aIsolateId
     *            The monitored isolate id
     */
    public void monitorStopped(String aIsolateId);
}