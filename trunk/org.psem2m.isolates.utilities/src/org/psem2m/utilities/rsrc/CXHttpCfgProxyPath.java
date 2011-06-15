package org.psem2m.utilities.rsrc;

// Classe - Http.conf Proxypath

public class CXHttpCfgProxyPath extends CXRsrcUriDir {

	// Constructeurs

	public CXHttpCfgProxyPath(String aPath) {
		super(aPath);
	}

	public CXHttpCfgProxyPath(CXHttpCfgProxyPath aPath) {
		super(aPath);
	}

	@Override
	public CXHttpCfgProxyPath clone() {
		return new CXHttpCfgProxyPath(this);
	}

	// Static

	public static CXHttpCfgProxyPath newInstance(String aPath, String aSuffix) {
		return newInstance(new CXRsrcUriDir(aPath), aSuffix);
	}

	public static CXHttpCfgProxyPath newInstance(CXRsrcUriDir aPath,
			String aSuffix) {
		return new CXHttpCfgProxyPath(aPath == null ? null : aPath.concat(
				aSuffix).getPath());
	}
}
