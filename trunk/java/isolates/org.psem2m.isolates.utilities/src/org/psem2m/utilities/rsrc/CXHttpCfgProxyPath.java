package org.psem2m.utilities.rsrc;

/**
 * Classe - Http.conf Proxypath
 * 
 * @author ogattaz
 * 
 */
public class CXHttpCfgProxyPath extends CXRsrcUriDir {

	/**
	 * @param aPath
	 * @param aSuffix
	 * @return
	 */
	public static CXHttpCfgProxyPath newInstance(CXRsrcUriDir aPath, String aSuffix) {
		return new CXHttpCfgProxyPath(aPath == null ? null : aPath.concat(aSuffix).getPath());
	}

	/**
	 * @param aPath
	 * @param aSuffix
	 * @return
	 */
	public static CXHttpCfgProxyPath newInstance(String aPath, String aSuffix) {
		return newInstance(new CXRsrcUriDir(aPath), aSuffix);
	}

	/**
	 * @param aPath
	 */
	public CXHttpCfgProxyPath(CXHttpCfgProxyPath aPath) {
		super(aPath);
	}

	/**
	 * @param aPath
	 */
	public CXHttpCfgProxyPath(String aPath) {
		super(aPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrcUriDir#clone()
	 */
	@Override
	public CXHttpCfgProxyPath clone() {
		return new CXHttpCfgProxyPath(this);
	}
}
