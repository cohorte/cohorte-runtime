/**
 * 
 */
package org.psem2m.isolates.slave.agent.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.wiring.FrameworkWiring;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.bundles.BundleInfo;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.slave.agent.ISvcAgent;
import org.psem2m.signals.ISignalData;
import org.psem2m.signals.ISignalListener;
import org.psem2m.signals.ISignalReceiver;

/**
 * Implementation of the isolate Agent
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-slave-agent-core-factory")
@Provides(specifications = ISvcAgent.class)
public class AgentCore implements ISvcAgent, ISignalListener, BundleListener {

    // TODO: inject the Python StateUpdater service

    /** Agent bundle context */
    private final BundleContext pContext;

    /** The agent core critical section flag */
    private final AtomicBoolean pCriticalSection = new AtomicBoolean(false);

    /** Isolate logger, injected by iPOJO */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Platform directories service, injected by iPOJO */
    @Requires
    private IPlatformDirsSvc pPlatformDirs;

    /** The scheduler */
    private ScheduledExecutorService pScheduler;

    /** Signal receiver */
    @Requires
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
        pContext = aBundleContext;
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
        final Bundle bundle = aBundleEvent.getBundle();
        final long bundleId = bundle.getBundleId();

        if (pCriticalSection.get()) {
            // In critical section : ignore events, cancel timeouts
            cancelTimeout(bundleId);
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
                                pLogger.logSevere(this, "bundleChanged",
                                        "Can't restart bundle", bundleId, e);
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

            // Re-install it
            try {
                final long newId = installBundle(bundle.getLocation());
                startBundle(newId);

            } catch (final BundleException ex) {
                pLogger.logSevere(this, "bundleChanged",
                        "Couldn't reinstall bundle", bundle.getSymbolicName());
            }
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

            pLogger.logDebug(this, "bundleChanged",
                    "Bundle timeout cancellation=", cancelled);
        }

        // Remove it from the map
        pUpdateTimeouts.remove(aBundleId);
    }

    /**
     * Retrieves the context of the bundle with the given ID.
     * 
     * @param aBundleId
     *            A bundle ID
     * @return The context of the bundle with the given ID, null if not found
     */
    public BundleInfo getBundleInfo(final long aBundleId) {

        final Bundle bundle = pContext.getBundle(aBundleId);
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

        final Bundle[] bundles = pContext.getBundles();
        final BundleInfo[] bundlesInfo = new BundleInfo[bundles.length];

        int i = 0;
        for (final Bundle bundle : bundles) {
            bundlesInfo[i] = new BundleInfo(bundle);
            i++;
        }

        return bundlesInfo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.ISignalListener#
     * handleReceivedSignal(java.lang.String, org.psem2m.signals.ISignalData)
     */
    @Override
    public Object handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        if (ISignalsConstants.ISOLATE_STOP_SIGNAL.equals(aSignalName)) {

            // Log what happened
            pLogger.logInfo(this, "handleReceivedSignal",
                    "STOP signal received. Killing isolate.");

            // Enter critical section
            if (!pCriticalSection.compareAndSet(false, true)) {

                pLogger.logSevere(this, "handle STOP signal",
                        "Killing isolate while already in critical section !");

                // Force value (just in case...)
                pCriticalSection.set(true);
            }

            // Kill ourselves
            killIsolate();
        }

        return null;
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

        return pContext.installBundle(aBundleUrl).getBundleId();
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        pSignalReceiver.unregisterListener(
                ISignalsConstants.ISOLATE_STOP_SIGNAL, this);

        // Stop the scheduled timeouts
        pScheduler.shutdownNow();
        pScheduler = null;

        // Unregister the bundle listener
        pContext.removeBundleListener(this);
        pUpdateTimeouts.clear();

        // log the invalidation
        pLogger.logInfo(this, "invalidate", "Slave agent gone");
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

        pLogger.logInfo(this, "killIsolate", "Kill this isolate [%s:%s]",
                pPlatformDirs.getIsolateName(), pPlatformDirs.getIsolateUID());

        // Use a thread to be responsive
        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    // Stop the platform
                    pContext.getBundle(0).stop();

                } catch (final BundleException e) {
                    pLogger.logSevere(this, "validatePojo",
                            "Can't stop the framework", e);
                }
            }
        }, "agent-stop").start();
    }

    /**
     * Refreshes packages (like the refresh command in Felix / Equinox). The
     * bundle ID array can be null, to refresh the whole framework.
     * 
     * @param aBundleIdArray
     *            An array containing the UID of the bundles to refresh, null to
     *            refresh all
     * @return True on success, false on error
     */
    public boolean refreshPackages(final long[] aBundleIdArray) {

        // Prepare the bundle array
        List<Bundle> bundles = null;

        if (aBundleIdArray != null) {
            bundles = new ArrayList<Bundle>(aBundleIdArray.length);
            for (final long bundleId : aBundleIdArray) {
                final Bundle bundle = pContext.getBundle(bundleId);
                if (bundle != null) {
                    bundles.add(bundle);
                }
            }
        }

        // Get the wiring 'service'
        final FrameworkWiring fwWiring = pContext.getBundle(0).adapt(
                FrameworkWiring.class);

        if (fwWiring == null) {
            pLogger.logWarn(this, "refreshPackages",
                    "System bundle couldn't be adapted to FrameworkWiring.");
            return false;
        }

        // Refresh bundles (and packages)
        fwWiring.refreshBundles(bundles, (FrameworkListener[]) null);

        return true;
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

        final Bundle bundle = pContext.getBundle(aBundleId);
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

        final Bundle bundle = pContext.getBundle(aBundleId);
        if (bundle == null) {
            return false;
        }

        bundle.stop();
        return true;
    }

    /**
     * Removes the given bundle
     * 
     * @param aBundleId
     *            Bundle's UID
     * @return True on success, False if the bundle wasn't found
     * @throws BundleException
     *             An error occurred while removing the bundle
     */
    public boolean uninstallBundle(final long aBundleId) throws BundleException {

        final Bundle bundle = pContext.getBundle(aBundleId);
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

        final Bundle bundle = pContext.getBundle(aBundleId);
        if (bundle == null) {
            return false;
        }

        bundle.update();
        return true;
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Set up the scheduler, before the call to addBundleListener.
        pScheduler = Executors.newScheduledThreadPool(1);

        // Register to the signal receiver for the STOP signal
        pSignalReceiver.registerListener(ISignalsConstants.ISOLATE_STOP_SIGNAL,
                this);

        // Register to bundle events : replaces the guardian thread.
        pContext.addBundleListener(this);

        // Log the validation
        pLogger.logInfo(this, "validate", "Slave agent ready");
    }
}
