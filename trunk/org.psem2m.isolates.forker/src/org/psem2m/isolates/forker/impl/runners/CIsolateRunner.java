/**
 * File:   CIsolateRunner.java
 * Author: Thomas Calmant
 * Date:   7 juil. 2011
 */
package org.psem2m.isolates.forker.impl.runners;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.base.boot.IsolateStatus;
import org.psem2m.isolates.base.bundles.BundleRef;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.forker.IBundleForkerLoggerSvc;
import org.psem2m.isolates.forker.IIsolateRunner;
import org.psem2m.isolates.forker.IProcessRef;
import org.psem2m.isolates.forker.impl.processes.ProcessRef;
import org.psem2m.isolates.services.conf.IIsolateDescr;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.utilities.bootstrap.IBootstrapConstants;
import org.psem2m.utilities.logging.CActivityFormaterBasic;
import org.psem2m.utilities.logging.IActivityFormater;

/**
 * OSGi framework isolate runner
 * 
 * @author Thomas Calmant
 */
public class CIsolateRunner extends CPojoBase implements IIsolateRunner {

    /**
     * Isolate state description after reading an IsolateStatus message
     * 
     * @author Thomas Calmant
     */
    protected enum IsolateStatusAction {
	/** Isolate is still booting (do nothing) */
	BOOTING,
	/** An error occurred while starting the isolate */
	FAILED,
	/** Isolate is started (can stop listening) */
	STARTED,
    }

    /** Equinox framework names */
    public static final String[] EQUINOX_NAMES = new String[] {
	    "org.eclipse.osgi", "org.eclipse.osgi_3.7.0.v20110613.jar",
	    "equinox.jar" };

    /** Felix framework names */
    public static final String[] FELIX_NAMES = new String[] {
	    "org.apache.felix.main", "org.apache.felix.main-3.2.2.jar",
	    "felix.jar" };

    /** Bootstrap long argument prefix */
    public static final String LONG_ARGUMENT_PREFIX = "--";

    /** Supported isolate kinds */
    public static final Map<String, String[]> SUPPORTED_ISOLATE_KINDS = new HashMap<String, String[]>();

    /*
     * Map initialization
     */
    static {
	SUPPORTED_ISOLATE_KINDS.put("felix", FELIX_NAMES);
	SUPPORTED_ISOLATE_KINDS.put("equinox", EQUINOX_NAMES);
    }

    /** Base debug port, <= 0 if not used */
    private int pBaseDebugPort = -1;

    /** Bundle finder service, injected by iPOJO */
    private IBundleFinderSvc pBundleFinderSvc;

    /** The logger service, injected by iPOJO */
    private IBundleForkerLoggerSvc pBundleForkerLoggerSvc;

    /** Launched isolate index */
    private int pIsolateIndex = 0;

    /** Java isolate runner service, injected by iPOJO */
    private IJavaRunner pJavaRunner;

    /** Basic log formatter */
    private final CActivityFormaterBasic pLogFormatter = new CActivityFormaterBasic();

    /** The platform directory service, injected by iPOJO */
    private IPlatformDirsSvc pPlatformDirsSvc;

    /**
     * Default constructor
     */
    public CIsolateRunner() {
	super();
    }

