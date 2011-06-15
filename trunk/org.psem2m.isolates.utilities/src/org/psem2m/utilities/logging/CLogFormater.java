package org.psem2m.utilities.logging;

/**
 * @author Olivier Gattaz < olivier dot gattaz at isandlatech dot com >
 * 
 */
public class CLogFormater {

	private static String DUMMY_SHORT_HASHCODE = "0000";

	private final CLogTools pTools = CLogTools.getInstance();

	private final CLogToolsException pToolsException = CLogToolsException
			.getInstance();

	public CLogFormater() {
		super();
	}

	/**
	 * @param aSB
	 *            a stringbuffer to be appended
	 * @param aObjects
	 *            a table of object
	 * @return the given StringBuffer
	 */
	public StringBuilder addTextsInLogLine(StringBuilder aSB,
			Object... aObjects) {

		if (aObjects == null || aObjects.length == 0)
			return aSB;

		Object wObj;
		for (int wI = 0; wI < aObjects.length; wI++) {
			wObj = aObjects[wI];
			if (wObj == null)
				aObjects[wI] = CLogTools.LIB_NULL;
			else if (wObj instanceof Throwable)
				aObjects[wI] = pToolsException.eInString((Throwable) wObj, '|');

		}

		if (aObjects.length == 1) {
			if (aObjects[0] == null)
				return aSB;

			if (aObjects[0] instanceof Throwable)
				return aSB.append(pToolsException
						.eStackToString((Throwable) aObjects[0]));

			return aSB.append(String.valueOf(aObjects[0]));
		}

		// if the first object is a format
		if (aObjects[0].toString().indexOf('%') > -1) {
			return aSB.append(String.format(aObjects[0].toString(),
					pTools.removeOneObject(aObjects, 0)));
		}

		boolean wIsId = false;
		boolean wIsValue = false;
		String wStr;
		int wMax = aObjects.length;
		for (int wI = 0; wI < wMax; wI++) {
			wIsValue = wIsId;
			wStr = aObjects[wI].toString();
			if (wStr != null) {
				wIsId = wStr.endsWith("=");

				if (wIsValue)
					aSB.append('[');

				aSB.append(wStr);

				if (wIsValue)
					aSB.append(']');
				if (!wIsId)
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
	public String formatLogLine(Object... aObjects) {
		StringBuilder wSB = new StringBuilder(128);
		addTextsInLogLine(wSB, aObjects);
		return wSB.toString();
	}

	/**
	 * @param aWho
	 * @return
	 */
	String getWhoObjectId(Object aWho) {
		if (aWho == null)
			return CLogTools.LIB_NULL;

		if (aWho instanceof Class)
			return ((Class<?>) aWho).getSimpleName() + '_'
					+ DUMMY_SHORT_HASHCODE;

		return new StringBuffer().append(aWho.getClass().getSimpleName())
				.append('_').append(pTools.strAdjustRight(aWho.hashCode(), 4))
				.toString();
	}

}
