/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz (isandlaTech) - initial API and implementation
 *******************************************************************************/
package org.psem2m.utilities;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CXJavaRunContext {

	/**
	 * @param aSTEs
	 * @param aMethodName
	 * @return
	 */
	private static int findMethodIdx(final StackTraceElement[] aSTEs,
			final String aMethodName) {
		int wMax = (aSTEs != null) ? aSTEs.length : 0;

		for (int wI = 0; wI < wMax; wI++) {
			if (aSTEs[wI].getMethodName().equals(aMethodName)) {
				return wI;
			}
		}
		return -1;
	}

	/**
	 * <pre>
	 *  getStackTrace,getStackTrace,getCallingMethod,doCmdeMethods,monitorCommand,execLine,monitor,main,main
	 * </pre>
	 * 
	 * @return the current method name:
	 */

	public static String getCallingMethod() {
		// CALLING_METHOD => 2 previous "getCallingMethod"
		return getMethod("getCallingMethod", 2);
	}

	/**
	 * <pre>
	 *  getStackTrace,getStackTrace,getCurrentMethod,doCmdeMethods,monitorCommand,execLine,monitor,main,main
	 * </pre>
	 * 
	 * @return the calling method name
	 */
	public static String getCurrentMethod() {

		// CURRENT_METHOD => 1 previous "getCurrentMethod"
		return getMethod("getCurrentMethod", 1);

	}

	/**
	 * @param aToolMethodName
	 * @param aBefore
	 * @return
	 */
	private static String getMethod(final String aToolMethodName,
			final int aBefore) {

		StackTraceElement[] wSTEs = getStackTrace();
		int wMax = (wSTEs != null) ? wSTEs.length : 0;

		if (wMax < 1) {
			return null;
		}
		int wIdx = findMethodIdx(wSTEs, aToolMethodName);
		if (wIdx < 0) {
			return null;
		}
		int wIdxCallingMethod = wIdx + aBefore;
		return (wIdxCallingMethod < wMax) ? wSTEs[wIdxCallingMethod]
				.getMethodName() : null;

	}

	/**
	 * <pre>
	 *  getStackTrace,getStackTrace,getStackTrace,getMethod,getPreCallingMethod,doCmdeMethods,monitorCommand,execLine,monitor,main,main
	 * </pre>
	 * 
	 * @return
	 */
	public static String getPreCallingMethod() {
		// CALLING_METHOD => 3 previous "getPreCallingMethod"
		return getMethod("getPreCallingMethod", 3);
	}

	/**
	 * *
	 * 
	 * <pre>
	 * getStackTrace,getStackTrace,getStackMethods,doCmdeMethods,monitorCommand,execLine,monitor,main,main
	 * </pre>
	 * 
	 * @return the list of the name of the methods currently in the stack.
	 */
	public static String[] getStackMethods() {
		StackTraceElement[] wSTEs = getStackTrace();
		int wMax = (wSTEs != null) ? wSTEs.length : 0;
		String[] wSMs = new String[wSTEs.length];
		if (wMax > 0) {

			for (int wI = 0; wI < wMax; wI++) {
				wSMs[wI] = wSTEs[wI].getMethodName();
			}
		}
		return wSMs;
	}

	/**
	 * @return
	 */
	private static java.lang.StackTraceElement[] getStackTrace() {
		return Thread.currentThread().getStackTrace();
	}
}
