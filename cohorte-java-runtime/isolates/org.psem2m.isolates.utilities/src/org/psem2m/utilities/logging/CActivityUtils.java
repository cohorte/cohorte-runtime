/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz (isandlaTech) - initial API and implementation
 *******************************************************************************/
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
		} catch (Exception e) {
			System.out.println(CXException.eInString(e));
			return Level.OFF;
		}
	}
}
