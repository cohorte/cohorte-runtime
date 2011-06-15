package org.psem2m.utilities;

import java.util.logging.Level;

public class CFakeLogFormatter {
    public static String format(String id, Object aWho, Level aLevel,
    		CharSequence aWhat, CharSequence aLine) {

	return "[" + id + "][" + System.currentTimeMillis() + "][" + aWho
		+ "][" + aLevel + "] " + aWhat + " :: " + aLine + "\n";
    }
}
