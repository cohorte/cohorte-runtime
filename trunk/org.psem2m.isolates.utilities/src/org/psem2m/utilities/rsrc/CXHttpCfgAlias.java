package org.psem2m.utilities.rsrc;

// Classe - Http.conf alias

public class CXHttpCfgAlias extends CXRsrcUriDir {

	// Constructeur

	public CXHttpCfgAlias(String aPath) {
		super(aPath, true, false);
	}

	public CXHttpCfgAlias(CXHttpCfgAlias aPath) {
		super(aPath);
	}

	@Override
	public CXHttpCfgAlias clone() {
		return new CXHttpCfgAlias(this);
	}

	// Static

	public static CXHttpCfgAlias newInstance(String aPath, String aSuffix) {
		return newInstance(new CXRsrcUriDir(aPath), aSuffix);
	}

	public static CXHttpCfgAlias newInstance(CXRsrcUriDir aPath, String aSuffix) {
		return new CXHttpCfgAlias(aPath == null ? null : aPath.concat(aSuffix)
				.getPath());
	}
}