    /**
     * Converts an array of bundle references to an URL list. Ignores invalid
     * names.
     * 
     * @param aBundles
     *            Bundles to be converted.
     * @return A list of URLs
     */
    protected List<URL> bundlesToUrls(final BundleRef[] aBundles) {

	List<URL> bundleURLs = new ArrayList<URL>();

	// Loop on bundles
	for (BundleRef bundleRef : aBundles) {
	    try {
		URL bundleUrl = bundleRef.getUri().toURL();
		bundleURLs.add(bundleUrl);

	    } catch (MalformedURLException e) {
		e.printStackTrace();
	    }
	}

	return bundleURLs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.forker.IIsolateRunner#canRun(java.lang.String)
     */
    @Override
    public boolean canRun(final String aIsolateKind) {
	return SUPPORTED_ISOLATE_KINDS.keySet().contains(aIsolateKind);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.CXObjectBase#destroy()
     */
    @Override
    public void destroy() {
	// ...
    }

    /**
     * Computes the action to do according to the given isolate status
     * 
     * @param aStatus
     *            The last isolate status received
     * @return The action to apply
     */
    protected IsolateStatusAction handleIsolateStatus(
	    final IsolateStatus aStatus) {

	switch (aStatus.getState()) {

	case IsolateStatus.STATE_FAILURE:
	    return IsolateStatusAction.FAILED;

	case IsolateStatus.STATE_BUNDLES_STARTED:
	    return IsolateStatusAction.STARTED;
	}

	return IsolateStatusAction.BOOTING;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() throws BundleException {

	// logs in the bundle logger
	pBundleForkerLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
		toDescription());
    }

    /**
     * Prepares a property definition as a bootstrap program argument
     * 
     * @param aKey
     *            Property name
     * @param aValue
     *            Property value
     * @return The property definition as a bootstrap argument
     */
    protected String makeBootstrapDefinition(final String aKey,
	    final String aValue) {

	StringBuilder bootstrapDef = new StringBuilder(aKey.length()
		+ aValue.length() + 1);
	bootstrapDef.append(aKey);
	bootstrapDef.append("=");
	bootstrapDef.append(aValue);

	return bootstrapDef.toString();
    }

    /**
     * Prepares bootstrap arguments. Indicates the kind of framework to be used
     * and sets up isolate properties.
     * 
     * @param aIsolateConfiguration
     *            Description of the isolate
     * 
     * @return The bootstrap arguments, never null
     */
    protected List<String> prepareBootstrapArguments(
	    final IIsolateDescr aIsolateConfiguration) {

	final List<String> bootstrapArguments = new ArrayList<String>();

	// Framework to be used (in lower case)
	bootstrapArguments.add(makeBootstrapDefinition(
		IBootstrapConstants.CONFIG_FRAMEWORK, aIsolateConfiguration
			.getKind().toString().toLowerCase()));

	// Isolate ID
	bootstrapArguments.add(makeBootstrapDefinition(
		IPlatformProperties.PROP_PLATFORM_ISOLATE_ID,
		aIsolateConfiguration.getId()));

	// PSEM2M Home
	bootstrapArguments.add(makeBootstrapDefinition(
		IPlatformProperties.PROP_PLATFORM_HOME, pPlatformDirsSvc
			.getPlatformHomeDir().getAbsolutePath()));

	// PSEM2M Base
	bootstrapArguments.add(makeBootstrapDefinition(
		IPlatformProperties.PROP_PLATFORM_BASE, pPlatformDirsSvc
			.getPlatformBaseDir().getAbsolutePath()));

	return bootstrapArguments;
    }

    /**
     * Prepares the JVM classpath argument according to the isolate kind
     * 
     * @param aBootstrapFile
     *            The bootstrap JAR file
     * 
     * @return the JVM classpath argument
     */
    protected List<String> prepareClasspathArgument(final File aBootstrapFile,
	    final String aKind) {

	final List<String> classpathArgument = new ArrayList<String>();
	classpathArgument.add("-cp");

	// Don't forget the current directory as a classpath
	StringBuilder classPath = new StringBuilder();

	// Find the framework main bundle
	BundleRef mainBundleRef;

	String[] mainBundleNames = SUPPORTED_ISOLATE_KINDS.get(aKind);
	if (mainBundleNames == null) {
	    // Use Felix framework by default, we should never come here
	    mainBundleNames = FELIX_NAMES;

	    pBundleForkerLoggerSvc
		    .logWarn(this, "prepareClasspathArgument",
			    "Unsupported isolate kind, using the Felix framework by default");
	}

	mainBundleRef = pBundleFinderSvc.findBundle(mainBundleNames);
	if (mainBundleRef == null) {
	    pBundleForkerLoggerSvc.logSevere(this, "prepareClasspathArgument",
		    "Can't find an OSGi framework file, searching for : "
			    + Arrays.toString(mainBundleNames));
	}

	// Add the bootstrap JAR
	classPath.append(aBootstrapFile.getAbsolutePath());
	classPath.append(File.pathSeparator);

	// Add the OSGi framework
	classPath.append(mainBundleRef.getFile().getAbsolutePath());
	classPath.append(File.pathSeparator);

	classPath.append(".");
	classpathArgument.add(classPath.toString());
	return classpathArgument;
    }

    /**
     * Sets up the debug mode members
     */
    protected void prepareDebugMode() {

	// Test if the debug port is indicated
	final String debugPortStr = System
		.getProperty(IPlatformProperties.PROP_BASE_DEBUG_PORT);
	if (debugPortStr == null) {
	    return;
	}

	// Prepare the base port to be used
	try {
	    pBaseDebugPort = Integer.parseInt(debugPortStr);

	    if (pBaseDebugPort <= 0 || pBaseDebugPort > 65535) {
		throw new NumberFormatException("Invalid port number");
	    }

	} catch (NumberFormatException ex) {

	    // Reset the port value, if needed
	    pBaseDebugPort = -1;

	    pBundleForkerLoggerSvc.logWarn(this, "prepareDebugMode",
		    "Can't activate Debug Mode, invalide port number : ",
		    debugPortStr);
	}
    }

    /**
     * Prints the given log record on the standard output
     * 
     * @param aLogRecord
     *            The log record to print
     */
    protected void printLog(final LogRecord aLogRecord) {

	System.out.println(pLogFormatter.format(aLogRecord,
		!IActivityFormater.WITH_END_LINE));

	Throwable thrown = aLogRecord.getThrown();
	if (thrown != null) {
	    thrown.printStackTrace(System.out);
	}
    }

    /**
     * Reads and prints bootstrap log records
     * 
     * @param aProcessRef
     *            Bootstrap process reference
     * 
     * @return True if the process is running, False if it died before the
     *         method returns
     */
    protected boolean readMessages(final IProcessRef aProcessRef) {

	// Grab a reference to the process
	if (!(aProcessRef instanceof ProcessRef)) {
	    return false;
	}

	// Prepare the object stream
	Process process = ((ProcessRef) aProcessRef).getProcess();
	InputStream processOutput = process.getInputStream();

	try {

	    ObjectInputStream objectStream = new ObjectInputStream(
		    processOutput);

	    // Loop until the end of the bootstrap
	    while (true) {

		try {
		    // Read the object
		    Object readObject = objectStream.readObject();

		    // Test if we are reading an isolate status
		    if (readObject instanceof IsolateStatus) {

			IsolateStatusAction action = handleIsolateStatus((IsolateStatus) readObject);
			pBundleForkerLoggerSvc.log(Level.INFO, this,
				"StatusFrom." + aProcessRef.getPid(),
				"Received status : " + readObject);

			switch (action) {
			case STARTED:
			    return true;

			case FAILED:
			    return false;
			}

		    }
		    // Try to print the log record
		    else if (readObject instanceof LogRecord) {
			pBundleForkerLoggerSvc.log((LogRecord) readObject);
			printLog((LogRecord) readObject);

		    } else if (readObject instanceof CharSequence) {
			pBundleForkerLoggerSvc.logInfo(this, "LogFrom."
				+ aProcessRef.getPid(), readObject.toString());

		    } else {
			pBundleForkerLoggerSvc.logWarn(this, "LogFrom."
				+ aProcessRef.getPid(), "Unknown log format",
				readObject);
		    }

		} catch (ClassNotFoundException e) {
		    pBundleForkerLoggerSvc.logWarn(this, "LogFrom."
			    + aProcessRef.getPid(), "Class Not Found", e);
		}
	    }

	} catch (EOFException e) {

	    pBundleForkerLoggerSvc.logInfo(this,
		    "LogFrom." + aProcessRef.getPid(), "Isolate gone : ",
		    process.exitValue());

	} catch (IOException e) {
	    e.printStackTrace();
	}

	return false;
    }

    /**
     * Prepares the debug mode parameters, if needed
     * 
     * @param aJavaOptions
     *            Java program arguments (before -jar)
     */
    protected void setupDebugMode(final List<String> aJavaOptions) {

	if (aJavaOptions == null || pBaseDebugPort <= 0) {
	    return;
	}

	// JVM debug mode
	aJavaOptions.add("-Xdebug");

	// Connection parameter
	final String connectStr = String.format(
		"-Xrunjdwp:transport=dt_socket,address=127.0.0.1:%d,suspend=y",
		pBaseDebugPort + pIsolateIndex);

	aJavaOptions.add(connectStr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.forker.IIsolateRunner#startIsolate(org.psem2m.isolates
     * .base.conf.IIsolateDescr)
     */
    @Override
    public IProcessRef startIsolate(final IIsolateDescr aIsolateConfiguration)
	    throws Exception {

	final List<String> javaOptions = new ArrayList<String>();

	// A new isolate is launched
	pIsolateIndex++;

	// Find the bootstrap JAR file
	File bootstrapFile = pBundleFinderSvc.getBootstrap();

	// Add the class path argument
	javaOptions.addAll(prepareClasspathArgument(bootstrapFile,
		aIsolateConfiguration.getKind()));

	// Set debug mode, if needed
	setupDebugMode(javaOptions);

	// Add isolate VM arguments
	javaOptions.addAll(aIsolateConfiguration.getVMArgs());

	// Add the Bootstrap main class name
	javaOptions.add(IBundleFinderSvc.BOOTSTRAP_MAIN_CLASS);

	// Add its arguments
	javaOptions.addAll(prepareBootstrapArguments(aIsolateConfiguration));

	// Set up the working directory
	File workingDirectory = pPlatformDirsSvc
		.getIsolateWorkingDir(aIsolateConfiguration.getId());
	if (!workingDirectory.exists()) {
	    workingDirectory.mkdirs();
	}

	// Run the bootstrap
	IProcessRef processRef = pJavaRunner.runJava(javaOptions, null,
		workingDirectory);
	if (processRef == null) {
	    return null;
	}

	// Wait for the "running" or "failure" of the isolate
	if (readMessages(processRef)) {
	    // Success
	    return processRef;

	} else {
	    // Failure
	    return null;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() throws BundleException {

	// logs in the bundle logger
	pBundleForkerLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",
		toDescription());
    }
}
