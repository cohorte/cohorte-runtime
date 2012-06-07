package org.psem2m.utilities.rsrc;

public class CXHttpsAddress extends CXRsrcUrlAddress {
	public final static int DEF_PORT = 443;
	public final static String PROTOCOL = "https";
	public final static String PROTOCOL_URL = PROTOCOL + PROT_SEP;
	public final static int PROTOCOL_URL_LEN = PROTOCOL_URL.length();

	public CXHttpsAddress(String aDomainName, int aPort) {
		super(aDomainName, aPort);
	}

	protected CXHttpsAddress(CXHttpsAddress aAddr) {
		super(aAddr);
	}

	@Override
	public CXHttpsAddress clone() {
		return new CXHttpsAddress(this);
	}

	@Override
	protected int getDefaultPort() {
		return DEF_PORT;
	}

	@Override
	protected String getProtocol() {
		return PROTOCOL;
	}

	@Override
	public boolean isSecured() {
		return true;
	}
}
