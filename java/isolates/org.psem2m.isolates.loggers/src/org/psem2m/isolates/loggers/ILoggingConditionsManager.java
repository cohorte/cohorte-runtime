package org.psem2m.isolates.loggers;

import java.util.List;

import org.psem2m.utilities.IXDescriber;

/**
 * @author ogattaz
 *
 */
public interface ILoggingConditionsManager extends IXDescriber {

	/**
	 * Deletes all the files, closes the channel and remove it from the map.
	 *
	 * @param aConditionsId
	 *            the id of the logging conditions
	 * @return the number of deleted files
	 * @throws CLoggingConditionsException
	 *             if the logging conditions isn't closed or if IO error
	 */
	int cleanLoggingConditionsFile(String aConditionsId)
			throws CLoggingConditionsException;

	/**
	 * @return the list of available LoggingConditions
	 */
	List<ILoggingConditions> getLoggingConditions();

	/**
	 * @return the list of the IDs of the available channels
	 */
	List<String> getLoggingConditionsIds();

	boolean hasLoggingConditions(String aConditionsId);

	/**
	 * Creates or retrieves a logging conditions => a set of logging condition
	 *
	 * @param aConditionsId
	 *            the id of the logging conditions
	 * @return a new or an existing logging conditions
	 */
	ILoggingConditions newLoggingConditions(String aConditionsId);

	/**
	 * @param aConditionsId
	 *            the id of the logging conditions
	 * @return true if done
	 */
	boolean removeLoggingConditions(String aConditionsId);

}
