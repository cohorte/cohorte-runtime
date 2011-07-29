/**
 * File:   CForkerTester.java
 * Author: Thomas Calmant
 * Date:   16 juin 2011
 */
package org.psem2m.isolates.forker.tester;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.psem2m.isolates.base.bundles.BundleRef;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.commons.IIsolateConfiguration.IsolateKind;
import org.psem2m.isolates.commons.forker.IForker;
import org.psem2m.isolates.commons.forker.IsolateConfiguration;
import org.psem2m.utilities.logging.CLogToolsException;
import org.psem2m.utilities.teststools.CConsoleTester;

/**
 * @author Thomas Calmant
 * 
 */
public class CForkerTester extends CConsoleTester {

    /** Pre-defined tests for the 'fork' command */
    public static final String FORK_FELIX_COMMAND = "fork-felix";

    /** Fork command */
    public static final String FORK_JAVA_COMMAND = "fork-java";

    /**
     * @param args
     */
    public static void main(final String[] args) {
	try {
	    CConsoleTester.main(new CForkerTester(args));
	} catch (Throwable e) {
	    System.out.println("main(): EXCEPTION\n");
	    System.out.println(CLogToolsException.getInstance().eInString(e));
	    System.exit(1);
	}
    }

    /** The Felix forker service */
    protected IForker pFelixForker;

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
    }

    @Override
    protected void buildHelp(final StringBuilder aHelp) {
	addHelpTitle(aHelp, "Forker Tester");

	addHelpLine(aHelp, FORK_JAVA_COMMAND + " [args]");
	addHelpSubLine(aHelp, "launches the java interpreter "
		+ "with the given arguments ('args')");

	addHelpLine(aHelp, FORK_FELIX_COMMAND
		+ "bundle1 [bundle2 [...]] (INCOMPLETE)");
	addHelpSubLine(aHelp,
		"launches the felix forker with the given bundles");
    }

    /**
     * Uses the Felix forker
     * 
     * @param aCommandLine
     * @return
     * @throws Exception
     */
    protected boolean doForkFelix(final String aCommandLine) throws Exception {

	String[] elements = aCommandLine.split(" ");

	if (elements.length < 2) {
	    throw new InvalidParameterException("Missing arguments");
	}

	IBundleFinderSvc bundleFinder = getBundleFinder();
	if (bundleFinder == null) {
	    throw new Exception("Test requires a bundle finder service");
	}

	List<BundleRef> bundles = new ArrayList<BundleRef>();
	for (int i = 1; i < elements.length; i++) {

	    BundleRef bundleRef = bundleFinder.findBundle(elements[i]);
	    bundles.add(bundleRef);
	}

	IsolateConfiguration isolateConfig = new IsolateConfiguration(
		"isolat-felix", IsolateKind.FELIX,
		bundles.toArray(new BundleRef[0]));

	IForker forker = getForker();
	forker.startIsolate(isolateConfig);
	return true;
    }

    /**
     * Tests the standard forker
     * 
     * @param aCommandLine
     * @return
     * @throws Exception
     */
    protected boolean doForkJava(final String aCommandLine) throws Exception {

	String[] elements = aCommandLine.split(" ");

	String[] cmdArray = null;
	if (elements.length > 1) {
	    cmdArray = new String[elements.length - 1];

	    for (int i = 1; i < elements.length; i++) {
		cmdArray[i - 1] = elements[i];
	    }
	}

	IsolateConfiguration isolateConfig = new IsolateConfiguration(
		"isolat-java", IsolateKind.JAVA);

	IForker forker = getForker();
	forker.startIsolate(isolateConfig);

	return true;
    }

    /**
     * Retrieves the first bundle finder found
     * 
     * @return The first bundle finder found, null if none
     */
    protected IBundleFinderSvc getBundleFinder() {

	BundleContext context = CBundleForkerTestActivator.getInstance()
		.getContext();
	try {
	    ServiceReference[] refs = context.getServiceReferences(
		    IBundleFinderSvc.class.getName(), null);

	    if (refs != null && refs.length != 0) {
		return (IBundleFinderSvc) context.getService(refs[0]);
	    }

	} catch (InvalidSyntaxException e) {
	    logInfo("Error searching for a bundle finder : ");
	    logInfo(e);
	}

	return null;
    }

    /**
     * Retrieves the forker service
     * 
     * @return The forker service, null if not found
     */
    protected IForker getForker() {

	BundleContext context = CBundleForkerTestActivator.getInstance()
		.getContext();
	try {
	    ServiceReference[] refs = context.getServiceReferences(
		    IForker.class.getName(), null);

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

	if (aCommand.equals(FORK_JAVA_COMMAND)) {
	    return doForkJava(aLine);

	} else if (aCommand.equals(FORK_FELIX_COMMAND)) {
	    return doForkFelix(aLine);
	}

	return false;
    }
}
