/**
 * File:   ProcessBuilderRunner.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.forker.impl.processes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.psem2m.isolates.forker.IProcessRef;
import org.psem2m.isolates.forker.IProcessRunner;

/**
 * @author Thomas Calmant
 * 
 */
public class ProcessBuilderRunner implements IProcessRunner {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.forker.IProcessRunner#killProcess(org.psem2m.isolates
     * .forker.IProcessRef)
     */
    @Override
    public void killProcess(final IProcessRef aProcessReference) {

        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.forker.IProcessRunner#runProcess(java.lang.String,
     * java.lang.String[], java.util.Map, java.io.File)
     */
    @Override
    public IProcessRef runProcess(final String aExecutable,
            final String[] aArguments, final Map<String, String> aEnvironment,
            final File aWorkingDirectory) throws IOException {

        // Prepare the command line
        final List<String> commandLine = new ArrayList<String>();
        commandLine.add(aExecutable);

        if (aArguments != null) {
            for (final String argument : aArguments) {
                commandLine.add(argument);
            }
        }

        // Set up the process builder
        final ProcessBuilder builder = new ProcessBuilder(commandLine);
        builder.directory(aWorkingDirectory);

        if (aEnvironment != null && !aEnvironment.isEmpty()) {
            builder.environment().putAll(aEnvironment);
        }

        final Process javaRuntimeProcess = builder.start();
        return new ProcessRef(-1, javaRuntimeProcess);
    }
}
