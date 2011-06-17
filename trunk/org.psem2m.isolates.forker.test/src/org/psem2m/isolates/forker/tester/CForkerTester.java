/**
 * File:   CForkerTester.java
 * Author: Thomas Calmant
 * Date:   16 juin 2011
 */
package org.psem2m.isolates.forker.tester;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.psem2m.isolates.commons.PlatformConfiguration;
import org.psem2m.isolates.commons.forker.IForker;
import org.psem2m.isolates.commons.forker.IsolateConfiguration;
import org.psem2m.isolates.commons.forker.ProcessConfiguration;
import org.psem2m.utilities.teststools.CConsoleTester;

/**
 * @author Thomas Calmant
 * 
 */
public class CForkerTester extends CConsoleTester {

    /** Fork command */
    private static final String FORK_COMMAND = "fork";

    /** Pre-defined tests for the 'fork' command */
    private static final String FORK_FELIX_COMMAND = "fork-felix";

    /** The Felix forker service */
    protected IForker pFelixForker;

    /** The platform configuration */
    private PlatformConfiguration pPlatformConfiguration;

    /** The standard forker service */
    protected IForker pStdForker;

    /**
     * Standard constructor
     * 
     * @param aArgs
     * @throws Exception
     */
    public CForkerTester(final String[] aArgs) throws Exception {
	super(aArgs);

	pPlatformConfiguration = new PlatformConfiguration("/home/tcalmant",
		"/home/tcalmant/programmation/workspaces/psem2m/platforms/felix");

	pPlatformConfiguration
		.addCommonBundle("org.apache.felix.ipojo-1.8.0.jar");
    }

    @Override
    protected void buildHelp(final StringBuilder aHelp) {
	addHelpTitle(aHelp, "Forker Tester");

	addHelpLine(aHelp, FORK_COMMAND + " exec [args]");
	addHelpSubLine(aHelp, "launches the given executable file ('exec'), "
		+ "with the given arguments ('args')");

	addHelpLine(aHelp, FORK_FELIX_COMMAND + "dir bundle1 [bundle2 [...]]");
	addHelpSubLine(aHelp, "launches the felix forker in the given 'dir'"
		+ "with the given bundles");
    }

    /**
     * Tests the standard forker
     * 
     * @param aCommandLine
     * @return
     * @throws Exception
     */
    protected boolean doFork(final String aCommandLine) throws Exception {

	String[] elements = aCommandLine.split(" ");

	String[] cmdArray = null;
	if (elements.length > 1) {
	    cmdArray = new String[elements.length - 1];

	    for (int i = 1; i < elements.length; i++) {
		cmdArray[i - 1] = elements[i];
	    }
	}

	IsolateConfiguration isolateConfig = new IsolateConfiguration("toto");
	ProcessConfiguration processConfig = new ProcessConfiguration(cmdArray,
		isolateConfig);

	getForker("standard").runProcess(pPlatformConfiguration, processConfig);
	return true;
    }

    /**
     * Uses the Felix forker
     * 
     * @param aCommandLine
     * @return
     * @throws InvalidParameterException
     * @throws IOException
     */
    protected boolean doForkFelix(final String aCommandLine)
	    throws InvalidParameterException, IOException {

	String[] elements = aCommandLine.split(" ");

	if (elements.length < 2) {
	    throw new InvalidParameterException("Missing arguments");
	}

	String workingDir = elements[1];

	String[] bundles = null;
	if (elements.length > 2) {
	    bundles = new String[elements.length - 2];
	    for (int i = 2; i < elements.length; i++) {
		bundles[i - 2] = elements[i];
	    }
	}

	IsolateConfiguration isolateConfig = new IsolateConfiguration(
		"isolat-felix", bundles, -1);

	ProcessConfiguration processConfig = new ProcessConfiguration(null,
		null, workingDir, isolateConfig);

	System.out.println("Running..." + Arrays.toString(bundles));
	getForker("felix").runProcess(pPlatformConfiguration, processConfig);
	return true;
    }

    /**
     * Retrieves the forker service for the given target
     * 
     * @param aForkerTarget
     *            Forker target (standard or felix)
     * @return The forker service, null if not found
     */
    protected IForker getForker(final String aForkerTarget) {

	BundleContext context = Activator.getContext();
	try {
	    ServiceReference[] refs = context.getServiceReferences(
		    IForker.class.getName(), "(target=" + aForkerTarget + ")");

	    if (refs != null && refs.length != 0) {
		return (IForker) context.getService(refs[0]);
	    }

	} catch (InvalidSyntaxException e) {
	    logInfo("Error searching for a forker : ");
	    logInfo(e);
	}

	return null;
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

	} else if (aCommand.equals(FORK_FELIX_COMMAND)) {
	    return doForkFelix(aLine);
	}

	return false;
    }

}
