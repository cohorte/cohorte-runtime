package org.psem2m.utilities.rsrc;

import org.psem2m.utilities.CXBase64Coder;

// Class Authentication Basic

public class CXHttpAuthentBasic extends CXHttpAuthentication {
	public final static String AUTHENT_TYPE = "Basic";

	// Constructeurs

	public CXHttpAuthentBasic(String aUser, String aPassword) {
		super(AUTHENT_TYPE, aUser, aPassword);
	}

	protected CXHttpAuthentBasic(CXHttpAuthentication aAuthent) {
		this(aAuthent.getUser(), aAuthent.getPwd());
	}

	@Override
	public CXHttpAuthentBasic clone() {
		return new CXHttpAuthentBasic(this);
	}

	@Override
	public String getEncodedAuthorization() {

		if (!hasAuthentication())
			return null;

		StringBuilder wTmp = new StringBuilder(getUser()).append(":").append(
				getPwd());
		return new StringBuilder(AUTHENT_TYPE).append(" ")
				.append(CXBase64Coder.encode(wTmp.toString().getBytes()))
				.toString();

	}
}
