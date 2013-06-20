package org.psem2m.utilities.rsrc;

/**
 * @author ogattaz
 * 
 */
public class CXHttpsAddress extends CXRsrcUrlAddress {

	public static final int DEF_PORT = 443;
	public static final String PROTOCOL = "https";
	public static final String PROTOCOL_URL = PROTOCOL + PROT_SEP;
	public static final int PROTOCOL_URL_LEN = PROTOCOL_URL.length();

	/**
	 * @param aAddr
	 */
	protected CXHttpsAddress(CXHttpsAddress aAddr) {
		super(aAddr);
	}

	/**
	 * @param aDomainName
	 * @param aPort
	 */
	public CXHttpsAddress(String aDomainName, int aPort) {
		super(aDomainName, aPort);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrcUrlAddress#clone()
	 */
	@Override
	public CXHttpsAddress clone() {
		return new CXHttpsAddress(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrcUrlAddress#getDefaultPort()
	 */
	@Override
	protected int getDefaultPort() {
		return DEF_PORT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrcUrlAddress#getProtocol()
	 */
	@Override
	protected String getProtocol() {
		return PROTOCOL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrcUrlAddress#isSecured()
	 */
	@Override
	public boolean isSecured() {
		return true;
	}
}
