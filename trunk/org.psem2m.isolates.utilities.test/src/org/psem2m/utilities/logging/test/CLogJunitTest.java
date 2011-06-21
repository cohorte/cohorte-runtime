package org.psem2m.utilities.logging.test;

import org.psem2m.utilities.teststools.CQualityTestBase;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CLogJunitTest extends CQualityTestBase {

	private final static String[] START_ARGS = { "-junit" };

	private CLogTester pLogTester;

	@Override
	public void setUp() throws Exception {

		pLogTester = new CLogTester(START_ARGS);
		logInfo(this, "startTests", "CLogTester App=[%s]",
				pLogTester.getIdentifier());

	}

	public void testLogLog() throws Exception {
		logMethodName(this, "testLogLog");

		pLogTester.execLine("log log 2000 1m 10");
	}

	public void testLogTxt() throws Exception {
		logMethodName(this, "testLogTxt");

		pLogTester.execLine("log txt 5000 2m 10");
	}

	public void testLogXml() throws Exception {
		logMethodName(this, "testLogXml");

		pLogTester.execLine("log xml 2000 1m 10");
	}
}
