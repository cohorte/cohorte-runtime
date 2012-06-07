package org.psem2m.eclipse.runner;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Thomas Calmant
 */
public class RunnerPlugin extends AbstractUIPlugin {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.psem2m.eclipse.runner";

	/** The shared instance */
	private static RunnerPlugin sPlugin;

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static RunnerPlugin getDefault() {
		return sPlugin;
	}

	/**
	 * Logs the given status
	 * 
	 * @param aStatus
	 *            Status to be logged
	 */
	public static void log(final IStatus aStatus) {
		sPlugin.getLog().log(aStatus);
	}

	/**
	 * Logs the given error
	 * 
	 * @param aMessage
	 *            Error message
	 * @param aThrowable
	 *            Associated exception or error
	 */
	public static void logError(final String aMessage,
			final Throwable aThrowable) {

		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, aMessage,
				aThrowable);
		sPlugin.getLog().log(status);
	}

	/**
	 * Logs the given information
	 * 
	 * @param aMessage
	 *            Message to be logged
	 */
	public static void logInfo(final String aMessage) {

		IStatus status = new Status(IStatus.INFO, PLUGIN_ID, aMessage);
		sPlugin.getLog().log(status);
	}

	/**
	 * Logs the given warning
	 * 
	 * @param aMessage
	 *            Message to be logged
	 */
	public static void logWarning(final String aMessage) {

		IStatus status = new Status(IStatus.WARNING, PLUGIN_ID, aMessage);
		sPlugin.getLog().log(status);
	}

	/**
	 * Utility method to prepare CoreException objects
	 * 
	 * @param aMessage
	 *            Exception description
	 * @param aThrowable
	 *            Exception cause
	 * @return The CoreException object
	 */
	public static CoreException prepareException(final String aMessage,
			final Throwable aThrowable) {

		return new CoreException(new Status(IStatus.ERROR,
				RunnerPlugin.PLUGIN_ID, aMessage, aThrowable));
	}

	/**
	 * The constructor
	 */
	public RunnerPlugin() {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(final BundleContext aContext) throws Exception {
		super.start(aContext);
		sPlugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(final BundleContext aContext) throws Exception {
		sPlugin = null;
		super.stop(aContext);
	}
}
