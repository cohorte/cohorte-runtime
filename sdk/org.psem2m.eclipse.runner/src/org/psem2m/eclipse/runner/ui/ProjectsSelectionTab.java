/**
 * File:   ProjectsSelectionTab.java
 * Author: Thomas Calmant
 * Date:   6 sept. 2011
 */
package org.psem2m.eclipse.runner.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.ow2.chameleon.eclipse.ipojo.exporter.IPojoExporterPlugin;
import org.psem2m.eclipse.runner.IRunnerConfigurationConstants;
import org.psem2m.eclipse.runner.RunnerPlugin;

/**
 * Project list selection tab
 * 
 * @author Thomas Calmant
 */
public class ProjectsSelectionTab extends AbstractLaunchConfigurationTab {

	/** Projects list */
	private Table pProjectsTable;

	/** "Use build.properties" button */
	private Button pUseBuildProperties;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	public void createControl(final Composite aParent) {

		// Prepare the page content
		final Composite pageRoot = new Composite(aParent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		pageRoot.setLayout(layout);

		// Prepare the project selection
		createProjectSelectionTable(pageRoot);

		// Add the build.properties option
		pUseBuildProperties = createCheckButton(pageRoot,
				"Use build.properties");

		// Set the main control
		setControl(pageRoot);
	}

	/**
	 * Creates the projects selection controls
	 * 
	 * @param aParent
	 *            Parent widget
	 */
	protected void createProjectSelectionTable(final Composite aParent) {

		// Set a grid layout
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		aParent.setLayout(layout);

		// Prepare the project image
		final Image projectImage = PlatformUI.getWorkbench().getSharedImages()
				.getImage(IDE.SharedImages.IMG_OBJ_PROJECT);

		// Prepare the table
		pProjectsTable = new Table(aParent, SWT.BORDER | SWT.CHECK
				| SWT.H_SCROLL | SWT.V_SCROLL);
		pProjectsTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Fill it
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot()
				.getProjects()) {

			try {
				if (project.hasNature(JavaCore.NATURE_ID)) {
					// Only Java projects are shown
					final TableItem item = new TableItem(pProjectsTable,
							SWT.NONE);
					item.setText(project.getName());
					item.setImage(projectImage);
					item.setData(project);
				}

			} catch (CoreException ex) {
				IPojoExporterPlugin.logWarning("Can't test nature of "
						+ project.getName(), ex);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	@Override
	public String getName() {
		return "Projects";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse
	 * .debug.core.ILaunchConfiguration)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initializeFrom(final ILaunchConfiguration aConfiguration) {

		try {
			pUseBuildProperties.setSelection(aConfiguration.getAttribute(
					IRunnerConfigurationConstants.EXPORT_USE_BUILD_PROPERTIES,
					true));

			setTableSelection(aConfiguration.getAttribute(
					IRunnerConfigurationConstants.EXPORT_PROJECTS_LIST,
					new ArrayList<String>()));

		} catch (CoreException e) {
			RunnerPlugin.logError("Error reading launch configuration", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse
	 * .debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(
			final ILaunchConfigurationWorkingCopy aConfiguration) {

		// Use build.properties
		aConfiguration.setAttribute(
				IRunnerConfigurationConstants.EXPORT_USE_BUILD_PROPERTIES,
				pUseBuildProperties.getSelection());

		// Projects list
		final List<String> projectsList = new ArrayList<String>();
		for (TableItem item : pProjectsTable.getItems()) {

			// Grab all checked projects
			if (item.getChecked()) {
				final Object itemData = item.getData();
				if (itemData instanceof String) {
					projectsList.add((String) itemData);
				}
			}
		}

		aConfiguration.setAttribute(
				IRunnerConfigurationConstants.EXPORT_PROJECTS_LIST,
				projectsList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.
	 * debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy aConfiguration) {

		// Use build.properties by default
		aConfiguration
				.setAttribute(
						IRunnerConfigurationConstants.EXPORT_USE_BUILD_PROPERTIES,
						true);

		// No export, by default (empty list)
		aConfiguration.setAttribute(
				IRunnerConfigurationConstants.EXPORT_PROJECTS_LIST,
				new ArrayList<String>());
	}

	/**
	 * Checks the table projects according to the given list
	 * 
	 * @param aProjectList
	 *            Selected project list
	 */
	protected void setTableSelection(final List<String> aProjectList) {

		for (TableItem item : pProjectsTable.getItems()) {

			final Object itemData = item.getData();
			if (itemData instanceof String) {
				item.setChecked(aProjectList.contains(itemData));
			}
		}
	}
}
