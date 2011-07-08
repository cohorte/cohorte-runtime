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
import java.util.logging.LogRecord;

import org.psem2m.isolates.commons.IBundleRef;
import org.psem2m.isolates.commons.IIsolateConfiguration;
import org.psem2m.isolates.commons.IIsolateConfiguration.IsolateKind;
import org.psem2m.isolates.commons.Utilities;
import org.psem2m.isolates.forker.IProcessRef;
import org.psem2m.isolates.forker.impl.processes.ProcessRef;
import org.psem2m.utilities.bootstrap.IBootstrapConstants;
import org.psem2m.utilities.logging.CActivityFormaterBasic;

/**
 * @author Thomas Calmant
 * 
 */
public class CIsolateRunner extends JavaRunner {

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

    /** Basic log formatter */
    private CActivityFormaterBasic pLogFormatter = new CActivityFormaterBasic();

    /**
     * Converts an array of bundle references to an URL list. Ignores invalid
     * names.
     * 
     * @param aBundles
     *            Bundles to be converted.
     * @return A list of URLs
     */
    protected List<URL> bundlesToUrls(final IBundleRef[] aBundles) {

	List<URL> bundleURLs = new ArrayList<URL>();

	// Loop on bundles
	for (IBundleRef bundleRef : aBundles) {
	    try {
		URL bundleUrl = bundleRef.getUri().toURL();
		bundleURLs.add(bundleUrl);

	    } catch (MalformedURLException e) {
		e.printStackTrace();
	    }
	}

	return bundleURLs;
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
	bootstrapArguments.add(IBootstrapConstants.UNSERIALIZE_COMMAND);

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
	    final IBundleRef aBootstrapRef, final IsolateKind aKind) {

	List<String> classpathArgument = new ArrayList<String>();
	classpathArgument.add("-cp");

	// Don't forget the current directory as a classpath
	StringBuilder classPath = new StringBuilder();

	// Find the framework main bundle
	IBundleRef mainBundleRef;
	switch (aKind) {
	case FELIX:
	    mainBundleRef = Utilities.findBundle(getPlatformConfiguration(),
		    FELIX_NAMES);
	    break;

	case EQUINOX:
	    mainBundleRef = Utilities.findBundle(getPlatformConfiguration(),
		    EQUINOX_NAMES);
	    break;

	default:
	    mainBundleRef = null;
	    break;
	}

	// Add the boostrap JAR
	classPath.append(aBootstrapRef.getFile().getPath());
	classPath.append(File.pathSeparator);

	// Add the found framework, if any
	if (mainBundleRef != null) {
	    classPath.append(mainBundleRef.getFile().getPath());
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

	System.out.println(pLogFormatter.format(aLogRecord));

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
     */
    protected void readMessages(final IProcessRef aProcessRef) {

	// Grab a reference to the process
	if (!(aProcessRef instanceof ProcessRef)) {
	    return;
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

		    // Try to print the log record
		    if (readObject instanceof LogRecord) {

		    } else if (readObject instanceof CharSequence) {
			System.out.println(readObject.toString());
		    } else {
			System.out.println("[BOOTSTRAP] UNKNWON LOG FORMAT");
		    }

		} catch (ClassNotFoundException e) {
		    System.out.println("Can't read class : " + e);
		}
	    }

	} catch (EOFException e) {
	    System.out.println("Isolate gone");

	} catch (IOException e) {
	    e.printStackTrace();
	}
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

	// Common bundles
	bundleURLs.addAll(bundlesToUrls(getPlatformConfiguration()
		.getCommonBundlesRef()));

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
	IBundleRef bootstrapRef = Utilities.findBundle(
		getPlatformConfiguration(), BOOTSTRAP_NAMES);

	// Add the class path argument
	javaOptions.addAll(prepareClasspathArgument(bootstrapRef, kind));

	// Add the Bootstrap main class name
	javaOptions.add(BOOTSTRAP_MAIN_CLASS);

	// Add its arguments
	javaOptions.addAll(prepareBootstrapArguments(kind));

	// Run the file
	IProcessRef processRef = runJava(javaOptions,
		aIsolateConfiguration.getEnvironment(),
		createWorkingDirectory(aIsolateConfiguration.getId()));

	// Writes the bundles configuration to the process
	sendConfiguration(processRef, aIsolateConfiguration);

	// Wait for the end of the isolate
	readMessages(processRef);
	return processRef;
    }
}
