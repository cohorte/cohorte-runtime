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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class InternalSignalsDirectory implements IInternalSignalsDirectory {

    /** Forkers ID prefix */
    public static final String FORKER_PREFIX = "org.psem2m.internals.isolates.forker";

    /** Monitors ID prefix */
    public static final String MONITOR_PREFIX = "org.psem2m.internals.isolates.monitor";

    /** The configuration file finder */
    @Requires
    private IFileFinderSvc pFinder;

    /** Alias -&gt; Host registry */
    private final Map<String, String> pHostAliases = new HashMap<String, String>();

    /** Host -&gt; Isolates registry */
    private final Map<String, Set<String>> pHostIsolates = new HashMap<String, Set<String>>();

    /** Isolate -&gt; Access URL registry */
    private final Map<String, String> pIsolateAccesses = new HashMap<String, String>();

    /** Isolate -&gt; Host registry */
    private final Map<String, String> pIsolatesHost = new HashMap<String, String>();

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.forkers.aggregator.impl.IInternalSignalsDirectory#addIsolate
     * (java.lang.String, java.lang.String, int)
     */
    @Override
    public synchronized boolean addIsolate(final String aIsolateId,
            final String aHostName, final int aPort) {

        if (aIsolateId == null || aIsolateId.isEmpty() || aHostName == null
                || aHostName.isEmpty()) {

            // Invalid parameters
            pLogger.logSevere(this, "addIsolate", "Can't register isolate id=",
                    aIsolateId, "hostname=", aHostName);
            return false;
        }

        // Forge the access URL
        final StringBuilder builder = new StringBuilder();
        builder.append("http://");
        builder.append(aHostName);
        builder.append(":");
        builder.append(aPort);

        // Store the isolate
        final String realHostName = getRealHost(aHostName);

        pIsolatesHost.put(aIsolateId, realHostName);
        pIsolateAccesses.put(aIsolateId, builder.toString());

        Set<String> hostList = pHostIsolates.get(realHostName);
        if (hostList == null) {
            // Create the list if needed
            hostList = new HashSet<String>();
            pHostIsolates.put(aHostName, hostList);
        }

        hostList.add(aIsolateId);

        return true;
    }

    /**
     * Retrieves all isolates IDs that starts with the given prefix.
     * 
     * @param aPrefix
     *            A string prefix
     * @return All matching isolates, or null
     */
    public synchronized String[] getAllPrefixedIds(final String aPrefix) {

        final List<String> matching = new ArrayList<String>();

        for (final String isolate : pIsolateAccesses.keySet()) {
            if (isolate.startsWith(aPrefix)) {
                matching.add(isolate);
            }
        }

        if (matching.isEmpty()) {
            // Nothing to return
            return null;
        }

        return matching.toArray(new String[0]);
    }

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
    public synchronized String getForkerForHost(final String aHostName) {

        final Set<String> isolates = pHostIsolates.get(getRealHost(aHostName));
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
     * org.psem2m.forkers.aggregator.impl.IInternalSignalsDirectory#getForkers()
     */
    @Override
    public String[] getForkers() {

        return getAllPrefixedIds(FORKER_PREFIX);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.forkers.aggregator.impl.IInternalSignalsDirectory#
     * getHostForIsolate(java.lang.String)
     */
    @Override
    public String getHostForIsolate(final String aIsolateId) {

        return getRealHost(pIsolatesHost.get(aIsolateId));
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

        return pIsolateAccesses.get(aIsolateId);
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
            return getAllPrefixedIds(MONITOR_PREFIX);

        case FORKER:
            return getAllPrefixedIds(FORKER_PREFIX);
        }

        // Ignore others
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalsDirectory#getIsolates
     * (java.lang.String[])
     */
    @Override
    public synchronized String[] getIsolates(final String[] aIsolatesIds) {

        if (aIsolatesIds == null) {
            return null;
        }

        final List<String> isolates = new ArrayList<String>(aIsolatesIds.length);

        for (final String isolateId : aIsolatesIds) {
            final String access = getIsolate(isolateId);
            if (access != null) {
                isolates.add(access);
            }
        }

        return isolates.toArray(new String[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.forkers.aggregator.impl.IInternalSignalsDirectory#
     * getIsolatesForHost(java.lang.String)
     */
    @Override
    public String[] getIsolatesForHost(final String aHost) {

        final Set<String> isolates = pHostIsolates.get(getRealHost(aHost));
        if (isolates == null || isolates.isEmpty()) {
            return null;
        }

        return isolates.toArray(new String[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.forkers.aggregator.impl.IInternalSignalsDirectory#getMonitors
     * ()
     */
    @Override
    public String[] getMonitors() {

        return getAllPrefixedIds(MONITOR_PREFIX);
    }

    /**
     * Retrieves the real name of the given host, as it could be an alias
     * 
     * @param aHostName
     *            A host name
     * @return The real host name
     */
    protected String getRealHost(final String aHostName) {

        synchronized (pHostAliases) {
            final String host = pHostAliases.get(aHostName);
            if (host != null) {
                // Recursive aliases resolution
                return getRealHost(host);
            }
        }

        return aHostName;
    }

    /**
     * Component invalidation
     */
    @Invalidate
    public void invalidate() {

        // Clear registries
        synchronized (pHostAliases) {
            pHostAliases.clear();
        }

        pHostIsolates.clear();
        pIsolateAccesses.clear();
        pIsolatesHost.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.services.remote.signals.ISignalsDirectory#reloadDirectory
     * ()
     */
    @Override
    public synchronized boolean reloadDirectory() {

        final JsonDirectoryReader reader = new JsonDirectoryReader(this,
                pLogger);

        boolean result = false;

        final File[] confFiles = pFinder.find("conf/internal-directory.js");
        if (confFiles != null) {

            for (final File confFile : confFiles) {
                try {
                    result |= reader.loadFile(confFile);

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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.forkers.aggregator.impl.IInternalSignalsDirectory#removeForker
     * (java.lang.String)
     */
    @Override
    public synchronized boolean removeIsolate(final String aForkerId) {

        if (aForkerId == null) {
            return false;
        }

        boolean success = false;

        // Store the isolate host
        final String host = pIsolatesHost.get(aForkerId);

        // Unregister isolate
        if (pIsolateAccesses.remove(aForkerId) != null) {
            // Isolate was known at least here
            success = true;
        }

        if (pIsolatesHost.remove(aForkerId) != null) {
            // Isolate was known at least here
            success = true;
        }

        final Set<String> hostIsolates = pHostIsolates.get(host);
        if (hostIsolates != null) {
            hostIsolates.remove(aForkerId);

            // Isolate was known at least here
            success = true;
        }

        return success;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.forkers.aggregator.impl.IInternalSignalsDirectory#setHostAlias
     * (java.lang.String, java.lang.String)
     */
    @Override
    public void setHostAlias(final String aHost, final String aAlias) {

        synchronized (pHostAliases) {
            pHostAliases.put(aAlias, getRealHost(aHost));
        }
    }

    /**
     * Component validation
     */
    @Validate
    public void validate() {

        reloadDirectory();
    }
}
