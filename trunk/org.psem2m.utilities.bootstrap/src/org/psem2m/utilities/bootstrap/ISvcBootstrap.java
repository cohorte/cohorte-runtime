/**
 * File:   ISvcBootstrap.java
 * Author: Thomas Calmant
 * Date:   22 juil. 2011
 */
package org.psem2m.utilities.bootstrap;

import java.net.URL;

import org.osgi.framework.launch.Framework;

/**
 * @author Thomas Calmant
 *
 */
public interface ISvcBootstrap {

    /**
     * Boot the OSGi pFramework according to the given configuration
     * 
     * @return The launched framework, null on error
     */
    public abstract Framework createFramework();

    /**
     * Install bundles into the framework
     * 
     * @param aBundlesConfiguration
     *            List of bundles to install
     * 
     * @return True on success, False if an error occurred
     */
    public abstract boolean installBundles(final URL[] aBundlesConfiguration);

    /**
     * Sets the message sender. Null argument will be ignored
     * 
     * @param aMessageSender
     *            A message sender
     */
    public abstract void setMessageSender(final IMessageSender aMessageSender);

    /**
     * Starts installed bundles
     * 
     * @return False if an error occurred
     */
    public abstract boolean startBundles();

    /**
     * Starts the framework
     * 
     * @return True on success, False on error
     */
    public abstract boolean startFramework();

    /**
     * Stops the framework
     * 
     * @return True on success, false on error
     */
    public abstract boolean stopFramework();

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
    public abstract boolean waitForStop(final int aTimeout)
	    throws InterruptedException;

}