package org.psem2m.utilities.rsrc;

// Ressource de type bytes - Contenu jamais null

public class CXRsrcByte extends CXRsrc<byte[]> {
	public final static String MIMETYPE_BYTES = "application/octet-stream";

	public CXRsrcByte(CXRsrcUriPath aPath, byte[] aContent, long aTimeStampSyst) {
		super(aPath, aContent == null ? new byte[0] : aContent, aTimeStampSyst);
	}

	// Get and Set

	@Override
	public boolean isEmpty() {
		return getContent().length != 0;
	}

	@Override
	public int getLength() {
		return getContent().length;
	}

	@Override
	public String getContentType() {
		return hasMimeType() ? getMimeType().getIdentifier() : MIMETYPE_BYTES;
	}

	@Override
	public byte[] getByteContent() {
		return getContent();
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = super.addDescriptionInBuffer(aSB);
		descrAddProp(aSB, "Length", getLength());
		return aSB;
	}
}
