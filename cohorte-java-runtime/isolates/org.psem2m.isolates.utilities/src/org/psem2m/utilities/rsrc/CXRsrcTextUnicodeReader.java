package org.psem2m.utilities.rsrc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.Charset;

import org.psem2m.utilities.CXBytesUtils;

// Classe - Adresse web

/**
 * @author isan
 * 
 */
public class CXRsrcTextUnicodeReader {

	private final static int READ_BUF_SIZE = 0x8000;

	/**
	 * @param aBom
	 * @param aBomRef
	 * @return
	 */
	private static boolean checkBOM(byte[] aBom, byte[] aBomRef) {
		if (aBom == null || aBomRef == null || aBom.length < aBomRef.length) {
			return false;
		}
		int wI = 0;
		while (wI < aBomRef.length && aBom[wI] == aBomRef[wI]) {
			wI++;
		}
		return wI == aBomRef.length;
	}

	/**
	 * Code transpose de notepad++ pour detecter les UTF-8 sans BOM
	 * 
	 * @param aBuffer
	 * @param aUrlCnxEncoding
	 *            encoding renvoye par URL - On check d'abord le BOM puis on
	 *            choisit aUrlCnxEncoding si pas de BOM et si
	 *            aUrlCnxEncoding!=vide
	 * @param aDefCharset
	 * @return
	 */
	public static CXRsrcTextReadInfo determineEncoding(CXRsrcByteArray aBuffer,
			String aUrlCnxEncoding, Charset aDefCharset) {
		if (aBuffer == null) {
			return new CXRsrcTextReadInfo();
		}
		EXUnicodeEncoding eEncoding = null;
		boolean wHasBOM = false;
		if (aBuffer.getSize() > CXBytesUtils.BOM_SIZE_MAX) {
			byte wBOM[] = aBuffer.copyTo(new byte[CXBytesUtils.BOM_SIZE_MAX]);
			// Detection BOM standard
			if (checkBOM(wBOM, EXUnicodeEncoding.UTF_16BE.getBom())) {
				eEncoding = EXUnicodeEncoding.UTF_16BE;
			} else if (checkBOM(wBOM, EXUnicodeEncoding.UTF_16LE.getBom())) {
				eEncoding = EXUnicodeEncoding.UTF_16LE;
			} else if (checkBOM(wBOM, EXUnicodeEncoding.UTF_32BE.getBom())) {
				eEncoding = EXUnicodeEncoding.UTF_32BE;
			} else if (checkBOM(wBOM, EXUnicodeEncoding.UTF_32LE.getBom())) {
				eEncoding = EXUnicodeEncoding.UTF_32LE;
			} else if (checkBOM(wBOM, EXUnicodeEncoding.UTF_8.getBom())) {
				eEncoding = EXUnicodeEncoding.UTF_8;
			}
			wHasBOM = eEncoding != null;
		}
		if (!wHasBOM && aBuffer.checkUTF_8()) {
			// Detection sequence UTF-8 OK
			eEncoding = EXUnicodeEncoding.UTF_8;
		}
		if (!wHasBOM) {
			if (eEncoding != null) {
				// Sequence UTF-8 detectee
				return new CXRsrcTextReadInfo(aBuffer.toArray(), eEncoding.getEncoding(), false,
						aDefCharset);
			} else if (aUrlCnxEncoding != null && aUrlCnxEncoding.trim().isEmpty()) {
				// Encoding renvoye par URLConnexion
				return new CXRsrcTextReadInfo(aBuffer.toArray(), aUrlCnxEncoding, false,
						aDefCharset);
			} else {
				return new CXRsrcTextReadInfo(aBuffer.toArray(), null, false, aDefCharset);
			}
		} else {
			return new CXRsrcTextReadInfo(aBuffer.toArray(eEncoding.getBomLen()),
					eEncoding.getEncoding(), true, aDefCharset);
		}
	}

	/**
	 * methode publique pour etre utilise dans le bundle httpclient
	 * 
	 * @param aInputStream
	 * @param aUrlCnxEncoding
	 *            Lors de la lecture vie URLConnection on peut recuperer
	 *            l'encoding -> Si on trouve pas de BOM on prend cet encoding si
	 *            non vide
	 * @param aDefCharset
	 * @return
	 * @throws IOException
	 */
	public static CXRsrcTextReadInfo readAll(InputStream aInputStream, String aUrlCnxEncoding,
			Charset aDefCharset) throws IOException {
		if (aInputStream == null) {
			return new CXRsrcTextReadInfo();
		}
		CXRsrcByteArray wReadBytes = new CXRsrcByteArray();
		byte[] wReadBuffer = new byte[READ_BUF_SIZE];
		boolean wEof = false;
		do {
			int wReadSize = aInputStream.read(wReadBuffer, 0, READ_BUF_SIZE);
			wEof = wReadSize <= 0;
			if (!wEof) {
				wReadBytes.add(wReadBuffer, wReadSize);
			}
		} while (!wEof);
		return determineEncoding(wReadBytes, aUrlCnxEncoding, aDefCharset);
	}

	/**
	 * @param aUrlConnection
	 * @param aDefCharset
	 * @return
	 * @throws IOException
	 */
	public static CXRsrcTextReadInfo readAll(URLConnection aUrlConnection, Charset aDefCharset)
			throws IOException {
		InputStream wInputStream = null;
		try {
			wInputStream = aUrlConnection.getInputStream();
			return readAll(wInputStream, aUrlConnection.getContentEncoding(), aDefCharset);
		} catch (IOException e) {
			throw (e);
		} finally {
			if (wInputStream != null) {
				wInputStream.close();
			}
		}
	}

	/**
	 * @param path
	 * @param aDefCharset
	 * @return
	 * @throws IOException
	 */
	public static String readAllFile(String path, String aDefCharset) throws IOException {
		File f = new File(path);
		CXRsrcTextReadInfo ri = readAll(new FileInputStream(f), aDefCharset, null);
		return ri.getContent();
	}

}
