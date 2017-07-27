package org.psem2m.isolates.loggers;

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
public interface ILoggingCondition extends IXDescriber {

	/**
	 * @return
	 */
	String getComment();

	/**
	 * @return
	 */
	String getId();

	/**
	 * @return
	 */
	Level getLevel();

	/**
	 * @param aLevel
	 * @return true if the level matches
	 */
	public boolean isOn(final Level aLevel);

	/**
	 * @param aConditionLevel
	 * @return the previous Level
	 */
	Level setLevel(final Level aConditionLevel);

	/**
	 * @return
	 */
	JSONObject toJson() throws JSONException;
}
