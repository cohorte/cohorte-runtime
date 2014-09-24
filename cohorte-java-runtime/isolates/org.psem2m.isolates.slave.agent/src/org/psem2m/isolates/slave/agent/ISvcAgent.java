/**
 * File:   ISvcAgent.java
 * Author: Thomas Calmant
 * Date:   8 sept. 2011
 */
package org.psem2m.isolates.slave.agent;

/**
 * Describes an isolate agent service
 * 
 * @author Thomas Calmant
 */
public interface ISvcAgent {

    /**
     * Stops and removes all bundles added by this agent, stop the OSGI
     * framework and the basic bundles, and kill the process
     */
    void killIsolate();
}
