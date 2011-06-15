package org.psem2m.utilities.rsrc;

import org.psem2m.utilities.scripting.CXJsObjectBase;

// Classe - Adresse web

public abstract class CXRsrcUrlAddress extends CXJsObjectBase implements Cloneable {
	public final static String PROT_SEP = "://";
	public final static String PORT_SEP = ":";
	public final static char URL_SEP = '/';

	private String pDomainName = null;
	private int pTcpPort = 0;
	private String pAddress = null;

	protected CXRsrcUrlAddress(CXRsrcUrlAddress aAddr) {
		super();
		if (aAddr != null) {
			pDomainName = aAddr.pDomainName;
			pTcpPort = aAddr.pTcpPort;
			pAddress = aAddr.pAddress;
		}
	}

	public CXRsrcUrlAddress(String aDomainName, int aPort) {
		pDomainName = aDomainName != null && aDomainName.trim().length() != 0 ? aDomainName
				: null;
		pTcpPort = aPort;
		if (pDomainName != null && pTcpPort > 0) {
			StringBuilder wTmp = new StringBuilder(getProtocol()).append(
					PROT_SEP).append(pDomainName);
			if (pTcpPort != getDefaultPort())
				wTmp.append(PORT_SEP).append(String.valueOf(pTcpPort));
			pAddress = wTmp.toString();
		}
	}

	@Override
	public abstract CXRsrcUrlAddress clone();

	public boolean isValid() {
		return pAddress != null && pAddress.length() != 0;
	}

	public boolean isSecured() {
		return false;
	}

	public String getDomainName() {
		return isValid() ? pDomainName : "";
	}

	public int getTcpPort() {
		return isValid() ? pTcpPort : 0;
	}

	// http://hostname:port
	public String getAddress() {
		return pAddress;
	}

	public String getUrlPath(String aPath) {
		if (aPath == null || aPath.isEmpty())
			return pAddress;
		return new StringBuilder().append(pAddress).append(aPath).toString();
	}

	public String getUrlPath(CXRsrcUriPath aResourcePath) {
		if (aResourcePath == null)
			return pAddress;
		return new StringBuilder().append(pAddress)
				.append(aResourcePath.getFullPath()).toString();
	}

	public String getUrlPath(CXRsrcUriDir aDir) {
		if (aDir == null || aDir.isEmpty())
			return pAddress;
		return new StringBuilder().append(pAddress)
				.append(aDir.getPath(true, true)).toString();
	}

	public String getUrlPath(CXRsrcUriDir aDir, String aResourceId) {
		if (aDir == null || aDir.isEmpty())
			return getAddress();
		return new StringBuilder().append(pAddress)
				.append(aDir.getUrlPath(aResourceId)).toString();
	}

	protected abstract int getDefaultPort();

	protected abstract String getProtocol();

	// Interface IXtdDescriber

	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = super.addDescriptionInBuffer(aSB);
		descrAddProp(aSB, "DomainName", pDomainName == null ? DESCR_NONE
				: pDomainName);
		descrAddProp(aSB, "TcpPort", pTcpPort);
		descrAddProp(aSB, "Address", pAddress == null ? DESCR_NONE : pAddress);
		return aSB;
	}
}
