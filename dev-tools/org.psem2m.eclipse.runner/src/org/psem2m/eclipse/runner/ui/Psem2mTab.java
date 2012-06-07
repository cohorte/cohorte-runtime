/**
 * File:   Psem2mTab.java
 * Author: "Thomas Calmant"
 * Date:   23 août 2011
 */
package org.psem2m.eclipse.runner.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.psem2m.eclipse.runner.IRunnerConfigurationConstants;

/**
 * PSEM2M configuration tab
 * 
 * @author Thomas Calmant
 */
public class Psem2mTab extends AbstractLaunchConfigurationTab {

	/**
	 * This listener must be called on SWT.VERIFY events, to test if the
	 * inserted character is between 0 and 9.
	 * 
	 * @author Thomas Calmant
	 */
	protected class IntegerTextListener implements Listener {

		@Override
		public void handleEvent(final Event aEvent) {

			final String text = aEvent.text;

			// Test all character
			for (char character : text.toCharArray()) {

				// Refuse on first invalid character
				if (character < '0' || character > '9') {
					aEvent.doit = false;
					return;
				}
			}
		}
	}

	/**
	 * Tab elements modification listener
	 * 
	 * @author Thomas Calmant
	 */
	private class ModificationListener implements ModifyListener,
			SelectionListener {

		@Override
		public void modifyText(final ModifyEvent aEvent) {
			setDirty(true);
			updateLaunchConfigurationDialog();
		}

		@Override
		public void widgetDefaultSelected(final SelectionEvent aEvent) {
			setDirty(true);
			updateLaunchConfigurationDialog();
		}

		@Override
		public void widgetSelected(final SelectionEvent aEvent) {
			setDirty(true);
			updateLaunchConfigurationDialog();
		}
	}

	/** Base port for remote debugging */
	private Text pBaseDebugPort;

	/** Exported bundles path */
	private Text pExportedBundlesPath;

	/** Common integer verification listener */
	private final IntegerTextListener pIntegerTextListener = new IntegerTextListener();

	/** Common modification listener */
	private final ModificationListener pModificationListener = new ModificationListener();

	/** Number of isolates (of remote debug configurations to create) */
	private Text pNbDebuggers;

	/** PSEM2M Base path */
	private Text pPlatformBase;

	/** PSEM2M Home path */
	private Text pPlatformHome;

	/** Working directory */
	private Text pWorkingDirectory;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	public void createControl(final Composite aParent) {

		// Create the root control
		Composite composite = new Composite(aParent, SWT.NONE);
		setGridLayout(composite, 1);

		setControl(composite);

		// The PSEM2M environment configuration group
		createEnvironmentGroup(composite);

		// Create debug options group
		createDebugGroup(composite);
	}

	/**
	 * Prepares the debug options group
	 * 
	 * @param aParent
	 *            Parent widget
	 */
	private void createDebugGroup(final Composite aParent) {

		// The group
		final Group debugGroup = new Group(aParent, SWT.NONE);
		debugGroup.setText("Debug options");

		final GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		debugGroup.setLayout(layout);
		debugGroup.setLayoutData(gridData);

		// Number of isolates
		Label label = new Label(debugGroup, SWT.RIGHT);
		label.setText("Number of processes : ");
		gridData = new GridData(GridData.BEGINNING);
		gridData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		label.setLayoutData(gridData);

		pNbDebuggers = new Text(debugGroup, SWT.BORDER);
		gridData = new GridData(GridData.BEGINNING);
		gridData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		pNbDebuggers.setLayoutData(gridData);

		// Only accept integers
		pNbDebuggers.addListener(SWT.Verify, pIntegerTextListener);
		pNbDebuggers.addModifyListener(pModificationListener);

		// Base debug port
		label = new Label(debugGroup, SWT.RIGHT);
		label.setText("Base debug port : ");
		gridData = new GridData(GridData.BEGINNING);
		gridData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		label.setLayoutData(gridData);

		pBaseDebugPort = new Text(debugGroup, SWT.BORDER);
		gridData = new GridData(GridData.BEGINNING);
		gridData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		pBaseDebugPort.setLayoutData(gridData);

		// Only accept integers
		pBaseDebugPort.addListener(SWT.Verify, pIntegerTextListener);
		pBaseDebugPort.addModifyListener(pModificationListener);
	}

