/**
 * 
 */
package org.psem2m.isolates.slave.agent.core;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.commons.IBundleInfo;
import org.psem2m.isolates.commons.impl.BundleInfo;

/**
 * @author Thomas Calmant
 */
public class AgentCore extends CPojoBase {

    /** Agent bundle context */
    private BundleContext pBundleContext = null;

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
     * Retrieves the context of the bundle with the given ID.
     * 
     * @param aBundleId
     *            A bundle ID
     * @return The context of the bundle with the given ID, null if not found
     */
    public IBundleInfo getBundleInfo(final long aBundleId) {

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
    public IBundleInfo[] getBundlesState() {

        Bundle[] bundles = pBundleContext.getBundles();
        IBundleInfo[] bundlesInfo = new IBundleInfo[bundles.length];

        int i = 0;
        for (Bundle bundle : bundles) {
            bundlesInfo[i] = new BundleInfo(bundle);
            i++;
        }

        return bundlesInfo;
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
        // ...
    }

    /**
     * Refreshes packages (like the refresh command in Felix / Equinox). The
     * bundle ID array can be null, to refresh the whole framework
     * 
     * @param aBundleIdArray
     * @return
     */
    // public boolean refreshPackages(final long aBundleIdArray[]) {
    //
    // ServiceReference svcRef = pBundleContext
    // .getServiceReference(PackageAdmin.class.getName());
    // if (svcRef == null) {
    // return false;
    // }
    //
    // // Grab the service
    // PackageAdmin packadmin = (PackageAdmin) pBundleContext
    // .getService(svcRef);
    // if (packadmin == null) {
    // return false;
    // }
    //
    // // Refresh packages
    // packadmin.refreshPackages(null);
    //
    // // Release the service
    // pBundleContext.ungetService(svcRef);
    //
    // return true;
    // }

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

        bundle.start();
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

        bundle.uninstall();
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
        // ...
    }
}
