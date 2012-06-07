/**
 * File:   IProcessRunner.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.forker;

import java.io.File;
import java.io.IOException;
import java.util.Map;



/**
 * Describes a process runner
 * 
 * @author Thomas Calmant
 */
public interface IProcessRunner {

    /**
     * Kills the given process
     * 
     * @param aProcessReference
     *            A reference to the process to kill
     */
    void killProcess(IProcessRef aProcessReference);

    /**
     * Starts an executable
     * 
     * @param aExecutable
     *            Complete path to the executable file to run
     * @param aArguments
     *            Executable arguments
     * @param aEnvironment
     *            Environment of the process
     * @param aWorkingDirectory
     *            Working directory of the process
     * @return A reference to the new process, null on error.
     * @throws IOException
     *             An error occurred while trying to run the given executable
     */
    IProcessRef runProcess(String aExecutable, String[] aArguments,
	    Map<String, String> aEnvironment, File aWorkingDirectory)
	    throws IOException;
}
