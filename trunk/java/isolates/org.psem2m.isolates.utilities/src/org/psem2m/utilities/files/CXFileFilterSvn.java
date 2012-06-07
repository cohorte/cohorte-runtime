package org.psem2m.utilities.files;

import java.io.FileFilter;

/**
 * @author ogattaz
 * 
 */
public class CXFileFilterSvn extends CXFileFilterSubPath implements FileFilter {

	private final static String SVN_DIR_NAME = ".svn";

	public CXFileFilterSvn(FileFilter aSubFileFilter, boolean aInclude) {
		super(SVN_DIR_NAME, aSubFileFilter, aInclude);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder wSB = new StringBuilder();
		wSB.append(String.format("CXFileFilterSvn(%s)", includer()));
		if (hasSubFileFilter())
			wSB.append(SEPARATOR).append(getSubFileFilter().toString());
		return wSB.toString();
	}
}
