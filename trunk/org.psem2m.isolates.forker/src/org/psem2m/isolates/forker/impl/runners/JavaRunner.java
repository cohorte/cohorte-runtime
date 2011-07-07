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

import org.psem2m.isolates.commons.IIsolateConfiguration;
import org.psem2m.isolates.commons.IPlatformConfiguration;
import org.psem2m.isolates.forker.IProcessRef;
import org.psem2m.isolates.forker.IProcessRunner;

/**
 * Runs the Java interpreter for the given isolate
 * 
 * @author Thomas Calmant
 */
public class JavaRunner extends AbstractRunner {

    /** The Java executable */
    private String pJavaExecutable;

    /**
     * Runs a Java interpreter with the given parameters
     * 
     * @param aArguments
     *            Interpreter arguments
     * @param aEnvironment
     *            Process environment
     * @param aWorkingDirectory
     *            Process working directory
     * @return A reference to the java interpreter process
     * @throws IOException
     *             An error occurred while trying to run Java
     */
    public IProcessRef runJava(final String[] aArguments,
	    final Map<String, String> aEnvironment, final File aWorkingDirectory)
	    throws IOException {

	IProcessRunner runner = getProcessRunner();
	return runner.runProcess(pJavaExecutable, aArguments, aEnvironment,
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
     * org.psem2m.isolates.forker.IIsolateRunner#setConfiguration(org.psem2m
     * .isolates.commons.IPlatformConfiguration)
     */
    @Override
    public void setConfiguration(
	    final IPlatformConfiguration aPlatformConfiguration) {

	super.setConfiguration(aPlatformConfiguration);
	pJavaExecutable = aPlatformConfiguration.getJavaExecutable();
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

	return runJava(aIsolateConfiguration.getArguments(),
		aIsolateConfiguration.getEnvironment(), null);
    }
}
