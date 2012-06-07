/**
 * File:   GuardianThread.java
 * Author: Thomas Calmant
 * Date:   8 sept. 2011
 */
package org.psem2m.isolates.slave.agent.core;

import java.util.Map;
import java.util.Map.Entry;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.services.conf.beans.BundleDescription;

/**
 * Guardian thread, monitoring bundles installed by the agent
 * 
 * @author Thomas Calmant
 */
public class GuardianThread extends Thread {

    /** Thread name */
    public static final String THREAD_NAME = "PSEM2M-Agent-Guardian";

    /** Parent agent */
    private AgentCore pAgentCore;

    /**
     * Sets up the guardian
     * 
     * @param aParent
     *            Parent agent
     */
    public GuardianThread(final AgentCore aParent) {

        super(THREAD_NAME);
        pAgentCore = aParent;

        // Set this thread as a daemon one, to avoid mess when the JVM exits
        setDaemon(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        // Main monitoring loop

        while (!isInterrupted()) {

            final Map<Long, BundleDescription> bundles = pAgentCore
                    .getInstalledBundles();

            synchronized (bundles) {
                // Synchronized, to avoid problems

                // Test'em all
                for (final Entry<Long, BundleDescription> entry : bundles
                        .entrySet()) {

                    final long bundleId = entry.getKey();
                    final BundleDescription bundleDescr = entry.getValue();

                    // Test if the bundle is valid
                    final Bundle osgiBundle = pAgentCore.getBundle(bundleId);
                    if (osgiBundle == null) {

                        if (!bundleDescr.getOptional()) {
                            // TODO handle the missing bundle
                            System.err.println("MISSING BUNDLE: "
                                    + bundleDescr.getSymbolicName());
                        }
                        // Don't care if the bundle is optional

                    } else {
                        // Test the bundle state
                        if (osgiBundle.getState() != Bundle.ACTIVE) {
                            try {
                                // Try to wake it up
                                pAgentCore.startBundle(bundleId);

                            } catch (final BundleException e) {
                                if (!bundleDescr.getOptional()) {
                                    // TODO handle this case
                                    System.err.println("INVALID STATE BUNDLE: "
                                            + bundleDescr.getSymbolicName()
                                            + " - " + e);

                                } else {
                                    System.out
                                            .println("WARNING: can't wake up bundle "
                                                    + bundleDescr
                                                            .getSymbolicName());
                                }
                            }
                        }
                    }
                }
            }

            // FIXME for debug purpose : slow down
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e1) {
                // Ignore sleep errors
            }
        }

        // End of monitoring
    }
}
