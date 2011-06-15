package org.psem2m.utilities.logging;

import org.psem2m.utilities.CXDateTime;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.IXDescriber;
import org.psem2m.utilities.files.CXFile;
import org.psem2m.utilities.files.CXFileText;

public class CActivityFileText extends CXFileText implements IXDescriber {
	private final static String LABEL_LASTMOD = "LastModified";

	private final static String LABEL_PATH = "Path";

	private static final long serialVersionUID = -1882311684643515716L;

	private final long pTimeWhenFound;

	/**
	 * @param aFile
	 */
	public CActivityFileText(CXFile aFile) {
		super(aFile);
		pTimeWhenFound = aFile.lastModified();
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aBuffer) {
		CXStringUtils
				.appendKeyValInBuff(aBuffer, LABEL_PATH, getAbsolutePath());
		CXStringUtils.appendKeyValInBuff(aBuffer, LABEL_LASTMOD,
				CXDateTime.time2StrAAAAMMJJHHMMSS(lastModified()));
		return aBuffer;
	}

	public int calcDescriptionLength() {
		return 128;
	}

	/**
	 * @return
	 */
	public boolean isSameWhenFound() {
		return lastModified() == pTimeWhenFound;
	}

	@Override
	public String toDescription() {
		return addDescriptionInBuffer(
				new StringBuilder(calcDescriptionLength())).toString();
	}

}
