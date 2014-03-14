package org.psem2m.utilities.logging;

import java.util.ArrayList;
import java.util.List;

import org.psem2m.utilities.CXStringUtils;

/**
 * @author ogattaz
 * 
 */
public enum EActivityLogColumn {
	C1_MILLI, C2_NANO, C3_DATE, C4_TIME, C5_LEVEL, C6_THREAD, C7_INSTANCE, C8_METHOD, C9_TEXT;

	/**
	 * @param aCol
	 * @param aLinDef
	 * @return
	 */
	public static boolean isColumnOn(EActivityLogColumn aCol,
			EActivityLogColumn[] aLinDef) {
		if (aCol == null) {
			return false;
		}
		for (EActivityLogColumn wCol : aLinDef) {
			if (aCol == wCol) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param aLinDef
	 *            a line definition
	 * @return
	 */
	public static String lineDefToString(EActivityLogColumn[] aLinDef) {
		if (aLinDef == null) {
			return "linedef null";
		}
		if (aLinDef.length == 0) {
			return "linedef empty";
		}
		List<String> wStringList = new ArrayList<String>();
		for (EActivityLogColumn wCol : aLinDef) {
			wStringList.add(wCol.name());
		}
		return CXStringUtils.stringListToString(wStringList);
	}
}
