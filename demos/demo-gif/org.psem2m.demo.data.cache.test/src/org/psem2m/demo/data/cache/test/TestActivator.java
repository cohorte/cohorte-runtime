/**
 * File:   TestActivator.java
 * Author: Thomas Calmant
 * Date:   17 oct. 2011
 */
package org.psem2m.demo.data.cache.test;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The cache tester activator
 * 
 * @author Thomas Calmant
 */
public class TestActivator implements BundleActivator {

    /** The activator singleton */
    private static TestActivator sSingleton;

    /**
     * Retrieves the activator instance
     * 
     * @return the activator instance
     */
    public static TestActivator getInstance() {

        return sSingleton;
    }

    /** The bundle context */
    private BundleContext pContext;

    /**
     * Default constructor
     */
    public TestActivator() {

        sSingleton = this;
    }

    /**
     * Retrieves the instance bundle context
     * 
     * @return The bundle context
     */
    public BundleContext getContext() {

        return pContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(final BundleContext aContext) throws Exception {

        pContext = aContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(final BundleContext aContext) throws Exception {

        pContext = null;
    }
}
