/**
 * 
 */
package org.psem2m.isolates.slave.agent.core;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.base.activators.IIsolateLoggerSvc;
import org.psem2m.isolates.base.bundles.BundleInfo;
import org.psem2m.isolates.base.bundles.BundleRef;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.base.isolates.boot.IBootstrapMessageSender;
import org.psem2m.isolates.base.isolates.boot.IsolateStatus;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.constants.ISignalsConstants;
import org.psem2m.isolates.services.conf.IBundleDescr;
import org.psem2m.isolates.services.conf.IIsolateDescr;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;
import org.psem2m.isolates.slave.agent.ISvcAgent;

/**
 * Implementation of the isolate Agent
 * 
 * @author Thomas Calmant
 */
public class AgentCore extends CPojoBase implements ISvcAgent {

    /** Bootstrap message sender */
    private IBootstrapMessageSender pBootstrapSender;

    /** Agent bundle context */
    private BundleContext pBundleContext;

    /** Bundle finder, injected by iPOJO */
    private IBundleFinderSvc pBundleFinderSvc;

    /** Configuration service, injected by iPOJO */
    private ISvcConfig pConfigurationSvc;

    /** The bundle state monitoring thread */
    private GuardianThread pGuardianThread;

    /** Bundles installed by the agent : bundle ID -&gt; bundle description map */
    private final Map<Long, IBundleDescr> pInstalledBundles = new HashMap<Long, IBundleDescr>();

    /** Isolate logger, injected by iPOJO */
    private IIsolateLoggerSvc pLoggerSvc;

    /** Platform directories service, injected by iPOJO */
    private IPlatformDirsSvc pPlatformDirsSvc;

