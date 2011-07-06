/**
 * File:   OsgiBootstrap.java
 * Author: Thomas Calmant
 * Date:   6 juil. 2011
 */
package org.psem2m.utilities.bootstrap;

import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * Bootstrap for OSGi frameworks
 * 
 * @author Thomas Calmant
 */
public class OsgiBootstrap {

    /** Framework configuration key */
    public static final String CONFIG_FRAMEWORK = IBootstrapConstants.PROPERTY_PREFIX
	    + "osgi.framework";

    /** Default OSGi pFramework : Felix */
    public static final String DEFAULT_FRAMEWORK = "org.apache.felix.framework.FrameworkFactory";

    /** Framework factories content */
    public static final Map<String, String> FRAMEWORK_FACTORIES = new TreeMap<String, String>();

    static {
	// Initialize the static map
	FRAMEWORK_FACTORIES.put("eclipse",
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

	String osgiFramework = pBootstrapConfiguration.get(CONFIG_FRAMEWORK);
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
     */
    public void populateFramework(final URL[] aBundlesConfiguration) {

	for (URL url : aBundlesConfiguration) {
	    System.out.println("Installing : " + url);
	    try {
		Bundle bundle = pFramework.getBundleContext().installBundle(
			url.toString());
		bundle.start();
	    } catch (BundleException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }
}
