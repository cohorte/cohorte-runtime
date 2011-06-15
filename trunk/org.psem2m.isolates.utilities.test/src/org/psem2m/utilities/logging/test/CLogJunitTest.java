package org.psem2m.utilities.logging.test;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.psem2m.utilities.teststools.CQualityTestBase;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CLogJunitTest extends CQualityTestBase {

	private final static String[] START_ARGS = { "-junit" };

	@Rule
	public ExpectedException pExpectedException = ExpectedException.none();
	private CLogTester pLogTester;

	@Before
	public void startTests() throws Exception {

		pLogTester = new CLogTester(START_ARGS);
		logInfo(this, "startTests", "CLogTester App=[%s]",
				pLogTester.getIdentifier());

	}

	@Test
	public void testLogLog() throws Exception {
		logMethodName(this, "testLogLog");

		pLogTester.execLine("log log 2000 1m 10");

	}

	@Test
	public void testLogTxt() throws Exception {
		logMethodName(this, "testLogTxt");

		pLogTester.execLine("log txt 5000 2m 10");

	}

	@Test
	public void testLogXml() throws Exception {
		logMethodName(this, "testLogXml");

		pLogTester.execLine("log xml 2000 1m 10");

	}
}
