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
package org.psem2m.utilities;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CXBytesUtils {

	private static byte BOM_UTF_16BE[] = { (byte) 0xFE, (byte) 0xFF };

	private static byte BOM_UTF_16LE[] = { (byte) 0xFF, (byte) 0xFE };

	private static byte BOM_UTF_32BE[] = { (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF };

	private static byte BOM_UTF_32LE[] = { (byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00 };

	private static byte BOM_UTF_8[] = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

	/**
	 * java.nio.charset.Charset: ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1
	 */
	private final static String DEF_ISO_8859_1_CONSTANT_JAVA = "ISO-8859-1";

	/**
	 * java.nio.charset.Charset: Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the
	 * Basic Latin block of the Unicode character set
	 */
	private final static String DEF_US_ASCII_CONSTANT_JAVA = "US-ASCII";

	/**
	 * java.nio.charset.Charset: Sixteen-bit UCS Transformation Format, byte
	 * order identified by an optional byte-order mark
	 */
	private final static String DEF_UTF_16_CONSTANT_JAVA = "UTF-16";

	/**
	 * java.nio.charset.Charset: Sixteen-bit UCS Transformation Format,
	 * big-endian byte order
	 */
	private final static String DEF_UTF_16BE_CONSTANT_JAVA = "UTF-16BE";

	/**
	 * java.nio.charset.Charset: Sixteen-bit UCS Transformation Format,
	 * little-endian byte order
	 */
	private final static String DEF_UTF_16LE_CONSTANT_JAVA = "UTF-16LE";

	/**
	 * java.nio.charset.Charset: Eight-bit UCS Transformation Format
	 */
	private final static String DEF_UTF_8_CONSTANT_JAVA = "UTF-8";

	public final static String ENCODING_ISO_8859_1 = DEF_ISO_8859_1_CONSTANT_JAVA;

	public final static String ENCODING_US_ASCII = DEF_US_ASCII_CONSTANT_JAVA;

	public final static String ENCODING_UTF_16 = DEF_UTF_16_CONSTANT_JAVA;

	public final static String ENCODING_UTF_16BE = DEF_UTF_16BE_CONSTANT_JAVA;

	public final static String ENCODING_UTF_16LE = DEF_UTF_16LE_CONSTANT_JAVA;

	public final static String ENCODING_UTF_32BE = "UTF-32BE";

	public final static String ENCODING_UTF_32LE = "UTF-32LE";

	public final static String ENCODING_UTF_8 = DEF_UTF_8_CONSTANT_JAVA;

	private final static String HEXA_BUFFER_DIGITS = ".x0123456789ABCDEF";

	private final static char[] HEXA_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'A', 'B', 'C', 'D', 'E', 'F' };

	private final static String HEXA_PREFIX = ".x";

	public static int LEN_OF_BYTE = 1;

	public static int LEN_OF_INT = 4;

	public static int LEN_OF_LONG = 8;

	public static int LEN_OF_SHORT = 2;

	private final static String MESS_NOT_ENOUGH_LARGE = "The byte array is not enough large. It should have at least [%s] bytes to accept an [%s]";

	/**
	 * ajoute la representation HEXA d'un octet dans un StringBuilder
	 * 
	 * @param aSB
	 * @param mybyte
	 * @return
	 */
	static public StringBuilder addByte2DecimalInSB(final StringBuilder aSB, final byte mybyte) {
		aSB.append('.');
		aSB.append('d');
		aSB.append(CXStringUtils.strAdjustRight(mybyte, 3));
		return aSB;
	}

	/**
	 * ajoute la representation HEXA d'un octet dans un CStringBuilder
	 * 
	 * @param aCSB
	 * @param mybyte
	 * @return
	 */
	static public StringBuilder addByte2HexaInCSB(final StringBuilder aSB, final byte mybyte) {
		aSB.append('.');
		aSB.append('x');
		aSB.append(HEXA_DIGITS[(0x00F0 & mybyte) >> 4]);
		aSB.append(HEXA_DIGITS[0x000F & mybyte]);
		return aSB;
	}

	/**
	 * ajoute la representation HEXA d'un octet dans un StringBuilder
	 * 
	 * @param aSB
	 * @param mybyte
	 * @return
	 */
	static public StringBuilder addByte2HexaInSB(final StringBuilder aSB, final byte mybyte) {
		aSB.append('.');
		aSB.append('x');
		aSB.append(HEXA_DIGITS[(0x00F0 & mybyte) >> 4]);
		aSB.append(HEXA_DIGITS[0x000F & mybyte]);
		return aSB;
	}

	/**
	 * Ajoute un "byte" (1) octets au buffer.
	 * 
	 * @param aBuffer
	 * @param aPos
	 * @param aValue
	 * @return la nouvelle position
	 * @throws ArrayIndexOutOfBoundsException
	 */
	static public int appendByteInBuffer(final byte[] aBuffer, final int aPos, final int aValue)
			throws ArrayIndexOutOfBoundsException {
		if (aBuffer.length - aPos < LEN_OF_BYTE) {
			throw new IllegalArgumentException("Byte array should contain at least 2 bytes");
		}

		aBuffer[aPos] = (byte) (aValue & 0x000000FF);

		return aPos + LEN_OF_BYTE;
	}

	/**
	 * Ajoute un "short" 2 octets au buffer
	 * 
	 * @param aBuffer
	 * @param aPos
	 * @param aInfo
	 * @return la nouvelle position
	 * @throws ArrayIndexOutOfBoundsException
	 */
	static public int appendBytesInBuffer(final byte[] aBuffer, final int aPos, final byte[] aInfo)
			throws ArrayIndexOutOfBoundsException {
		int wLen = aInfo.length;
		System.arraycopy(aInfo, 0, aBuffer, aPos, wLen);
		return aPos + wLen;
	}

	/**
	 * Ajoute un "entier" 4 octets au buffer.
	 * 
	 * @param aBuffer
	 * @param aPos
	 * @param aValue
	 * @return la nouvelle position
	 * @throws IllegalArgumentException
	 */
	public static int appendBytetInBuffer(final byte[] aBuffer, final int aPos, final int aValue)
			throws IllegalArgumentException {
		if (aBuffer.length - aPos < LEN_OF_BYTE) {
			throw new IllegalArgumentException("Byte array should contain at least " + LEN_OF_BYTE
					+ " bytes");
		}

		aBuffer[aPos] = (byte) (aValue & 0x000000FF);

		return aPos + LEN_OF_BYTE;
	}

	/**
	 * 
	 * @param aDestBuffer
	 * @param aDestPos
	 * @param aAddedBuffer
	 * @return
	 * @throws ArrayIndexOutOfBoundsException
	 */
	static public byte[] appendInExpendableBuffer(final byte[] aDestBuffer, final int aDestPos,
			final byte[] aAddedBuffer) throws ArrayIndexOutOfBoundsException {
		return appendInExpendableBuffer(aDestBuffer, aDestPos, aAddedBuffer, 0, aAddedBuffer.length);
	}

	/**
	 * Ajoute aAddedLen octets du aAddedBuffer a partir de aAddedOffset dans le
	 * buffer "extensible" aDestBuffer en position aDestPos
	 * 
	 * @param aDestBuffer
	 * @param aDestPos
	 * @param aAddedBuffer
	 * @param aOffset
	 * @param aAddedLen
	 * @return
	 * @throws ArrayIndexOutOfBoundsException
	 */
	static public byte[] appendInExpendableBuffer(final byte[] aDestBuffer, final int aDestPos,
			final byte[] aAddedBuffer, final int aAddedOffset, final int aAddedLen)
			throws ArrayIndexOutOfBoundsException {
		if (aDestBuffer.length - aDestPos >= aAddedLen) {
			System.arraycopy(aAddedBuffer, aAddedOffset, aDestBuffer, aDestPos, aAddedLen);
			return aDestBuffer;
		} else {
			byte[] wBuffer = new byte[aDestPos + aAddedLen];
			System.arraycopy(aDestBuffer, 0, wBuffer, 0, aDestPos);
			System.arraycopy(aAddedBuffer, aAddedOffset, wBuffer, aDestPos, aAddedLen);
			return wBuffer;
		}
	}

	/**
	 * Ajoute un "entier" 4 octets au buffer.
	 * 
	 * @return la nouvelle position
	 * @param aBuffer
	 * @param aPos
	 * @param aValue
	 * @return la nouvelle position
	 * @throws IllegalArgumentException
	 */
	public static int appendIntInBuffer(final byte[] aBuffer, final int aPos, final int aValue)
			throws IllegalArgumentException {
		if (aBuffer.length - aPos < LEN_OF_INT) {
			throw new IllegalArgumentException(builNotEnoughLargeMess(LEN_OF_INT, "int"));
		}

		for (int i = 3; i >= 0; i--) {
			aBuffer[aPos + 3 - i] = (byte) (aValue >>> (8 * i));
		}
		return aPos + LEN_OF_INT;
	}

	/**
	 * Ajoute un "long" 8 octets au buffer.
	 * 
	 * @param aBuffer
	 * @param aPos
	 * @param aValue
	 * @return la nouvelle position
	 * @throws IllegalArgumentException
	 */
	public static int appendLongInBuffer(final byte[] aBuffer, final int aPos, final long aValue)
			throws IllegalArgumentException {
		if (aBuffer.length - aPos < LEN_OF_LONG) {
			throw new IllegalArgumentException(builNotEnoughLargeMess(LEN_OF_LONG, "long"));
		}

		for (int i = 7; i >= 0; i--) {
			aBuffer[aPos + 7 - i] = (byte) (aValue >>> (8 * i));
		}
		return aPos + LEN_OF_LONG;
	}

	/**
	 * Ajoute un n octets contenant la valeur "aValue" au buffer.
	 * 
	 * @param aBuffer
	 * @param aPos
	 * @param aValue
	 * @param aNb
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static int appendNBytesInBuffer(final byte[] aBuffer, int aPos, final byte aValue,
			final int aNb) throws IllegalArgumentException {
		if (aBuffer.length - aPos < LEN_OF_LONG) {
			throw new IllegalArgumentException("Byte array should contain at least 8 bytes");
		}
		int wI = 0;
		while (wI < aNb) {
			aBuffer[aPos] = aValue;
			aPos++;
			wI++;
		}
		return aPos + aNb;
	}

	/**
	 * Ajoute un "short" 2 octets au buffer
	 * 
	 * @param aBuffer
	 * @param aPos
	 * @param aValue
	 * @return la nouvelle position
	 * @throws ArrayIndexOutOfBoundsException
	 */
	static public int appendShortInBuffer(final byte[] aBuffer, final int aPos, final int aValue)
			throws ArrayIndexOutOfBoundsException {
		if (aBuffer.length - aPos < LEN_OF_SHORT) {
			throw new IllegalArgumentException("Byte array should contain at least 2 bytes");
		}

		aBuffer[aPos] = (byte) ((aValue >>> 8) & 0x000000FF);
		aBuffer[aPos + 1] = (byte) (aValue & 0x000000FF);

		return aPos + LEN_OF_SHORT;
	}

	/**
	 * @param aSize
	 * @param aContent
	 * @return
	 */
	private static String builNotEnoughLargeMess(final int aSize, final String aContent) {
		return String.format(MESS_NOT_ENOUGH_LARGE, String.valueOf(aSize), aContent);
	}

	/**
	 * @param mybyte
	 * @return la valeur ASCII d'un octet
	 */
	static public int byte2CodeASCII(final byte mybyte) {
		// the byte to translate is mybyte
		return (0x00FF & mybyte);
	}

	/**
	 * @param mybyte
	 * @return la representation hexa d'un octet
	 */
	static public String byte2Hexa(final byte mybyte) {
		return new String(byte2HexaInChars(mybyte));
	}

	/**
	 * @param mybyte
	 * @return
	 */
	static private char[] byte2HexaInChars(final byte mybyte) {
		char[] wResult = new char[4];
		wResult[0] = '.';
		wResult[1] = 'x';
		wResult[2] = HEXA_DIGITS[(0x00F0 & mybyte) >> 4];
		wResult[3] = HEXA_DIGITS[0x000F & mybyte];
		return wResult;
	}

	/**
	 * dump d'un buffer sous forme d'une chaine Decimale : .d001.d255 ...
	 * 
	 * @param aBuffer
	 * @param aOffset
	 * @param aLong
	 * @return
	 */
	static public String byteBufferBin2Decimal(final byte[] aBuffer, final int aOffset,
			final int aLong) {
		if (aLong > aBuffer.length - aOffset) {
			StringBuilder wMess = new StringBuilder(256);
			wMess.append("byteBufferBin2Hexa : aLong < aBuffer.length - aOffset !");
			CXStringUtils.appendKeyValInBuff(wMess, "Buffer.length", aBuffer.length);
			CXStringUtils.appendKeyValInBuff(wMess, "Offset", aOffset);
			CXStringUtils.appendKeyValInBuff(wMess, "Long", aLong);
			return wMess.toString();
		}

		StringBuilder wSB = new StringBuilder(aLong * 4);
		int wI = aOffset;
		int wMax = aOffset + aLong;
		try {
			while (wI < wMax) {
				addByte2DecimalInSB(wSB, aBuffer[wI]);
				wI++;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			wSB.append(" ArrayIndexOutOfBoundsException" + e.getLocalizedMessage());
		}
		return wSB.toString();
	}

	/**
	 * appel de la trace pour un buffer
	 */
	static public String byteBufferBin2Hexa(final byte[] aBuffer) {
		return byteBufferBin2Hexa(aBuffer, 0, aBuffer.length);
	}

	/**
	 * dump d'un buffer sous forme d'une chaine Hex : .xFF.xFE ...
	 * 
	 * @param aBuffer
	 * @param aOffset
	 * @param aLong
	 * @return
	 */
	static public String byteBufferBin2Hexa(final byte[] aBuffer, final int aOffset, final int aLong) {
		if (aLong > aBuffer.length - aOffset) {
			StringBuilder wMess = new StringBuilder(256);
			wMess.append("byteBufferBin2Hexa : aLong < aBuffer.length - aOffset !");
			CXStringUtils.appendKeyValInBuff(wMess, "Buffer.length", aBuffer.length);
			CXStringUtils.appendKeyValInBuff(wMess, "Offset", aOffset);
			CXStringUtils.appendKeyValInBuff(wMess, "Long", aLong);
			return wMess.toString();
		}

		StringBuilder wSB = new StringBuilder(aLong * 4);
		int wI = aOffset;
		int wMax = aOffset + aLong;
		try {
			while (wI < wMax) {
				addByte2HexaInSB(wSB, aBuffer[wI]);
				wI++;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			wSB.append(" ArrayIndexOutOfBoundsException" + e.getLocalizedMessage());
		}
		return wSB.toString();
	}

	/**
	 * @param aBuffer
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static int bytesToInt(final byte[] aBuffer) throws IllegalArgumentException {
		return bytesToInt(aBuffer, 0);
	}

	/**
	 * @param aBuffer
	 * @param aPos
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static int bytesToInt(final byte[] aBuffer, final int aPos)
			throws IllegalArgumentException {
		if (aBuffer.length - aPos < LEN_OF_INT) {
			throw new IllegalArgumentException("Byte array should contain at least 4 bytes");
		}
		int l = 0;
		for (int i = 0; i < LEN_OF_INT; i++) {
			l += (unsignedByteToLong(aBuffer[aPos + 3 - i]) << (8 * i));
		}
		return l;
	}

	/**
	 * @param aBuffer
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static long bytesToLong(final byte[] aBuffer) throws IllegalArgumentException {
		return bytesToLong(aBuffer, 0);
	}

	/**
	 * @param aBuffer
	 * @param aPos
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static long bytesToLong(final byte[] aBuffer, final int aPos)
			throws IllegalArgumentException {
		if (aBuffer.length - aPos < LEN_OF_LONG) {
			throw new IllegalArgumentException("Byte array should contain at least 8 bytes");
		}
		long l = 0;
		for (int i = 0; i < LEN_OF_LONG; i++) {
			l += (unsignedByteToLong(aBuffer[aPos + 7 - i]) << (8 * i));
		}
		return l;
	}

	/**
	 * @param aBuffer
	 * @param aPos
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static short bytesToShort(final byte[] aBuffer, final int aPos)
			throws IllegalArgumentException {
		if (aBuffer.length - aPos < LEN_OF_SHORT) {
			throw new IllegalArgumentException("Byte array should contain at least 2 bytes");
		}

		short l = 0;
		for (int i = 0; i < LEN_OF_SHORT; i++) {
			l += (unsignedByteToLong(aBuffer[aPos + 1 - i]) << (8 * i));
		}
		return l;
	}

	/**
	 * @param aBuffer
	 * @param aPos
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static int bytesToUnsignedShort(final byte[] aBuffer, final int aPos)
			throws IllegalArgumentException {
		if (aBuffer.length - aPos < LEN_OF_SHORT) {
			throw new IllegalArgumentException("Byte array should contain at least 2 bytes");
		}

		int l = 0;
		for (int i = 0; i < LEN_OF_SHORT; i++) {
			l += (unsignedByteToLong(aBuffer[aPos + 1 - i]) << (8 * i));
		}
		return l;
	}

	/**
	 * @param aLong
	 * @return
	 */
	public static byte[] byteToBytes(final byte aByte) {
		byte[] wBuff = new byte[1];
		wBuff[0] = aByte;
		return wBuff;
	}

	/**
	 * 
	 * @param aBuffer
	 * @param aBomRef
	 * @return
	 */
	private static boolean checkBOM(final byte[] aBuffer, final byte[] aBomRef) {
		int wMax = Math.min(aBuffer.length, aBomRef.length);
		int wI = 0;
		while (wI < wMax && aBuffer[wI] == aBomRef[wI]) {
			wI++;
		}
		return wI == wMax;
	}

	/**
	 * Computes the failure function using a boot-strapping process, where the
	 * pattern is matched against itself.
	 * 
	 * @param pattern
	 * @return
	 */
	private static int[] computeFailure(final byte[] pattern) {
		int[] failure = new int[pattern.length];
		int j = 0;
		for (int i = 1; i < pattern.length; i++) {
			// System.out.println("i: " + i);
			while (j > 0 && pattern[j] != pattern[i]) {
				// System.out.println("j: " + j);
				j = failure[j - 1];
			}
			if (pattern[j] == pattern[i]) {
				j++;
			}
			failure[i] = j;
		}
		return failure;
	}

	/**
	 * @param data
	 * @param pattern
	 * @return
	 */
	public static boolean contains(final byte[] data, final byte[] pattern) {
		return contains(data, 0, data.length, pattern);
	}

	/**
	 * @param data
	 * @param aOffset
	 * @param aMax
	 * @param pattern
	 * @return
	 */
	public static boolean contains(final byte[] data, final int aOffset, final int aLen,
			final byte[] pattern) {
		return (indexOf(data, aOffset, aLen, pattern) > -1);
	}

	/**
	 * @param aBuffer
	 * @return
	 */
	public static int getBomLength(final byte[] aBuffer) {
		if (checkBOM(aBuffer, BOM_UTF_8)) {
			return BOM_UTF_8.length;
		} else if (checkBOM(aBuffer, BOM_UTF_16BE)) {
			return BOM_UTF_16BE.length;
		} else if (checkBOM(aBuffer, BOM_UTF_16LE)) {
			return BOM_UTF_16LE.length;
		} else if (checkBOM(aBuffer, BOM_UTF_32BE)) {
			return BOM_UTF_32BE.length;
		} else if (checkBOM(aBuffer, BOM_UTF_32LE)) {
			return BOM_UTF_32LE.length;
		}

		return 0;
	}

	/**
	 * @param aEncoding
	 * @return
	 */
	public static int getBomLength(final String aEncoding) {
		String wPlatformEncoding = System.getProperty("file.encoding");
		if (wPlatformEncoding != null && wPlatformEncoding.length() > 0
				&& wPlatformEncoding.equals(aEncoding)) {
			return 0;
		}

		if (ENCODING_UTF_8.equals(aEncoding)) {
			return BOM_UTF_8.length;
		}
		if (ENCODING_UTF_16BE.equals(aEncoding)) {
			return BOM_UTF_16BE.length;
		}
		if (ENCODING_UTF_16LE.equals(aEncoding)) {
			return BOM_UTF_16LE.length;
		}
		if (ENCODING_UTF_32BE.equals(aEncoding)) {
			return BOM_UTF_32BE.length;
		}
		if (ENCODING_UTF_32LE.equals(aEncoding)) {
			return BOM_UTF_32LE.length;
		}

		return 0;
	}

	/**
	 * 
	 * The Java I/O classes will read and write Unicode data in the
	 * "default character encoding" by default. The default encoding depends on
	 * some combination of the JVM, the underlying OS, and the OS-level locale
	 * settings. For example, on my Windows 2000 computer, the default encoding
	 * is CP-1252, a Microsoft-specific variant of Latin-1.
	 * 
	 * @param aBuffer
	 * @return
	 */
	public static String getEncoding(final byte[] aBuffer) {
		String wEncoding = System.getProperty("file.encoding");
		if (checkBOM(aBuffer, BOM_UTF_8)) {
			wEncoding = ENCODING_UTF_8;
		} else if (checkBOM(aBuffer, BOM_UTF_16BE)) {
			wEncoding = ENCODING_UTF_16BE;
		} else if (checkBOM(aBuffer, BOM_UTF_16LE)) {
			wEncoding = ENCODING_UTF_16LE;
		} else if (checkBOM(aBuffer, BOM_UTF_32BE)) {
			wEncoding = ENCODING_UTF_32BE;
		} else if (checkBOM(aBuffer, BOM_UTF_32LE)) {
			wEncoding = ENCODING_UTF_32LE;
		}
		return wEncoding;
	}

	/**
	 * @param aHexaBuffer
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static byte[] hexaBufferToBytes(final String aHexaBuffer)
			throws IllegalArgumentException {
		/*
		 * test complet du buffer
		 */
		testHexaBufferBytes(aHexaBuffer, true);

		int wStreamSize = aHexaBuffer.length() / 4;

		byte[] wBytes = new byte[wStreamSize];

		String wHexa;
		int wOffsetNextDot;
		int wOffsetDot = 0;
		int wI = 0;
		while (wI < wStreamSize) {
			wOffsetNextDot = aHexaBuffer.indexOf('.', wOffsetDot + 1);
			if (wOffsetNextDot == -1) {
				wOffsetNextDot = aHexaBuffer.length();
			}

			wHexa = aHexaBuffer.substring(wOffsetDot + 2, wOffsetNextDot).toLowerCase();

			try {
				wBytes[wI] = (byte) (Integer.parseInt(wHexa, 16) & 0x000000FF);
			} catch (Exception e) {
				throw new IllegalArgumentException("Can't parse hexa value [" + wHexa
						+ "] localized at offset [" + (wOffsetDot + 2) + "] in hexaBuffer ["
						+ aHexaBuffer + "]");
			}
			wOffsetDot = wOffsetNextDot;
			wI++;
		}

		return wBytes;
	}

	/**
	 * @param data
	 * @param pattern
	 * @return
	 */
	public static int indexOf(final byte[] data, final byte[] pattern) {
		return indexOf(data, 0, data.length, pattern);
	}

	/**
	 * @param data
	 * @param aOffset
	 * @param aMax
	 * @param pattern
	 * @return
	 */
	public static int indexOf(final byte[] data, final int aOffset, final int aLen,
			final byte[] pattern) {
		if (aLen < 1 || aOffset < 0) {
			return -1;
		}
		int[] failure = computeFailure(pattern);
		int j = 0;
		int wMax = aOffset + aLen;
		for (int i = aOffset; i < wMax; i++) {
			while (j > 0 && pattern[j] != data[i]) {
				j = failure[j - 1];
			}
			if (pattern[j] == data[i]) {
				j++;
			}
			if (j == pattern.length) {
				return i - pattern.length + 1;
			}
		}
		return -1;
	}

	/**
	 * @param aInt
	 * @return
	 */
	public static byte[] intToBytes(final int aInt) {
		byte[] wBuffer = new byte[LEN_OF_INT];
		appendIntInBuffer(wBuffer, 0, aInt);
		return wBuffer;
	}

	/**
	 * @param aValue
	 * @return
	 */
	public static byte[] intToOneByte(final int aValue) {
		byte[] wBuff = new byte[1];
		wBuff[0] = (byte) (aValue & 0x000000FF);
		return wBuff;
	}

	/**
	 * 
	 * @param aHexaBuffer
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static boolean isHexaBufferBytes(final String aHexaBuffer) {
		return testHexaBufferBytes(aHexaBuffer, false);
	}

	/**
	 * @param aLong
	 * @return
	 */
	public static byte[] longToBytes(final long aLong) {
		byte[] wBuffer = new byte[LEN_OF_LONG];
		appendLongInBuffer(wBuffer, 0, aLong);
		return wBuffer;
	}

	/**
	 * One byte oo Int
	 * 
	 * @param aBuffer
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static int oneByteToInt(final byte[] aBuffer, final int aPos)
			throws IllegalArgumentException {
		return unsignedByteToInt(aBuffer[aPos]);
	}

	/**
	 * @param aBuffer
	 * @return
	 */
	public static byte[] removeBom(byte[] aBuffer) {
		int wBomLength = getBomLength(aBuffer);
		if (wBomLength > 0) {
			int wSize = aBuffer.length - wBomLength;
			byte[] wBuffer = new byte[wSize];
			System.arraycopy(aBuffer, wBomLength, wBuffer, 0, wSize);
			aBuffer = wBuffer;
		}
		return aBuffer;
	}

	/**
	 * @param aShort
	 * @return
	 */
	public static byte[] shortToBytes(final short aShort) {
		byte[] wBuffer = new byte[LEN_OF_SHORT];
		appendShortInBuffer(wBuffer, 0, aShort);
		return wBuffer;
	}

	/**
	 * @param aBuffer
	 * @param aPrefix
	 * @return
	 */
	public static boolean startWith(final byte[] aBuffer, final byte[] aPrefix) {

		if (aBuffer == null || aPrefix == null || aBuffer.length < aPrefix.length) {
			return false;
		}

		int wMax = aPrefix.length;
		int wI = 0;
		while (wI < wMax) {
			if (aBuffer[wI] != aPrefix[wI]) {
				return false;
			}
			wI++;
		}

		return true;
	}

	/**
	 * @param aBuffer
	 * @param aHexaPrefix
	 * @return
	 */
	public static boolean startWith(final byte[] aBuffer, final String aHexaPrefix) {
		return startWith(aBuffer, hexaBufferToBytes(aHexaPrefix));
	}

	/**
	 * 
	 * @param aHexaBuffer
	 * @param aThrowingException
	 * @return
	 * @throws IllegalArgumentException
	 */
	private static boolean testCharsOfHexaBufferBytes(final String aHexaBuffer,
			final boolean aThrowingException) throws IllegalArgumentException {
		int wMax = aHexaBuffer.length();
		char wChar;
		int wI = 0;
		while (wI < wMax) {
			wChar = aHexaBuffer.charAt(wI);
			if (HEXA_BUFFER_DIGITS.indexOf(wChar) < 0) {
				if (aThrowingException) {
					throwBadHexaBufferBytes("At the offset [" + wI
							+ "], the buffer contains the char [" + wChar
							+ "]  which is not a valid character (" + HEXA_PREFIX + ").");
				} else {
					return false;
				}
			}
			wI++;
		}
		return true;
	}

	/**
	 * 
	 * @param aHexaBuffer
	 * @param aThrowingException
	 * @return true si la chaine est de la forme ".x00.x01.xE5 etc..."
	 * @throws IllegalArgumentException
	 */
	private static boolean testHexaBufferBytes(final String aHexaBuffer,
			final boolean aThrowingException) throws IllegalArgumentException {
		if (aHexaBuffer == null) {
			if (aThrowingException) {
				throwBadHexaBufferBytes("The buffer is null.");
			} else {
				return false;
			}
		}
		if ((aHexaBuffer.length() % 4) != 0) {
			if (aThrowingException) {
				throwBadHexaBufferBytes("The buffer length is [" + aHexaBuffer.length() + "]");
			} else {
				return false;
			}
		}
		if (!aHexaBuffer.startsWith(HEXA_PREFIX)) {
			if (aThrowingException) {
				throwBadHexaBufferBytes("The buffer des not start with [" + HEXA_PREFIX + "].");
			} else {
				return false;
			}
		}

		return testCharsOfHexaBufferBytes(aHexaBuffer, aThrowingException);
	}

	/**
	 * @throws IllegalArgumentException
	 */
	private static void throwBadHexaBufferBytes(final String aMess) throws IllegalArgumentException {
		String wMess = "Hexa string length should be at least a multiple of 4 characters like \".xFF.x0A\";";
		if (aMess != null && aMess.length() > 0) {
			wMess += aMess;
		}
		throw new IllegalArgumentException(wMess);
	}

	/**
	 * @param b
	 * @return
	 */
	public static int unsignedByteToInt(final byte b) {
		return (b & 128) + (b & 127);
	}

	/**
	 * @param b
	 * @return
	 */
	public static long unsignedByteToLong(final byte b) {
		return (b & 128) + (b & 127);
	}

	/**
	 * Dummy constructor.
	 */
	private CXBytesUtils() {
	}

}