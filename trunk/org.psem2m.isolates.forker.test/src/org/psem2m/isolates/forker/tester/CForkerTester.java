/**
 * File:   CForkerTester.java
 * Author: Thomas Calmant
 * Date:   16 juin 2011
 */
package org.psem2m.isolates.forker.tester;

import java.util.StringTokenizer;

import org.psem2m.isolates.forker.impl.ProcessFactory;
import org.psem2m.utilities.teststools.CConsoleTester;

/**
 * @author Thomas Calmant
 * 
 */
public class CForkerTester extends CConsoleTester {

    /** Fork command */
    private static final String FORK_COMMAND = "fork";

    /** Pre-defined tests for the 'fork' command */
    private static final String FORK_TEST_COMMAND = "fork-test";

    /**
     * Standard constructor
     * 
     * @param aArgs
     * @throws Exception
     */
    public CForkerTester(final String[] aArgs) throws Exception {
	super(aArgs);
    }

    @Override
    protected void buildHelp(final StringBuilder aHelp) {
	addHelpTitle(aHelp, "Forker Tester");

	addHelpLine(aHelp, FORK_COMMAND + " exec [args]");
	addHelpSubLine(aHelp, "launches the given executable file ('exec'), "
		+ "with the given arguments ('args')");

	addHelpLine(aHelp, FORK_TEST_COMMAND + " n");
	addHelpSubLine(aHelp, "launches the fork test number 'n'");
    }

    protected boolean doFork(final String aCommandLine) throws Exception {

	String[] elements = aCommandLine.split(" ");

	String exec = elements[1];
	String[] args = null;
	if (elements.length > 2) {
	    args = new String[elements.length - 2];

	    for (int i = 2; i < elements.length; i++) {
		args[i - 2] = elements[i];
	    }
	}

	if (ProcessFactory.startProcess(exec, args, ".") != null) {
	    logInfo("Fork successful");
	    return true;
	}

	throw new Exception("Error using the process factory");
    }

    protected boolean doForkTest(final String aCommandLine) {
	logInfo("No pre-defined tests available...");
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

	if (aCommand.equals(FORK_COMMAND)) {
	    return doFork(aLine);

	} else if (aCommand.equals(FORK_TEST_COMMAND)) {
	    return doForkTest(aLine);
	}

	return false;
    }

}
