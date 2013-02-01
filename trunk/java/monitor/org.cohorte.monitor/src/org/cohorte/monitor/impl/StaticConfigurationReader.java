/**
 * File:   StaticConfigurationReader.java
 * Author: Thomas Calmant
 * Date:   30 janv. 2013
 */
package org.cohorte.monitor.impl;

import java.io.File;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.monitor.api.IPlatformController;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.services.dirs.IFileFinderSvc;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;

/**
 * Loads an application configuration file and tells the platform controller to
 * start the described isolate
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-platform-static-isolates-factory")
public class StaticConfigurationReader {

    /** The isolate starter */
    @Requires
    private IPlatformController pController;

    /** The default file to load */
    @Property(name = "static-file", value = "")
    private String pDefaultFile;

    /** File finder */
    @Requires
    private IFileFinderSvc pFinder;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** FIXME: debug */
    @Requires
    private IPlatformDirsSvc pPlatform;

    /**
     * Loads isolates from the given configuration file and
     * 
     * @param aFile
     *            The configuration file to read
     */
    public void load(final String aFile) {

        if (aFile == null || aFile.isEmpty()) {
            // TODO: Bad argument
            return;
        }

        // TODO: handle future finder API
        final File[] confFiles = pFinder.find(aFile);
        if (confFiles == null || confFiles.length == 0) {
            // TODO: File not found
            return;
        }

        // TODO: parse the configuration file

        // TODO: call the controller
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Load the default file
        load(pDefaultFile);

        // Do it easily
        pLogger.logDebug(this, "TEST", "Trying to start isolate 'toto'");
        try {
            final String result = pController.startIsolate("osgi", "java",
                    "isolate", "Toto", pPlatform.getIsolateNode(), null, null,
                    null);
            pLogger.logDebug(this, "TEST", "UID=", result);
        } catch (final Throwable ex) {
            pLogger.logSevere(this, "TEST", "Error=", ex);
        }
    }
}
