package org.psem2m.utilities.rsrc;

/**
 * Classe - Http.conf alias
 * 
 * @author ogattaz
 * 
 */
public class CXHttpCfgAlias extends CXRsrcUriDir {

	/**
	 * @param aPath
	 * @param aSuffix
	 * @return
	 */
	public static CXHttpCfgAlias newInstance(CXRsrcUriDir aPath, String aSuffix) {
		return new CXHttpCfgAlias(aPath == null ? null : aPath.concat(aSuffix).getPath());
	}

	/**
	 * @param aPath
	 * @param aSuffix
	 * @return
	 */
	public static CXHttpCfgAlias newInstance(String aPath, String aSuffix) {
		return newInstance(new CXRsrcUriDir(aPath), aSuffix);
	}

	/**
	 * @param aPath
	 */
	public CXHttpCfgAlias(CXHttpCfgAlias aPath) {
		super(aPath);
	}

	/**
	 * @param aPath
	 */
	public CXHttpCfgAlias(String aPath) {
		super(aPath, true, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrcUriDir#clone()
	 */
	@Override
	public CXHttpCfgAlias clone() {
		return new CXHttpCfgAlias(this);
	}
}
