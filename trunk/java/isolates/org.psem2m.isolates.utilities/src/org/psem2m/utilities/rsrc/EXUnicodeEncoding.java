package org.psem2m.utilities.rsrc;

import java.nio.charset.Charset;

public enum EXUnicodeEncoding implements IUnicodeEncoding {
	UTF_16BE(new byte[] { (byte) 0xFE, (byte) 0xFF },
			EXUnicodeEncoding.ENCODING_UTF_16BE), UTF_16LE(new byte[] {
			(byte) 0xFF, (byte) 0xFE }, EXUnicodeEncoding.ENCODING_UTF_16LE), UTF_32BE(
			new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF },
			EXUnicodeEncoding.ENCODING_UTF_32BE), UTF_32LE(new byte[] {
			(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00 },
			EXUnicodeEncoding.ENCODING_UTF_32LE), UTF_8(new byte[] {
			(byte) 0xEF, (byte) 0xBB, (byte) 0xBF },
			EXUnicodeEncoding.ENCODING_UTF_8);

	private byte[] pBom = null;
	private String pEncoding = null;
	private Charset pCharset = null;

	private EXUnicodeEncoding(byte[] aBom, String aEncoding) {
		pBom = aBom;
		pEncoding = aEncoding;
		pCharset = Charset.forName(pEncoding);
	}

	public String getEncoding() {
		return pEncoding;
	}

	public Charset getCharset() {
		return pCharset;
	}

	public byte[] getBom() {
		return pBom;
	}

	public int getBomLen() {
		return pBom == null ? 0 : pBom.length;
	}
}