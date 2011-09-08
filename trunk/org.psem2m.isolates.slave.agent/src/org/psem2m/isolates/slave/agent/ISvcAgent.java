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
     * Stops and removes all bundles added by this agent.
     */
    void neutralizeIsolate();

    /**
     * Installs and starts required bundles to complete the isolate
     * configuration.
     * 
     * Implementations should grab the isolate ID somehow, e.g. using system
     * properties, and call {@link #prepareIsolate(String)}.
     * 
     * Callers should call {@link #neutralizeIsolate()} on error.
     * 
     * @throws Exception
     *             An error occurred while preparing the isolate
     */
    void prepareIsolate() throws Exception;

    /**
     * Installs and starts required bundles to complete the isolate
     * configuration.
     * 
     * Implementations should update the isolate ID system property. Sadly, it
     * seems we can't change the working directory in Java.
     * 
     * Callers should call {@link #neutralizeIsolate()} on error.
     * 
     * @param aIsolateId
     *            ID of the isolate to prepare
     * @throws Exception
     *             An error occurred while preparing the isolate
     */
    void prepareIsolate(String aIsolateId) throws Exception;
}
