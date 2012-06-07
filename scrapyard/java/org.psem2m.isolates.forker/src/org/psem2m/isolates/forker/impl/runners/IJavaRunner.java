/**
 * File:   IJavaRunner.java
 * Author: Thomas Calmant
 * Date:   28 juil. 2011
 */
package org.psem2m.isolates.forker.impl.runners;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.psem2m.isolates.forker.IIsolateRunner;
import org.psem2m.isolates.forker.IProcessRef;

/**
 * Java specific runner
 * 
 * @author Thomas Calmant
 */
public interface IJavaRunner extends IIsolateRunner {

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
    IProcessRef runJava(final List<String> aArguments,
	    final Map<String, String> aEnvironment, final File aWorkingDirectory)
	    throws IOException;

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
    IProcessRef runJavaJar(final File aJarFile,
	    final List<String> aJavaOptions, final List<String> aJarArguments,
	    final Map<String, String> aEnvironment, final File aWorkingDirectory)
	    throws IOException;

}
