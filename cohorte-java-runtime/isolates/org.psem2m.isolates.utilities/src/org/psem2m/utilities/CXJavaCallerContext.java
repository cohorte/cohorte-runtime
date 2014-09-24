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
 * @author isandlaTech - ogattaz
 * 
 */
public class CXJavaCallerContext {

	/**
	 * @author ogattaz
	 * 
	 */
	private static class ClassStackContext extends SecurityManager {
		/**
		 * @return
		 */
		Class<?>[] getContext() {
			/*
			 * Returns the current execution stack as an array of classes.
			 * 
			 * The length of the array is the number of methods on the execution
			 * stack. The element at index 0 is the class of the currently
			 * executing method, the element at index 1 is the class of that
			 * method's caller, and so on.
			 */
			return getClassContext();
		}
	}

	private static final ClassStackContext CONTEXT = new ClassStackContext();

	private static final int INTERNAL_CALL_COUNT = 1;

	private static final int NOLIMIT = -1;

	/**
	 * Utility to obtain the <code>Class</code> issuing the call to the current
	 * method of execution
	 * 
	 * @return The calling <code>Class</code>
	 */
	public static Class<?> getCaller() {
		Class<?>[] wCallers = getCallers();
		int wMax = wCallers.length;
		return wCallers[wMax - 1 - INTERNAL_CALL_COUNT];
	}

	/**
	 * @param wDeep
	 * @return
	 */
	public static Class<?> getCaller(int wDeep) {
		Class<?>[] wCallers = getCallers();
		int wMax = wCallers.length;
		if (wDeep > wMax) {
			wDeep = wMax;
		}
		return wCallers[wDeep];
	}

	/**
	 * @param aPackageId
	 * @return
	 */
	public static Class<?> getCaller(String aPackageId) {
		Class<?>[] wCallers = getCallers();
		int wMax = wCallers.length;
		int wI = 0;
		while (wI < wMax) {
			if (wCallers[wI].getName().startsWith(aPackageId)) {
				return wCallers[wI];
			}
			wI++;
		}
		return null;
	}

	/**
	 * @return
	 */
	public static Class<?>[] getCallers() {
		return CONTEXT.getContext();
	}

	/**
	 * @param wLimit
	 * @return
	 */
	public static Class<?>[] getCallers(int wLimit) {
		Class<?>[] wCallers = getCallers();
		int wMax = wCallers.length;
		if (wLimit < 0) {
			wLimit = wMax;
		}
		if (wMax > wLimit) {
			wMax = wLimit;
		}
		Class<?>[] wSubCallers = new Class<?>[wMax];
		int wI = 0;
		while (wI < wMax) {
			wSubCallers[wI] = wCallers[wI];
			wI++;
		}
		return wSubCallers;
	}

	/**
	 * @return
	 */
	public static String[] getCallersNames() {
		return getCallersNames(NOLIMIT);
	}

	/**
	 * @param wLimit
	 * @return
	 */
	public static String[] getCallersNames(int wLimit) {
		Class<?>[] wCallers = getCallers();
		int wMax = wCallers.length;
		if (wLimit > NOLIMIT && wMax > wLimit) {
			wMax = wLimit;
		}
		String[] wNames = new String[wMax];
		int wI = 0;
		while (wI < wMax) {
			wNames[wI] = wCallers[wI].getName();
			wI++;
		}
		return wNames;
	}
}