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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

import org.psem2m.utilities.CXOSUtils;
import org.psem2m.utilities.IXDescriber;

/**
 * @author ogattaz
 * 
 */
/**
 * @author ogattaz
 *
 */
/**
 * @author ogattaz
 * 
 */
public class CLogLineBuffer implements Serializable, java.lang.Appendable, java.lang.CharSequence {

	public final static char DESCR_VALUE_END = ']';

	public final static String DESCR_VALUE_START = "=[";

	/** taille maximun d'un nom de classe **/
	private final static int LENGTH_MAX_CLASSNAME = 20;

	/** taille maximun d'un nom de methode **/
	private final static int LENGTH_MAX_METHODNAME = 20;

	private final static String LIB_CAUSE = "cause...\n";

	private final static String LIB_CLASS = "class";

	private final static String LIB_MESS = "mess";

	private final static String LIB_STACK = "stack";

	private final static String LIB_WHY = "why";

	private final static String METHOD_GETLISTOFMESS = "getListOfMessages";

	private final static String METHOD_WHY = "getWhyString";

	private final static String PART_SEPARATOR = " | ";

	private final static String PREFIX = " @@@@ ";

	private static final long serialVersionUID = -2517225541954853319L;

	/** attention : LENGTH_MAX_METHODNAME spaces **/
	private final static String SPACES_METHODNAME = "                    ";

	final static char[] TENZEROCHARS = { '0', '0', '0', '0', '0', '0', '0', '0', '0', '0' };

	final static char VALUES_SEP = ';';

	private final StringBuilder pBuffer;

	private final CLogToolsException pToolsException = CLogToolsException.getInstance();

	/**
   *
   */
	public CLogLineBuffer() {
		this(256);
	}

	/**
	 * @param length
	 */
	public CLogLineBuffer(int length) {
		this(new StringBuilder(length));
	}

	/**
	 * @param aStr
	 */
	public CLogLineBuffer(String aStr) {
		this(new StringBuilder(aStr));
	}

	/**
	 * @param aBuffer
	 */
	private CLogLineBuffer(StringBuilder aBuffer) {
		super();
		pBuffer = aBuffer;

	}

	/**
	 * @param aValue
	 * @param aLen
	 * @return
	 */
	private CLogLineBuffer addAlignStringToLeft(String aValue, int aLen) {
		int wLen = aValue.length();

		if (wLen < aLen) {
			append(aValue);
			append(' ', aLen - wLen);
		} else if (wLen > aLen) {
			append(aValue.substring(0, aLen));
		} else {
			append(aValue);
		}

		return this;
	}

	/**
	 * @param aThrowable
	 */
	public void addEClassAndMessInSB(Throwable aThrowable) {
		String wMess = aThrowable.getMessage();
		boolean wHasMess = (wMess != null && wMess.length() > 0);
		if (wHasMess) {
			append(wMess).append(PART_SEPARATOR);
		}
		append(aThrowable.getClass().getSimpleName());
		append(PART_SEPARATOR);
		append(pToolsException.firstLineOfStackInString(aThrowable));
	}

	/**
	 * @param aValue
	 * @param aLen
	 * @return
	 */
	private CLogLineBuffer addNumStringInText(int aValue, int aLen) {
		String wResult = String.valueOf(aValue);
		int wLen = wResult.length();
		if (wLen == aLen) {
			return append(wResult);
		} else if (wLen < aLen) {
			return append(TENZEROCHARS, 0, aLen - wLen).append(wResult);
		} else {
			// if (wResult.length() > aLen)
			return append(wResult.substring(wLen - aLen));
		}
	}

	/**
	 * @param b
	 * @return
	 */
	public CLogLineBuffer append(boolean b) {
		pBuffer.append(b);
		return this;
	}

	/**
	 * @param str
	 * @return
	 */
	public CLogLineBuffer append(char str[]) {
		pBuffer.append(str);
		return this;
	}

	@Override
	public CLogLineBuffer append(char c) {
		pBuffer.append(c);
		return this;
	}

	/**
	 * @param aChar
	 * @param aLen
	 * @return
	 */
	public CLogLineBuffer append(char aChar, int aLen) {
		for (int wI = 0; wI < aLen; wI++) {
			append(aChar);
		}
		return this;
	}

