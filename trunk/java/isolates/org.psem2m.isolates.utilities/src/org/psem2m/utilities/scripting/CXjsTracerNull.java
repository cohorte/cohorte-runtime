package org.psem2m.utilities.scripting;


public class CXjsTracerNull implements IXjsTracer {

	private static final IXjsTracer sTracerNull = new CXjsTracerNull();

	public static IXjsTracer getInstance() {
		return sTracerNull;
	}

	@Override
	public boolean isTraceInfosOn() {
		return false;
	}

	@Override
	public boolean isTraceDebugOn() {
		return false;
	}

	@Override
	public void trace(Object aObj, CharSequence aS) {

	}

	@Override
	public void trace(Object aObj, Throwable e) {
	}

	@Override
	public void trace(CharSequence aSB) {
	}

	@Override
	public void trace(Object aObj, CharSequence aSB, Throwable e) {
	}

}
