package org.psem2m.utilities.logging;

import java.lang.reflect.Array;
import java.util.List;

/**
 * The Hunspell java bindings are licensed under LGPL, see the file COPYING.txt
 * in the root of the distribution for the exact terms.
 * 
 * @author Olivier Gattaz < olivier dot gattaz at isandlatech dot com >
 * @date 28/04/2011 (dd/mm/yy)
 */
public class CLogTools {

	final static String EMPTY = "";
	final static String LIB_EMPTY = "empty";
	final static String LIB_NULL = "null";

	private final static CLogTools sLogTools = new CLogTools();

	/**
	 * @param aValues
	 * @param aSeparator
	 * @return
	 */
	String arrayToString(Object[] aValues, String aSeparator) {
		return arrayToString(aValues, aSeparator, false);
	}

	/**
	 * @param aValues
	 * @param aSeparator
	 * @return
	 */
	String arrayToString(Object[] aValues, String aSeparator,
			boolean aJumpIfNull) {
		if (aValues == null)
			return String.valueOf(aValues);

		StringBuilder wSB = new StringBuilder(256);
		int wLenAfterSep = 0;
		int wMax = aValues.length;
		Object wValue;
		for (int wI = 0; wI < wMax; wI++) {
			if (wSB.length() > wLenAfterSep) {
				wSB.append(aSeparator);
				wLenAfterSep = wSB.length();
			}
			wValue = aValues[wI];
			if (wValue != null || !aJumpIfNull) {
				String wStr = String.valueOf(wValue);
				if (wStr.isEmpty())
					wStr = LIB_EMPTY;
				wSB.append(wStr);
			}
		}
		return wSB.toString();
	}

	/**
	 * @param aObjects
	 *            an array of objects
	 * @return the common class of the objects stored in the array
	 */
	Class<?> calcClassOfArrayElmts(Object[] aObjects) {
		return calcClassOfArrayElmts(aObjects, null);
	}

	/**
	 * @param aObjects
	 *            an array of objects
	 * @param aObjectToAdd
	 *            an object to add in the array
	 * @return the common class of the objects stored in the array and that of
	 *         the object to be added
	 */
	Class<?> calcClassOfArrayElmts(Object[] aObjects, Object aObjectToAdd) {
		if (aObjects == null)
			return Object.class;
		int wMax = aObjects.length;
		if (wMax < 1)
			return Object.class;
		if (aObjects[0] == null)
			return Object.class;

		// if the class of all the objets stored ine the array is the same, it's
		// the found class.
		Class<?> wFoundClass = aObjects[0].getClass();
		for (int wI = 1; wI < wMax; wI++) {
			if (aObjects[wI] == null || aObjects[wI].getClass() != wFoundClass) {
				wFoundClass = Object.class;
				// break
				wI = wMax;
			}
		}
		// if the object to add isn't null and if the found class does'nt equal
		// the
		// class of the object => Object class
		if (aObjectToAdd != null && wFoundClass != aObjectToAdd.getClass())
			wFoundClass = Object.class;

		return wFoundClass;
	}

	/**
	 * @return
	 */
	static CLogTools getInstance() {
		return sLogTools;
	}

	/**
	 * @param aObjects
	 *            an array of objects
	 * @param aObjectToInsert
	 *            an object to add in the array
	 * @return the new array of objects with the inserted object
	 */
	Object[] insertFirstOneObject(Object[] aObjects, Object aObjectToInsert) {
		return insertOneObject(aObjects, aObjectToInsert, 0);
	}

	/**
	 * @param aObjects
	 *            an array of objects
	 * @param aObjectToInsert
	 *            an object to add in the array
	 * @param aIdx
	 *            the index of the position of the inserted object
	 * @return the new array of objects with the inserted object
	 */
	Object[] insertOneObject(Object[] aObjects, Object aObjectToInsert, int aIdx) {
		if (aObjects == null)
			return aObjects;
		int wPreviousLen = aObjects.length;
		if (wPreviousLen < 1)
			return aObjects;

		validObjectsIndex(aObjects, aIdx);

		int wNewLen = wPreviousLen + 1;
		Object[] wNewArray = (Object[]) Array.newInstance(
				calcClassOfArrayElmts(aObjects, aObjectToInsert), wNewLen);

		// if we must add the object first
		if (aIdx == 0) {
			System.arraycopy(aObjects, 0, wNewArray, 1, wPreviousLen);
			wNewArray[0] = aObjectToInsert;
			// if we must remove the last object
		} else if (aIdx == wPreviousLen - 1) {
			System.arraycopy(aObjects, 0, wNewArray, 0, wPreviousLen);
			wNewArray[wNewLen - 1] = aObjectToInsert;
			//
		} else {
			// wLen = 10 and aIdx = 5 => wNewLen = 9
			// wSubLenA = aIdx = 5 (old index 0 to 4)
			// wSubLenb = wNewMax- aIdx = 4 (old index 6 to 9)
			System.arraycopy(aObjects, 0, wNewArray, 0, aIdx);
			System.arraycopy(aObjects, aIdx, wNewArray, aIdx + 1, wPreviousLen
					- aIdx);
			wNewArray[aIdx] = aObjectToInsert;

		}
		return wNewArray;
	}

