/**
 * File:   JarExporter.java
 * Author: Thomas Calmant
 * Date:   6 sept. 2011
 */
package org.psem2m.eclipse.runner.work;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.internal.core.project.BundleProjectDescription;
import org.ow2.chameleon.eclipse.ipojo.exporter.core.BundleExporter;
import org.psem2m.eclipse.runner.RunnerPlugin;

/**
 * Exports project bundles with the iPOJO Nature exporter
 * 
 * @author Thomas Calmant
 */
public class JarExporter {

	/** Bundle Exporter */
	private BundleExporter pExporter;

	/**
	 * Prepares the bundle exporter
	 * 
	 * @param aOutputFolder
	 *            Bundles output folder
	 */
	public JarExporter(final String aOutputFolder) {

		pExporter = new BundleExporter();
		pExporter.setOutputFolder(aOutputFolder);
		pExporter.setUseBuildProperties(true);
	}

	/**
	 * Exports the given projects
	 * 
	 * @param aExportedProjects
	 *            Projects to be exported
	 * @param aMonitor
	 *            Progress monitor
	 * 
	 * @return True if all projects have been exported, false is one generated
	 *         an error
	 */
	public boolean export(final IProject[] aExportedProjects,
			final IProgressMonitor aMonitor) {

		boolean fullSuccess = true;

		for (IProject project : aExportedProjects) {
			try {
				pExporter.exportBundle(project, aMonitor);

			} catch (CoreException ex) {

				fullSuccess = false;
				RunnerPlugin.logError(
						"Error exporting project '" + project.getName() + "'",
						ex);
			}
		}

		return fullSuccess;
	}

	/**
	 * Exports the given plugins
	 * 
	 * @param aSelectedPlugins
	 *            Bundles IDs representing the projects to be exported
	 * @param aMonitor
	 *            Progress monitor
	 * 
	 * @return True if all projects have been exported, false is one generated
	 *         an error
	 */
	public boolean export(final List<String> aSelectedPlugins,
			final IProgressMonitor aMonitor) {

		return export(findProjectsFromId(aSelectedPlugins), aMonitor);
	}

	/**
	 * Finds projects with the given bundle IDs
	 * 
	 * @param aPluginsIds
	 *            Bundle IDs list
	 * @return Projects corresponding to the given IDs
	 */
	@SuppressWarnings("restriction")
	public IProject[] findProjectsFromId(final List<String> aPluginsIds) {

		// Use a copy of the argument
		final List<String> pluginsIds = new ArrayList<String>(aPluginsIds);
		final List<IProject> models = new ArrayList<IProject>();

		// Test all projects
		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		for (IProject project : projects) {

			try {
				// Read the project description
				final BundleProjectDescription bundleProject = new BundleProjectDescription(
						project);

				// Test if the plug-in ID corresponds to the bundle symbolic
				// name
				final String bundleId = bundleProject.getSymbolicName();
				if (pluginsIds.contains(bundleId)) {

					// No need to keep it in further tests
					pluginsIds.remove(bundleId);
				}

			} catch (CoreException e) {
				// Not a bundle project, try next one
			}
		}

		if (models.size() != aPluginsIds.size()) {
			RunnerPlugin.logWarning("Some plug-ins to export were not found");
		}

		return models.toArray(new IProject[0]);
	}
}
