package org.psem2m.utilities.rsrc;

import org.psem2m.utilities.CXBase64Coder;
import org.psem2m.utilities.scripting.CXJsObjectBase;


public class CXHttpProxy extends CXJsObjectBase implements Cloneable {
	private String pHostName = null;
	private int pTcpPort = 0;
	private String pAuthUser = null;
	private String pAuthPassword = null;

	public CXHttpProxy(String aHostName, int aTcpPort, String aAuthUser,
			String aAuthPassword) {
		pHostName = aHostName.trim();
		pTcpPort = aTcpPort;
		pAuthUser = aAuthUser == null ? "" : aAuthUser.trim();
		pAuthPassword = aAuthPassword == null ? "" : aAuthPassword;
	}

	protected CXHttpProxy(CXHttpProxy aProxy) {
		super();
		if (aProxy != null) {
			pHostName = aProxy.pHostName;
			pTcpPort = aProxy.pTcpPort;
			pAuthUser = aProxy.pAuthUser;
			pAuthPassword = aProxy.pAuthPassword;
		}
	}

	@Override
	public CXHttpProxy clone() {
		return new CXHttpProxy(this);
	}

	public boolean isValid() {
		return pHostName != null && pHostName.length() != 0 && pTcpPort > 0;
	}

	public String getHostName() {
		return pHostName;
	}

	public String getTcpPortStr() {
		return String.valueOf(pTcpPort);
	}

	public int getTcpPort() {
		return pTcpPort;
	}

	public String getEncodedAuthorization() {
		if (!hasAuthentication())
			return null;

		StringBuilder wTmp = new StringBuilder(pAuthUser).append(":").append(
				pAuthPassword);
		wTmp.toString().getBytes();
		StringBuilder wSB = new StringBuilder();
		wSB.append("basic ").append(
				CXBase64Coder.encode(wTmp.toString().getBytes()));
		return wSB.toString();
	}

	public boolean hasAuthentication() {
		return pAuthUser != null && pAuthUser.length() != 0;
	}

	public String getAuthUser() {
		return pAuthUser;
	}

	public String getAuthPwd() {
		return pAuthPassword;
	}

	// Interface IXtdDescriber

	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = super.addDescriptionInBuffer(aSB);
		descrAddProp(aSB, "ProxyHostName", getHostName());
		descrAddLine(aSB, "ProxyPort", getTcpPort());
		descrAddProp(aSB, "ProxyAuthUser", getAuthUser());
		descrAddLine(aSB, "ProxyAuthPwd", getAuthPwd());
		return aSB;
	}
}
