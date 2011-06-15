package org.psem2m.utilities.logging;

import java.util.logging.Level;

public class CActivityUtils {

	private static final Level[] LEVELS = { Level.OFF, Level.SEVERE,
			Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER,
			Level.FINEST, Level.ALL };

	/**
	 * @param aLevelName
	 * @return
	 */
	private static String cleanLevelName(String aLevelName) {
		if (aLevelName != null && aLevelName.length() > 0) {
			int wMax = aLevelName.length();
			StringBuilder wSB = new StringBuilder(wMax);
			char wChar;
			int wI = 0;
			while (wI < wMax) {
				wChar = aLevelName.charAt(wI);
				if (Character.isLetter(wChar))
					wSB.append(wChar);
				wI++;
			}

			aLevelName = wSB.toString();
		}
		return aLevelName;
	}

	/**
	 * @param aLevel
	 * @return
	 */
	static Level levelToLevel(String aLevelName) {
		aLevelName = cleanLevelName(aLevelName);
		if (aLevelName != null) {
			int wMax = LEVELS.length;
			int wI = 0;
			while (wI < wMax) {
				if (LEVELS[wI].getName().equalsIgnoreCase(aLevelName))
					return LEVELS[wI];
				wI++;
			}
		}
		return Level.OFF;
	}
}
