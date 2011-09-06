/**
 * File:   JavaRunner.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.forker.impl.runners;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.base.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.commons.IIsolateConfiguration;
import org.psem2m.isolates.commons.IIsolateConfiguration.IsolateKind;
import org.psem2m.isolates.forker.IBundleForkerLoggerSvc;
import org.psem2m.isolates.forker.IProcessRef;
import org.psem2m.isolates.forker.IProcessRunner;
import org.psem2m.isolates.forker.impl.processes.ProcessBuilderRunner;
import org.psem2m.utilities.CXJvmUtils;
import org.psem2m.utilities.CXOSUtils;

/**
 * Runs the Java interpreter for the given isolate
 * 
 * @author Thomas Calmant
 */
public class JavaRunner extends CPojoBase implements IJavaRunner {

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
     * @see
     * org.psem2m.isolates.forker.IIsolateRunner#canRun(org.psem2m.isolates.
     * commons.IIsolateConfiguration.IsolateKind)
     */
    @Override
    public boolean canRun(final IsolateKind aIsolateKind) {
	return aIsolateKind == IsolateKind.JAVA;
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
     * Retrieves the java interpreter path, based on java.home property
     * 
     * @return The path to the java interpreter
     */
    public String getJavaExecutable() {

	StringBuilder javaExecutablePath = new StringBuilder();
	javaExecutablePath.append(System
		.getProperty(CXJvmUtils.SYSPROP_JAVA_HOME));
	javaExecutablePath.append(File.separator);
	javaExecutablePath.append("bin");
	javaExecutablePath.append(File.separator);
	javaExecutablePath.append("java");

	if (CXOSUtils.isOsWindowsFamily()) {
	    javaExecutablePath.append(".exe");
	}

	return javaExecutablePath.toString();
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
     * org.psem2m.isolates.forker.impl.runners.AbstractRunner#doStartIsolate
     * (org. psem2m.isolates.commons.IIsolateConfiguration)
     */
    @Override
    public IProcessRef startIsolate(
	    final IIsolateConfiguration aIsolateConfiguration) throws Exception {

	// Non null list
	List<String> javaArguments = new ArrayList<String>();

	// Add isolate arguments, if any
	String[] isolateArguments = aIsolateConfiguration.getArguments();
	if (isolateArguments != null) {
	    javaArguments.addAll(Arrays.asList(isolateArguments));
	}

	// Working directory
	File workingDirectory = pPlatformDirsSvc
		.getIsolateWorkingDir(aIsolateConfiguration.getId());

	return runJava(javaArguments, aIsolateConfiguration.getEnvironment(),
		workingDirectory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() throws BundleException {

	pJavaExecutable = getJavaExecutable();

	// logs in the bundle logger
	pBundleForkerLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",
		toDescription());
    }
}
