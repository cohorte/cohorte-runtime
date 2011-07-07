/**
 * File:   OsgiBootstrap.java
 * Author: Thomas Calmant
 * Date:   6 juil. 2011
 */
package org.psem2m.utilities.bootstrap;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * Bootstrap for OSGi frameworks
 * 
 * @author Thomas Calmant
 */
public class OsgiBootstrap {

    /** Default OSGi pFramework : Felix */
    public static final String DEFAULT_FRAMEWORK = "org.apache.felix.framework.FrameworkFactory";

    /** Framework factories content */
    public static final Map<String, String> FRAMEWORK_FACTORIES = new TreeMap<String, String>();

    /** OSGI storage cleaning option */
    public static final String OSGI_STORAGE_CLEAN = "org.osgi.framework.storage.clean";

    /** OSGi storage cleaning option value : onFirstInit */
    public static final String OSGI_STORAGE_CLEAN_ON_INIT = "onFirstInit";

    static {
	// Initialize the static map
	FRAMEWORK_FACTORIES.put("equinox",
		"org.eclipse.osgi.launch.EquinoxFactory");
	FRAMEWORK_FACTORIES.put("felix",
		"org.apache.felix.framework.FrameworkFactory");
    }

    /** The bootstrap configuration */
    private Map<String, String> pBootstrapConfiguration = new TreeMap<String, String>();

    /** The launched framework */
    private Framework pFramework;

    /** The framework/system configuration */
    private Map<String, String> pFrameworkConfiguration = new TreeMap<String, String>();

    /** The installed bundles list */
    private List<Bundle> pInstalledBundles = new ArrayList<Bundle>();

    /**
     * Prepares the bootstrap members
     * 
     * @param aBootstrapConfiguration
     *            Bootstrap configuration map
     */
    public OsgiBootstrap(final Map<String, String> aBootstrapConfiguration,
	    final Map<String, String> aFrameworkConfiguration) {

	if (aBootstrapConfiguration != null) {
	    pBootstrapConfiguration.putAll(aBootstrapConfiguration);
	}

	if (aFrameworkConfiguration != null) {
	    pFrameworkConfiguration.putAll(aFrameworkConfiguration);
	}

	// Flush the cache by default
	if (!aFrameworkConfiguration.containsKey(OSGI_STORAGE_CLEAN)) {
	    aFrameworkConfiguration.put(OSGI_STORAGE_CLEAN,
		    OSGI_STORAGE_CLEAN_ON_INIT);
	}
    }

    /**
     * Boot the OSGi pFramework according to the given configuration
     * 
     * @return The launched framework, null on error
     */
    public Framework createFramework() {

	final FrameworkFactory factory = getFrameworkFactory();
	if (factory == null) {
	    // Could not find the factory
	    return null;
	}

	// Create the framework object
	pFramework = factory.newFramework(pFrameworkConfiguration);

	try {
	    // Initializes the framework
	    pFramework.init();

	} catch (BundleException e) {
	    e.printStackTrace();
	    pFramework = null;
	}

	return pFramework;
    }

    /**
     * Retrieves the OSGi pFramework factory
     * 
     * @return A FrameworkFactory instance, null on error
     */
    protected FrameworkFactory getFrameworkFactory() {

	String osgiFramework = pBootstrapConfiguration
		.get(IBootstrapConstants.CONFIG_FRAMEWORK);
	if (osgiFramework == null) {
	    osgiFramework = DEFAULT_FRAMEWORK;
	}

	// Try to find the factory
	String factoryName = FRAMEWORK_FACTORIES.get(osgiFramework);

	// If none found, consider the configuration as the factory
	if (factoryName == null) {
	    factoryName = osgiFramework;
	}

	// Find and instantiate the class
	try {
	    return (FrameworkFactory) Class.forName(factoryName).newInstance();

	} catch (InstantiationException e) {
	    e.printStackTrace();
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	}

	return null;
    }

    /**
     * Install bundles into the framework
     * 
     * @param aBundlesConfiguration
     *            List of bundles to install
     * 
     */
    public void installBundles(final URL[] aBundlesConfiguration) {

	for (URL url : aBundlesConfiguration) {
	    try {
		pInstalledBundles.add(pFramework.getBundleContext()
			.installBundle(url.toString()));

	    } catch (BundleException e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * Starts installed bundles
     */
    public void startBundles() {

	for (Bundle bundle : pInstalledBundles) {
	    try {
		bundle.start();
	    } catch (BundleException e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * Starts the framework
     * 
     * @return True on success, False on error
     */
    public boolean startFramework() {

	try {
	    pFramework.start();
	    return true;

	} catch (BundleException e) {
	    e.printStackTrace();
	}

	return false;
    }

    /**
     * Stops the framework
     * 
     * @return True on success, false on error
     */
    public boolean stopFramework() {

	if (pFramework.getState() != Bundle.ACTIVE) {
	    return false;
	}

	try {
	    pFramework.stop();
	    return true;

	} catch (BundleException e) {
	    e.printStackTrace();
	}

	return false;
    }

    /**
     * Waits for the framework to stop
     * 
     * @param aTimeout
     *            Operation timeout in milliseconds, 0 for infinite
     * @return True if the framework stopped (successfully or by error), false
     *         if the timeout raised.
     * @throws InterruptedException
     *             The waiting timer was interrupted
     */
    public boolean waitForStop(final int aTimeout) throws InterruptedException {

	FrameworkEvent event = pFramework.waitForStop(aTimeout);
	return event.getType() != FrameworkEvent.WAIT_TIMEDOUT;
    }
}
