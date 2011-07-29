/**
 * File:   PojoErrorHandler.java
 * Author: Thomas Calmant
 * Date:   21 juil. 2011
 */
package org.psem2m.isolates.config.impl;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ErrorHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * @author Thomas Calmant
 * 
 */
public class PojoErrorHandler implements ErrorHandler {

    /**
     * Default constructor
     */
    public PojoErrorHandler() {
	// ...
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.felix.ipojo.ErrorHandler#onError(org.apache.felix.ipojo.
     * ComponentInstance, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void onError(final ComponentInstance aComponentInstance,
	    final String aMessage, final Throwable aThrowable) {

	System.err.println("[iPOJO Error] " + aMessage);
	if (aThrowable != null) {
	    aThrowable.printStackTrace();
	}

	try {
	    // Try to stop the bundle on the first error
	    if (aComponentInstance != null) {

		BundleContext context = aComponentInstance.getContext();
		if (context != null) {
		    Bundle badBundle = context.getBundle();
		    aComponentInstance.dispose();
		    badBundle.stop();
		}
	    }

	} catch (BundleException e) {
	    // We're really not lucky...
	    e.printStackTrace();
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.felix.ipojo.ErrorHandler#onWarning(org.apache.felix.ipojo.
     * ComponentInstance, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void onWarning(final ComponentInstance aComponentInstance,
	    final String aMessage, final Throwable aThrowable) {

	System.err.println("[iPOJO Warning] " + aMessage);
	if (aThrowable != null) {
	    aThrowable.printStackTrace();
	}
    }

}
