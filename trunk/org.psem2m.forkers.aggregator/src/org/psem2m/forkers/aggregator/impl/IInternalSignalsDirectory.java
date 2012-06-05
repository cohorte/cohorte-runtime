/**
 * File:   IInternalSignalsDirectory.java
 * Author: Thomas Calmant
 * Date:   25 mai 2012
 */
package org.psem2m.forkers.aggregator.impl;

import org.psem2m.isolates.services.remote.signals.ISignalsDirectory;

/**
 * Interface implemented by internal directories
 * 
 * @author Thomas Calmant
 */
public interface IInternalSignalsDirectory extends ISignalsDirectory {

    /**
     * Adds an isolate to the internal directory
     * 
     * @param aIsolateId
     *            The isolate ID
     * @param aHostName
     *            The host name to access the isolate
     * @param aPort
     *            The port to access the isolate
     * @return True if the isolate has been added to the registry
     */
    boolean addIsolate(String aIsolateId, String aHostName, int aPort);

    /** Get the forker ID for the given host name */
    String getForkerForHost(String aHostName);

    /** Get all forkers IDs */
    String[] getForkers();

    /**
     * Retrieves the host name of the given isolate
     * 
     * @param aIsolateId
     *            An isolate ID
     * @return The isolate host name or null.
     */
    String getHostForIsolate(String aIsolateId);

    /**
     * Retrieves all isolates corresponding to the given host
     * 
     * @param aHost
     *            A host name
     * @return All isolates corresponding to the given host, or null
     */
    String[] getIsolatesForHost(String aHost);

    /** Get all monitors IDs */
    String[] getMonitors();

    /**
     * Removes an isolate from the internal directory
     * 
     * @param aIsolateId
     *            The isolate ID
     * @return True if the isolate has been removed
     */
    boolean removeIsolate(String aIsolateId);

    /**
     * Sets an alias for the given isolate ID
     * 
     * @param aHost
     *            A host name
     * @param aAlias
     *            An alias for the given isolate ID
     */
    void setHostAlias(String aHost, String aAlias);
}
