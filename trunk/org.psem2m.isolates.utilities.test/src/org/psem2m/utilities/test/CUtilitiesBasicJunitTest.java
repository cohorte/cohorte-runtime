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
	public void testA_Timer() throws Exception {
		logMethodName(this, "testLogLog");

		pAppTester.execLine("timer");
	}

	/**
	 * @throws Exception
	 */
	public void testB_Calls() throws Exception {
		logMethodName(this, "testLogTxt");

		pAppTester.execLine("calls");
	}

	/**
	 * @throws Exception
	 */
	public void testC_Methos() throws Exception {
		logMethodName(this, "testLogXml");

		pAppTester.execLine("methods");
	}
}
