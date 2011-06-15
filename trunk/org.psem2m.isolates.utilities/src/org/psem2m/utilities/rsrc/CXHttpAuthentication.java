package org.psem2m.utilities.rsrc;

import org.psem2m.utilities.scripting.CXJsObjectBase;

public abstract class CXHttpAuthentication extends CXJsObjectBase implements
		Cloneable {

	private String pAuthentication = null;
	private String pUser = null;
	private String pPassword = null;

	// Constructeurs

	public CXHttpAuthentication(String aAuthentication, String aUser,
			String aPassword) {
		pAuthentication = aAuthentication;
		pUser = aUser == null ? "" : aUser;
		pPassword = aPassword == null ? "" : aPassword;
	}

	protected CXHttpAuthentication(CXHttpAuthentication aAuthent) {
		super();
		if (aAuthent != null) {
			pAuthentication = aAuthent.pAuthentication;
			pUser = aAuthent.pUser;
			pPassword = aAuthent.pPassword;
		}
	}

	@Override
	public abstract CXHttpAuthentication clone();

	// Get and Set
	public boolean isValid() {
		return pAuthentication != null && pAuthentication.length() != 0;
	}

	// Entete Http : Authorization: Basic YWRvbml4OmFkb25peA==
	public abstract String getEncodedAuthorization();

	public boolean hasAuthentication() {
		return isValid() && pUser != null && pUser.length() != 0;
	}

	public String getAuthentication() {
		return pAuthentication;
	}

	public String getUser() {
		return pUser;
	}

	public String getPwd() {
		return pPassword;
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = super.addDescriptionInBuffer(aSB);
		descrAddProp(aSB, "Authentication", pAuthentication);
		descrAddProp(aSB, "AuthUser", pUser);
		descrAddProp(aSB, "AuthPwd", pPassword);
		return aSB;
	}

	public static CXHttpAuthentication newAuthentication(String aType,
			String aUser, String aPwd) {
		CXHttpAuthentication wResult = null;
		if (aType != null) {
			if (aType.equalsIgnoreCase(CXHttpAuthentBasic.AUTHENT_TYPE))
				wResult = new CXHttpAuthentBasic(aUser, aPwd);
		}
		return wResult;
	}
}
