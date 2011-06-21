/**
 * File:   AbstractOSGiRunner.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.forker.impl.runners;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.psem2m.isolates.commons.IBundleRef;
import org.psem2m.isolates.commons.IIsolateConfiguration;
import org.psem2m.isolates.forker.IProcessRef;

/**
 * Abstract class to ease OSGi framework runners implementation. Abstract
 * methods are guaranteed to be called in the order getMainBundle,
 * prepareProfile, getXXX()
 * 
 * @author Thomas Calmant
 */
public abstract class AbstractOSGiRunner extends JavaRunner {

    /** The configuration of the current isolate */
    private IIsolateConfiguration pIsolateConfiguration;

    /**
     * Retrieves the arguments to use while running the framework.
     * "-jar main.jar" is not needed.
     * 
     * @return The framework arguments
     */
    protected abstract List<String> getArguments();

    /**
     * Retrieves the environment variables of the framework
     * 
     * @return The environment variables of the framework
     */
    protected abstract Map<String, String> getEnvironment();

    /**
     * Retrieves the configuration of the current isolate
     * 
     * @return The configuration of the current isolate
     */
    protected IIsolateConfiguration getIsolateConfiguration() {
	return pIsolateConfiguration;
    }

    /**
     * Retrieves the main bundle of the OSGi framework
     * 
     * @return The main bundle of the framework
     */
    protected abstract IBundleRef getMainBundle();

    /**
     * Prepares the isolate profile. The working directory has already been
     * created.
     * 
     * @param aWorkingDirectory
     *            The isolate working directory
     * @throws Exception
     *             An error occurred while preparing the isolate profile
     */
    protected abstract void prepareProfile(File aWorkingDirectory)
	    throws Exception;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.forker.impl.runners.JavaRunner#doStartIsolate(org
     * .psem2m .isolates.commons.IIsolateConfiguration)
     */
    @Override
    public final synchronized IProcessRef startIsolate(
	    final IIsolateConfiguration aIsolateConfiguration) throws Exception {

	pIsolateConfiguration = aIsolateConfiguration;

	final IBundleRef mainBundle = getMainBundle();
	final File frameworkFile = mainBundle.getFile();

	// Test the framework main bundle
	if (frameworkFile == null || !frameworkFile.isFile()) {
	    throw new FileNotFoundException("Can't find the main bundle : '"
		    + mainBundle + "'");
	}

	// Prepare the working directory
	File workingDirectory = createWorkingDirectory(pIsolateConfiguration
		.getId());

	// Prepare the isolate profile
	prepareProfile(workingDirectory);

	// Run it
	IProcessRef result = runJavaJar(frameworkFile, getArguments(),
		getEnvironment(), workingDirectory);

	return result;
    }
}