	/**
	 * Prepares the PSEM2M environment group
	 * 
	 * @param aParent
	 *            Parent widget
	 */
	private void createEnvironmentGroup(final Composite aParent) {

		// PSEM2M Home path
		{
			final Group homeGroup = new Group(aParent, SWT.NONE);
			homeGroup.setText("PSEM2M Home");

			final GridLayout layout = new GridLayout();
			layout.numColumns = 1;

			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			homeGroup.setLayout(layout);
			homeGroup.setLayoutData(gridData);

			pPlatformHome = new Text(homeGroup, SWT.BORDER);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
			pPlatformHome.setLayoutData(gridData);
			pPlatformHome.addModifyListener(pModificationListener);

			createPathSelectionGroup(homeGroup, pPlatformHome);
		}

		// PSEM2M Base path
		{
			final Group baseGroup = new Group(aParent, SWT.NONE);
			baseGroup.setText("PSEM2M Base");

			final GridLayout layout = new GridLayout();
			layout.numColumns = 1;

			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			baseGroup.setLayout(layout);
			baseGroup.setLayoutData(gridData);

			pPlatformBase = new Text(baseGroup, SWT.BORDER);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
			pPlatformBase.setLayoutData(gridData);
			pPlatformBase.addModifyListener(pModificationListener);

			createPathSelectionGroup(baseGroup, pPlatformBase);
		}

		// Working directory
		{
			final Group workGroup = new Group(aParent, SWT.NONE);
			workGroup.setText("Instance working directory");

			final GridLayout layout = new GridLayout();
			layout.numColumns = 1;

			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			workGroup.setLayout(layout);
			workGroup.setLayoutData(gridData);

			pWorkingDirectory = new Text(workGroup, SWT.BORDER);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
			pWorkingDirectory.setLayoutData(gridData);
			pWorkingDirectory.addModifyListener(pModificationListener);

			createPathSelectionGroup(workGroup, pWorkingDirectory);
		}

		// Bundle export path
		{
			final Group baseGroup = new Group(aParent, SWT.NONE);
			baseGroup.setText("Bundle export path");

			final GridLayout layout = new GridLayout();
			layout.numColumns = 1;

			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			baseGroup.setLayout(layout);
			baseGroup.setLayoutData(gridData);

			pExportedBundlesPath = new Text(baseGroup, SWT.BORDER);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
			pExportedBundlesPath.setLayoutData(gridData);
			pExportedBundlesPath.addModifyListener(pModificationListener);

			createPathSelectionGroup(baseGroup, pExportedBundlesPath);
		}
	}