	/**
	 * @param str
	 * @param offset
	 * @param len
	 * @return
	 */
	public CLogLineBuffer append(char str[], int offset, int len) {
		pBuffer.append(str, offset, len);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Appendable#append(java.lang.CharSequence)
	 */
	@Override
	public CLogLineBuffer append(CharSequence csq) {
		pBuffer.append(csq);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Appendable#append(java.lang.CharSequence, int, int)
	 */
	@Override
	public CLogLineBuffer append(CharSequence csq, int start, int end) {
		pBuffer.append(csq, start, end);
		return this;
	}

	/**
	 * @param obj
	 * @return
	 */
	public CLogLineBuffer append(Object obj) {
		pBuffer.append(obj);
		return this;
	}

	/**
	 * @param aValue
	 * @param aMaxLen
	 * @return
	 */
	public CLogLineBuffer append(Object aValue, int aMaxLen) {

		String wValue = (aValue != null) ? aValue.toString() : "null";
		int wLength = wValue.length();
		if (wLength > aMaxLen) {
			wValue = wValue.substring(0, aMaxLen);
		}
		pBuffer.append(wValue);
		if (wLength < aMaxLen) {
			int wMax = aMaxLen - wLength;
			int wI = 0;
			while (wI < wMax) {
				pBuffer.append(' '); // space
				wI++;
			}

		}
		return this;
	}

	/**
	 * @param aObject
	 * @return
	 */
	public CLogLineBuffer appendClassName(Object aObject) {
		if (aObject != null) {
			String wName;
			if (aObject instanceof Class<?>) {
				wName = ((Class<?>) aObject).getName();
			} else {
				wName = aObject.getClass().getName();
			}

			int wPos = wName.lastIndexOf('.');
			return append(((wPos > -1 && wPos < wName.length() - 1) ? wName.substring(wPos + 1)
					: wName), LENGTH_MAX_CLASSNAME);
		} else {
			return append('(').append("null").append(')');
		}
	}

	/**
	 * @since 1.5
	 */
	public CLogLineBuffer appendCodePoint(int codePoint) {
		pBuffer.appendCodePoint(codePoint);
		return this;
	}

	/**
	 * @return
	 */
	public CLogLineBuffer appendCR() {
		return append('\n');
	}

	/**
	 * ajoute le "temps" sous la forme "mm:ss:mmm" dans le
	 * 
	 * @return
	 */
	public CLogLineBuffer appendDateMMSSmmm() {
		Calendar wRightNow = Calendar.getInstance();
		addNumStringInText(wRightNow.get(Calendar.MINUTE), 2);
		this.append(':');
		addNumStringInText(wRightNow.get(Calendar.SECOND), 2);
		this.append('~');
		addNumStringInText(wRightNow.get(Calendar.MILLISECOND), 3);

		return this;
	}

	/**
	 * Ajoute un couple " id=[value]" dans le CTraceBuffer en cadrant l'id sur
	 * "aIdSize" caracteres et la valeur sur "aValueSize" caracteres
	 * 
	 * @param aSB
	 * @param aIdSize
	 * @param aId
	 * @param aValueSize
	 * @param aValue
	 */
	public CLogLineBuffer appendDescr(Integer aIdSize, String aId, Integer aValueSize, String aValue) {
		append(' ');
		addAlignStringToLeft(aId, aIdSize.intValue());
		append(DESCR_VALUE_START);
		addAlignStringToLeft(aValue, aValueSize.intValue());
		append(DESCR_VALUE_END);
		return this;
	}

	/**
	 * Ajoute un couple " id=[value]" dans le CTraceBuffer en cadrant l'id sur
	 * "aIdSize" caracteres.
	 * 
	 * @param aSB
	 * @param aIdSize
	 * @param aId
	 * @param aValueSize
	 * @param aValue
	 */
	public CLogLineBuffer appendDescr(Integer aIdSize, String aId, String aValue) {
		append(' ');
		addAlignStringToLeft(aId, aIdSize.intValue());
		append(DESCR_VALUE_START);
		append(aValue);
		append(DESCR_VALUE_END);
		return this;
	}

	/**
	 * Ajoute un couple " id=[value|valueB]" dans le CTraceBuffer en cadrant
	 * l'id sur "aIdSize" caracteres.
	 * 
	 * @param aSB
	 * @param aIdSize
	 * @param aId
	 * @param aValue
	 * @param aValueB
	 */
	public CLogLineBuffer appendDescr(Integer aIdSize, String aId, String aValue, String aValueB) {
		append(' ');
		addAlignStringToLeft(aId, aIdSize.intValue());
		append(DESCR_VALUE_START);
		append(aValue);
		append(VALUES_SEP).append(aValueB);
		append(DESCR_VALUE_END);
		return this;
	}

	/**
	 * @param aDescriber
	 * @return
	 */
	public CLogLineBuffer appendDescr(IXDescriber aDescriber) {
		try {
			aDescriber.addDescriptionInBuffer(this);
			return this;
		} catch (Exception e) {
			return appendDescr("ERROR DURING THE BUILT OF THE DESCRIPTION", e);
		}
	}

	/**
	 * Appends the "description " of the <code>boolean</code> value argument to
	 * this trace buffer:<br>
	 * <code>aId=[theBoolValue]</code>
	 * <p>
	 * The argument is converted to a string as if by the method
	 * <code>String.valueOf()</code>, and the characters of that string are then
	 * appended to this trace-buffer.
	 * <p>
	 * 
	 * @param aId
	 *            a <code>String</code> identifier.
	 * @param aBoolValue
	 *            a <code>boolean</code> value.
	 * @return a reference to this <code>CTraceBuffer</code> object.
	 * @see java.lang.CLogLineBuffer#append(java.lang.String)
	 */
	public CLogLineBuffer appendDescr(String aId, boolean aBoolValue) {
		return appendDescr(aId, String.valueOf(aBoolValue));
	}

	/**
	 * @param aId
	 * @param aBoolValue
	 * @param aIntValue
	 * @return
	 */
	public CLogLineBuffer appendDescr(String aId, boolean aBoolValue, int aIntValue) {
		return appendDescr(aId, String.valueOf(aBoolValue), String.valueOf(aIntValue));
	}

	/**
	 * @param aId
	 * @param aBoolValue
	 * @param aLongValue
	 * @return
	 */
	public CLogLineBuffer appendDescr(String aId, boolean aBoolValue, long aLongValue) {
		return appendDescr(aId, String.valueOf(aBoolValue), String.valueOf(aLongValue));
	}

	public CLogLineBuffer appendDescr(String aId, boolean aBoolValue, String aValue) {
		return appendDescr(aId, String.valueOf(aBoolValue), aValue);
	}

	/**
	 * Appends the "description" of the <code>int</code> argument to this trace
	 * buffer:<br>
	 * <code>aId=[theIntValue]</code>
	 * <p>
	 * The argument is converted to a string as if by the method
	 * <code>String.valueOf()</code>, and the characters of that string are then
	 * appended to this trace-buffer.
	 * 
	 * @param aId
	 *            a <code>String</code> identifier.
	 * @param aIntValue
	 *            the <code>int</code> value.
	 * @return a reference to this <code>CTraceBuffer</code> object.
	 * @see java.lang.CLogLineBuffer#append(java.lang.String)
	 */
	public CLogLineBuffer appendDescr(String aId, char aCharValue) {
		return appendDescr(aId, String.valueOf(aCharValue));
	}

	/**
	 * Appends the "description" of the <code>Class</code> argument to this
	 * trace buffer:<br>
	 * <code>aId=[theClassName]</code>
	 * <p>
	 * The argument is converted to a string as if by the method
	 * <code>getName()</code>, and the characters of that string are then
	 * appended to this trace-buffer like :<br>
	 * <code>aId=[theClasseName]</code>
	 * 
	 * @param aId
	 *            the <code>String</code> identifier.
	 * @param aClass
	 *            the <code>Class</code> which name is the value.
	 * @return a reference to this <code>CTraceBuffer</code> object.
	 * @see java.lang.CLogLineBuffer#append(java.lang.String)
	 */
	public CLogLineBuffer appendDescr(String aId, Class<?> aClass) {
		if (aClass == null) {
			return appendDescr(aId, (String) null);
		} else {
			String wName = aClass.getName();
			int wPos = wName.lastIndexOf('.');
			return appendDescr(aId,
					((wPos > -1 && wPos < wName.length() - 1) ? wName.substring(wPos + 1) : wName));
		}
	}

	/**
	 * @param aId
	 * @param aIntValue
	 * @param aValue
	 * @return
	 */
	public CLogLineBuffer appendDescr(String aId, double aDoubleValue) {
		return appendDescr(aId, String.valueOf(aDoubleValue));
	}

	/**
	 * @param aId
	 * @param aIntValue
	 * @return
	 */
	public CLogLineBuffer appendDescr(String aId, int aIntValue) {
		this.append(' ').append(aId).append(DESCR_VALUE_START).append(aIntValue)
				.append(DESCR_VALUE_END);
		return this;
	}

	/**
	 * @param aId
	 * @param aIntValue
	 * @param aIntValueB
	 * @return
	 */
	public CLogLineBuffer appendDescr(String aId, int aIntValue, int aIntValueB) {
		this.append(' ').append(aId).append(DESCR_VALUE_START);
		append(aIntValue);
		append(VALUES_SEP).append(aIntValueB);
		append(DESCR_VALUE_END);
		return this;
	}

	/**
	 * @param aId
	 * @param aIntValue
	 * @param aIntValueB
	 * @param aIntValueC
	 * @return
	 */
	public CLogLineBuffer appendDescr(String aId, int aIntValue, int aIntValueB, int aIntValueC) {
		append(' ').append(aId).append(DESCR_VALUE_START);
		append(aIntValue);
		append(VALUES_SEP).append(aIntValueB);
		append(VALUES_SEP).append(aIntValueC);
		append(DESCR_VALUE_END);
		return this;
	}

	/**
	 * @param aId
	 * @param aIntValue
	 * @param aValue
	 * @return
	 */
	public CLogLineBuffer appendDescr(String aId, int aIntValue, String aValue) {
		return appendDescr(aId, String.valueOf(aIntValue), aValue);
	}

	/**
	 * @param aId
	 * @param aDescriber
	 * @return
	 */
	public CLogLineBuffer appendDescr(String aId, IXDescriber aDescriber) {
		append(' ').append(aId).append(DESCR_VALUE_START);
		if (aDescriber != null) {
			appendDescr(aDescriber);
		} else {
			append((String) null);
		}
		append(DESCR_VALUE_END);
		return this;
	}

	/**
	 * Appends the "description" of the <code>long</code> argument to this trace
	 * buffer:<br>
	 * <code>aId=[theLongValue]</code>
	 * <p>
	 * The argument is converted to a string as if by the method
	 * <code>String.valueOf()</code>, and the characters of that string are then
	 * appended to this trace-buffer.
	 * 
	 * @param aId
	 *            a <code>String</code> identifier.
	 * @param aLongValue
	 *            the <code>long</code> value.
	 * @return a reference to this <code>CTraceBuffer</code> object.
	 * @see java.lang.CLogLineBuffer#append(java.lang.String)
	 */
	public CLogLineBuffer appendDescr(String aId, long aLongValue) {
		return appendDescr(aId, String.valueOf(aLongValue));
	}

	/**
	 * @param aId
	 * @param aLongValue
	 * @param aLongValueB
	 * @return
	 */
	public CLogLineBuffer appendDescr(String aId, long aLongValue, long aLongValueB) {
		return appendDescr(aId, String.valueOf(aLongValue), String.valueOf(aLongValueB));
	}

	/**
	 * @param aId
	 * @param aValues
	 * @return
	 */
	public CLogLineBuffer appendDescr(String aId, Object... aValues) {

		append(' ').append(aId).append(DESCR_VALUE_START);

		int wMax = (aValues != null) ? aValues.length : 0;
		if (wMax == 0) {

		} else {
			Object wValue;
			int wI = 0;
			append(aValues[wI]);
			wI++;
			while (wI < wMax) {
				wValue = aValues[wI];
				if (wValue != null) {
					append(';').append(wValue);
				}
				wI++;
			}
		}
		append(DESCR_VALUE_END);
		return this;
	}

	/**
	 * Appends the "description " of the <code>String</code> value argument to
	 * this trace buffer:<br>
	 * <code>aId=[theStringValue]</code>
	 * <p>
	 * The characters of the <code>String</code> argument are appended, in
	 * order, to the contents of this trace-buffer, increasing the length of
	 * this trace-buffer by the length of the argument.
	 * <p>
	 * If <code>str</code> is <code>null</code>, then the four characters
	 * <code>"null"</code> are appended to this trace-buffer.
	 * <p>
	 * 
	 * @param aId
	 *            a <code>String</code> identifier.
	 * @param aStringValue
	 *            a <code>String</code> value.
	 * @return a reference to this <code>CTraceBuffer</code> object.
	 * @see java.lang.CTraceBuffer#append(java.lang.String)
	 */

	/**
	 * Appends the "description " of the <code>boolean</code> value argument to
	 * this trace buffer:<br>
	 * <code>aId=[theBoolValue]</code>
	 * <p>
	 * The argument is converted to a string as if by the method
	 * <code>String.valueOf()</code>, and the characters of that string are then
	 * appended to this trace-buffer.
	 * <p>
	 * 
	 * @param aId
	 *            a <code>String</code> identifier.
	 * @param aBoolValue
	 *            a <code>boolean</code> value.
	 * @return a reference to this <code>CTraceBuffer</code> object.
	 * @see java.lang.CLogLineBuffer#append(java.lang.String)
	 */
	public CLogLineBuffer appendDescr(String aId, Throwable e) {
		String wClassName = e.getClass().getName();
		int wPos = wClassName.lastIndexOf('.');
		wClassName = ((wPos > -1 && wPos < wClassName.length() - 1) ? wClassName
				.substring(wPos + 1) : wClassName);

		String wMess = e.getLocalizedMessage();

		append(PREFIX);
		if (aId != null) {
			append(aId).append(DESCR_VALUE_START);
		}
		append(wClassName);

		if (wMess != null) {
			append(':').append(wMess);
		}

		append(':').append(pToolsException.getFirstPsem2mLineInStack(e));

		if (aId != null) {
			append(DESCR_VALUE_END);
		}

		return this;
	}

	/**
	 * 
	 * @param e
	 * @return
	 */
	public CLogLineBuffer appendDescr(Throwable e) {
		return appendDescr(null, e);
	}

	/**
	 * Appends the "description Left Aligned" of the <code>Class</code> argument
	 * to this trace buffer:<br>
	 * <code>aId=[theValue + space(s) to pad to the total length ]</code>
	 * <p>
	 * The argument is converted to a string as if by the method
	 * <code>getName()</code>, and the characters of that string are then
	 * appended to this trace-buffer like :<br>
	 * <code>aId=[theClasseName]</code>
	 * 
	 * @param aId
	 *            the <code>String</code> identifier.
	 * @param aClass
	 *            the <code>Class</code> which name is the value.
	 * @param aValueLength
	 *            the length.
	 * @return a reference to this <code>CTraceBuffer</code> object.
	 * @see java.lang.CLogLineBuffer#append(java.lang.String)
	 */
	public CLogLineBuffer appendDescrLeftAligned(String aId, Class<?> aClass, int aValueLength) {
		if (aClass == null) {
			return appendDescrLeftAligned(aId, (String) null, aValueLength);
		} else {
			String wName = aClass.getName();
			int wPos = wName.lastIndexOf('.');
			return appendDescrLeftAligned(aId,
					((wPos > -1 && wPos < wName.length() - 1) ? wName.substring(wPos + 1) : wName),
					aValueLength);
		}
	}

	/**
	 * Appends the "description Left Aligned" of the <code>String</code> value
	 * argument to this trace buffer:<br>
	 * <code>aId=[theStringValue + space(s) to pad to the total length ]</code>
	 * 
	 * @param aId
	 *            a <code>String</code> identifier.
	 * @param aStringValue
	 *            a <code>String</code> value.
	 * @param aValueLength
	 *            the length.
	 * @return a reference to this <code>CTraceBuffer</code> object.
	 * @see java.lang.CLogLineBuffer#append(java.lang.String)
	 */
	public CLogLineBuffer appendDescrLeftAligned(String aId, String aStringValue, int aValueLength) {
		this.append(' ').append(aId).append(DESCR_VALUE_START);
		int wLen = aStringValue.length();
		if (wLen > aValueLength) {
			this.append(aStringValue.substring(0, aValueLength));
		} else {
			this.append(aStringValue).append(' ', aValueLength - wLen);
		}
		this.append(DESCR_VALUE_END);
		return this;
	}

	/**
	 * Appends the "description Right Aligned" of the <code>int</code> argument
	 * to this trace buffer:<br>
	 * <code>aId=[space(s) to pad to the total length + theIntValue]</code>
	 * <p>
	 * The argument is converted to a string as if by the method
	 * <code>String.valueOf()</code>, and the characters of that string are then
	 * appended to this trace-buffer.
	 * 
	 * @param aId
	 *            a <code>String</code> identifier.
	 * @param aIntValue
	 *            the <code>int</code> value.
	 * @param aValueLength
	 *            the length.
	 * @return a reference to this <code>CTraceBuffer</code> object.
	 * @see java.lang.CLogLineBuffer#append(java.lang.String)
	 */
	public CLogLineBuffer appendDescrRightAligned(String aId, int aIntValue, int aValueLength) {
		return appendDescrRightAligned(aId, String.valueOf(aIntValue), aValueLength);
	}

	/**
	 * Appends the "description Right Aligned" of the <code>long</code> argument
	 * to this trace buffer:<br>
	 * <code>aId=[space(s) to pad to the total length + theLongValue]</code>
	 * <p>
	 * The argument is converted to a string as if by the method
	 * <code>String.valueOf()</code>, and the characters of that string are then
	 * appended to this trace-buffer.
	 * 
	 * @param aId
	 *            a <code>String</code> identifier.
	 * @param aLongValue
	 *            the <code>long</code> value.
	 * @param aValueLength
	 *            the length.
	 * @return a reference to this <code>CTraceBuffer</code> object.
	 * @see java.lang.CLogLineBuffer#append(java.lang.String)
	 */
	public CLogLineBuffer appendDescrRightAligned(String aId, long aLongValue, int aValueLength) {
		return appendDescrRightAligned(aId, String.valueOf(aLongValue), aValueLength);
	}

	/**
	 * Appends the "description Left Aligned" of the <code>String</code> value
	 * argument to this trace buffer:<br>
	 * <code>aId=[space(s) to pad to the total length + theStringValue]</code>
	 * 
	 * @param aId
	 *            a <code>String</code> identifier.
	 * @param aStringValue
	 *            a <code>String</code> value.
	 * @param aValueLength
	 *            the length.
	 * @return a reference to this <code>CTraceBuffer</code> object.
	 * @see java.lang.CLogLineBuffer#append(java.lang.String)
	 */
	public CLogLineBuffer appendDescrRightAligned(String aId, String aStringValue, int aValueLength) {
		this.append(' ').append(aId).append(DESCR_VALUE_START);
		int wLen = aStringValue.length();
		if (wLen > aValueLength) {
			this.append(aStringValue.substring(0, aValueLength));
		} else {
			this.append(' ', aValueLength - wLen).append(aStringValue);
		}
		this.append(DESCR_VALUE_END);
		return this;
	}

	/**
	 * @return
	 */
	public CLogLineBuffer appendLine() {
		return appendLine('-');
	}

	/**
	 * @param aChar
	 * @return
	 */
	public CLogLineBuffer appendLine(char aChar) {
		return appendLine(aChar, 80);
	}

	/**
	 * @param aChar
	 * @param aLen
	 * @return
	 */
	public CLogLineBuffer appendLine(char aChar, int aLen) {
		return appendLine(aChar, aLen, 0);
	}

	/**
	 * @param aChar
	 * @param aLen
	 * @param aMarginLeft
	 * @return
	 */
	public CLogLineBuffer appendLine(char aChar, int aLen, int aMarginLeft) {
		append('\n');
		if (aMarginLeft > 0) {
			append(' ', aMarginLeft);
		}
		append(aChar, aLen);
		return this;
	}

	/**
	 * 
	 * @param aBytes
	 * @return
	 */
	public synchronized CLogLineBuffer appendString(byte[] aBytes) {
		try {
			return append(new String(aBytes, CXOSUtils.ENCODING_UTF_8));
		} catch (UnsupportedEncodingException e) {
			append(e.getClass().getSimpleName());
			append(' ');
			append(e.getMessage());
			append(' ');
			append(new String(aBytes));

			return this;
		}
	}

	/**
	 * *
	 * 
	 * @return
	 */
	public CLogLineBuffer appendTab() {
		return append('\t');
	}

	/**
	 * 16j_000
	 * 
	 * @param aFormat
	 * @param aValues
	 * @return
	 */
	public CLogLineBuffer appendText(String aFormat, Object... aValues) {
		return append(' ').append(String.format(aFormat, aValues));
	}

	/**
	 * @param e
	 * @return
	 */
	public CLogLineBuffer appendThrowableDescr(Throwable e) {
		return appendThrowableDescr(e, '\n');
	}

	/**
	 * @param e
	 * @param aSeparator
	 * @return
	 */
	public CLogLineBuffer appendThrowableDescr(Throwable e, char aSeparator) {
		return appendThrowableDescr(e, String.valueOf(aSeparator));
	}

	/**
	 * @param e
	 * @param aSeparator
	 * @return
	 */
	public CLogLineBuffer appendThrowableDescr(Throwable e, String aSeparator) {
		append(LIB_CLASS).append('=').append('[').append(e.getClass().getName()).append(']');

		append(aSeparator);

		try {
			Class<?> wClass = e.getClass();
			Method wMethod = wClass.getMethod(METHOD_WHY);
			if (wMethod != null) {
				String wWhy = (String) wMethod.invoke(e);
				append(LIB_WHY).append('=').append('[').append(wWhy).append(']');
				append(aSeparator);
			}
		} catch (NoSuchMethodException e1) {
			// nothing
		} catch (Exception e2) {
			append(LIB_WHY).append('=').append('[')
					.append("Can't read the 'why' of the Throwable.").append(']')
					.append(aSeparator);
			append(LIB_WHY).append('=').append('[').append(e2.getMessage()).append(']')
					.append(aSeparator);
			append(LIB_WHY).append('=').append('[').append(e2.getClass().getName()).append(']')
					.append(aSeparator);
			append(LIB_WHY).append('=').append('[').append(pToolsException.eStackToString(e2))
					.append(']').append(aSeparator);
		}

		boolean wHasListOfMess = false;
		try {
			Class<?> wClass = e.getClass();
			Method wMethod = wClass.getMethod(METHOD_GETLISTOFMESS);

			// if the method exists
			if (wMethod != null) {
				wHasListOfMess = true;
				List<?> wComplement = (List<?>) wMethod.invoke(e);
				int wMax = wComplement.size();
				if (wMax > 1) {
					int wI = 0;
					while (wI < wMax) {
						append(LIB_MESS);
						append('(').append(wI + 1).append(')');
						append('=').append('[').append(wComplement.get(wI)).append(']');
						append(aSeparator);
						wI++;
					}
				}
			}
		} catch (NoSuchMethodException e1) {
			// nothing
		} catch (Exception e3) {
			append(LIB_MESS).append('=').append('[')
					.append("Can't get the 'message(s)' of the Throwable.").append(']')
					.append(aSeparator);
			append(LIB_MESS).append('=').append('[').append(e3.getMessage()).append(']')
					.append(aSeparator);
			append(LIB_MESS).append('=').append('[').append(e3.getClass().getName()).append(']')
					.append(aSeparator);
			append(LIB_MESS).append('=').append('[').append(pToolsException.eStackToString(e3))
					.append(']').append(aSeparator);
		}

		if (!wHasListOfMess) {
			Throwable wThrowable = e;
			String wMess;
			int wNbMess = 1;
			while (wThrowable != null) {
				wMess = wThrowable.getMessage();
				boolean wHasMess = (wMess != null && wMess.length() > 0);

				if (wHasMess || wNbMess > 1) {
					append(LIB_MESS);
					append('(').append(wNbMess).append(')');
					append('=').append('[');

					if (wNbMess == 1) {
						append(wMess);
					} else {
						addEClassAndMessInSB(wThrowable);
					}

					append(']');
					append(aSeparator);
				}

				wNbMess++;
				wThrowable = wThrowable.getCause();
			}
		}

		append(LIB_STACK);
		append('=').append('[');
		append(pToolsException.eStackToString(e).replace("\n", aSeparator)).append(']');
		append(aSeparator);

		if (e.getCause() != null) {
			append(LIB_CAUSE);
			appendThrowableDescr(e.getCause(), aSeparator);
		}

		return this;
	}

	@Override
	public char charAt(int index) {
		return pBuffer.charAt(index);
	}

	/**
   *
   */
	public CLogLineBuffer delete() {
		pBuffer.delete(0, pBuffer.length());
		return this;
	}

	/**
	 * @throws StringIndexOutOfBoundsException
	 *             {@inheritDoc}
	 */
	public CLogLineBuffer delete(int start, int end) {
		pBuffer.delete(start, end);
		return this;
	}

	/**
	 * @throws StringIndexOutOfBoundsException
	 *             {@inheritDoc}
	 */
	public CLogLineBuffer deleteCharAt(int index) {
		pBuffer.deleteCharAt(index);
		return this;
	}

	public byte[] getBytes() {
		return pBuffer.toString().getBytes();
	}

	/**
	 * @param charsetName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {
		return pBuffer.toString().getBytes(charsetName);
	}

	/**
	 * @return
	 */
	final char[] getChars() {
		int wLen = length();
		char[] wChars = new char[wLen];
		pBuffer.toString().getChars(0, wLen, wChars, 0);
		return wChars;
	}

	/**
	 * @param aChar
	 * @return
	 */
	public int indexOf(char aChar) {
		int wMax = length();
		int wI = 0;
		while (wI < wMax) {
			if (charAt(wI) == aChar) {
				return wI;
			}
			wI++;
		}
		return -1;
	}

	/**
	 * @param aOffset
	 * @param aChar
	 * @return
	 */
	public CLogLineBuffer insert(int aOffset, char aChar) {
		pBuffer.insert(aOffset, aChar);
		return this;
	}

	/**
	 * @param aOffset
	 * @param aString
	 * @return
	 */
	public CLogLineBuffer insert(int aOffset, String aString) {
		pBuffer.insert(aOffset, aString);
		return this;
	}

	/**
	 * Insert the "formated" MethodName argument at the beginin of this buffer:<br>
	 * <code>".aMethodName\t"</code>
	 * 
	 * @param aMethodName
	 *            a <code>String</code> name of a method
	 * @return a reference to this <code>CTraceBuffer</code> object.
	 * @see java.lang.CLogLineBuffer#append(java.lang.String)
	 */
	public CLogLineBuffer insertMethodName(String aMethodName) {
		int wLength = aMethodName.length();
		if (wLength > LENGTH_MAX_METHODNAME) {
			aMethodName = aMethodName.substring(0, LENGTH_MAX_METHODNAME);
		} else {
			int wMax = LENGTH_MAX_METHODNAME - wLength;
			aMethodName += SPACES_METHODNAME.substring(0, wMax);
		}
		this.replace(0, 0, aMethodName);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length() {
		return pBuffer.length();
	}

	/**
	 * @param aWhy
	 * @param aBy
	 * @return
	 */
	public CLogLineBuffer replace(char aWhy, char aBy) {
		int wMax = length();
		int wI = 0;
		while (wI < wMax) {
			if (charAt(wI) == aWhy) {
				setCharAt(wI, aBy);
			}
			wI++;
		}
		return this;
	}

	/**
	 * @throws StringIndexOutOfBoundsException
	 *             {@inheritDoc}
	 */
	public CLogLineBuffer replace(int start, int end, String str) {
		pBuffer.replace(start, end, str);
		return this;
	}

	/**
	 * @param index
	 * @param ch
	 */
	public void setCharAt(int index, char ch) {
		pBuffer.setCharAt(index, ch);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	@Override
	public CharSequence subSequence(int start, int end) {
		return pBuffer.subSequence(start, end);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return pBuffer.toString();
	}
}
