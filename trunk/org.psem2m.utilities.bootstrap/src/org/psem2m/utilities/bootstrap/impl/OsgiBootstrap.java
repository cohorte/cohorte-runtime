/**
 * File:   OsgiBootstrap.java
 * Author: Thomas Calmant
 * Date:   6 juil. 2011
 */
package org.psem2m.utilities.bootstrap.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.psem2m.utilities.bootstrap.IBootstrapConstants;
import org.psem2m.utilities.bootstrap.IMessageSender;

/**
 * Bootstrap for OSGi frameworks
 * 
 * @author Thomas Calmant
 */
public class OsgiBootstrap {

    /** Name to use in logs */
    private static final String CLASS_LOG_NAME = OsgiBootstrap.class
	    .getSimpleName();

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

    /** Log message sender */
    private IMessageSender pMessageSender;

    /**
     * Prepares the bootstrap members
     * 
     * @param aMessageSender
     *            Log message sender
     * 
     * @param aBootstrapConfiguration
     *            Bootstrap configuration map
     * 
     * @param aFrameworkConfiguration
     *            OSGi framework configuration map
     */
    public OsgiBootstrap(final IMessageSender aMessageSender,
	    final Map<String, String> aBootstrapConfiguration,
	    final Map<String, String> aFrameworkConfiguration) {

	pMessageSender = aMessageSender;

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

	// Force the system properties
	for (Entry<String, String> property : aFrameworkConfiguration
		.entrySet()) {
	    System.setProperty(property.getKey(), property.getValue());
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
	    pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
		    "createFramework", "Framework inialization error", e);

	    e.printStackTrace();
	    pFramework = null;
	}

	return pFramework;
    }

    /**
     * Retrieves the OSGi pFramework factory
     * 
     * @return A FrameworkFactory pSingleton, null on error
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
	    pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
		    "getFrameworkFactory", "Can't create a new instance", e);

	} catch (IllegalAccessException e) {
	    pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
		    "getFrameworkFactory", "Illegal class access", e);

	} catch (ClassNotFoundException e) {
	    pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
		    "getFrameworkFactory", "Factory class not found", e);

	}

	return null;
    }

    /**
     * Install bundles into the framework
     * 
     * @param aBundlesConfiguration
     *            List of bundles to install
     * 
     * @return True on success, False if an error occurred
     */
    public boolean installBundles(final URL[] aBundlesConfiguration) {

	boolean success = true;
	for (URL url : aBundlesConfiguration) {
	    try {
		pInstalledBundles.add(pFramework.getBundleContext()
			.installBundle(url.toString()));

	    } catch (BundleException e) {
		pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
			"getFrameworkFactory", "Error installing bundle '"
				+ url + "'", e);

		e.printStackTrace();
		success = false;
	    }
	}

	return success;
    }

    /**
     * Starts installed bundles
     * 
     * @return False if an error occurred
     */
    public boolean startBundles() {

	boolean success = true;
	for (Bundle bundle : pInstalledBundles) {
	    try {
		bundle.start();

	    } catch (BundleException e) {
		pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
			"startFramework",
			"Error starting bundle : '" + bundle.getSymbolicName()
				+ "'", e);

		e.printStackTrace();
		success = false;
	    }
	}

	return success;
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
	    pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
		    "startFramework", "Error starting the framework", e);
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
	    // The framework is already in a non-active state
	    return true;
	}

	try {
	    pFramework.stop();
	    return true;

	} catch (BundleException e) {
	    pMessageSender.sendMessage(Level.SEVERE, CLASS_LOG_NAME,
		    "stopFramework", "Error stopping the framework", e);

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