    /** Signal broadcaster */
    private ISignalBroadcaster pSignalBroadcaster;

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
    public String findBundleURL(final IBundleDescr aBundleDescr) {

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

        } catch (MalformedURLException ex) {
            pLoggerSvc.logWarn(this, "findBundleURL",
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

        Bundle bundle = pBundleContext.getBundle(aBundleId);
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

        Bundle[] bundles = pBundleContext.getBundles();
        BundleInfo[] bundlesInfo = new BundleInfo[bundles.length];

        int i = 0;
        for (Bundle bundle : bundles) {
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
    public Map<Long, IBundleDescr> getInstalledBundles() {

        return pInstalledBundles;
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

        pSignalBroadcaster.sendData(
                ISignalBroadcaster.EEmitterTargets.MONITORS,
                ISignalsConstants.ISOLATE_STATUS_SIGNAL, new IsolateStatus(
                        pPlatformDirsSvc.getIsolateId(),
                        IsolateStatus.STATE_STOPPED, 100));

        // Stop the guardian thread, if any
        if (pGuardianThread != null && pGuardianThread.isAlive()) {
            pGuardianThread.interrupt();
        }
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

        pLoggerSvc.logInfo(this, "killIsolate", "Kills this isolate [%s]",
                pPlatformDirsSvc.getIsolateId());

        // Neutralize the isolate
        neutralizeIsolate();

        try {
            // Stop the platform
            pBundleContext.getBundle(0).stop();

        } catch (BundleException e) {
            // Damn
            pLoggerSvc.logSevere(this, "validatePojo",
                    "Can't stop the framework", e);

            try {
                // Hara kiri
                pBundleContext.getBundle().stop();

            } catch (BundleException e1) {
                pLoggerSvc.logSevere(this, "validatePojo",
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

            for (Long bundleId : pInstalledBundles.keySet()) {

                try {
                    uninstallBundle(bundleId);

                } catch (BundleException ex) {
                    // Only log the error
                    pLoggerSvc.logWarn(this, "neutralizeIsolate",
                            "Error stopping bundle : ",
                            pInstalledBundles.get(bundleId).getSymbolicName(),
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
        final IIsolateDescr isolateDescr = pConfigurationSvc.getApplication()
                .getIsolate(aIsolateId);
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
            for (IBundleDescr bundleDescr : isolateDescr.getBundles()) {

                final String bundleName = bundleDescr.getSymbolicName();
                final String bundleUrl = findBundleURL(bundleDescr);
                if (bundleUrl != null) {
                    // Bundle found

                    try {
                        // Install bundle
                        long bundleId = installBundle(bundleUrl);

                        // Update the list of installed bundles
                        pInstalledBundles.put(bundleId, bundleDescr);

                    } catch (BundleException ex) {

                        switch (ex.getType()) {
                        case BundleException.DUPLICATE_BUNDLE_ERROR:
                            // Simply log this
                            pLoggerSvc.logInfo(this, "prepareIsolate",
                                    "Bundle ", bundleName,
                                    " is already installed");

                            break;

                        default:
                            if (bundleDescr.getOptional()) {
                                // Ignore error if the bundle is optional
                                pLoggerSvc.logWarn(this, "prepareIsolate",
                                        "Error installing ", bundleName, ex);

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
                        pLoggerSvc.logWarn(this, "prepareIsolate",
                                "Bundle not found : ", bundleName);

                    } else {
                        // Severe error
                        throw new FileNotFoundException("Can't find bundle : "
                                + bundleName);
                    }
                }
            }

            // Second loop : start bundles
            for (Long bundleId : pInstalledBundles.keySet()) {

                try {
                    startBundle(bundleId);

                } catch (BundleException ex) {
                    if (pInstalledBundles.get(bundleId).getOptional()) {
                        // Simply log
                        pLoggerSvc.logWarn(this, "prepareIsolate",
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
            for (long bundleId : aBundleIdArray) {

                Bundle bundle = pBundleContext.getBundle(bundleId);
                if (bundle != null) {
                    bundles[i++] = bundle;
                }
            }
        }

        // Grab the service
        ServiceReference svcRef = pBundleContext
                .getServiceReference(PackageAdmin.class.getName());
        if (svcRef == null) {
            return false;
        }

        PackageAdmin packadmin = (PackageAdmin) pBundleContext
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
     * Sets up the HTTP bundle system properties for the new isolate
     * configuration. Does nothing if the configured access URL is null, empty
     * or not based on HTTP.
     * 
     * @param aIsolateDescr
     *            The description of the new isolate configuration
     */
    protected void setupHttpProperties(final IIsolateDescr aIsolateDescr) {

        // Get the configured URL
        final String isolateAccessStr = aIsolateDescr.getAccessUrl();
        if (isolateAccessStr == null || isolateAccessStr.isEmpty()) {
            pLoggerSvc.log(Level.WARNING, this, "setupHttpProperties",
                    "No access URL defined for ", aIsolateDescr.getId());
            return;
        }

        // Make a real URL to extract information
        final URL isolateAccessUrl;
        try {
            isolateAccessUrl = new URL(isolateAccessStr);

        } catch (MalformedURLException e) {
            pLoggerSvc.log(Level.WARNING, this, "setupHttpProperties",
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
        int accessPort = isolateAccessUrl.getPort();
        if (accessPort == -1) {
            // No port defined, do nothing
            pLoggerSvc.log(Level.WARNING, this, "setupHttpProperties",
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

        Bundle bundle = pBundleContext.getBundle(aBundleId);
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

        Bundle bundle = pBundleContext.getBundle(aBundleId);
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

        Bundle bundle = pBundleContext.getBundle(aBundleId);
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

        Bundle bundle = pBundleContext.getBundle(aBundleId);
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

        // Prepare the current isolate, nobody else can do it
        try {
            prepareIsolate();

            // Start the guardian on success
            pGuardianThread = new GuardianThread(this);
            pGuardianThread.start();

            pBootstrapSender.sendStatus(IsolateStatus.STATE_AGENT_DONE, 100);
            pSignalBroadcaster.sendData(
                    ISignalBroadcaster.EEmitterTargets.MONITORS,
                    ISignalsConstants.ISOLATE_STATUS_SIGNAL, new IsolateStatus(
                            pPlatformDirsSvc.getIsolateId(),
                            IsolateStatus.STATE_AGENT_DONE, 100));

        } catch (Exception ex) {
            System.err.println("Preparation error : " + ex);
            ex.printStackTrace();

            pBootstrapSender.sendStatus(IsolateStatus.STATE_FAILURE, -1);

            pSignalBroadcaster.sendData(
                    ISignalBroadcaster.EEmitterTargets.MONITORS,
                    ISignalsConstants.ISOLATE_STATUS_SIGNAL, new IsolateStatus(
                            pPlatformDirsSvc.getIsolateId(),
                            IsolateStatus.STATE_FAILURE, -1));

            // Log the error
            pLoggerSvc.logSevere(this, "validatePojo",
                    "Error preparing this isolate : ", ex);

            // the the isolate !
            killIsolate();
        }
    }
}
