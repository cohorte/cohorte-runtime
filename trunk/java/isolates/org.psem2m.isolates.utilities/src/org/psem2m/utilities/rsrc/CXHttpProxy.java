package org.psem2m.utilities.rsrc;

import org.psem2m.utilities.CXBase64Coder;
import org.psem2m.utilities.scripting.CXJsObjectBase;

/**
 * @author ogattaz
 * 
 */
public class CXHttpProxy extends CXJsObjectBase implements Cloneable {

	private String pAuthPassword = null;
	private String pAuthUser = null;
	private String pHostName = null;
	private int pTcpPort = 0;

	/**
	 * @param aProxy
	 */
	protected CXHttpProxy(CXHttpProxy aProxy) {
		super();
		if (aProxy != null) {
			pHostName = aProxy.pHostName;
			pTcpPort = aProxy.pTcpPort;
			pAuthUser = aProxy.pAuthUser;
			pAuthPassword = aProxy.pAuthPassword;
		}
	}

	/**
	 * @param aHostName
	 * @param aTcpPort
	 * @param aAuthUser
	 * @param aAuthPassword
	 */
	public CXHttpProxy(String aHostName, int aTcpPort, String aAuthUser, String aAuthPassword) {
		pHostName = aHostName.trim();
		pTcpPort = aTcpPort;
		pAuthUser = aAuthUser == null ? "" : aAuthUser.trim();
		pAuthPassword = aAuthPassword == null ? "" : aAuthPassword;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.scripting.CXJsObjectBase#addDescriptionInBuffer(
	 * java.lang.Appendable)
	 */
	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = super.addDescriptionInBuffer(aSB);
		descrAddProp(aSB, "ProxyHostName", getHostName());
		descrAddLine(aSB, "ProxyPort", getTcpPort());
		descrAddProp(aSB, "ProxyAuthUser", getAuthUser());
		descrAddLine(aSB, "ProxyAuthPwd", getAuthPwd());
		return aSB;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public CXHttpProxy clone() {
		return new CXHttpProxy(this);
	}

	/**
	 * @return
	 */
	public String getAuthPwd() {
		return pAuthPassword;
	}

	/**
	 * @return
	 */
	public String getAuthUser() {
		return pAuthUser;
	}

	/**
	 * @return
	 */
	public String getEncodedAuthorization() {
		if (!hasAuthentication()) {
			return null;
		}

		StringBuilder wTmp = new StringBuilder(pAuthUser).append(":").append(pAuthPassword);
		wTmp.toString().getBytes();
		StringBuilder wSB = new StringBuilder();
		wSB.append("basic ").append(CXBase64Coder.encode(wTmp.toString().getBytes()));
		return wSB.toString();
	}

	/**
	 * @return
	 */
	public String getHostName() {
		return pHostName;
	}

	/**
	 * @return
	 */
	public int getTcpPort() {
		return pTcpPort;
	}

	/**
	 * @return
	 */
	public String getTcpPortStr() {
		return String.valueOf(pTcpPort);
	}

	/**
	 * @return
	 */
	public boolean hasAuthentication() {
		return pAuthUser != null && pAuthUser.length() != 0;
	}

	/**
	 * @return
	 */
	public boolean isValid() {
		return pHostName != null && pHostName.length() != 0 && pTcpPort > 0;
	}
}
