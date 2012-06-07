package org.psem2m.utilities.rsrc;

import java.io.UnsupportedEncodingException;

import org.psem2m.utilities.scripting.CXJsObjectBase;

// Ressource 

public abstract class CXRsrc<ClassContent> extends CXJsObjectBase {

	// Contenu
	private ClassContent pContent = null;
	// file://fdalbop/d$/Public/ex3subkey.ini ou
	// http://intradon.ech.adx:1898/index.html
	private CXRsrcUriPath pPath = null;
	private long pSystTimeStamp = 0;

	public CXRsrc(CXRsrcUriPath aPath, ClassContent aContent) {
		this(aPath, aContent, 0);
	}

	public CXRsrc(CXRsrcUriPath aPath, ClassContent aContent,
			long aSystTimeStamp) {
		super();
		pPath = aPath;
		pContent = aContent;
		pSystTimeStamp = aSystTimeStamp;
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = aSB == null ? new StringBuilder(512) : aSB;
		if (pPath != null)
			descrAddProp(aSB, "FileName", pPath.getName());
		descrAddProp(aSB, "ContenType", hasContentType() ? DESCR_NONE
				: getContentType());
		descrAddProp(aSB, "SystTimeStamp", pSystTimeStamp);
		descrAddProp(aSB, "Empty", isEmpty());
		descrAddProp(aSB, "Class", getClass().getSimpleName());
		return aSB;
	}

	// Get and Set

	public int compareTimeStampSyst(CXRsrc<ClassContent> aRsrc) {
		if (aRsrc == null || !aRsrc.hasTimeStampSyst()
				|| !this.hasTimeStampSyst())
			return -1;
		return new Long(this.getTimeStampSyst()).compareTo(new Long(aRsrc
				.getTimeStampSyst()));
	}

	public void destroy() {
		pContent = null;
		pPath = null;
	}

	public boolean equals(CXRsrc<ClassContent> aRsrc) {
		assert false : "Cette methode doit etre corrigee";
		// TODO - X - comparer le path
		if (aRsrc == null)
			return false;
		if (compareTimeStampSyst(aRsrc) != 0)
			return false;
		return this.getContent().equals(aRsrc.getContent());
	}

	public abstract byte[] getByteContent() throws UnsupportedEncodingException;

	public ClassContent getContent() {
		return pContent;
	}

	public abstract String getContentType();

	public String getFullPath() {
		return pPath == null ? "" : pPath.getFullPath();
	}

	public abstract int getLength();

	public CXMimeType getMimeType() {
		return hasMimeType() ? pPath.getMimeType() : null;
	}

	public CXRsrcUriPath getPath() {
		return pPath;
	}

	public long getTimeStampSyst() {
		return pSystTimeStamp;
	}

	public boolean hasContentType() {
		return getContentType() != null;
	}

	public boolean hasMimeType() {
		return pPath != null ? pPath.hasMimeType() : false;
	}

	public boolean hasTimeStampSyst() {
		return pSystTimeStamp > 0;
	}

	public boolean isEmpty() {
		return pContent == null;
	}

	// Description

	public boolean isText() {
		CXMimeType wMime = pPath != null ? pPath.getMimeType() : null;
		return wMime != null ? wMime.isText() : false;
	}
}
