package org.psem2m.utilities.rsrc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * Class Http server
 * 
 * @author ogattaz
 * 
 */
public class CXRsrcProviderHttp extends CXRsrcProvider {

	/**
	 * aFullAddress : http://host:port
	 * 
	 * @param aFullAddress
	 * @param aDefCharset
	 * @return
	 * @throws Exception
	 */
	public static CXRsrcProviderHttp newHttpServerFromUrl(String aFullAddress, Charset aDefCharset)
			throws Exception {
		CXRsrcProviderHttp wSrv = null;
		if (aFullAddress != null) {
			String wHost = null;
			int wPort = 0;
			String wTmp = null;
			boolean wSecured = false;
			if (aFullAddress.startsWith(CXHttpAddress.PROTOCOL_URL)) {
				wSecured = false;
				wTmp = aFullAddress.substring(CXHttpAddress.PROTOCOL_URL_LEN);
			} else if (aFullAddress.startsWith(CXHttpsAddress.PROTOCOL_URL)) {
				wSecured = true;
				wTmp = aFullAddress.substring(CXHttpsAddress.PROTOCOL_URL_LEN);
			}
			if (wTmp != null && wTmp.length() != 0) {
				int wIdx = wTmp.indexOf(CXRsrcUrlAddress.PORT_SEP);
				if (wIdx != -1) {
					wHost = wTmp.substring(0, wIdx);
					wIdx++;
					if (wIdx < wTmp.length()) {
						int wIdxPath = wTmp.indexOf(CXRsrcUrlAddress.URL_SEP);
						String wPortStr = wIdxPath == -1 ? wTmp.substring(wIdx) : wTmp.substring(
								wIdx, wIdxPath);
						try {
							wPort = Integer.parseInt(wPortStr);
						} catch (Exception e) {
							throw new Exception("Bad tcp port number [" + aFullAddress + "]");
						}
					}
				} else {
					int wIdxPath = wTmp.indexOf(CXRsrcUrlAddress.URL_SEP);
					wHost = wIdxPath == -1 ? wTmp : wTmp.substring(0, wIdxPath);
				}
			}
			// System.out.println("newHttpServerFromUrl - Host["+wHost+"] - Port["+wPort+"] - Url["
			// + aFullAddress + "]");
			if (wHost != null) {
				wSrv = new CXRsrcProviderHttp(wHost, wPort, wSecured, aDefCharset);
			}
		}
		if (wSrv == null || !wSrv.isValid()) {
			throw new Exception("Bad server address [" + aFullAddress + "]");
		}
		return wSrv;
	}

	private CXRsrcUrlAddress pAddress = null;
	private CXHttpAuthentication pAuthentication = null;
	private CXHttpProxy pHttpProxy = null;

	/**
	 * @param aProv
	 */
	protected CXRsrcProviderHttp(CXRsrcProviderHttp aProv) {
		super(aProv);
		if (aProv != null) {
			pAddress = aProv.pAddress != null ? aProv.pAddress.clone() : null;
			pAuthentication = aProv.pAuthentication != null ? aProv.pAuthentication.clone() : null;
			pHttpProxy = aProv.pHttpProxy != null ? aProv.pHttpProxy.clone() : null;
		}
	}

	/**
	 * @param aAddress
	 * @param aProxy
	 * @param aAuthentication
	 * @param aDefCharset
	 */
	public CXRsrcProviderHttp(CXRsrcUrlAddress aAddress, CXHttpProxy aProxy,
			CXHttpAuthentication aAuthentication, Charset aDefCharset) {
		super(aDefCharset);
		pAddress = aAddress != null ? aAddress.clone() : null;
		pAuthentication = aAuthentication != null ? aAuthentication.clone() : null;
		pHttpProxy = aProxy != null ? aProxy.clone() : null;
	}

	/**
	 * @param aHostName
	 * @param aSecured
	 * @param aDefCharset
	 */
	public CXRsrcProviderHttp(String aHostName, boolean aSecured, Charset aDefCharset) {
		this(aHostName, 0, aSecured, aDefCharset);
	}

	/**
	 * @param aHostName
	 * @param aDefCharset
	 */
	public CXRsrcProviderHttp(String aHostName, Charset aDefCharset) {
		this(aHostName, CXHttpAddress.DEF_PORT, aDefCharset);
	}

