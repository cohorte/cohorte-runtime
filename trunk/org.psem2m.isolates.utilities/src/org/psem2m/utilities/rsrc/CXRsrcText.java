package org.psem2m.utilities.rsrc;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

// Ressource de type text - Contenu jamais null

public class CXRsrcText extends CXRsrc<String> {
	private boolean pHasBOM = false;
	private String pEncoding = null;
	private String pContentType = null;

	// Constructors

	public CXRsrcText(CXRsrcUriPath aPath, CXRsrcTextReadInfo aContent) {
		this(aPath, aContent, 0);
	}

	public CXRsrcText(CXRsrcText aRsrc, String aNewContent) {
		super(aRsrc == null ? null : aRsrc.getPath(), aNewContent == null ? ""
				: aNewContent, aRsrc == null ? 0 : aRsrc.getTimeStampSyst());
		if (aRsrc != null) {
			pEncoding = aRsrc.pEncoding;
			pContentType = aRsrc.pContentType;
			pHasBOM = aRsrc.pHasBOM;
		}
	}

	public CXRsrcText(CXRsrcUriPath aPath, CXRsrcTextReadInfo aContent,
			long aTimeStampSyst) {
		super(aPath, aContent == null ? "" : aContent.getContent(),
				aTimeStampSyst);
		pHasBOM = aContent.hasBOM();
		pEncoding = aContent == null ? null : aContent.getEncoding();
		// Enconding par d�faut si null ou vide -> Utilis� pour transformer en
		// Bytes
		if (pEncoding == null)
			pEncoding = Charset.defaultCharset().name();
		if (aPath != null && aPath.hasMimeType()) {
			StringBuilder wSb = new StringBuilder(aPath.getMimeType()
					.getIdentifier());
			pContentType = wSb.append(";").append(pEncoding).toString();
		}
	}

	// Get and Set

	@Override
	public String getContentType() {
		return pContentType;
	}

	@Override
	public boolean isText() {
		return true;
	}

	@Override
	public byte[] getByteContent() throws UnsupportedEncodingException {
		return getContent() == null ? null : getContent().getBytes(pEncoding);
	}

	@Override
	public boolean isEmpty() {
		return getLength() == 0;
	}

	public String getEncoding() {
		return pEncoding;
	}

	public boolean hasBOM() {
		return pHasBOM;
	}

	@Override
	public int getLength() {
		String wCont = getContent();
		return wCont == null ? 0 : wCont.length();
	}

	// Description

	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = super.addDescriptionInBuffer(aSB);
		descrAddProp(aSB, "Length", getLength());
		descrAddProp(aSB, "Encoding", getEncoding());
		descrAddProp(aSB, "HasBOM", pHasBOM);
		return aSB;
	}
}
