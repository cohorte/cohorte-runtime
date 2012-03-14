/**
 * File:   PythonRunner.java
 * Author: Thomas Calmant
 * Date:   14 mars 2012
 */
package org.psem2m.isolates.forker.impl.runners;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.forker.IIsolateRunner;
import org.psem2m.isolates.forker.IProcessRef;
import org.psem2m.isolates.forker.IProcessRunner;
import org.psem2m.isolates.forker.impl.processes.ProcessBuilderRunner;
import org.psem2m.isolates.services.conf.IIsolateDescr;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;

/**
 * Runs a Python 2 or Python 3 interpreter for the given isolate
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-runner-python-factory", publicFactory = false)
@Provides(specifications = IIsolateRunner.class)
@Instantiate(name = "psem2m-runner-python")
public class PythonRunner extends CPojoBase implements IIsolateRunner {

    /** Isolate ID environment variable */
    public static final String ENV_ISOLATE_ID = "PSEM2M_ISOLATE_ID";

    /** Platform base environment variable */
    public static final String ENV_PLATFORM_BASE = "PSEM2M_BASE";

    /** Platform home environment variable */
    public static final String ENV_PLATFORM_HOME = "PSEM2M_HOME";

    /** Python 2 isolate kind */
    public static final String SUPPORTED_PYTHON_2 = "python";

    /** Python 3 isolate kind */
    public static final String SUPPORTED_PYTHON_3 = "python3";

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The platform directory service */
    @Requires
    private IPlatformDirsSvc pPlatformDirsSvc;

    /** Python 2 interpreter path */
    private String pPython2Interpreter;

    /** Python 3 interpreter path */
    private String pPython3Interpreter;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.forker.IIsolateRunner#canRun(java.lang.String)
     */
    @Override
    public boolean canRun(final String aIsolateKind) {

        return getInterpreter(aIsolateKind) != null;
    }

    /**
     * Looks for Python 2.x and 3.x interpreters in the path
     */
    protected void findPythonInterpreters() {

        final String[] python2_names = { "python", "python2", "python26",
                "python27", "python2.6", "python2.7" };

        final String[] python3_names = { "python3", "python31", "python32",
                "python3.1", "python3.2" };

        final String[] paths = { "/usr/bin", "/usr/local/bin", "/bin" };

        for (final String path : paths) {

            if (pPython2Interpreter == null) {
                for (final String python2_name : python2_names) {
                    final File file = new File(path, python2_name);
                    if (file.exists()) {

                        try {
                            // Found !
                            pPython2Interpreter = file.getCanonicalFile()
                                    .getAbsolutePath();
                            break;

                        } catch (final IOException e) {
                            // Error looking for the real path
                            pPython2Interpreter = null;
                        }
                    }
                }
            }

            if (pPython3Interpreter == null) {
                for (final String python3_name : python3_names) {
                    final File file = new File(path, python3_name);
                    if (file.exists()) {

                        try {
                            // Found !
                            pPython3Interpreter = file.getCanonicalFile()
                                    .getAbsolutePath();
                            break;

                        } catch (final IOException e) {
                            // Error looking for the real path
                            pPython3Interpreter = null;
                        }
                    }
                }
            }
        }
    }

    /**
     * Retrieves the path to the Python interpreter matching the given kind of
     * isolate
     * 
     * @param aKind
     *            A kind of isolate
     * @return The path to the Python interpreter, or null
     */
    protected String getInterpreter(final String aKind) {

        if (SUPPORTED_PYTHON_2.equalsIgnoreCase(aKind)) {
            // Python 2.x
            return pPython2Interpreter;

        } else if (SUPPORTED_PYTHON_3.equalsIgnoreCase(aKind)) {
            // Python 3.x
            return pPython3Interpreter;
        }

        return null;
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
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pPython2Interpreter = null;
        pPython3Interpreter = null;

        pLogger.logInfo(this, "invalidatePojo", "Python isolate runner Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.forker.IIsolateRunner#startIsolate(org.psem2m.isolates
     * .services.conf.IIsolateDescr)
     */
    @Override
    public IProcessRef startIsolate(final IIsolateDescr aIsolateConfiguration)
            throws Exception {

        /* Get the runner */
        final IProcessRunner runner = getProcessRunner();
        if (runner == null) {
            return null;
        }

        /* Choose the right interpreter */
        final String interpreter = getInterpreter(aIsolateConfiguration
                .getKind());
        if (interpreter == null) {
            // No corresponding interpreter
            return null;
        }

        /* Process environment */
        final Map<String, String> environment = new HashMap<String, String>();
        // PSEM2M Home
        environment.put(ENV_PLATFORM_HOME, pPlatformDirsSvc
                .getPlatformHomeDir().getAbsolutePath());

        // PSEM2M Base
        environment.put(ENV_PLATFORM_BASE, pPlatformDirsSvc
                .getPlatformBaseDir().getAbsolutePath());

        // Isolate ID
        environment.put(ENV_ISOLATE_ID, aIsolateConfiguration.getId());

        /* Interpreter arguments */
        final List<String> arguments = new ArrayList<String>();
        arguments.addAll(aIsolateConfiguration.getVMArgs());

        /* Working directory */
        final File workingDirectory = pPlatformDirsSvc
                .getIsolateWorkingDir(aIsolateConfiguration.getId());

        // Create the working directory
        if (workingDirectory.exists()) {
            Utilities.removeDirectory(workingDirectory);
        }
        workingDirectory.mkdirs();

        // Start the process
        return runner.runProcess(interpreter, arguments.toArray(new String[0]),
                environment, workingDirectory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pPython2Interpreter = null;
        pPython3Interpreter = null;
        findPythonInterpreters();

        pLogger.logInfo(this, "validatePojo", "Python isolate runner Ready");
    }
}
