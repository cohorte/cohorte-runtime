package org.psem2m.utilities.logging;

import java.util.logging.Level;

import org.psem2m.utilities.CXDateTime;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.IConstants;

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
public class CActivityFormaterBasic extends CActivityFormater {

    /** the width of the level column **/
    protected final static int LENGTH_LEVEL = 7;

    /** the width of the what column **/
    protected final static int LENGTH_WHAT = 25;

    /** the width of the who column **/
    protected final static int LENGTH_WHO = 27;

    /** **/
    private final static CActivityFormaterBasic sActivityFormaterBasic = new CActivityFormaterBasic();

    /** the column separator **/
    protected final static char SEP_COLUMN = ';';
    protected final static String SEP_COLUMN_DELIM = SEP_COLUMN + " ";

    /** the milliseconds separator **/
    protected final static char SEP_MILLI = '.';

    /**
     * @return
     */
    public static IActivityFormater getInstance() {

        return sActivityFormaterBasic;
    }

    /**
     * Explicit default constructor
     */
    public CActivityFormaterBasic() {

        super();
    }

    /**
     * Add a column delimitor in the log line
     * 
     * @param aSB
     * @return
     */
    StringBuilder addColummnDelimitorInLogLine(final StringBuilder aSB) {

        return super.addColummnDelimitorInLogLine(aSB, SEP_COLUMN_DELIM);
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
    	//pSB.setLength(0);
        //pSB.delete(0, pSB.length());
        StringBuilder pSB = new StringBuilder(128);

        pSB.append(aMillis);

        addColummnDelimitorInLogLine(pSB);
        pSB.append(getFormatedNanoSecs());

        addColummnDelimitorInLogLine(pSB);
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

    /**
     * @param aLevel
     * @return
     */
    protected String formatDate(final long aMillis) {

        return CXDateTime.time2StrAAAAMMJJ(aMillis, SEP_DATE);

    }

    /**
     * @param aLevel
     * @return
     */
    protected String formatLevel(final Level aLevel) {

        return CXStringUtils.strAdjustLeft(aLevel != null ? aLevel.getName()
                : IConstants.LIB_NULL, LENGTH_LEVEL, ' ');

    }

    protected String formatText(final String aText) {

        if (aText == null) {
            return IConstants.LIB_NULL;
        } else {
            return isMultiline() ? aText : aText.replace(SEP_LINE, '§');
        }
    }

    /**
     * @param aLevel
     * @return
     */
    protected String formatTime(final long aMillis) {

        return CXDateTime.time2StrHHMMSSmmm(aMillis, SEP_TIME);

    }

    /**
     * @param aLevel
     * @return
     */
    protected String formatWhat(final String aMethod) {

        return CXStringUtils.strAdjustRight(
                aMethod != null ? aMethod.replace(SEP_COLUMN, REPLACE_COLUMN)
                        : IConstants.EMPTY, LENGTH_WHAT, ' ');

    }

    /**
     * @param aLevel
     * @return
     */
    protected String formatWho(final String aWho) {

        return CXStringUtils.strAdjustRight(
                aWho != null ? aWho.replace(SEP_COLUMN, REPLACE_COLUMN)
                        : IConstants.EMPTY, LENGTH_WHO, ' ');

    }

}
