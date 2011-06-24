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

import org.psem2m.utilities.teststools.CQualityTestBase;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CUtilitiesBasicJunitTest extends CQualityTestBase {

	private CUtilitiesBasicTester pAppTester;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		pAppTester = new CUtilitiesBasicTester(START_ARGS);
		logInfo(this, "startTests", "App=[%s]", pAppTester.getIdentifier());
	}

	/**
	 * @throws Exception
	 */
	public void testA() throws Exception {
		logInfo(this, "test command=[%s]", CUtilitiesBasicTester.CMDE_TIMER);
		pAppTester.execLine(CUtilitiesBasicTester.CMDE_TIMER);
	}

	/**
	 * @throws Exception
	 */
	public void testB() throws Exception {
		logInfo(this, "test command=[%s]", CUtilitiesBasicTester.CMDE_CALLS);
		pAppTester.execLine(CUtilitiesBasicTester.CMDE_CALLS);
	}

	/**
	 * @throws Exception
	 */
	public void testC() throws Exception {
		logInfo(this, "test command=[%s]", CUtilitiesBasicTester.CMDE_METHODS);
		pAppTester.execLine(CUtilitiesBasicTester.CMDE_METHODS);
	}

	/**
	 * @throws Exception
	 */
	public void testD() throws Exception {
		logInfo(this, "test command=[%s]", CUtilitiesBasicTester.CMDE_JAVA);
		pAppTester.execLine(CUtilitiesBasicTester.CMDE_JAVA);
	}

	/**
	 * @throws Exception
	 */
	public void testE() throws Exception {
		logInfo(this, "test command=[%s]", CUtilitiesBasicTester.CMDE_ENV);
		pAppTester.execLine(CUtilitiesBasicTester.CMDE_ENV);
	}

	/**
	 * @throws Exception
	 */
	public void testF() throws Exception {
		logInfo(this, "test command=[%s]", CUtilitiesBasicTester.CMDE_OS);
		pAppTester.execLine(CUtilitiesBasicTester.CMDE_OS);
	}
}
