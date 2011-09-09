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
import org.psem2m.isolates.base.conf.IBundleDescr;

/**
 * Guardian thread, monitoring bundles installed by the agent
 * 
 * @author Thomas Calmant
 */
public class GuardianThread extends Thread {

    /** Parent agent */
    private AgentCore pAgentCore;

    /**
     * Sets up the guardian
     * 
     * @param aParent
     *            Parent agent
     */
    public GuardianThread(final AgentCore aParent) {

	super("PSEM2M-Agent-Guardian");
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

	    final Map<Long, IBundleDescr> bundles = pAgentCore
		    .getInstalledBundles();

	    synchronized (bundles) {
		// Synchronized, to avoid problems

		// Test'em all
		for (Entry<Long, IBundleDescr> entry : bundles.entrySet()) {

		    final long bundleId = entry.getKey();
		    final IBundleDescr bundleDescr = entry.getValue();

		    // Test if the bundle is valid
		    final Bundle osgiBundle = pAgentCore.getBundle(bundleId);
		    if (osgiBundle == null) {

			if (!bundleDescr.isOptional()) {
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

			    } catch (BundleException e) {
				if (!bundleDescr.isOptional()) {
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
	    } catch (InterruptedException e1) {
		e1.printStackTrace();
	    }
	}

	// End of monitoring
    }
}
