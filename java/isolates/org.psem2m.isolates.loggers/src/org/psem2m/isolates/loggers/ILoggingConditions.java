package org.psem2m.isolates.loggers;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import org.psem2m.utilities.IXDescriber;
import org.psem2m.utilities.json.JSONException;
import org.psem2m.utilities.json.JSONObject;

/**
 * CASE 0000222: Logging conditions to control the content of the trace log
 *
 * @author ogattaz
 *
 */
public interface ILoggingConditions extends IXDescriber {

	/**
	 * @return the number of deleted file
	 * @throws CLoggingConditionsException
	 */
	int cleanLoggingConditionsFile() throws CLoggingConditionsException;

	/**
	 * @param aConditionId
	 *            The ID of the condition to retreive.
	 * @return The found logging condition.
	 */
	ILoggingCondition getCondition(final Class<?> aConditionId);

	/**
	 * @param aConditionId
	 *            The ID of the condition to retreive.
	 * @return The found logging condition.
	 */
	ILoggingCondition getCondition(final String aConditionId);

	/**
	 * @return
	 */
	List<ILoggingCondition> getConditions();

	/**
	 * @return the file of the conditions (set of condition)
	 */
	File getFile();

	/**
	 * @return the id of the conditions (set of condition)
	 */
	String getId();

	/**
	 * @param aConditionId
	 *            The ID of the condition to test.
	 * @param aLevel
	 *            The level to test
	 * @return True if the given Level matches the current Level of the
	 */
	public boolean isOn(final Class<?> aConditionId, final Level aLevel);

	/**
	 * @param aConditionId
	 *            The ID of the condition to test.
	 * @param aLevel
	 *            The level to test.
	 * @return True if the given Level matches the current Level of the
	 *         condition.
	 */
	public boolean isOn(final String aConditionId, final Level aLevel);

	/**
	 * @param aConditionId
	 *            The ID of the condition to create.
	 * @param aInitialLevel
	 *            the level set only when the condition is created
	 * @param aComments
	 *            One or more comment lines.
	 * @return The created logging condition.
	 */
	ILoggingCondition newLoggingCondition(final Class<?> aConditionId,
			final Level aInitialLevel, final String... aComments);

	/**
	 * @param aConditionId
	 *            The ID of the condition to create.
	 * @param aInitialLevel
	 *            the level set only when the condition is created
	 * @param aComments
	 *            One or more comment lines.
	 * @return The created logging condition.
	 */
	ILoggingCondition newLoggingCondition(final String aConditionId,
			final Level aInitialLevel, final String... aComments);

	/**
	 * @param aConditionLevel
	 *            The level to set.
	 * @return the number of logging condition set.
	 */
	int setAllConditionsLevel(final Level aConditionLevel);

	/**
	 * @return the number of logging condition set to Level.ALL
	 */
	int setAllConditionsToAll();

	/**
	 * @return the number of logging condition set to Level.OFF
	 */
	int setAllConditionsToOff();

	/**
	 * @param aConditionId
	 *            The ID of the condition to set.
	 * @param aConditionLevel
	 *            The level to set.
	 * @return The previous Level.
	 */
	Level setConditionLevel(final Class<?> aConditionId,
			final Level aConditionLevel);

	/**
	 * @param aConditionId
	 *            The ID of the condition to set.
	 * @param aConditionLevel
	 *            The level to set.
	 * @return The previous Level.
	 */
	Level setConditionLevel(final String aConditionId,
			final Level aConditionLevel);

	/**
	 * @return
	 */
	JSONObject toJson() throws JSONException;

}
