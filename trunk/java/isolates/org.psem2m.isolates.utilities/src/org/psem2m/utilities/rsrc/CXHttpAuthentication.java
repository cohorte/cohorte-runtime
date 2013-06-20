package org.psem2m.utilities.rsrc;

import org.psem2m.utilities.scripting.CXJsObjectBase;

/**
 * @author ogattaz
 * 
 */
public abstract class CXHttpAuthentication extends CXJsObjectBase implements Cloneable {

	/**
	 * @param aType
	 * @param aUser
	 * @param aPwd
	 * @return
	 */
	public static CXHttpAuthentication newAuthentication(String aType, String aUser, String aPwd) {
		CXHttpAuthentication wResult = null;
		if (aType != null) {
			if (aType.equalsIgnoreCase(CXHttpAuthentBasic.AUTHENT_TYPE)) {
				wResult = new CXHttpAuthentBasic(aUser, aPwd);
			}
		}
		return wResult;
	}

	private String pAuthentication = null;
	private String pPassword = null;
	private String pUser = null;

	/**
	 * @param aAuthent
	 */
	protected CXHttpAuthentication(CXHttpAuthentication aAuthent) {
		super();
		if (aAuthent != null) {
			pAuthentication = aAuthent.pAuthentication;
			pUser = aAuthent.pUser;
			pPassword = aAuthent.pPassword;
		}
	}

	/**
	 * @param aAuthentication
	 * @param aUser
	 * @param aPassword
	 */
	public CXHttpAuthentication(String aAuthentication, String aUser, String aPassword) {
		pAuthentication = aAuthentication;
		pUser = aUser == null ? "" : aUser;
		pPassword = aPassword == null ? "" : aPassword;
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
		descrAddProp(aSB, "Authentication", pAuthentication);
		descrAddProp(aSB, "AuthUser", pUser);
		descrAddProp(aSB, "AuthPwd", pPassword);
		return aSB;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public abstract CXHttpAuthentication clone();

	/**
	 * @return
	 */
	public String getAuthentication() {
		return pAuthentication;
	}

	/**
	 * Entete Http : Authorization: Basic YWRvbml4OmFkb25peA==
	 * 
	 * @return
	 */
	public abstract String getEncodedAuthorization();

	/**
	 * @return
	 */
	public String getPwd() {
		return pPassword;
	}

	/**
	 * @return
	 */
	public String getUser() {
		return pUser;
	}

	/**
	 * @return
	 */
	public boolean hasAuthentication() {
		return isValid() && pUser != null && pUser.length() != 0;
	}

	// Get and Set
	/**
	 * @return
	 */
	public boolean isValid() {
		return pAuthentication != null && pAuthentication.length() != 0;
	}
}
