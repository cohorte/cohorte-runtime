/**
 * File:   PathSelectorGroup.java
 * Author: Thomas Calmant
 * Date:   23 août 2011
 */
package org.psem2m.eclipse.runner.ui;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * Handles path selection events
 * 
 * @author Thomas Calmant
 */
public class PathSelectorGroupListener implements SelectionListener {

	/** File system button */
	private Button pFileSystemButton;

	/** Path text widget */
	private Text pPathWidget;

	/** SWT shell */
	private Shell pShell;

	/** Variables button */
	private Button pVariablesButton;

	/** Workspace button */
	private Button pWorkspaceButton;

	/**
	 * Prepares the listener
	 * 
	 * @param aTextWidget
	 *            Text widget that will contain the selected path
	 */
	public PathSelectorGroupListener(final Shell aShell, final Text aTextWidget) {

		pShell = aShell;
		pPathWidget = aTextWidget;
	}

	/**
	 * Prompts the user to choose a working directory from the file system.
	 */
	protected void handleFileSystemSelected() {

		DirectoryDialog dialog = new DirectoryDialog(pShell, SWT.SAVE);
		dialog.setMessage("Choose a folder");
		dialog.setFilterPath(pPathWidget.getText());

		String text = dialog.open();
		if (text != null) {
			pPathWidget.setText(text);
		}
	}

	/**
	 * A variable entry button has been pressed for the given text field. Prompt
	 * the user for a variable and enter the result in the project field.
	 */
	private void handleVariablesSelected() {

		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(
				pShell);
		dialog.open();

		String variable = dialog.getVariableExpression();
		if (variable != null) {
			pPathWidget.insert(variable);
		}
	}

	/**
	 * Prompts the user for a working directory location within the workspace
	 * and sets the working directory as a String containing the workspace_loc
	 * variable or <code>null</code> if no location was obtained from the user.
	 */
	protected void handleWorkspaceSelected() {

		ContainerSelectionDialog containerDialog;
		containerDialog = new ContainerSelectionDialog(pShell, ResourcesPlugin
				.getWorkspace().getRoot(), false, "Workspace folder selection");
		containerDialog.open();

		Object[] resource = containerDialog.getResult();
		String text = null;
		if (resource != null && resource.length > 0) {
			text = newVariableExpression("workspace_loc",
					((IPath) resource[0]).toString());
		}

		if (text != null) {
			pPathWidget.setText(text);
		}
	}

	/**
	 * Returns a new variable expression with the given variable and the given
	 * argument.
	 * 
	 * @see IStringVariableManager#generateVariableExpression(String, String)
	 */
	protected String newVariableExpression(final String aVarName,
			final String aArgument) {

		return VariablesPlugin.getDefault().getStringVariableManager()
				.generateVariableExpression(aVarName, aArgument);
	}

	/**
	 * Sets the File System button to listen
	 * 
	 * @param aFileSystemButton
	 *            the File System button to listen
	 */
	public void setFileSystemButton(final Button aFileSystemButton) {
		pFileSystemButton = aFileSystemButton;
	}

	/**
	 * Sets the variables button to listen
	 * 
	 * @param aVariablesButton
	 *            the variables button to listen
	 */
	public void setVariablesButton(final Button aVariablesButton) {
		pVariablesButton = aVariablesButton;
	}

	/**
	 * Sets the workspace button to listen
	 * 
	 * @param aWorkspaceButton
	 *            the workspace button to listen
	 */
	public void setWorkspaceButton(final Button aWorkspaceButton) {
		pWorkspaceButton = aWorkspaceButton;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse
	 * .swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(final SelectionEvent aEvent) {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt
	 * .events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(final SelectionEvent aEvent) {

		final Object source = aEvent.getSource();

		if (source.equals(pWorkspaceButton)) {
			// Workspace folder selection
			handleWorkspaceSelected();

		} else if (source.equals(pFileSystemButton)) {
			// File system selection
			handleFileSystemSelected();

		} else if (source.equals(pVariablesButton)) {
			// Variables insertion
			handleVariablesSelected();
		}
	}
}
