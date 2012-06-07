package org.psem2m.utilities.rsrc;

public class CXHttpAddress extends CXRsrcUrlAddress {
	public final static int DEF_PORT = 80;
	public final static String PROTOCOL = "http";
	public final static String PROTOCOL_URL = PROTOCOL + PROT_SEP;
	public final static int PROTOCOL_URL_LEN = PROTOCOL_URL.length();

	public CXHttpAddress(String aDomainName) {
		super(aDomainName, DEF_PORT);
	}

	public CXHttpAddress(String aDomainName, int aPort) {
		super(aDomainName, aPort);
	}

	protected CXHttpAddress(CXHttpAddress aAddr) {
		super(aAddr);
	}

	@Override
	public CXHttpAddress clone() {
		return new CXHttpAddress(this);
	}

	@Override
	protected int getDefaultPort() {
		return DEF_PORT;
	}

	@Override
	protected String getProtocol() {
		return PROTOCOL;
	}
}
