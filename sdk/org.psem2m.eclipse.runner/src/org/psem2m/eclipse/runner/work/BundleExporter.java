/**
 * File:   BundleExporter.java
 * Author: Thomas Calmant
 * Date:   23 août 2011
 */
package org.psem2m.eclipse.runner.work;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.core.exports.PluginExportOperation;
import org.eclipse.pde.internal.core.project.BundleProjectDescription;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.wizards.exports.PluginExportWizard;
import org.eclipse.ui.progress.IProgressConstants;
import org.psem2m.eclipse.runner.RunnerPlugin;

/**
 * Generates and executes the Ant script file that exports the given plug-ins.
 * 
 * Use {@link #createAntScript()} or {@link #createAntScript(File)} then
 * {@link #runScript()}.
 * 
 * @author Thomas Calmant
 */
public class BundleExporter {

	/** Result output folder */
	private String pOutputFolder;

	/** Plug-ins list, comma separated */
	private List<String> pPluginsList;

	/**
	 * Prepares the bundle exporter.
	 * 
	 * @param aOutputFolder
	 *            Bundles output folder
	 * @param aPluginsList
	 *            Plug-ins to export (comma separated list)
	 */
	public BundleExporter(final String aOutputFolder,
			final List<String> aPluginsList) {

		pOutputFolder = aOutputFolder;
		pPluginsList = aPluginsList;
	}

	/**
	 * Runs the PDE Plug-in export wizard. Based on {@link PluginExportWizard}
	 * 
	 * @param aMonitor
	 *            Progress monitor
	 * @throws CoreException
	 *             An error occurred while using the Ant runner
	 */
	@SuppressWarnings("restriction")
	public void export(final IProgressMonitor aMonitor) throws CoreException {

		// Prepare the export information (as if we used the wizard dialog)
		final FeatureExportInfo info = new FeatureExportInfo();
		info.items = findProjectsModels(pPluginsList);
		info.toDirectory = true;
		info.useJarFormat = true;
		info.exportSource = false;
		info.exportSourceBundle = false;
		info.allowBinaryCycles = true;
		info.useWorkspaceCompiledClasses = true;
		info.destinationDirectory = pOutputFolder;
		info.zipFileName = "";
		info.signingInfo = null;
		info.qualifier = "none";

		// Prepare the export job
		final Object lock = new Object();

		final PluginExportOperation job = new PluginExportOperation(info,
				PDEUIMessages.PluginExportJob_name);

		job.setUser(true);
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.setProperty(IProgressConstants.ICON_PROPERTY,
				PDEPluginImages.DESC_PLUGIN_OBJ);
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				if (job.hasAntErrors()) {
					// If there were errors when running the ant scripts, inform
					// the user where the logs can be found.
					final File logLocation = new File(
							info.destinationDirectory, "logs.zip"); //$NON-NLS-1$
					RunnerPlugin.logError("Error, see " + logLocation, null);

				} else if (event.getResult().isOK()) {
					// All went fine
				}

				synchronized (lock) {
					lock.notify();
				}
			}
		});

		job.schedule();

		synchronized (lock) {

			try {
				// Wait for the end of the job
				lock.wait();
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
	}

	@SuppressWarnings("restriction")
	protected Object[] findProjectsModels(final List<String> aPluginsIds) {

		// Use a copy of the argument
		final List<String> pluginsIds = new ArrayList<String>(aPluginsIds);
		final List<IModel> models = new ArrayList<IModel>();

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

					// Store its PDE model
					final IModel model = PluginRegistry.findModel(project);
					if (model != null) {
						models.add(model);
					}

					// TODO: test if the project needs to be re-built
				}

			} catch (CoreException e) {
				// Not a bundle project, try next one
			}
		}

		if (models.size() != aPluginsIds.size()) {
			RunnerPlugin.logWarning("Some plug-ins to export were not found");
		}

		return models.toArray();
	}
}
