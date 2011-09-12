package org.psem2m.utilities.logging;

/**
 * 
 * Builds the text of a log line with an array of objects.
 * <ul>
 * <li>converts null of Thowable to strings
 * <li>if the first object is a format, the text is the result of the
 * String.format() method
 * <li>builds the text by appending the string value of each object.
 * <ul>
 * <li>if a string is endding by a character equal, it is and the next string
 * value is added with square brackets (eg: "id=", "1258" => "id=[1258)]" )
 * </ul>
 * </ul>
 * 
 * 
 * 
 * @author Olivier Gattaz < olivier dot gattaz at isandlatech dot com >
 * 
 */
public class CLogLineTextBuilder {

    private static String DUMMY_SHORT_HASHCODE = "0000";

    private static CLogLineTextBuilder sLogLineTextBuilder = new CLogLineTextBuilder();

    /**
     * @return
     */
    public static CLogLineTextBuilder getInstance() {
	return sLogLineTextBuilder;
    }

    private final CLogTools pTools = CLogTools.getInstance();

    private final CLogToolsException pToolsException = CLogToolsException
	    .getInstance();

    /**
     * Explicit default constructor
     */
    private CLogLineTextBuilder() {
	super();
    }

    /**
     * @param aSB
     *            a stringbuffer to be appended
     * @param aObjects
     *            a table of object
     * @return the given StringBuffer
     */
    public StringBuilder addTextsInLogLine(final StringBuilder aSB,
	    final Object... aObjects) {

	if (aObjects == null || aObjects.length == 0) {
	    return aSB;
	}

	// converts null of Thowable to strings
	Object wObj;
	for (int wI = 0; wI < aObjects.length; wI++) {
	    wObj = aObjects[wI];
	    if (wObj == null) {
		aObjects[wI] = CLogTools.LIB_NULL;
	    } else if (wObj instanceof Throwable) {
		aObjects[wI] = pToolsException.eInString((Throwable) wObj, '|');
	    }
	}

	// if there is only one info
	if (aObjects.length == 1) {
	    return aSB.append(String.valueOf(aObjects[0]));
	}

	// if the first object is a format, return the result of the
	// String.format() method
	if (aObjects[0].toString().indexOf('%') > -1) {
	    return aSB.append(String.format(aObjects[0].toString(),
		    pTools.removeOneObject(aObjects, 0)));
	}

	// builds the text by appending the string value of each object.
	boolean wIsId = false;
	boolean wIsValue = false;
	String wStr;
	int wMax = aObjects.length;
	for (int wI = 0; wI < wMax; wI++) {
	    wIsValue = wIsId;
	    wStr = String.valueOf(aObjects[wI]);
	    wIsId = wStr.endsWith("=");

	    if (wIsValue) {
		aSB.append('[');
	    }

	    aSB.append(wStr);

	    if (wIsValue) {
		aSB.append(']');
	    }
	    if (!wIsId) {
		aSB.append(' ');
	    }
	}
	return aSB;
    }

    /**
     * @param aWho
     * @param aLevel
     * @param aWhat
     * @param aObjects
     * @return
     */
    public String formatLogLine(final Object... aObjects) {
	StringBuilder wSB = new StringBuilder(128);
	addTextsInLogLine(wSB, aObjects);
	return wSB.toString();
    }

    /**
     * @param aWho
     * @return
     */
    public String formatWhoObjectId(final Object aWho) {
	if (aWho == null) {
	    return CLogTools.LIB_NULL;
	}

	if (aWho instanceof Class) {
	    return ((Class<?>) aWho).getName() + '_' + DUMMY_SHORT_HASHCODE;
	}

	return new StringBuffer().append(aWho.getClass().getName()).append('_')
		.append(pTools.strAdjustRight(aWho.hashCode(), 4)).toString();
    }

}
