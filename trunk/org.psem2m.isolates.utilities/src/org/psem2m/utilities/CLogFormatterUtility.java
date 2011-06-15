package org.psem2m.utilities;

import java.util.logging.Level;

/**
 * Trace/Log strings utility class
 * 
 * @author Thomas Calmant
 */
public class CLogFormatterUtility {

	/**
	 * Formats a log line from the given parameters
	 * 
	 * @param aChannelId
	 *            Output channel ID
	 * @param aWho
	 *            Message sender description
	 * @param aLevel
	 *            Log message level
	 * @param aWhat
	 *            Current operation
	 * @param aLine
	 *            Message
	 * @return A well formated line ready to logged
	 */
	public static String format(final CharSequence aChannelId,
			final CharSequence aWho, final Level aLevel,
			final CharSequence aWhat, final CharSequence... aLine) {

		String result = "[" + aChannelId + "][" + System.currentTimeMillis()
				+ "][" + aWho + "][" + aLevel + "] " + aWhat + " :: ";

		for (CharSequence element : aLine) {
			result += element;
		}

		return result + "\n";
	}

	/**
	 * Returns a String basic information about the given object
	 * 
	 * @param aObject
	 *            Object to be analized
	 * @return The simple class name of the object with its hash code, or the
	 *         String &lt null &gt
	 */
	public static String getRepresentation(final Object aObject) {
		if (aObject == null) {
			return "<null>";
		}

		return aObject.getClass().getSimpleName() + "@" + aObject.hashCode();
	}
}
