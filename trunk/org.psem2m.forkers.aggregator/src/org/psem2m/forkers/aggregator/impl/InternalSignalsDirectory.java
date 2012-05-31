/**
 * File:   InternalSignalsDirectory.java
 * Author: Thomas Calmant
 * Date:   25 mai 2012
 */
package org.psem2m.forkers.aggregator.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.services.dirs.IFileFinderSvc;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster.EEmitterTargets;
import org.psem2m.isolates.services.remote.signals.ISignalsDirectory;

/**
 * A simple internal directory provider.
 * 
 * Uses a JSON configuration file. Handles aliases.
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-signals-internal-directory-factory", publicFactory = false)
@Provides(specifications = { ISignalsDirectory.class,
        IInternalSignalsDirectory.class })
@Instantiate(name = "psem2m-signals-internal-directory")
public class InternalSignalsDirectory implements ISignalsDirectory,
        IInternalSignalsDirectory {

    /** Forkers ID prefix */
    public static final String FORKER_PREFIX = "org.psem2m.internals.isolates.forker";

    /** Monitors ID prefix */
    public static final String MONITOR_PREFIX = "org.psem2m.internals.isolates.monitor";

    /** The configuration file finder */
    @Requires
    private IFileFinderSvc pFinder;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The configuration reader */
    private JsonDirectoryReader pReader;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalsDirectory#
     * getCurrentIsolateId()
     */
    @Override
    public String getCurrentIsolateId() {

        // Isolate ID can change on slave agent order
        return System.getProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_ID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.forkers.aggregator.impl.IInternalSignalsDirectory#getForkerForHost
     * (java.lang.String)
     */
    @Override
    public String getForkerForHost(final String aHostName) {

        final String[] isolates = pReader.getIsolatesForHost(aHostName);
        if (isolates == null) {
            return null;
        }

        for (final String isolate : isolates) {
            if (isolate.startsWith(FORKER_PREFIX)) {
                return isolate;
            }
        }

        // Not found
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalsDirectory#getIsolate
     * (java.lang.String)
     */
    @Override
    public String getIsolate(final String aIsolateId) {

        return pReader.getIsolateAccess(aIsolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalsDirectory#getIsolates
     * (java.util.Collection)
     */
    @Override
    public String[] getIsolates(final Collection<String> aIsolatesIds) {

        if (aIsolatesIds == null) {
            return null;
        }

        return getIsolates(aIsolatesIds.toArray(new String[0]));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalsDirectory#getIsolates
     * (org.psem2m.isolates.services.remote.signals.ISignalBroadcaster.
     * EEmitterTargets)
     */
    @Override
    public String[] getIsolates(final EEmitterTargets aTargets) {

        switch (aTargets) {

        case MONITORS:
            return getIsolates("org.psem2m.internals.monitor");

        case FORKER:
            return getIsolates(FORKER_PREFIX);
        }

        // Ignore others
        return null;
    }

    /**
     * Retrieves the accesses to all isolates starting with the given prefix
     * 
     * @param aPrefix
     *            A prefix
     * @return All corresponding isolates, or null
     */
    protected String[] getIsolates(final String aPrefix) {

        final List<String> result = new ArrayList<String>();
        final Map<String, String> directory = pReader.getDirectoryCopy();

        for (final Entry<String, String> entry : directory.entrySet()) {

            if (entry.getKey().startsWith(aPrefix)) {
                result.add(entry.getValue());
            }
        }

        if (result.isEmpty()) {
            return null;
        }

        return result.toArray(new String[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalsDirectory#getIsolates
     * (java.lang.String[])
     */
    @Override
    public String[] getIsolates(final String[] aIsolatesIds) {

        if (aIsolatesIds == null) {
            return null;
        }

        final List<String> isolates = new ArrayList<String>(aIsolatesIds.length);

        for (final String isolateId : aIsolatesIds) {
            final String access = pReader.getIsolateAccess(isolateId);
            if (access != null) {
                isolates.add(access);
            }
        }

        return isolates.toArray(new String[0]);
    }

    /**
     * Component invalidation
     */
    @Invalidate
    public void invalidate() {

        pReader.reset();
        pReader.setLogger(null);
        pReader = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalsDirectory#reloadDirectory
     * ()
     */
    @Override
    public boolean reloadDirectory() {

        boolean result = false;

        final File[] confFiles = pFinder.find("conf/internal-directory.js");

        pLogger.logSevere(this, "reloadDir", "confFiles=", confFiles);

        if (confFiles != null) {

            for (final File confFile : confFiles) {
                try {
                    result |= pReader.loadFile(confFile);

                } catch (final FileNotFoundException ex) {
                    // Ignore this error
                    pLogger.logWarn(this, "reloadDirectory",
                            "File not found: ", confFile.getAbsolutePath());
                    continue;
                }
            }
        }

        return result;
    }

    /**
     * Component validation
     */
    @Validate
    public void validate() {

        pReader = new JsonDirectoryReader();
        pReader.setLogger(pLogger);
        reloadDirectory();
    }
}
