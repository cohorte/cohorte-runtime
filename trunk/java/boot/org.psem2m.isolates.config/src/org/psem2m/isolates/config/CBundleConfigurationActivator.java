/**
 * File:   CBundleConfigurationActivator.java
 * Author: Thomas Calmant
 * Date:   21 juil. 2011
 */
package org.psem2m.isolates.config;

import org.psem2m.isolates.base.activators.CActivatorBase;

/**
 * @author Thomas Calmant
 * 
 */
public class CBundleConfigurationActivator extends CActivatorBase {

    /** An instance of the bundle activator */
    private static CBundleConfigurationActivator sBundleInstance = null;

    /**
     * Retrieves an already created instance of the bundle activator, null if
     * the activator was never called
     * 
     * @return An instance of the activator or null
     */
    public static CBundleConfigurationActivator getInstance() {

        return sBundleInstance;
    }

    /**
     * Sets up the bundle activator instance reference if needed
     */
    public CBundleConfigurationActivator() {

        super();

        if (sBundleInstance == null) {
            sBundleInstance = this;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CActivatorBase#getBundleId()
     */
    @Override
    public String getBundleId() {

        return getClass().getPackage().getName();
    }
}
