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

    /**
     * Tests the Felix forker
     */
    public void testFelixFork() throws Exception {
	logMethodName(this, "testFelixFork");

	pForkerTester.execLine(CForkerTester.FORK_FELIX_COMMAND + " "
		+ "org.apache.felix.shell-1.4.2.jar "
		+ "org.apache.felix.shell.tui-1.4.1.jar "
		+ "org.apache.felix.shell.remote-1.1.2.jar ");
    }

    /**
     * Tests standard forker
     * 
     * @throws Exception
     */
    public void testForkSimple() throws Exception {
	logMethodName(this, "testForkSimple");

	pForkerTester.execLine(CForkerTester.FORK_JAVA_COMMAND
		+ " /usr/bin/gnome-terminal -x htop");
    }
}
