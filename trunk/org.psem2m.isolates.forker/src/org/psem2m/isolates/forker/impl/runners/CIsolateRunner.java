/**
 * File:   CIsolateRunner.java
 * Author: Thomas Calmant
 * Date:   7 juil. 2011
 */
package org.psem2m.isolates.forker.impl.runners;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.psem2m.isolates.commons.IBundleRef;
import org.psem2m.isolates.commons.IIsolateConfiguration;
import org.psem2m.isolates.commons.IIsolateConfiguration.IsolateKind;
import org.psem2m.isolates.commons.Utilities;
import org.psem2m.isolates.forker.IProcessRef;
import org.psem2m.isolates.forker.impl.processes.ProcessRef;
import org.psem2m.utilities.bootstrap.IBootstrapConstants;

/**
 * @author Thomas Calmant
 * 
 */
public class CIsolateRunner extends JavaRunner {

    /** Possible bootstrap names */
    public static final String[] BOOTSTRAP_NAMES = new String[] {
	    "org.psem2m.bootstrap.jar", "bootstrap.jar" };

    /** Equinox framework names */
    public static final String[] EQUINOX_NAMES = new String[] {
	    "org.eclipse.osgi_3.7.0.v20110613.jar", "equinox.jar" };

    /** Felix framework names */
    public static final String[] FELIX_NAMES = new String[] {
	    "org.apache.felix.main-3.2.2.jar", "felix.jar" };

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
     * @return the JVM classpath argument
     */
    protected List<String> prepareClasspathArgument(final IsolateKind aKind) {

	List<String> classpathArgument = new ArrayList<String>();
	classpathArgument.add("-cp");

	// Don't forget the current directory as a classpath
	StringBuilder classPath = new StringBuilder();
	classPath.append(".");

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

	// Add the found framework, if any
	if (mainBundleRef != null) {
	    classPath.append(":");
	    classPath.append(mainBundleRef.getUri());
	}

	classpathArgument.add(classPath.toString());
	return classpathArgument;
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

	// Add the class path argument
	javaOptions.addAll(prepareClasspathArgument(kind));

	// Find the bootstrap JAR file
	IBundleRef bootstrapRef = Utilities.findBundle(
		getPlatformConfiguration(), BOOTSTRAP_NAMES);

	// Prepare its arguments
	List<String> bootstrapArguments = prepareBootstrapArguments(kind);

	// Run the file
	IProcessRef processRef = runJavaJar(bootstrapRef.getFile(),
		javaOptions, bootstrapArguments,
		aIsolateConfiguration.getEnvironment(),
		createWorkingDirectory(aIsolateConfiguration.getId()));

	// Writes the bundles configuration to the process
	sendConfiguration(processRef, aIsolateConfiguration);

	return processRef;
    }
}
