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
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.base.IPlatformDirsSvc;
import org.psem2m.isolates.base.boot.IsolateStatus;
import org.psem2m.isolates.base.bundles.BundleRef;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.commons.IIsolateConfiguration;
import org.psem2m.isolates.commons.IIsolateConfiguration.IsolateKind;
import org.psem2m.isolates.forker.IBundleForkerLoggerSvc;
import org.psem2m.isolates.forker.IIsolateRunner;
import org.psem2m.isolates.forker.IProcessRef;
import org.psem2m.isolates.forker.impl.processes.ProcessRef;
import org.psem2m.utilities.bootstrap.IBootstrapConstants;
import org.psem2m.utilities.files.CXFileDir;
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

	/** Bootstrap main class */
	public static final String BOOTSTRAP_MAIN_CLASS = "org.psem2m.utilities.bootstrap.Main";

	/** Possible bootstrap names */
	public static final String[] BOOTSTRAP_NAMES = new String[] {
			"org.psem2m.bootstrap.jar", "bootstrap.jar" };

	/** Equinox framework names */
	public static final String[] EQUINOX_NAMES = new String[] {
			"org.eclipse.osgi_3.7.0.v20110613.jar", "equinox.jar" };

	/** Felix framework names */
	public static final String[] FELIX_NAMES = new String[] {
			"org.apache.felix.main-3.2.2.jar", "felix.jar" };

	/** Bootstrap long argument prefix */
	public static final String LONG_ARGUMENT_PREFIX = "--";

	/** Bundle finder service, injected by iPOJO */
	private IBundleFinderSvc pBundleFinderSvc;

	/** The logger service, injected by iPOJO */
	private IBundleForkerLoggerSvc pBundleForkerLoggerSvc;

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
	 * @see
	 * org.psem2m.isolates.forker.impl.runners.JavaRunner#canRun(org.psem2m.
	 * isolates.commons.IIsolateConfiguration.IsolateKind)
	 */
	@Override
	public boolean canRun(final IsolateKind aIsolateKind) {
		return aIsolateKind == IsolateKind.EQUINOX
				|| aIsolateKind == IsolateKind.FELIX;
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
	 * Prepares bootstrap arguments. Tells it to wait for serialized data on its
	 * standard input
	 * 
	 * @param aKind
	 *            Kind of isolate (of framework)
	 * 
	 * @return The bootstrap arguments, never null
	 */
	protected List<String> prepareBootstrapArguments(final IsolateKind aKind) {

		List<String> bootstrapArguments = new ArrayList<String>();

		// Use serialized data for communication
		bootstrapArguments.add(LONG_ARGUMENT_PREFIX
				+ IBootstrapConstants.UNSERIALIZE_COMMAND);

		// Framework to be used
		StringBuilder bootstrapFramework = new StringBuilder(
				IBootstrapConstants.CONFIG_FRAMEWORK);
		bootstrapFramework.append("=");
		bootstrapFramework.append(aKind.toString().toLowerCase());

		bootstrapArguments.add(bootstrapFramework.toString());

		return bootstrapArguments;
	}

	/**
	 * Prepares the JVM classpath argument according to the isolate kind
	 * 
	 * @param aBootstrapRef
	 * 
	 * @return the JVM classpath argument
	 */
	protected List<String> prepareClasspathArgument(
			final BundleRef aBootstrapRef, final IsolateKind aKind) {

		List<String> classpathArgument = new ArrayList<String>();
		classpathArgument.add("-cp");

		// Don't forget the current directory as a classpath
		StringBuilder classPath = new StringBuilder();

		// Find the framework main bundle
		BundleRef mainBundleRef;
		switch (aKind) {
		case FELIX:
			mainBundleRef = pBundleFinderSvc.findBundle(FELIX_NAMES);
			break;

		case EQUINOX:
			mainBundleRef = pBundleFinderSvc.findBundle(EQUINOX_NAMES);
			break;

		default:
			mainBundleRef = null;
			break;
		}

		// Add the boostrap JAR
		classPath.append(aBootstrapRef.getFile().getAbsolutePath());
		classPath.append(File.pathSeparator);

		// Add the found framework, if any
		if (mainBundleRef != null) {
			classPath.append(mainBundleRef.getFile().getAbsolutePath());
			classPath.append(File.pathSeparator);
		}

		classPath.append(".");
		classpathArgument.add(classPath.toString());
		return classpathArgument;
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
	 * Sends the bundles URL array to the standard input of the bootstrap
	 * 
	 * @param aProcessRef
	 *            The ran process
	 * @param aIsolateConfiguration
	 *            The isolate configuration
	 */
	protected void sendConfiguration(final IProcessRef aProcessRef,
			final IIsolateConfiguration aIsolateConfiguration) {

		// Grab a reference to the process
		if (!(aProcessRef instanceof ProcessRef)) {
			return;
		}

		// Prepare the object stream
		Process process = ((ProcessRef) aProcessRef).getProcess();
		OutputStream processInput = process.getOutputStream();

		// Convert bundle references to URLs
		List<URL> bundleURLs = new ArrayList<URL>();

		// Isolate bundles
		bundleURLs.addAll(bundlesToUrls(aIsolateConfiguration.getBundles()));

		try {
			ObjectOutputStream objectStream = new ObjectOutputStream(
					processInput);

			// Send data
			objectStream.writeObject(bundleURLs.toArray(new URL[0]));

			// Close it
			objectStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.forker.impl.runners.JavaRunner#startIsolate(org.psem2m
	 * .isolates.commons.IIsolateConfiguration)
	 */
	@Override
	public IProcessRef startIsolate(
			final IIsolateConfiguration aIsolateConfiguration) throws Exception {

		IsolateKind kind = aIsolateConfiguration.getKind();
		List<String> javaOptions = new ArrayList<String>();

		// Find the bootstrap JAR file
		BundleRef bootstrapRef = pBundleFinderSvc.findBundle(BOOTSTRAP_NAMES);

		// Add the class path argument
		javaOptions.addAll(prepareClasspathArgument(bootstrapRef, kind));

		// Add the Bootstrap main class name
		javaOptions.add(BOOTSTRAP_MAIN_CLASS);

		// Add its arguments
		javaOptions.addAll(prepareBootstrapArguments(kind));

		// Set up the working directory
		CXFileDir workingDirectory = pPlatformDirsSvc
				.getIsolateWorkingDir(aIsolateConfiguration.getId());
		if (!workingDirectory.exists()) {
			workingDirectory.mkdirs();
		}

		// Run the file
		IProcessRef processRef = pJavaRunner.runJava(javaOptions,
				aIsolateConfiguration.getEnvironment(), workingDirectory);

		// Writes the bundles configuration to the process
		sendConfiguration(processRef, aIsolateConfiguration);

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
