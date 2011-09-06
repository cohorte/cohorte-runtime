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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
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

	/**
	 * Simple listener that sets the dirty state on any modification
	 */
	private final Listener pEventListener = new Listener() {

		@Override
		public void handleEvent(final Event aEvent) {
			// Set dirty state on
			setDirty(true);
			updateLaunchConfigurationDialog();
		}
	};

	/** IDE Project image */
	private final Image pProjectImage = PlatformUI.getWorkbench()
			.getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);

	/** Projects list */
	private Table pProjectsTable;

	/** "Select all" button */
	private Button pSelectAll;

	/**
	 * Project selection buttons
	 */
	private final Listener pSelectButtonListener = new Listener() {

		@Override
		public void handleEvent(final Event aEvent) {
			selectAllProjects(pSelectAll.equals(aEvent.widget));
		}
	};

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

		final Rectangle rect = aParent.getClientArea();
		pageRoot.setSize(rect.width - rect.x, rect.height - rect.y);

		// Prepare the project selection
		createProjectSelectionTable(pageRoot);

		// Add the build.properties option
		pUseBuildProperties = createCheckButton(pageRoot,
				"Use build.properties");
		pUseBuildProperties.addListener(SWT.Selection, pEventListener);

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
		layout.numColumns = 2;
		aParent.setLayout(layout);

		// Prepare the table
		pProjectsTable = new Table(aParent, SWT.BORDER | SWT.CHECK
				| SWT.H_SCROLL | SWT.V_SCROLL);

		GridData tableData = new GridData(GridData.FILL_BOTH);
		tableData.horizontalSpan = 2;
		pProjectsTable.setLayoutData(tableData);

		// Fill it
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot()
				.getProjects()) {

			try {
				if (project.hasNature(JavaCore.NATURE_ID)) {
					// Only Java projects are shown
					final TableItem item = new TableItem(pProjectsTable,
							SWT.NONE);
					item.setText(project.getName());
					item.setImage(pProjectImage);
					item.setData(project.getName());
				}

			} catch (CoreException ex) {
				IPojoExporterPlugin.logWarning("Can't test nature of "
						+ project.getName(), ex);
			}
		}

		pProjectsTable.addListener(SWT.Selection, pEventListener);

		// Buttons
		pSelectAll = new Button(aParent, SWT.NONE);
		pSelectAll.setText("Select all");
		pSelectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pSelectAll.addListener(SWT.Selection, pSelectButtonListener);

		Button unselectAll = new Button(aParent, SWT.NONE);
		unselectAll.setText("Unselect all");
		unselectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		unselectAll.addListener(SWT.Selection, pSelectButtonListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return pProjectImage;
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

	/**
	 * Checks or un-checks all projects in the table
	 * 
	 * @param aCheck
	 *            True to check all, False to un-check all
	 */
	public void selectAllProjects(final boolean aCheck) {

		// Check or un-check all items
		for (TableItem item : pProjectsTable.getItems()) {
			item.setChecked(aCheck);
		}

		// Set dirty state
		setDirty(true);
		updateLaunchConfigurationDialog();
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
			item.setChecked(aProjectList.contains(itemData));
		}
	}
}
