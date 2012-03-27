/**
 * 
 */
package org.psem2m.isolates.slave.agent.core;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.base.bundles.BundleInfo;
import org.psem2m.isolates.base.bundles.BundleRef;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.base.isolates.boot.IBootstrapMessageSender;
import org.psem2m.isolates.base.isolates.boot.IsolateStatus;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.conf.beans.BundleDescription;
import org.psem2m.isolates.services.conf.beans.IsolateDescription;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;
import org.psem2m.isolates.services.remote.signals.ISignalData;
import org.psem2m.isolates.services.remote.signals.ISignalListener;
import org.psem2m.isolates.services.remote.signals.ISignalReceiver;
import org.psem2m.isolates.slave.agent.ISvcAgent;

/**
 * Implementation of the isolate Agent
 * 
 * @author Thomas Calmant
 */
public class AgentCore extends CPojoBase implements ISvcAgent, ISignalListener,
        BundleListener {

    /** Bootstrap message sender */
    private IBootstrapMessageSender pBootstrapSender;

    /** Agent bundle context */
    private BundleContext pBundleContext;

    /** Bundle finder, injected by iPOJO */
    private IBundleFinderSvc pBundleFinderSvc;

    /** Configuration service, injected by iPOJO */
    private ISvcConfig pConfigurationSvc;

    /** The agent core critical section flag */
    private AtomicBoolean pCriticalSection = new AtomicBoolean(false);

    /** Bundles installed by the agent : bundle ID -&gt; bundle description map */
    private final Map<Long, BundleDescription> pInstalledBundles = new LinkedHashMap<Long, BundleDescription>();

    /** Isolate logger, injected by iPOJO */
    private IIsolateLoggerSvc pIsolateLoggerSvc;

    /** Platform directories service, injected by iPOJO */
    private IPlatformDirsSvc pPlatformDirsSvc;

    /** The scheduler */
    private ScheduledExecutorService pScheduler;

    /** Signal broadcaster */
    private ISignalBroadcaster pSignalBroadcaster;

    /** Signal receiver */
    private ISignalReceiver pSignalReceiver;

    /** The update timeouts */
    private final Map<Long, ScheduledFuture<?>> pUpdateTimeouts = new HashMap<Long, ScheduledFuture<?>>();

    /**
     * Sets up the agent (called by iPOJO)
     * 
     * @param aBundleContext
     *            Bundle context
     */
    public AgentCore(final BundleContext aBundleContext) {

        super();
        pBundleContext = aBundleContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.
     * BundleEvent)
     */
    @Override
    public void bundleChanged(final BundleEvent aBundleEvent) {

        // Keep a reference to the bundle ID
        final long bundleId = aBundleEvent.getBundle().getBundleId();

        if (pCriticalSection.get()) {
            // In critical section : ignore events, cancel timeouts
            cancelTimeout(bundleId);
            return;
        }

        final BundleDescription bundleDescr;
        synchronized (pInstalledBundles) {
            // Find the monitored bundle description
            bundleDescr = pInstalledBundles.get(bundleId);
        }

        if (bundleDescr == null) {
            // Non-monitored bundle
            return;
        }

        switch (aBundleEvent.getType()) {

        case BundleEvent.STARTED:
        case BundleEvent.UPDATED: {
            /*
             * An updated bundle has be (re)started, cancel the update timeout
             */
            cancelTimeout(bundleId);
            break;
        }

        case BundleEvent.UNRESOLVED:
        case BundleEvent.STOPPED: {
            /*
             * A bundle has stopped or unresolved. It can be for an update so
             * wait a little
             * 
             * Prepare a ScheduledFuture based timeout
             */
            // Cancel previous timeout
            cancelTimeout(bundleId);

            // Prepare a new one
            final ScheduledFuture<?> future = pScheduler.schedule(
                    new Runnable() {

                        @Override
                        public void run() {

                            // Remove ourself from the timeouts map
                            pUpdateTimeouts.remove(bundleId);

                            // Try to restart the bundle
                            try {
                                startBundle(bundleId);

                            } catch (final Exception e) {
                                pIsolateLoggerSvc.logSevere(this,
                                        "bundleChanged",
                                        "Can't restart bundle", bundleId, e);

                                if (!bundleDescr.getOptional()) {
                                    pIsolateLoggerSvc.logSevere(this,
                                            "bundleChanged", "Failed bundle",
                                            bundleId,
                                            "is not optional : reset isolate.");

                                    // Reset isolate on error
                                    safeIsolateReset();
                                }
                            }
                        }

                    }, 1500, TimeUnit.MILLISECONDS);

            pUpdateTimeouts.put(bundleId, future);
            break;
        }

        case BundleEvent.UNINSTALLED: {
            /*
             * A bundle has been uninstalled, bad for us
             */
            cancelTimeout(bundleId);
            handleUninstalledBundleEvent(bundleDescr);
            break;
        }

        default:
            // Ignore other cases, if any
            break;
        }
    }

    /**
     * Cancels a bundle event timeout
     * 
     * @param aBundleId
     *            Bundle ID
     */
    protected void cancelTimeout(final long aBundleId) {

        final ScheduledFuture<?> future = pUpdateTimeouts.get(aBundleId);
        if (future != null) {
            final boolean cancelled = future.cancel(false);

            pIsolateLoggerSvc.logDebug(this, "bundleChanged",
                    "Bundle timeout cancellation=", cancelled);
        }

        // Remove it from the map
        pUpdateTimeouts.remove(aBundleId);
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
     * Prepares the URL to install the given bundle. Uses the given file path,
     * if any, then the given symbolic name.
     * 
     * @param aBundleDescr
     *            A description of the bundle to install
     * @return The install URL of the bundle, null on error.
     */
    public String findBundleURL(final BundleDescription aBundleDescr) {

        final String bundleFileName = aBundleDescr.getFile();
        BundleRef bundleRef = null;

        if (bundleFileName != null && !bundleFileName.isEmpty()) {
            // A file name was given
            bundleRef = pBundleFinderSvc.findBundle(bundleFileName);
        }

        if (bundleRef == null) {
            // No corresponding file was found, use the symbolic name
            bundleRef = pBundleFinderSvc.findBundle(aBundleDescr
                    .getSymbolicName());
        }

        if (bundleRef == null) {
            // Bundle not found
            return null;
        }

        try {
            // Retrieve its URL
            return bundleRef.getUri().toURL().toString();

        } catch (final MalformedURLException ex) {
            pIsolateLoggerSvc.logWarn(this, "findBundleURL",
                    "Error preparing bundle URL", ex);
        }

        // Return null on error
        return null;
    }

    /**
     * Retrieves the given OSGi bundle representation
     * 
     * @param aBundleId
     *            Bundle ID
     * @return The OPSGi bundle or null if the ID is invalid.
     * 
     * @see BundleContext#getBundle(long)
     */
    public Bundle getBundle(final long aBundleId) {

        return pBundleContext.getBundle(aBundleId);
    }

    /**
     * Retrieves the context of the bundle with the given ID.
     * 
     * @param aBundleId
     *            A bundle ID
     * @return The context of the bundle with the given ID, null if not found
     */
    public BundleInfo getBundleInfo(final long aBundleId) {

        final Bundle bundle = pBundleContext.getBundle(aBundleId);
        if (bundle == null) {
            return null;
        }

        return new BundleInfo(bundle);
    }

    /**
     * Retrieves all bundles information
     * 
     * @return Bundles information
     */
    public BundleInfo[] getBundlesState() {

        final Bundle[] bundles = pBundleContext.getBundles();
        final BundleInfo[] bundlesInfo = new BundleInfo[bundles.length];

        int i = 0;
        for (final Bundle bundle : bundles) {
            bundlesInfo[i] = new BundleInfo(bundle);
            i++;
        }

        return bundlesInfo;
    }

    /**
     * Retrieves the bundles successfully installed by this agent, as a bundle
     * ID -&gt; Bundle description map.
     * 
     * @return The installed bundles.
     */
    public Map<Long, BundleDescription> getInstalledBundles() {

        return pInstalledBundles;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalListener#
     * handleReceivedSignal(java.lang.String,
     * org.psem2m.isolates.services.remote.signals.ISignalData)
     */
    @Override
    public void handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        if (aSignalName.equals(ISignalsConstants.ISOLATE_STOP_SIGNAL)) {

            // Log what happened
            pIsolateLoggerSvc.logInfo(this, "handleReceivedSignal",
                    "STOP signal received. Killing isolate.");

            // Enter critical section
            if (!pCriticalSection.compareAndSet(false, true)) {

                pIsolateLoggerSvc.logSevere(this, "handle STOP signal",
                        "Killing isolate while already in critical section !");

                // Force value (just in case...)
                pCriticalSection.set(true);
            }

            // Kill ourselves
            killIsolate();
        }
    }

    /**
     * Called when an UNINSTALLED signal has been triggered for a bundle
     * 
     * @param aBundleDescr
     *            The internal bundle description
     */
    protected void handleUninstalledBundleEvent(
            final BundleDescription aBundleDescr) {

        if (aBundleDescr.getOptional()) {
            // Just log it
            pIsolateLoggerSvc.logWarn(this, "handleUninstalledBundleEvent",
                    "The optional bundle", aBundleDescr.getSymbolicName(),
                    "has been uninstalled.");

        } else {
            // Very bad
            pIsolateLoggerSvc.logSevere(this, "handleUninstalledBundleEvent",
                    "The *mandatory* bundle", aBundleDescr.getSymbolicName(),
                    "has been uninstalled.");

            // Try to re-install it
            boolean needsIsolateReset = true;
            final String bundleFile = findBundleURL(aBundleDescr);
            if (bundleFile != null && !bundleFile.isEmpty()) {
                try {
                    // Re-install the bundle (new ID)
                    final long bundleId = installBundle(bundleFile);

                    // Add it in the map
                    pInstalledBundles.put(bundleId, aBundleDescr);

                    // Start it (needs to have the description in the map)
                    startBundle(bundleId);

                    // All done
                    needsIsolateReset = false;

                } catch (final BundleException e) {
                    pIsolateLoggerSvc.logSevere(this,
                            "handleUninstalledBundleEvent",
                            "Error trying to reinstall",
                            aBundleDescr.getSymbolicName(), ": Reset isolate",
                            e);
                }

            } else {
                // No valid file path found
                pIsolateLoggerSvc.logSevere(this,
                        "handleUninstalledBundleEvent",
                        "No bundle file found for",
                        aBundleDescr.getSymbolicName(), ": Reset isolate");
            }

            if (needsIsolateReset) {
                safeIsolateReset();
            }
        }

    }

    /**
     * Installs the given bundle
     * 
     * @param aBundleUrl
     *            A URL (generally file:...) to the bundle file
     * @return The bundle ID
     * @throws BundleException
     *             An error occurred while installing the bundle
     */
    public long installBundle(final String aBundleUrl) throws BundleException {

        return pBundleContext.installBundle(aBundleUrl).getBundleId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() {

        // Unregister the bundle listener
        pBundleContext.removeBundleListener(this);

        // Stop the scheduled timeouts
        pScheduler.shutdownNow();
        pScheduler = null;

        pUpdateTimeouts.clear();

        // Send the stop signal
        final IsolateStatus status = pBootstrapSender.sendStatus(
                IsolateStatus.STATE_AGENT_STOPPED, 100);

        pSignalBroadcaster.sendData(
                ISignalBroadcaster.EEmitterTargets.MONITORS,
                ISignalsConstants.ISOLATE_STATUS_SIGNAL, status);
    }

    /**
     * Tests if the given bundle is a fragment. Fragment bundles can't be
     * started.
     * 
     * @param aBundle
     *            Bundle to be tested
     * @return True if the bundle is a fragment
     */
    public boolean isFragment(final Bundle aBundle) {

        return aBundle.getHeaders().get(Constants.FRAGMENT_HOST) != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.slave.agent.ISvcAgent#killIsolate()
     */
    @Override
    public void killIsolate() {

        pIsolateLoggerSvc.logInfo(this, "killIsolate",
                "Kill this isolate [%s]", pPlatformDirsSvc.getIsolateId());

        // Neutralize the isolate
        neutralizeIsolate();

        try {
            // Stop the platform
            pBundleContext.getBundle(0).stop();

        } catch (final BundleException e) {
            // Damn
            pIsolateLoggerSvc.logSevere(this, "validatePojo",
                    "Can't stop the framework", e);

            try {
                // Hara kiri
                pBundleContext.getBundle().stop();

            } catch (final BundleException e1) {
                pIsolateLoggerSvc.logSevere(this, "validatePojo",
                        "Agent suicide FAILED (you're in trouble)", e1);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.slave.agent.ISvcAgent#neutralizeIsolate()
     */
    @Override
    public void neutralizeIsolate() {

        synchronized (pInstalledBundles) {
            // Synchronized map, to avoid messing with the guardian

            final List<Entry<Long, BundleDescription>> wList = new ArrayList<Entry<Long, BundleDescription>>(
                    pInstalledBundles.entrySet());

            Long wBundleId;
            for (int wI = wList.size() - 1; wI > -1; wI--) {
                wBundleId = wList.get(wI).getKey();

                try {
                    pIsolateLoggerSvc.logInfo(this, "neutralizeIsolate",
                            "BundleId=[%d]", wBundleId);
                    uninstallBundle(wBundleId);

                } catch (final BundleException ex) {
                    // Only log the error
                    pIsolateLoggerSvc.logWarn(this, "neutralizeIsolate",
                            "Error stopping bundle : ",
                            pInstalledBundles.get(wBundleId).getSymbolicName(),
                            ex);
                }
            }

            /*
             * Clear the map (outside the loop : do not touch the map in a
             * foreach loop)
             */
            pInstalledBundles.clear();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.slave.agent.ISvcAgent#prepareIsolate()
     */
    @Override
    public void prepareIsolate() throws Exception {

        // Find the isolate ID
        final String isolateId = pPlatformDirsSvc.getIsolateId();
        if (isolateId == null) {
            throw new IllegalArgumentException("No explicit isolate ID found");
        }

        // Prepare it
        prepareIsolate(isolateId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.slave.agent.ISvcAgent#prepareIsolate(java.lang.String
     * )
     */
    @Override
    public void prepareIsolate(final String aIsolateId) throws Exception {

        // Read the configuration
        final IsolateDescription isolateDescr = pConfigurationSvc
                .getApplication().getIsolate(aIsolateId);
        if (isolateDescr == null) {
            throw new IllegalArgumentException("Isolate '" + aIsolateId
                    + "' is not defined in the configuration.");
        }

        // Update the system property in any case, before installing bundles
        System.setProperty(IPlatformProperties.PROP_PLATFORM_ISOLATE_ID,
                aIsolateId);

        // FIXME Update the HTTP port system properties
        setupHttpProperties(isolateDescr);

        synchronized (pInstalledBundles) {
            // Synchronized map, to avoid messing with the guardian

            // First loop : install bundles
            for (final BundleDescription bundleDescr : isolateDescr
                    .getBundles()) {

                final String bundleName = bundleDescr.getSymbolicName();
                final String bundleUrl = findBundleURL(bundleDescr);
                if (bundleUrl != null) {
                    // Bundle found

                    try {

                        // set the properties of the bundle to be installed
                        if (bundleDescr.hasProperties()) {
                            System.getProperties().putAll(
                                    bundleDescr.getProperties());
                        }

                        // Install bundle
                        final long bundleId = installBundle(bundleUrl);

                        // Update the list of installed bundles
                        pInstalledBundles.put(bundleId, bundleDescr);

                    } catch (final BundleException ex) {

                        switch (ex.getType()) {
                        case BundleException.DUPLICATE_BUNDLE_ERROR:
                            // Simply log this
                            pIsolateLoggerSvc.logInfo(this, "prepareIsolate",
                                    "Bundle ", bundleName,
                                    " is already installed");

                            break;

                        default:
                            if (bundleDescr.getOptional()) {
                                // Ignore error if the bundle is optional
                                pIsolateLoggerSvc.logWarn(this,
                                        "prepareIsolate", "Error installing ",
                                        bundleName, ex);

                            } else {
                                // Propagate the exception
                                throw ex;
                            }
                        }
                    }

                } else {
                    // Bundle not found : throw an error if it's mandatory

                    if (bundleDescr.getOptional()) {
                        // Simply log
                        pIsolateLoggerSvc.logWarn(this, "prepareIsolate",
                                "Bundle not found : ", bundleName);

                    } else {
                        // Severe error
                        throw new FileNotFoundException("Can't find bundle : "
                                + bundleName);
                    }
                }
            }

            // Second loop : start bundles
            for (final Long bundleId : pInstalledBundles.keySet()) {

                try {
                    startBundle(bundleId);

                } catch (final BundleException ex) {
                    if (pInstalledBundles.get(bundleId).getOptional()) {
                        // Simply log
                        pIsolateLoggerSvc.logWarn(this, "prepareIsolate",
                                "Can't start bundle ",
                                pInstalledBundles.get(bundleId)
                                        .getSymbolicName(), ex);

                        System.err.println(ex);

                    } else {
                        // Propagate error if the bundle is not optional
                        throw ex;

                    }
                }
            }

            // End of synchronization here : the guardian will test bundles
        }
    }

    /**
     * Refreshes packages (like the refresh command in Felix / Equinox). The
     * bundle ID array can be null, to refresh the whole framework.
     * 
     * FIXME {@link PackageAdmin} is now deprecated (OSGi 4.3), but Felix 3.2.2
     * does currently not support the new way.
     * 
     * @param aBundleIdArray
     *            An array containing the UID of the bundles to refresh, null to
     *            refresh all
     * @return True on success, false on error
     */
    public boolean refreshPackages(final long[] aBundleIdArray) {

        // Prepare the bundle array
        Bundle[] bundles = null;

        if (aBundleIdArray != null) {
            bundles = new Bundle[aBundleIdArray.length];
            int i = 0;
            for (final long bundleId : aBundleIdArray) {

                final Bundle bundle = pBundleContext.getBundle(bundleId);
                if (bundle != null) {
                    bundles[i++] = bundle;
                }
            }
        }

        // Grab the service
        final ServiceReference svcRef = pBundleContext
                .getServiceReference(PackageAdmin.class.getName());
        if (svcRef == null) {
            return false;
        }

        final PackageAdmin packadmin = (PackageAdmin) pBundleContext
                .getService(svcRef);
        if (packadmin == null) {
            return false;
        }

        try {
            // Refresh packages
            packadmin.refreshPackages(bundles);

        } finally {
            // Release the service in any case
            pBundleContext.ungetService(svcRef);
        }

        return true;
    }

    /**
     * Enters a critical section to reset the isolate.
     * 
     * In case of fatal error, kills the isolate.
     */
    protected void safeIsolateReset() {

        // Enter critical section
        if (pCriticalSection.compareAndSet(false, true)) {
            try {

                // Reset isolate
                neutralizeIsolate();
                prepareIsolate();

            } catch (final Exception e) {
                // An error at this level is fatal
                pIsolateLoggerSvc.logSevere(this, "bundleChanged",
                        "Error reseting isolate. Abandon.", e);

                killIsolate();

            } finally {
                // End of critical section
                pCriticalSection.set(false);
            }

        } else {
            pIsolateLoggerSvc.logSevere(this, "bundleChanged - INCOHERENT",
                    "Incoherent state : Can't start a critical section...");

            killIsolate();
        }
    }

    /**
     * Sets up the HTTP bundle system properties for the new isolate
     * configuration. Does nothing if the configured access URL is null, empty
     * or not based on HTTP.
     * 
     * @param aIsolateDescr
     *            The description of the new isolate configuration
     */
    protected void setupHttpProperties(final IsolateDescription aIsolateDescr) {

        // Get the configured URL
        final String isolateAccessStr = aIsolateDescr.getAccessUrl();
        if (isolateAccessStr == null || isolateAccessStr.isEmpty()) {
            pIsolateLoggerSvc.log(Level.WARNING, this, "setupHttpProperties",
                    "No access URL defined for ", aIsolateDescr.getId());
            return;
        }

        // Make a real URL to extract information
        final URL isolateAccessUrl;
        try {
            isolateAccessUrl = new URL(isolateAccessStr);

        } catch (final MalformedURLException e) {
            pIsolateLoggerSvc.log(Level.WARNING, this, "setupHttpProperties",
                    "Invalid access URL '", isolateAccessStr, "' for ",
                    aIsolateDescr.getId());
            return;
        }

        // Test the access protocol
        if (!"http".equals(isolateAccessUrl.getProtocol())) {
            // Ignore non-HTTP access URL
            return;
        }

        // Find the access port
        final int accessPort = isolateAccessUrl.getPort();
        if (accessPort == -1) {
            // No port defined, do nothing
            pIsolateLoggerSvc.log(Level.WARNING, this, "setupHttpProperties",
                    "No port defined in URL '", isolateAccessStr, "' for ",
                    aIsolateDescr.getId());
            return;
        }

        // Everything is OK, set up the properties
        System.setProperty("org.osgi.service.http.port",
                Integer.toString(accessPort));
        System.setProperty("org.apache.felix.http.jettyEnabled", "true");
    }

    /**
     * Starts the given bundle
     * 
     * @param aBundleId
     *            Bundle's UID
     * @return True on success, False if the bundle wasn't found
     * @throws BundleException
     *             An error occurred while starting the bundle
     */
    public boolean startBundle(final long aBundleId) throws BundleException {

        final Bundle bundle = pBundleContext.getBundle(aBundleId);
        if (bundle == null) {
            return false;
        }

        if (!isFragment(bundle)) {
            // Fragments can't be started
            bundle.start();
        }

        // Ignore the fact that the bundle is a fragment
        return true;
    }

    /**
     * Stops the given bundle
     * 
     * @param aBundleId
     *            Bundle's UID
     * @return True on success, False if the bundle wasn't found
     * @throws BundleException
     *             An error occurred while stopping the bundle
     */
    public boolean stopBundle(final long aBundleId) throws BundleException {

        final Bundle bundle = pBundleContext.getBundle(aBundleId);
        if (bundle == null) {
            return false;
        }

        bundle.stop();
        return true;
    }

    /**
     * Un-installs the given bundle
     * 
     * @param aBundleId
     *            Bundle's UID
     * @return True on success, False if the bundle wasn't found
     * @throws BundleException
     *             An error occurred while removing the bundle
     */
    public boolean uninstallBundle(final long aBundleId) throws BundleException {

        final Bundle bundle = pBundleContext.getBundle(aBundleId);
        if (bundle == null) {
            return false;
        }

        if (bundle.getState() != Bundle.UNINSTALLED) {
            // Avoid to do the job for nothing
            bundle.uninstall();
        }

        return true;
    }

    /**
     * Updates the given bundle
     * 
     * @param aBundleId
     *            Bundle's UID
     * @return True on success, False if the bundle wasn't found
     * @throws BundleException
     *             An error occurred while updating the bundle
     */
    public boolean updateBundle(final long aBundleId) throws BundleException {

        final Bundle bundle = pBundleContext.getBundle(aBundleId);
        if (bundle == null) {
            return false;
        }

        bundle.update();
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() {

        // Set up the scheduler, before the call to addBundleListener.
        pScheduler = Executors.newScheduledThreadPool(1);

        // Register to the signal receiver for the STOP signal
        pSignalReceiver.registerListener(ISignalsConstants.ISOLATE_STOP_SIGNAL,
                this);

        // Prepare the current isolate, nobody else can do it
        try {
            pCriticalSection.set(true);
            prepareIsolate();
            pCriticalSection.set(false);

            /*
             * Register to bundle events : replaces the guardian thread.
             */
            pBundleContext.addBundleListener(this);

            // Send the signal to say we are good
            final IsolateStatus status = pBootstrapSender.sendStatus(
                    IsolateStatus.STATE_AGENT_DONE, 100);

            // Broadcast the same isolate status (same time stamp)
            pSignalBroadcaster.sendData(
                    ISignalBroadcaster.EEmitterTargets.MONITORS,
                    ISignalsConstants.ISOLATE_STATUS_SIGNAL, status);

        } catch (final Exception ex) {
            System.err.println("Preparation error : " + ex);
            ex.printStackTrace();

            final IsolateStatus status = pBootstrapSender.sendStatus(
                    IsolateStatus.STATE_FAILURE, -1);

            // Broadcast the same isolate status (same time stamp)
            pSignalBroadcaster.sendData(
                    ISignalBroadcaster.EEmitterTargets.MONITORS,
                    ISignalsConstants.ISOLATE_STATUS_SIGNAL, status);

            // Log the error
            pIsolateLoggerSvc.logSevere(this, "validatePojo",
                    "Error preparing this isolate : ", ex);

            // the the isolate !
            killIsolate();
        }
    }
}
