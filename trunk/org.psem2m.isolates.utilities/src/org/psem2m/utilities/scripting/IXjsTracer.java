package org.psem2m.utilities.scripting;


public interface IXjsTracer {

	public final static String CONSTRUCTOR = "<init>";

	/**
	 * @return
	 */
	public boolean isTraceDebugOn();

	/**
	 * @return
	 */
	public boolean isTraceInfosOn();

	/**
	 * @param aSB
	 */
	public void trace(CharSequence aSB);

	/**
	 * @param aObj
	 * @param aS
	 */
	public void trace(Object aObj, CharSequence aS);

	/**
	 * @param aObj
	 * @param aSB
	 * @param e
	 */
	public void trace(Object aObj, CharSequence aSB, Throwable e);

	/**
	 * @param aObj
	 * @param e
	 */
	public void trace(Object aObj, Throwable e);

}