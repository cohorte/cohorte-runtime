package org.psem2m.utilities.rsrc;

import org.psem2m.utilities.CXBase64Coder;

/**
 * Class Authentication Basic
 * 
 * @author ogattaz
 * 
 */
public class CXHttpAuthentBasic extends CXHttpAuthentication {

	public static final String AUTHENT_TYPE = "Basic";

	/**
	 * @param aAuthent
	 */
	protected CXHttpAuthentBasic(CXHttpAuthentication aAuthent) {
		this(aAuthent.getUser(), aAuthent.getPwd());
	}

	/**
	 * @param aUser
	 * @param aPassword
	 */
	public CXHttpAuthentBasic(String aUser, String aPassword) {
		super(AUTHENT_TYPE, aUser, aPassword);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXHttpAuthentication#clone()
	 */
	@Override
	public CXHttpAuthentBasic clone() {
		return new CXHttpAuthentBasic(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.rsrc.CXHttpAuthentication#getEncodedAuthorization()
	 */
	@Override
	public String getEncodedAuthorization() {

		if (!hasAuthentication()) {
			return null;
		}

		StringBuilder wTmp = new StringBuilder(getUser()).append(":").append(getPwd());
		return new StringBuilder(AUTHENT_TYPE).append(" ")
				.append(CXBase64Coder.encode(wTmp.toString().getBytes())).toString();

	}
}