	/**
	 * Create a 3-button path selection group : workspace, file system,
	 * variables
	 * 
	 * @param aParent
	 *            Parent element
	 */
	public void createPathSelectionGroup(final Composite aParent,
			final Text aPathWidget) {

		// Listener preparation
		final PathSelectorGroupListener listener = new PathSelectorGroupListener(
				getShell(), aPathWidget);

		// 3-buttons group
		Composite buttonComposite = new Composite(aParent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 3;
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(gridData);
		buttonComposite.setFont(aParent.getFont());

		// Workspace folder selection
		Button workspaceLocationButton = createPushButton(buttonComposite,
				"Workspace...", null);
		listener.setWorkspaceButton(workspaceLocationButton);
		workspaceLocationButton.addSelectionListener(listener);

		// File system folder selection
		Button fileSystemLocationButton = createPushButton(buttonComposite,
				"File System...", null);
		listener.setFileSystemButton(fileSystemLocationButton);
		fileSystemLocationButton.addSelectionListener(listener);

		// Variables injection
		Button variablesLocationButton = createPushButton(buttonComposite,
				"Variables", null);
		listener.setVariablesButton(variablesLocationButton);
		variablesLocationButton.addSelectionListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	@Override
	public String getName() {
		return "PSEM2M";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse
	 * .debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(final ILaunchConfiguration aConfiguration) {

		// Home path
		setConfiguredText(aConfiguration,
				IRunnerConfigurationConstants.PSEM2M_HOME, "", pPlatformHome);

		// Base path
		setConfiguredText(aConfiguration,
				IRunnerConfigurationConstants.PSEM2M_BASE, "", pPlatformBase);

		// Working directory
		setConfiguredText(aConfiguration,
				IRunnerConfigurationConstants.WORKING_DIRECTORY, "",
				pWorkingDirectory);

		// Export path
		setConfiguredText(aConfiguration,
				IRunnerConfigurationConstants.EXPORT_OUTPUT_FOLDER, "",
				pExportedBundlesPath);

		// Debuggers count
		setConfiguredInteger(aConfiguration,
				IRunnerConfigurationConstants.DEBUG_COUNT,
				IRunnerConfigurationConstants.DEFAULT_DEBUG_COUNT, pNbDebuggers);

		// Base remote debug port
		setConfiguredInteger(aConfiguration,
				IRunnerConfigurationConstants.DEBUG_PORT,
				IRunnerConfigurationConstants.DEFAULT_DEBUG_PORT,
				pBaseDebugPort);
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

		// Home path
		aConfiguration.setAttribute(IRunnerConfigurationConstants.PSEM2M_HOME,
				pPlatformHome.getText());

		// Base path
		aConfiguration.setAttribute(IRunnerConfigurationConstants.PSEM2M_BASE,
				pPlatformBase.getText());

		// Working directory
		aConfiguration.setAttribute(
				IRunnerConfigurationConstants.WORKING_DIRECTORY,
				pWorkingDirectory.getText());

		// Export path
		aConfiguration.setAttribute(
				IRunnerConfigurationConstants.EXPORT_OUTPUT_FOLDER,
				pExportedBundlesPath.getText());

		// Debuggers count
		aConfiguration.setAttribute(IRunnerConfigurationConstants.DEBUG_COUNT,
				Integer.valueOf(pNbDebuggers.getText()));

		// Base remote debug port
		aConfiguration.setAttribute(IRunnerConfigurationConstants.DEBUG_PORT,
				Integer.valueOf(pBaseDebugPort.getText()));
	}

	/**
	 * Sets the text field content using the given configuration or the default
	 * value.
	 * 
	 * @param aConfiguration
	 *            Launch configuration to be used
	 * @param aConfigurationKey
	 *            Text field key in the launch configuration
	 * @param aDefaultValue
	 *            Value to be used if the text field key is not present
	 * @param aTextField
	 *            Target text field
	 */
	protected void setConfiguredInteger(
			final ILaunchConfiguration aConfiguration,
			final String aConfigurationKey, final int aDefaultValue,
			final Text aTextField) {

		int value;
		try {
			value = aConfiguration.getAttribute(aConfigurationKey,
					aDefaultValue);

		} catch (CoreException e) {
			value = aDefaultValue;
		}

		aTextField.setText(Integer.toString(value));
	}

	/**
	 * Sets the text field content using the given configuration or the default
	 * value.
	 * 
	 * @param aConfiguration
	 *            Launch configuration to be used
	 * @param aConfigurationKey
	 *            Text field key in the launch configuration
	 * @param aDefaultValue
	 *            Value to be used if the text field key is not present
	 * @param aTextField
	 *            Target text field
	 */
	protected void setConfiguredText(final ILaunchConfiguration aConfiguration,
			final String aConfigurationKey, final String aDefaultValue,
			final Text aTextField) {

		String value;
		try {
			value = aConfiguration.getAttribute(aConfigurationKey,
					String.valueOf(aDefaultValue));

		} catch (CoreException e) {
			value = aDefaultValue;
		}

		aTextField.setText(value);
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

		// Home path
		aConfiguration.setAttribute(IRunnerConfigurationConstants.PSEM2M_HOME,
				"");

		// Base path
		aConfiguration.setAttribute(IRunnerConfigurationConstants.PSEM2M_BASE,
				"");

		// Export path
		aConfiguration.setAttribute(
				IRunnerConfigurationConstants.EXPORT_OUTPUT_FOLDER, "");

		// Debuggers count
		aConfiguration.setAttribute(IRunnerConfigurationConstants.DEBUG_COUNT,
				IRunnerConfigurationConstants.DEFAULT_DEBUG_COUNT);

		// Base remote debug port
		aConfiguration.setAttribute(IRunnerConfigurationConstants.DEBUG_PORT,
				IRunnerConfigurationConstants.DEFAULT_DEBUG_PORT);
	}

	/**
	 * Applies a new grid layout to the given control
	 * 
	 * @param aControl
	 *            Control to use
	 * @param aNumColumns
	 *            Number of columns in the grid layouts
	 */
	protected void setGridLayout(final Composite aControl, final int aNumColumns) {

		GridLayout layout = new GridLayout();
		layout.numColumns = aNumColumns;

		aControl.setLayout(layout);
		aControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
}
