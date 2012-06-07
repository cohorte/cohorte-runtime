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
package org.psem2m.isolates.utilities.test.basic;

import org.psem2m.utilities.teststools.CQualityTestBase;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CJUnitBasic extends CQualityTestBase {

	private CBasicTester pAppTester;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		pAppTester = new CBasicTester(START_ARGS);
		logInfo(this, "startTests", "App=[%s]", pAppTester.getIdentifier());
	}

	/**
	 * @throws Exception
	 */
	public void testA() throws Exception {
		logInfo(this, null, "test command=[%s]",
				CBasicTester.CMDE_TIMER);
		pAppTester.execLine(CBasicTester.CMDE_TIMER);
	}

	/**
	 * @throws Exception
	 */
	public void testB() throws Exception {
		logInfo(this, null, "test command=[%s]",
				CBasicTester.CMDE_CALLS);
		pAppTester.execLine(CBasicTester.CMDE_CALLS);
	}

	/**
	 * @throws Exception
	 */
	public void testC() throws Exception {
		logInfo(this, null, "test command=[%s]",
				CBasicTester.CMDE_METHODS);
		pAppTester.execLine(CBasicTester.CMDE_METHODS);
	}

	/**
	 * @throws Exception
	 */
	public void testD() throws Exception {
		logInfo(this, null, "test command=[%s]",
				CBasicTester.CMDE_JAVA);
		pAppTester.execLine(CBasicTester.CMDE_JAVA);
	}

	/**
	 * @throws Exception
	 */
	public void testE() throws Exception {
		logInfo(this, null, "test command=[%s]", CBasicTester.CMDE_ENV);
		pAppTester.execLine(CBasicTester.CMDE_ENV);
	}

	/**
	 * @throws Exception
	 */
	public void testF() throws Exception {
		logInfo(this, null, "test command=[%s]", CBasicTester.CMDE_OS);
		pAppTester.execLine(CBasicTester.CMDE_OS);
	}

	/**
	 * @param aLine
	 */
	@Override
	protected void writeLog(final String aLine) {
		System.out.println(aLine);
	}
}
