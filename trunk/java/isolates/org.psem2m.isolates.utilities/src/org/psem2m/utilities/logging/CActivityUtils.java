package org.psem2m.utilities.logging;

import java.util.logging.Level;

import org.psem2m.utilities.CXException;

/**
 * @author ogattaz
 * 
 */
public class CActivityUtils {

    // to be shure that the CActivityLevel class is loaded
    static {
        CActivityLevel.getSortedLevels();
    }

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
                if (Character.isLetter(wChar)) {
                    wSB.append(wChar);
                }
                wI++;
            }

            aLevelName = wSB.toString().toUpperCase();
        }
        return aLevelName;
    }

    /**
     * @param aLevel
     * @return
     */
    static Level levelToLevel(String aLevelName) {

        aLevelName = cleanLevelName(aLevelName);
        if (aLevelName == null) {
            return Level.OFF;
        }

        try {
            return CActivityLevel.parse(aLevelName);
        } catch (Throwable e) {
            System.out.println(CXException.eInString(e));
            return Level.OFF;
        }
    }
}
