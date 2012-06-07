package org.psem2m.utilities.scripting;

public class CXJsExcepLoad extends CXJsException {

	private static final long serialVersionUID = 1L;

	public CXJsExcepLoad(CXJsSourceMain aSrcMain, Throwable e, String aMessage) {
		super(aSrcMain, aMessage, e, "loadingSource");
	}

	public CXJsExcepLoad(CXJsSourceMain aSrcMain, String aMessage) {
		this(aSrcMain, (Exception) null, aMessage);
	}

	public CXJsExcepLoad(CXJsSourceMain aSrcMain, String aMessage,
			String aAction) {
		super(aSrcMain, aMessage, aAction);
	}
}
