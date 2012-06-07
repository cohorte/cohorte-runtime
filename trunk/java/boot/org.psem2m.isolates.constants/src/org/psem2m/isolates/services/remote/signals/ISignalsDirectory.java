/**
 * File:   ISignalsDirectory.java
 * Author: Thomas Calmant
 * Date:   19 déc. 2011
 */
package org.psem2m.isolates.services.remote.signals;

import java.util.Collection;

/**
 * Defines a directory for the PSEM2M Signals services. Provides access strings
 * to each isolates or to a group of isolates.
 * 
 * @author Thomas Calmant
 */
public interface ISignalsDirectory {

    /**
     * Retrieves the host isolate ID
     * 
     * @return The current isolate ID
     */
    String getCurrentIsolateId();

    /**
     * Retrieves the access string to the given isolate. Returns null if the
     * isolate is unknown. Access to the current isolate and the forker can be
     * returned by this method.
     * 
     * @param aIsolateId
     *            An isolate ID
     * @return The access string to the isolate, or null.
     */
    String getIsolate(String aIsolateId);

    /**
     * Retrieves the access string of each of the given isolates. Unknown
     * isolates are ignored. Returns null if all given isolates are unknown.
     * Current isolate and the forker (in PSEM2M) access URLs <strong>must
     * not</strong> be returned by this method.
     * 
     * @param aIsolatesIds
     *            A collection of isolate IDs
     * @return Access strings to the known isolates, null if none is known.
     */
    String[] getIsolates(Collection<String> aIsolatesIds);

    /**
     * Retrieves the access string of each isolate corresponding to the given
     * target. Returns null if the predefined targets value is unknown.
     * 
     * <p>
     * Targets can be :
     * </p>
     * <ul>
     * <li>ALL : Returns isolates and monitors access URLs, except the forker
     * (in PSEM2M) and the current isolate</li>
     * <li>ISOLATES : Returns isolates access URLs, except the current one</li>
     * <li>MONITORS : Returns monitors access URLs, excluding the forker and the
     * current isolate.</li>
     * <li>FORKER : Returns the forker access URL (in PSEM2M)</li>
     * <li>LOCAL : Returns the current isolate access URL (for information only)
     * </li>
     * </ul>
     * 
     * @param aTargets
     *            Predefined targets
     * @return Access strings to the targets, null if the value is unknown.
     */
    String[] getIsolates(ISignalBroadcaster.EEmitterTargets aTargets);

    /**
     * Retrieves the access string of each of the given isolates. Unknown
     * isolates are ignored. Returns null if all given isolates are unknown.
     * Current isolate and the forker (in PSEM2M) access URLs <strong>must
     * not</strong> be returned by this method.
     * 
     * @param aIsolatesIds
     *            An array of isolate IDs
     * @return Access strings to the known isolates, null if none is known.
     */
    String[] getIsolates(String[] aIsolatesIds);

    /**
     * Asks the directory to refresh or reload its content.
     * 
     * @return True if the directory changed
     */
    boolean reloadDirectory();
}
