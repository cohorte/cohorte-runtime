package org.psem2m.utilities.rsrc;

import java.nio.charset.Charset;

import org.psem2m.utilities.CXBytesUtils;

/**
 * @author ogattaz
 * 
 */
public enum EXUnicodeEncoding {
	UTF_16BE(CXBytesUtils.BOM_UTF_16BE, CXBytesUtils.ENCODING_UTF_16BE), UTF_16LE(
			CXBytesUtils.BOM_UTF_16LE, CXBytesUtils.ENCODING_UTF_16LE), UTF_32BE(
			CXBytesUtils.BOM_UTF_32BE, CXBytesUtils.ENCODING_UTF_32BE), UTF_32LE(
			CXBytesUtils.BOM_UTF_32LE, CXBytesUtils.ENCODING_UTF_32LE), UTF_8(
			CXBytesUtils.BOM_UTF_8, CXBytesUtils.ENCODING_UTF_8);

	private byte[] pBom = null;
	private Charset pCharset = null;
	private String pEncoding = null;

	/**
	 * @param aBom
	 * @param aEncoding
	 */
	private EXUnicodeEncoding(byte[] aBom, String aEncoding) {
		pBom = aBom.clone();
		pEncoding = aEncoding;
		pCharset = Charset.forName(pEncoding);
	}

	public byte[] getBom() {
		return pBom.clone();
	}

	public int getBomLen() {
		return pBom == null ? 0 : pBom.length;
	}

	public Charset getCharset() {
		return pCharset;
	}

	public String getEncoding() {
		return pEncoding;
	}
}