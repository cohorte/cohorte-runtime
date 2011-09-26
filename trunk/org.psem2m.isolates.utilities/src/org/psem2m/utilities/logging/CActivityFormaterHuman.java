package org.psem2m.utilities.logging;

import java.util.logging.Level;

/**
 * 
 * <pre> TimeStamp TimeStamp TimeStamp TimeStamp Level Thread name Instance id
 * Method LogLine (millis) (nano) (date) (hhmmss.sss)
 * 1309180295049;00000065317000;2011/06/27;15:11:35:049;INFO ;
 * FelixStartLevel;CIsolateLogger_2236 ;__validatePojo ;EnvContext: </pre>
 * 
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CActivityFormaterHuman extends CActivityFormaterBasic {

    /** **/
    private final static CActivityFormaterHuman sActivityFormaterHuman = new CActivityFormaterHuman();

    /**
     * @return
     */
    public static IActivityFormater getInstance() {

        return sActivityFormaterHuman;
    }

    /**
     * Explicit default constructor
     */
    public CActivityFormaterHuman() {

        super();
        acceptMultiline(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.logging.CActivityFormater#format(long,
     * java.util.logging.Level, java.lang.String, java.lang.String,
     * java.lang.String, boolean)
     */
    @Override
    public synchronized String format(final long aMillis, final Level aLevel,
            final String aSourceClassName, final String aSourceMethodName,
            final String aText, final boolean aWhithEndLine) {

        // clean the buffer
        pSB.delete(0, pSB.length());

        pSB.append(formatDate(aMillis));

        addColummnDelimitorInLogLine(pSB);
        pSB.append(formatTime(aMillis));

        addColummnDelimitorInLogLine(pSB);
        pSB.append(formatLevel(aLevel));

        addColummnDelimitorInLogLine(pSB);
        addThreadNameInLogLine(pSB, Thread.currentThread().getName());

        addColummnDelimitorInLogLine(pSB);
        pSB.append(formatWho(aSourceClassName));

        addColummnDelimitorInLogLine(pSB);
        pSB.append(formatWhat(aSourceMethodName));

        addColummnDelimitorInLogLine(pSB);
        pSB.append(formatText(aText));
        if (aWhithEndLine) {
            pSB.append(SEP_LINE);
        }
        return pSB.toString();
    }

}
