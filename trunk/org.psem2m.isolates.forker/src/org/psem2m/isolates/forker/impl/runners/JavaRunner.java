/**
 * File:   JavaRunner.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.forker.impl.runners;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.forker.IBundleForkerLoggerSvc;
import org.psem2m.isolates.forker.IProcessRef;
import org.psem2m.isolates.forker.IProcessRunner;
import org.psem2m.isolates.forker.impl.processes.ProcessBuilderRunner;
import org.psem2m.isolates.services.conf.IIsolateDescr;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;

/**
 * Runs the Java interpreter for the given isolate
 * 
 * @author Thomas Calmant
 */
public class JavaRunner extends CPojoBase implements IJavaRunner {

    /** Supported isolate kind */
    public static final String SUPPORTED_ISOLATE_KIND = "java";

    /** The logger */
    private IBundleForkerLoggerSvc pBundleForkerLoggerSvc;

    /** The Java executable */
    private String pJavaExecutable;

    /** The platform directory service */
    private IPlatformDirsSvc pPlatformDirsSvc;

    /**
     * Default constructor
     */
    public JavaRunner() {
	super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.forker.IIsolateRunner#canRun(java.lang.String)
     */
    @Override
    public boolean canRun(final String aIsolateKind) {
	return SUPPORTED_ISOLATE_KIND.equals(aIsolateKind);
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
     * Retrieves a process runner pSingleton, corresponding to the running
     * operating system.
     * 
     * @return A OS-dependent process runner pSingleton
     */
    protected IProcessRunner getProcessRunner() {
	// TODO Use a service ?
	return new ProcessBuilderRunner();
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
     * Prepares a Java interpreter argument to define a JVM property
     * 
     * @param aKey
     *            Property name
     * @param aValue
     *            Property value
     * @return The property definition argument
     */
    protected String makeJavaProperty(final String aKey, final String aValue) {

	final StringBuilder propertyDef = new StringBuilder(aKey.length()
		+ aValue.length() + 3);

	propertyDef.append("-D");
	propertyDef.append(aKey);
	propertyDef.append("=");
	propertyDef.append(aValue);

	return propertyDef.toString();
    }

    /**
     * Runs a Java interpreter with the given parameters
     * 
     * @param aArguments
     *            Interpreter arguments
     * @param aEnvironment
     *            Process environment
     * @param aWorkingDirectory
     *            The process working directory
     * @return A reference to the java interpreter process
     * @throws IOException
     *             An error occurred while trying to run Java
     */
    @Override
    public IProcessRef runJava(final List<String> aArguments,
	    final Map<String, String> aEnvironment, final File aWorkingDirectory)
	    throws IOException {

	IProcessRunner runner = getProcessRunner();

	return runner.runProcess(pJavaExecutable,
		aArguments.toArray(new String[0]), aEnvironment,
		aWorkingDirectory);
    }

    /**
     * Runs a Java Jar file with the given arguments
     * 
     * @param aJarFile
     *            Jar file to execute
     * @param aJavaOptions
     *            JVM options, added before "-jar"
     * @param aJarArguments
     *            Additional Arguments
     * @param aEnvironment
     *            Process environment
     * @param aWorkingDirectory
     *            Process working directory
     * @return A reference to Java interpreter process
     * @throws IOException
     *             An error occurred while trying to run Java
     */
    @Override
    public IProcessRef runJavaJar(final File aJarFile,
	    final List<String> aJavaOptions, final List<String> aJarArguments,
	    final Map<String, String> aEnvironment, final File aWorkingDirectory)
	    throws IOException {

	// Prepare the Java interpreter JAR indication
	List<String> arguments = new ArrayList<String>();

	// JVM options
	if (aJavaOptions != null) {
	    arguments.addAll(aJavaOptions);
	}

	// Jar file
	arguments.add("-jar");
	arguments.add(aJarFile.getAbsolutePath());

	// Append JAR program arguments
	if (aJarArguments != null) {
	    for (String jarArgument : aJarArguments) {
		arguments.add(jarArgument);
	    }
	}

	// Run it
	IProcessRunner runner = getProcessRunner();
	return runner.runProcess(pJavaExecutable,
		arguments.toArray(new String[0]), aEnvironment,
		aWorkingDirectory);
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

	// Java arguments list
	final List<String> javaArguments = new ArrayList<String>();

	// Isolate ID
	javaArguments.add(makeJavaProperty(
		IPlatformProperties.PROP_PLATFORM_ISOLATE_ID,
		aIsolateConfiguration.getId()));

	// PSEM2M Home
	javaArguments.add(makeJavaProperty(
		IPlatformProperties.PROP_PLATFORM_HOME, pPlatformDirsSvc
			.getPlatformHomeDir().getAbsolutePath()));

	// PSEM2M Base
	javaArguments.add(makeJavaProperty(
		IPlatformProperties.PROP_PLATFORM_BASE, pPlatformDirsSvc
			.getPlatformBaseDir().getAbsolutePath()));

	// Working directory
	File workingDirectory = pPlatformDirsSvc
		.getIsolateWorkingDir(aIsolateConfiguration.getId());

	// Run the isolate
	return runJava(javaArguments, null, workingDirectory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() throws BundleException {

	// Store the Java executable path
	pJavaExecutable = pPlatformDirsSvc.getJavaExecutable()
		.getAbsolutePath();

	// logs in the bundle logger
	pBundleForkerLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",
		toDescription());
    }
}
