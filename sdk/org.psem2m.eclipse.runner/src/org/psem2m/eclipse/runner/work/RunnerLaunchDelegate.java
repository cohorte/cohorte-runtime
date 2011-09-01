/**
 * File:   RunnerLaunchDelegate.java
 * Author: Thomas Calmant
 * Date:   22 août 2011
 */
package org.psem2m.eclipse.runner.work;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.pde.ui.launcher.BundlesTab;
import org.psem2m.eclipse.runner.IRunnerConfigurationConstants;
import org.psem2m.eclipse.runner.RunnerPlugin;

/**
 * PSEM2M Runner core worker
 * 
 * @author Thomas Calmant
 */
public class RunnerLaunchDelegate implements ILaunchConfigurationDelegate {

	/**
	 * Extends the given location string if it contains Eclipse variables. The
	 * target must be a directory.
	 * 
	 * Converts workspace relative and other Eclipse special path variables to
	 * their real file system target.
	 * 
	 * @param aLocation
	 *            A location that could contain Eclipse variables
	 * @return The target directory File object, null on error
	 */
	protected File getExtendedPath(final String aLocation) {

		if (aLocation == null) {
			return null;
		}

		final IStringVariableManager varMan = VariablesPlugin.getDefault()
				.getStringVariableManager();

		try {
			String expandedLocation = varMan
					.performStringSubstitution(aLocation);

			if (expandedLocation.length() > 0) {
				File path = new File(expandedLocation);
				if (path.isDirectory()) {
					return path;
				}
			}

		} catch (CoreException ex) {
			// Do nothing...
		}

		return null;
	}

	/**
	 * Retrieves the list of plug-ins to export
	 * 
	 * @param aConfiguration
	 *            The launch configuration
	 * @return The comma-separated list of plug-ins to export
	 * @throws CoreException
	 *             An error occurred while reading the configuration
	 */
	protected List<String> getWorkspaceBundlesList(
			final ILaunchConfiguration aConfiguration) throws CoreException {

		String selectedPlugins = aConfiguration.getAttribute(
				IPDELauncherConstants.WORKSPACE_BUNDLES, "");

		if (selectedPlugins.isEmpty()) {
			RunnerPlugin.logWarning("No workspace bundle selected.");
		}

		return preparePluginsList(selectedPlugins);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.
	 * eclipse.debug.core.ILaunchConfiguration, java.lang.String,
	 * org.eclipse.debug.core.ILaunch,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void launch(final ILaunchConfiguration aConfiguration,
			final String aMode, final ILaunch aLaunch,
			final IProgressMonitor aMonitor) throws CoreException {

		IProgressMonitor monitor = aMonitor;
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		// Debug mode flag
		final boolean debugMode = ILaunchManager.DEBUG_MODE.equals(aMode);

		// Retrieve PSEM2M home and base
		final File platformHome = getExtendedPath(aConfiguration.getAttribute(
				IRunnerConfigurationConstants.PSEM2M_HOME, (String) null));

		final File platformBase = getExtendedPath(aConfiguration.getAttribute(
				IRunnerConfigurationConstants.PSEM2M_BASE, (String) null));

		final File workingDir = getExtendedPath(aConfiguration.getAttribute(
				IRunnerConfigurationConstants.WORKING_DIRECTORY, (String) null));

		// Extract the plug-ins list
		final List<String> selectedPlugins = getWorkspaceBundlesList(aConfiguration);

		// Retrieve the bundles output folder
		final File outputFolder = getExtendedPath(aConfiguration.getAttribute(
				IRunnerConfigurationConstants.EXPORT_OUTPUT_FOLDER,
				(String) null));

		if (outputFolder == null) {
			RunnerPlugin.logError("No output folder set", null);
			return;
		}

		// Retrieves debug informations
		final Integer nbDebugConfigurations = aConfiguration.getAttribute(
				IRunnerConfigurationConstants.DEBUG_COUNT,
				IRunnerConfigurationConstants.DEFAULT_DEBUG_COUNT);

		final Integer baseDebugPort = aConfiguration.getAttribute(
				IRunnerConfigurationConstants.DEBUG_PORT,
				IRunnerConfigurationConstants.DEFAULT_DEBUG_PORT);

		// Stop if needed
		if (monitor.isCanceled()) {
			// Stop here
			monitor.done();
			return;
		}

		// Prepare the remote debuggers, if in debug mode
		final RemoteDebugConfigurationCreator remoteDebug = new RemoteDebugConfigurationCreator(
				nbDebugConfigurations);

		try {
			if (debugMode) {
				remoteDebug.createLaunchConfigurations(baseDebugPort);
				remoteDebug.run();
			}

			// Stop if needed
			if (monitor.isCanceled()) {
				monitor.done();
				return;
			}

			// Export bundles with PDE
			final BundleExporter bundleExporter = new BundleExporter(
					outputFolder.getAbsolutePath(), selectedPlugins);
			bundleExporter.export(monitor);

			// Stop if needed
			if (monitor.isCanceled()) {
				monitor.done();
				return;
			}

			// Run the bootstrap, with debug arguments if needed
			final PlatformRunner platformRunner = new PlatformRunner(
					platformHome, platformBase, workingDir, baseDebugPort);

			/*
			 * It runs the script, which ends immediately, so it's useless to
			 * wait for it
			 */
			platformRunner.runPlatform(aLaunch, debugMode);

		} catch (CoreException ex) {
			// Clean up the mess
			if (debugMode) {
				remoteDebug.deleteLaunchConfigurations();
				remoteDebug.killAll();
			}

			// End of work
			monitor.done();

			// Propagate the exception
			throw ex;
		}
	}

	/**
	 * Removes the start level and auto start informations from the plug-ins
	 * list
	 * 
	 * @param aPluginList
	 *            A plug-in list from {@link BundlesTab}.
	 * @return The simplified plug-ins list, null if aPluginList was null
	 */
	public List<String> preparePluginsList(final String aPluginList) {

		if (aPluginList == null) {
			return null;
		}

		final String[] plugins = aPluginList.split(",");
		final List<String> resultList = new ArrayList<String>(plugins.length);

		for (String plugin : plugins) {

			// Find the start level, if any
			int endOfName = plugin.indexOf('@');
			if (endOfName == -1) {
				// If no start level indicated, find the auto-start
				endOfName = plugin.indexOf(':');
			}

			final String pluginName;
			if (endOfName == -1) {
				// Nothing found : raw copy
				pluginName = plugin;

			} else {
				// Extract the plug-in name
				pluginName = plugin.substring(0, endOfName);
			}

			// Add it to the list
			resultList.add(pluginName);
		}

		return resultList;
	}
}