	/**
	 * @param aValues
	 * @param aSeparator
	 * @return
	 */
	String listToString(List<?> aValues, String aSeparator) {
		return listToString(aValues, aSeparator, false);
	}

	/**
	 * @param aValues
	 * @param aSeparator
	 * @return
	 */
	String listToString(List<?> aValues, String aSeparator, boolean aJumpIfNull) {
		if (aValues == null)
			return String.valueOf(aValues);

		StringBuilder wSB = new StringBuilder(256);
		int wLenAfterSep = 0;
		for (Object wObj : aValues) {
			if (wSB.length() > wLenAfterSep) {
				wSB.append(aSeparator);
				wLenAfterSep = wSB.length();
			}
			if (wObj != null || !aJumpIfNull) {
				String wStr = String.valueOf(wObj);
				if (wStr.isEmpty())
					wStr = LIB_EMPTY;
				wSB.append(wStr);
			}
		}
		return wSB.toString();
	}

	/**
	 * @param aObjects
	 *            the original array of objects
	 * @param aIdx
	 *            the index of the object to remove
	 * @return the new array of objects
	 * @throws IndexOutOfBoundsException
	 */
	Object[] removeOneObject(Object[] aObjects, int aIdx)
			throws IndexOutOfBoundsException {
		if (aObjects == null)
			return aObjects;
		int wLen = aObjects.length;
		if (wLen < 1)
			return aObjects;

		validObjectsIndex(aObjects, aIdx);

		int wNewLen = wLen - 1;
		Object[] wNewArray = (Object[]) Array.newInstance(
				calcClassOfArrayElmts(aObjects), wNewLen);

		// if we must remove the first object
		if (aIdx == 0)
			System.arraycopy(aObjects, 1, wNewArray, 0, wNewLen);
		// if we must remove the last object
		else if (aIdx == wLen - 1)
			System.arraycopy(aObjects, 0, wNewArray, 0, wNewLen);
		//
		else {
			// wLen = 10 and aIdx = 5 => wNewLen = 9
			// wSubLenA = aIdx = 5 (old index 0 to 4)
			// wSubLenb = wNewMax- aIdx = 4 (old index 6 to 9)
			System.arraycopy(aObjects, 0, wNewArray, 0, aIdx);
			System.arraycopy(aObjects, aIdx + 1, wNewArray, aIdx, wNewLen
					- aIdx);
		}

		return wNewArray;
	}

	/**
	 * @param aValue
	 * @param aLen
	 * @param aLeadingChar
	 * @return
	 */
	String strAdjustLeft(String aValue, int aLen, char aLeadingChar) {
		int wLen = aValue.length();
		if (wLen < aLen)
			return aValue + strFromChar(aLeadingChar, aLen - wLen);
		else if (wLen > aLen)
			return aValue.substring(0, aLen);
		else
			return aValue;
	}

	/**
	 * @param aValue
	 * @param aLen
	 * @return
	 */
	String strAdjustRight(long aValue, int aLen) {
		return strAdjustRight(String.valueOf(aValue), aLen, '0');
	}

	/**
	 * @param aValue
	 * @param aLen
	 * @param aLeadingChar
	 * @return
	 */
	String strAdjustRight(String aValue, int aLen, char aLeadingChar) {
		int wLen = aValue.length();
		if (wLen < aLen)
			return strFromChar(aLeadingChar, aLen - wLen) + aValue;
		else if (wLen > aLen)
			return aValue.substring(aValue.length() - aLen);
		else
			return aValue;
	}

	/**
	 * @param aChar
	 * @param aLen
	 * @return
	 */
	String strFromChar(char aChar, int aLen) {
		if (aLen < 1)
			return "";
		if (aLen == 1)
			return String.valueOf(aChar);
		char[] wBuffer = new char[aLen];
		for (int wI = 0; wI < aLen; wI++) {
			wBuffer[wI] = aChar;
		}
		return String.valueOf(wBuffer);
	}

	/**
	 * @param aObjects
	 * @param aIdx
	 * @throws IndexOutOfBoundsException
	 */
	void validObjectsIndex(Object[] aObjects, int aIdx)
			throws IndexOutOfBoundsException {
		if (aObjects == null)
			throw new IndexOutOfBoundsException("the target array is null");
		int wLen = aObjects.length;
		if (aIdx < 0 || aIdx > wLen - 1)
			throw new IndexOutOfBoundsException(String.format(
					"index [%d] is less than zero", aIdx));
		if (aIdx > wLen - 1)
			throw new IndexOutOfBoundsException(String.format(
					"index [%d] is greater than len-1 (len=[%d])", aIdx, wLen));
	}

	/**
	 * 
	 */
	private CLogTools() {
		super();
	}
}
