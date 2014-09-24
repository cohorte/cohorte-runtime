package org.psem2m.utilities.rsrc;

/**
 * Ressource de type bytes - Contenu jamais null
 * 
 * @author ogattaz
 * 
 */
public class CXRsrcByte extends CXRsrc<byte[]> {
	public final static String MIMETYPE_BYTES = "application/octet-stream";

	/**
	 * @param aPath
	 * @param aContent
	 * @param aTimeStampSyst
	 */
	public CXRsrcByte(CXRsrcUriPath aPath, byte[] aContent, long aTimeStampSyst) {
		super(aPath, aContent == null ? new byte[0] : aContent, aTimeStampSyst);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.rsrc.CXRsrc#addDescriptionInBuffer(java.lang.Appendable
	 * )
	 */
	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = super.addDescriptionInBuffer(aSB);
		descrAddProp(aSB, "Length", getLength());
		return aSB;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrc#getByteContent()
	 */
	@Override
	public byte[] getByteContent() {
		return getContent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrc#getContentType()
	 */
	@Override
	public String getContentType() {
		return hasMimeType() ? getMimeType().getIdentifier() : MIMETYPE_BYTES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrc#getLength()
	 */
	@Override
	public int getLength() {
		return getContent().length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrc#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return getContent().length != 0;
	}
}
