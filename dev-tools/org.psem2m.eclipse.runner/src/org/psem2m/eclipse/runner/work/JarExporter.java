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
	 * Exports the given projects using iPOJO Nature exporter
	 * 
	 * @param aSelectedProjectsNames
	 *            Names of the projects to be exported
	 * @param aMonitor
	 *            Progress monitor
	 * 
	 * @return True if all projects have been exported, false is one generated
	 *         an error
	 */
	public boolean export(final List<String> aSelectedProjectsNames,
			final IProgressMonitor aMonitor) {

		return export(findProjectsFromName(aSelectedProjectsNames), aMonitor);
	}

	/**
	 * Finds the projects corresponding to the given names
	 * 
	 * @param aProjectNames
	 *            Project names
	 * @return The corresponding projects
	 */
	public IProject[] findProjectsFromName(final List<String> aProjectNames) {

		final List<IProject> selectedProjects = new ArrayList<IProject>(
				aProjectNames.size());

		// Test all names
		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		for (IProject project : projects) {

			if (aProjectNames.contains(project.getName())) {
				// Project found
				selectedProjects.add(project);
			}
		}

		return selectedProjects.toArray(new IProject[0]);
	}
}
