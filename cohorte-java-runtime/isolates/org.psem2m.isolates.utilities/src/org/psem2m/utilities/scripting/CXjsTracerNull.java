package org.psem2m.utilities.scripting;

/**
 * @author ogattaz
 * 
 */
public class CXjsTracerNull implements IXjsTracer {

	private static final IXjsTracer sTracerNull = new CXjsTracerNull();

	/**
	 * @return
	 */
	public static IXjsTracer getInstance() {
		return sTracerNull;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.scripting.IXjsTracer#isTraceDebugOn()
	 */
	@Override
	public boolean isTraceDebugOn() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.scripting.IXjsTracer#isTraceInfosOn()
	 */
	@Override
	public boolean isTraceInfosOn() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.scripting.IXjsTracer#trace(java.lang.CharSequence)
	 */
	@Override
	public void trace(CharSequence aSB) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.scripting.IXjsTracer#trace(java.lang.Object,
	 * java.lang.CharSequence)
	 */
	@Override
	public void trace(Object aObj, CharSequence aS) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.scripting.IXjsTracer#trace(java.lang.Object,
	 * java.lang.CharSequence, java.lang.Throwable)
	 */
	@Override
	public void trace(Object aObj, CharSequence aSB, Throwable e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.scripting.IXjsTracer#trace(java.lang.Object,
	 * java.lang.Throwable)
	 */
	@Override
	public void trace(Object aObj, Throwable e) {
	}

}
