/**
 * File:   RunnerTabGroup.java
 * Author: Thomas Calmant
 * Date:   22 août 2011
 */
package org.psem2m.eclipse.runner.ui;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * Prepares the Run configuration tabs
 * 
 * @author Thomas Calmant
 */
public class RunnerTabGroup extends AbstractLaunchConfigurationTabGroup {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTabGroup#createTabs(org.eclipse
	 * .debug.ui.ILaunchConfigurationDialog, java.lang.String)
	 */
	@Override
	public void createTabs(final ILaunchConfigurationDialog aDialog,
			final String aMode) {

		// Tabs in the tab group
		ILaunchConfigurationTab tabs[] = new ILaunchConfigurationTab[] {
				new Psem2mTab(), new ProjectsSelectionTab(),
				new EnvironmentTab(), new CommonTab() };

		setTabs(tabs);
	}
}
