package org.psem2m.utilities.rsrc;

public class CXHttpAddress extends CXRsrcUrlAddress {

	public static final int DEF_PORT = 80;
	public static final String PROTOCOL = "http";
	public static final String PROTOCOL_URL = PROTOCOL + PROT_SEP;
	public static final int PROTOCOL_URL_LEN = PROTOCOL_URL.length();

	/**
	 * @param aAddr
	 */
	protected CXHttpAddress(CXHttpAddress aAddr) {
		super(aAddr);
	}

	/**
	 * @param aDomainName
	 */
	public CXHttpAddress(String aDomainName) {
		super(aDomainName, DEF_PORT);
	}

	/**
	 * @param aDomainName
	 * @param aPort
	 */
	public CXHttpAddress(String aDomainName, int aPort) {
		super(aDomainName, aPort);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrcUrlAddress#clone()
	 */
	@Override
	public CXHttpAddress clone() {
		return new CXHttpAddress(this);
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
}
