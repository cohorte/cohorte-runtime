/**
 * File:   IForker.java
 * Author: Thomas Calmant
 * Date:   14 juin 2011
 */
package org.psem2m.isolates.forker;

/**
 * Represents a forker service
 * 
 * @author Thomas Calmant
 */
public interface IForker {

	/**
	 * Stops all running isolates and the pool of isolates
	 */
	public void dispose();

	public void execute(String aIsolateID, String aCommand,
			String[] aEnvironment);

	/**
	 * Initializes the forker and its pool of isolates.
	 * 
	 * @param aPoolSize
	 *            Number of pre-loaded basic isolates
	 * @return True on success
	 */
	public boolean initializeForker(int aPoolSize);

	/**
	 * Kills the given isolate
	 * 
	 * @param aIsolateId
	 *            The ID of the isolate to kill
	 * @return True on success
	 */
	public boolean kill(String aIsolateId);
}
