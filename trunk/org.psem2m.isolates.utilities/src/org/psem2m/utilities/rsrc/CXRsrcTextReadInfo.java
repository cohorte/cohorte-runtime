package org.psem2m.utilities.rsrc;

import java.nio.charset.Charset;

// Classe - Retour lecture d'une texte par ReaderInfo
// -> Contenu et Charset jamais null

public class CXRsrcTextReadInfo {
	private final static Charset sCharsetUtf8 = Charset.forName("UTF-8");
	private Charset pCharset = null;
	private String pContent = null;
	private boolean pHasBOM = false;

	public CXRsrcTextReadInfo() {
		this(null, null, false, null);
	}

	public CXRsrcTextReadInfo(byte[] aContent, String aEncoding,
			boolean aHasBOM, Charset aDefCharset) {
		pHasBOM = aHasBOM;
		pCharset = encoding2Charset(aEncoding, aDefCharset);
		// Charset pour ne pas avoir de throw exception
		pContent = aContent == null ? "" : new String(aContent, pCharset);
	}

	private CXRsrcTextReadInfo(String javaString) {
		pHasBOM = false;
		pCharset = sCharsetUtf8;
		pContent = javaString == null ? "" : javaString;
	}

	public String getContent() {
		return pContent;
	}

	public Charset getCharset() {
		return pCharset;
	}

	public String getEncoding() {
		return pCharset.displayName();
	}

	public boolean hasBOM() {
		return pHasBOM;
	}

	public int getLength() {
		return pContent.length();
	}

	public boolean isEmpty() {
		return pContent.length() == 0;
	}

	public static Charset encoding2Charset(String aEncoding, Charset aDefCharset) {
		if (aDefCharset == null)
			aDefCharset = Charset.defaultCharset();
		if (aEncoding == null)
			return aDefCharset;
		aEncoding = aEncoding.trim();
		if (aEncoding.isEmpty())
			return aDefCharset;
		if (Charset.isSupported(aEncoding))
			return Charset.forName(aEncoding);
		return aDefCharset;
	}

	public static CXRsrcTextReadInfo newInstanceFromString(String javaString) {
		return new CXRsrcTextReadInfo(javaString);
	}
}
