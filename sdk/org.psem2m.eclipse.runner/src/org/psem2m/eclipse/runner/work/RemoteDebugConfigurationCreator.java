/**
 * File:   RemoteDebugConfigurationCreator.java
 * Author: Thomas Calmant
 * Date:   23 août 2011
 */
package org.psem2m.eclipse.runner.work;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.psem2m.eclipse.runner.RunnerPlugin;

/**
 * Prepares all needed debug configurations
 * 
 * @author Thomas Calmant
 */
public class RemoteDebugConfigurationCreator {

	/** The list of current configurations */
	private final List<ILaunchConfiguration> pLaunchConfigurations = new ArrayList<ILaunchConfiguration>();

	private List<ILaunch> pLaunchList = new ArrayList<ILaunch>();

	/** The number of configurations to set up */
	private int pNbInstances;

	/**
	 * Sets up the creator
	 * 
	 * @param aNbInstances
	 *            Number of configurations to set up
	 */
	public RemoteDebugConfigurationCreator(final int aNbInstances) {

		pNbInstances = aNbInstances;
	}

	/**
	 * Creates all needed configurations
	 * 
	 * @param aFirstListeningPort
	 *            Base socket listening port
	 * @throws CoreException
	 *             An error occurred while preparing configurations
	 */
	public void createLaunchConfigurations(final int aFirstListeningPort)
			throws CoreException {

		// Delete current configurations
		if (!pLaunchConfigurations.isEmpty()) {
			deleteLaunchConfigurations();
		}

		// Retrieve the launch configuration type
		final ILaunchConfigurationType remoteDebugType = DebugPlugin
				.getDefault()
				.getLaunchManager()
				.getLaunchConfigurationType(
						IJavaLaunchConfigurationConstants.ID_REMOTE_JAVA_APPLICATION);

		// Create and store all configurations needed
		for (int i = 0; i < pNbInstances; i++) {

			// Prepare the configuration
			final ILaunchConfigurationWorkingCopy remoteDebugConfig;

			try {
				remoteDebugConfig = remoteDebugType.newInstance(null,
						"PSEM2M-Runner-Isolate-" + i);

			} catch (CoreException ex) {
				deleteLaunchConfigurations();
				throw ex;
			}

			// Set the socket listen mode
			remoteDebugConfig
					.setAttribute(
							IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR,
							IJavaLaunchConfigurationConstants.ID_SOCKET_LISTEN_VM_CONNECTOR);

			// Set the listening port
			final Map<String, Object> connectorMap = new HashMap<String, Object>();
			connectorMap.put("port", Integer.toString(aFirstListeningPort + i));

			remoteDebugConfig.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP,
					connectorMap);

			// Set the source path
			remoteDebugConfig.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH,
					false);

			remoteDebugConfig.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH,
					getAllJavaProjectsMemento());

			// Store it
			pLaunchConfigurations.add(remoteDebugConfig);
		}
	}

	/**
	 * Deletes all generated launch configurations
	 */
	public void deleteLaunchConfigurations() {

		// Delete all configurations
		for (ILaunchConfiguration config : pLaunchConfigurations) {
			try {
				config.delete();

			} catch (CoreException ex) {
				RunnerPlugin
						.logError(
								"An error occurred while cleaning remote debug configurations",
								ex);
			}
		}

		// Clear the list
		pLaunchConfigurations.clear();
	}

	/**
	 * Retrieves all Java project source paths in the workspace
	 * 
	 * @return An array ready to be used with
	 *         {@link IJavaLaunchConfigurationConstants#ATTR_SOURCE_PATH}
	 */
	protected List<String> getAllJavaProjectsMemento() {

		final List<String> resultList = new ArrayList<String>();

		// Get all projects in workspace
		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		for (IProject project : projects) {

			try {
				if (project.hasNature(JavaCore.NATURE_ID)) {
					// Project has a Java nature
					final IJavaProject javaProject = (IJavaProject) project
							.getNature(JavaCore.NATURE_ID);

					// Prepare the runtime entry
					final IRuntimeClasspathEntry classpathEntry = JavaRuntime
							.newProjectRuntimeClasspathEntry(javaProject);

					// Store its memento (XML String version)
					resultList.add(classpathEntry.getMemento());
				}

			} catch (CoreException ex) {
				// Ignore errors : we're just helping the user
			}
		}

		return resultList;
	}

	/**
	 * Kills all launched processes
	 */
	public void killAll() {

		for (ILaunch launch : pLaunchList) {

			try {
				// Terminate it
				launch.terminate();

			} catch (CoreException ex) {
				RunnerPlugin.logError("Error stopping a Remote Debug launch",
						ex);
			}
		}
	}

	/**
	 * Run all prepared configurations
	 */
	public void run() throws CoreException {

		for (final ILaunchConfiguration config : pLaunchConfigurations) {
			/* Don't use DebugUITools.launch() : it messes with threads */
			ILaunch launch = DebugUITools.buildAndLaunch(config,
					ILaunchManager.DEBUG_MODE, new NullProgressMonitor());

			pLaunchList.add(launch);
		}
	}
}
