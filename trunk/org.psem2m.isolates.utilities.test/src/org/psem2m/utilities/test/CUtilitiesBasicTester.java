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
package org.psem2m.utilities.test;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.psem2m.utilities.CXArray;
import org.psem2m.utilities.CXJavaRunContext;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.CXTimer;
import org.psem2m.utilities.logging.CLogToolsException;
import org.psem2m.utilities.teststools.CConsoleTester;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CUtilitiesBasicTester extends CConsoleTester {

	private final static String CMDE_CALLS = "calls";
	private final static String CMDE_METHODS = "methods";
	private final static String CMDE_TIMER = "timer";

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		try {
			CConsoleTester.main(new CUtilitiesBasicTester(args));
		} catch (Throwable e) {
			System.out.println("main(): EXCEPTION\n");
			System.out.println(CLogToolsException.getInstance().eInString(e));
			System.exit(1);
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public CUtilitiesBasicTester(final String[] args) throws Exception {
		super(args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.teststools.CConsoleTester#buildHelp(java.lang.
	 * StringBuilder)
	 */
	@Override
	protected void buildHelp(final StringBuilder aHelp) {
		addHelpTitle(aHelp, "CLogTester");
		addHelpLine(aHelp, CMDE_CALLS + "");
		addHelpSubLine(aHelp, "...");
		addHelpLine(aHelp, CMDE_METHODS + "");
		addHelpSubLine(aHelp, "...");

		addHelpLine(aHelp, CMDE_TIMER + "");
		addHelpSubLine(aHelp, "test the CXTimer class");
	}

	/**
	 * @param aST
	 * @throws Exception
	 */
	private void doCmdeCallA(final StringTokenizer aST) throws Exception {
		doCmdeCallB(aST);

	}

	/**
	 * @param aST
	 * @throws Exception
	 */
	private void doCmdeCallB(final StringTokenizer aST) throws Exception {
		logInfo("StackMethods=[%s]",
				CXArray.arrayToString(CXJavaRunContext.getStackMethods(), ","));

		CXTimer wTimerB = new CXTimer(CXTimer.START);
		String wPreCallingMethod = CXJavaRunContext.getPreCallingMethod();
		wTimerB.stop();
		logInfo("PreCallingMethod=[%s]", wPreCallingMethod);
		logInfo("PreCallingMethod Duration=[%s]",
				wTimerB.getDurationStrMicroSec());

		CXTimer wTimerA = new CXTimer(CXTimer.START);
		String wCallingMethod = CXJavaRunContext.getCallingMethod();
		wTimerA.stop();
		logInfo("CallingMethod=[%s]", wCallingMethod);
		logInfo("CallingMethod Duration=[%s]", wTimerA.getDurationStrMicroSec());

		CXTimer wTimer = new CXTimer(CXTimer.START);
		String wCurrentMethod = CXJavaRunContext.getCurrentMethod();
		wTimer.stop();
		logInfo("CurrentMethod=[%s]", wCurrentMethod);
		logInfo("CurrentMethod Duration=[%s]", wTimer.getDurationStrMicroSec());

	}

	/**
	 * @param aST
	 * @return
	 * @throws Exception
	 */
	private boolean doCmdeCalls(final StringTokenizer aST) throws Exception {

		logInfo(CMDE_CALLS + " begin");

		doCmdeCallA(aST);
		logInfoAndPrompt(CMDE_CALLS + " end");
		return true;
	}

	/**
	 * @param aST
	 * @return
	 * @throws Exception
	 */
	private boolean doCmdeMethods(final StringTokenizer aST) throws Exception {
		logInfo(CMDE_METHODS + " begin");

		CXTimer wTimer = new CXTimer(CXTimer.START);
		String[] wMethods = CXJavaRunContext.getStackMethods();
		wTimer.stop();

		logInfo(CXArray.arrayToString(wMethods, ","));
		logInfo("getStackMethods Duration=[%s]",
				wTimer.getDurationStrMicroSec());

		logInfoAndPrompt(CMDE_METHODS + " end");
		return true;
	}

	/**
	 * @param aST
	 * @return
	 * @throws Exception
	 */
	private boolean doCmdeTimer(final StringTokenizer aST) throws Exception {
		logInfo(CMDE_TIMER + " begin");
		List<String> wStrs = new ArrayList<String>();
		CXTimer wTimer = new CXTimer(CXTimer.START);

		try {
			for (int wI = 0; wI < 100; wI++) {
				// implmenetation SUN => 2 milliseconds + 0 nanoseconds
				Thread.sleep(2, 499000);
				wStrs.add(CXStringUtils.strFromChar('-', 1024));
			}
		} catch (InterruptedException e) {
			// nothing
		}
		wTimer.stop();
		logInfo("DurationMs=[%f]", wTimer.getDurationMs());
		logInfo("DurationMs=[%d]", wTimer.getDurationNs());
		logInfo("DurationMilliSec=[%s]", wTimer.getDurationStrMilliSec());
		logInfo("DurationMicroSec=[%s]", wTimer.getDurationStrMicroSec());
		logInfo("HeapStart=[%s]", wTimer.getHeapStart());
		logInfo("HeapStop=[%s]", wTimer.getHeapStop());
		logInfo("HeapDelta=[%s]", wTimer.getHeapDelta());

		logInfoAndPrompt(CMDE_TIMER + " end");
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.teststools.CConsoleTester#monitorCommand(java.lang
	 * .String, java.lang.String, java.util.StringTokenizer)
	 */
	@Override
	public boolean monitorCommand(final String aCommand, final String aLine,
			final StringTokenizer aST) throws Exception {
		if (CMDE_METHODS.equalsIgnoreCase(aCommand)) {
			return doCmdeMethods(aST);
		} else if (CMDE_CALLS.equalsIgnoreCase(aCommand)) {
			return doCmdeCalls(aST);
		} else if (CMDE_TIMER.equalsIgnoreCase(aCommand)) {
			return doCmdeTimer(aST);
		} else {
			return false;
		}
	}
}
