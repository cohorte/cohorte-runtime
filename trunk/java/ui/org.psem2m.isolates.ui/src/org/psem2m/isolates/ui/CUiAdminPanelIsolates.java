package org.psem2m.isolates.ui;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.monitoring.IIsolatePresenceListener;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;
import org.psem2m.isolates.ui.admin.api.EUiAdminPanelLocation;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanel;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanelControler;
import org.psem2m.isolates.ui.admin.api.IUiAdminSvc;
import org.psem2m.signals.ISignalDirectory;

/**
 * 
 * Provides IIsolatePresenceListener : la référence de
 * 
 * @author ogattaz
 * 
 */
@Component(name = "psem2m-isolates-ui-admin-factory", publicFactory = false)
@Instantiate(name = "psem2m-isolates-ui-admin")
@Provides(specifications = IIsolatePresenceListener.class)
public class CUiAdminPanelIsolates extends CPojoBase implements IUiAdminPanelControler,
		IIsolatePresenceListener {

	private CIsolatesTreeModel pIsolatesTreeModel;

	/** The logger */
	@Requires
	private IIsolateLoggerSvc pLogger;

	private CJPanelIsolates pPanelIsolates = null;

	/** the ISignalDirectory service ( eg. getAllIsolates() ) */
	@Requires
	private ISignalDirectory pSignalDirectory;

	/** the UiAdminPanel returned by the IUiAdminScv */
	private IUiAdminPanel pUiAdminPanel = null;

	/** the IUiAdminScv */
	@Requires
	private IUiAdminSvc pUiAdminSvc;

	/**
     * 
     */
	private void destroyContent() {

		if (pUiAdminPanel != null) {
			pUiAdminSvc.removeUiAdminPanel(pUiAdminPanel);

			pUiAdminPanel.getPanel().removeAll();
		}

		if (pPanelIsolates != null) {
			pPanelIsolates = null;
		}

		if (pIsolatesTreeModel != null) {
			pIsolatesTreeModel = null;
		}
	}

	/**
     * 
     */
	private void initContent() {

		/* the tree model */
		pIsolatesTreeModel = new CIsolatesTreeModel(pSignalDirectory);

		final Runnable wRunnable = new Runnable() {

			@Override
			public void run() {

				try {
					/* The parent panel */
					pUiAdminPanel = pUiAdminSvc.newUiAdminPanel("Isolates",
							"Isolates list and managment.", null, CUiAdminPanelIsolates.this,
							EUiAdminPanelLocation.FIRST);

					final JPanel parentPanel = pUiAdminPanel.getPanel();

					/* The tree panel */
					pPanelIsolates = new CJPanelIsolates(pLogger, parentPanel, pIsolatesTreeModel);

				} catch (final Exception e) {
					pLogger.logSevere(CUiAdminPanelIsolates.this, "initContent", e);
				}
			}
		};

		SwingUtilities.invokeLater(wRunnable);
	}

	@Override
	@Invalidate
	public void invalidatePojo() throws BundleException {

		// logs in the bundle output
		pLogger.logInfo(this, "invalidatePojo", "INVALIDATE", toDescription());

		try {

			// remove isolates panel
			destroyContent();

		} catch (final Exception e) {
			pLogger.logSevere(this, "invalidatePojo", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.services.monitoring.IIsolatePresenceListener#isolateLost
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void isolateLost(final String aUID, final String aName, final String aNode) {

		pLogger.logInfo(this, "isolateLost", "Del - Node=[%s] IsolateId=[%s]", aNode, aUID);

		if (pIsolatesTreeModel != null) {
			pIsolatesTreeModel.removeIsolate(aUID, aName, aNode);
		}

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				if (pPanelIsolates != null) {
					pPanelIsolates.updateTree();
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.services.monitoring.IIsolatePresenceListener#isolateReady
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void isolateReady(final String aUID, final String aName, final String aNode) {

		pLogger.logInfo(this, "isolateReady", "Add - Node=[%s] IsolateId=[%s]", aNode, aUID);

		if (pIsolatesTreeModel != null) {
			pIsolatesTreeModel.addIsolate(aUID, aName, aNode);
		}

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				if (pPanelIsolates != null) {
					pPanelIsolates.updateTree();
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.ui.admin.api.IUiAdminPanelControler#setUiAdminFont
	 * (org.psem2m.isolates.ui.admin.api.EUiAdminFont)
	 */
	@Override
	public void setUiAdminFont(final EUiAdminFont aUiAdminFont) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
	 */
	@Override
	@Validate
	public void validatePojo() throws BundleException {

		// logs in the bundle output
		pLogger.logInfo(this, "validatePojo", "VALIDATE", toDescription());

		try {
			// Set up GUI in a thread
			initContent();

		} catch (final Exception e) {
			pLogger.logSevere(this, "validatePojo", e);
		}
	}

}