	/**
	 * @param aHostName
	 * @param aPort
	 * @param aSecured
	 * @param aDefCharset
	 */
	public CXRsrcProviderHttp(String aHostName, int aPort, boolean aSecured, Charset aDefCharset) {
		this(null, null, null, aDefCharset);
		boolean wSecured = (aSecured && aPort != CXHttpAddress.DEF_PORT)
				|| aPort == CXHttpsAddress.DEF_PORT;
		int wPort = aPort > 0 ? aPort : (wSecured ? CXHttpsAddress.DEF_PORT
				: CXHttpAddress.DEF_PORT);
		setAddress(wSecured ? new CXHttpsAddress(aHostName, wPort) : new CXHttpAddress(aHostName,
				wPort));
	}

	/**
	 * @param aHostName
	 * @param aPort
	 * @param aDefCharset
	 */
	public CXRsrcProviderHttp(String aHostName, int aPort, Charset aDefCharset) {
		this(aHostName, aPort, false, aDefCharset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.rsrc.CXRsrcProvider#addDescriptionInBuffer(java.
	 * lang.Appendable)
	 */
	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		StringBuilder wSB = new StringBuilder();
		descrAddLine(wSB, "Address", pAddress == null ? "" : pAddress.getAddress());
		super.addDescriptionInBuffer(wSB);
		descrAddLine(wSB, "Authentication", hasAuthentication() ? "yes" : "no");
		if (hasAuthentication()) {
			descrAddLine(wSB, "Authentication", getAuthentication().toString());
		}
		descrAddLine(wSB, "Proxy", hasProxy() ? "yes" : "no");
		if (hasProxy()) {
			descrAddLine(wSB, getProxy().toString());
		}
		return descrAddLine(aSB, wSB);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrcProvider#clone()
	 */
	@Override
	public CXRsrcProviderHttp clone() {
		return new CXRsrcProviderHttp(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.rsrc.CXRsrcProvider#existsFulPath(org.psem2m.utilities
	 * .rsrc.CXRsrcUriPath)
	 */
	@Override
	protected boolean existsFulPath(CXRsrcUriPath aPath) {
		try {
			URLConnection wCnx = openConnection(urlNew(aPath));
			return ((HttpURLConnection) wCnx).getResponseCode() == HttpURLConnection.HTTP_OK;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * @return
	 */
	public CXRsrcUrlAddress getAddress() {
		return pAddress;
	}

	/**
	 * @return
	 */
	public CXHttpAuthentication getAuthentication() {
		return pAuthentication;
	}

	/**
	 * @return
	 */
	public CXHttpProxy getProxy() {
		return pHttpProxy;
	}

	/**
	 * @return
	 */
	public boolean hasAuthentication() {
		return pAuthentication != null && pAuthentication.isValid();
	}

	/**
	 * @return
	 */
	public boolean hasProxy() {
		return pHttpProxy != null && pHttpProxy.isValid();
	}

	/*
	 * True si acces fichier en local du serveur - False si access remote (http)
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrcProvider#isLocal()
	 */
	@Override
	public boolean isLocal() {
		return false;
	}

	/**
	 * @return
	 */
	public boolean isSecured() {
		return pAddress == null ? false : pAddress.isSecured();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrcProvider#isValid()
	 */
	@Override
	public boolean isValid() {
		return super.isValid() && pAddress != null && pAddress.isValid();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.rsrc.CXRsrcProvider#openConnection(java.net.URL)
	 */
	@Override
	protected URLConnection openConnection(URL aUrl) throws IOException {
		URLConnection wCnx = super.openConnection(aUrl);
		Properties wSysProp = System.getProperties();
		if (hasProxy()) {
			wSysProp.put("http.proxyHost", pHttpProxy.getHostName());
			wSysProp.put("http.proxyPort", pHttpProxy.getTcpPortStr());
			if (pHttpProxy.hasAuthentication()) {
				wCnx.setRequestProperty("Proxy-Authorization", pHttpProxy.getEncodedAuthorization());
			}
		} else {
			wSysProp.put("http.proxyHost", "");
			wSysProp.put("http.proxyPort", "");
		}
		if (hasAuthentication()) {
			wCnx.setRequestProperty("Authorization", pAuthentication.getEncodedAuthorization());
		}
		return wCnx;
	}

	/**
	 * @param aAddress
	 */
	public void setAddress(CXRsrcUrlAddress aAddress) {
		pAddress = aAddress;
	}

	/**
	 * @param aAuthentication
	 */
	public void setAuthentication(CXHttpAuthentication aAuthentication) {
		pAuthentication = aAuthentication;
	}

	/**
	 * Interface IXtdDescriber
	 * 
	 * @param aHttpProxy
	 */
	public void setProxy(CXHttpProxy aHttpProxy) {
		pHttpProxy = aHttpProxy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrcProvider#urlGetAddress()
	 */
	@Override
	public String urlGetAddress() {
		return pAddress == null ? null : pAddress.getAddress();
	}
}
