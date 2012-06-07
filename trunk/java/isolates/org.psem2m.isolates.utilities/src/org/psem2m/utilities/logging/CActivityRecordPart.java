package org.psem2m.utilities.logging;

import org.psem2m.utilities.CXStringUtils;

public class CActivityRecordPart extends CActivityObject {
	private final static String LABEL_POSCOLSEP = "PosColumnSep";

	private final static String LABEL_POSNEXTCOL = "PosNextColumn";

	private final static String LABEL_VALUE = "Value";

	private final int pPosColumnSep;

	private final int pPosNextColumn;
	private final String pValue;

	/**
	 * @param aPosColumnSep
	 * @param aValue
	 * @param aNewLine
	 */
	CActivityRecordPart(int aPosColumnSep, String aValue, int aPosNextColumn) {
		super(CActivityRecordPart.class.getSimpleName());
		pPosColumnSep = aPosColumnSep;
		pValue = aValue;
		pPosNextColumn = aPosNextColumn;
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aBuffer) {
		super.addDescriptionInBuffer(aBuffer);
		CXStringUtils.appendKeyValInBuff(aBuffer, LABEL_POSCOLSEP,
				pPosColumnSep);
		CXStringUtils.appendKeyValInBuff(aBuffer, LABEL_VALUE, pValue);
		CXStringUtils.appendKeyValInBuff(aBuffer, LABEL_POSNEXTCOL,
				pPosNextColumn);
		return aBuffer;
	}

	@Override
	public void destroy() {
		// ...
	}

	/**
	 * @return
	 */
	int getPosColumnSep() {
		return pPosColumnSep;
	}

	int getPosNextColumn() {
		return pPosNextColumn;
	}

	/**
	 * @return
	 */
	String getValue() {
		return pValue;
	}

}
