/**
 * File:   CForkerJunitTest.java
 * Author: Thomas Calmant
 * Date:   16 juin 2011
 */
package org.psem2m.isolates.forker.tester;

import org.psem2m.utilities.teststools.CQualityTestBase;

/**
 * @author Thomas Calmant
 * 
 */
public class CForkerJunitTest extends CQualityTestBase {

    /** Tester arguments */
    private final static String[] START_ARGS = { "-junit" };

    /** Forker tester console */
    private CForkerTester pForkerTester;

    @Override
    public void setUp() throws Exception {

	pForkerTester = new CForkerTester(START_ARGS);
	logInfo(this, "startTests", "CLogTester App=[%s]",
		pForkerTester.getIdentifier());
    }

    public void testForkSimple() throws Exception {
	logMethodName(this, "testLogLog");

	pForkerTester.execLine("fork /usr/bin/gnome-terminal -x htop");
    }
}
