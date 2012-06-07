/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz  (isandlaTech) - 18 nov. 2011 - initial API and implementation
 *******************************************************************************/
package org.psem2m.isolates.ui.admin.panels;

import java.util.concurrent.Executor;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.ui.admin.CBundleUiActivator;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;
import org.psem2m.isolates.ui.admin.api.EUiAdminPanelLocation;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanel;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanelControler;
import org.psem2m.isolates.ui.admin.api.IUiAdminSvc;

/**
 * @author ogattaz
 * 
 */
@Component(name = "psem2m-ui-admin-panel-bundles-factory", publicFactory = false)
@Instantiate(name = "psem2m-ui-admin-panel-bundles")
public class CUiAdminPanelBundles extends CPojoBase implements
        IUiAdminPanelControler {

    /**
     * @author ogattaz
     * 
     */
    class CBundleListener implements BundleListener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework
         * .BundleEvent)
         */
        @Override
        public void bundleChanged(final BundleEvent aBundleEvent) {

            // Returns the type of lifecyle event.
            pLogger.logInfo(this, "bundleChanged", "BundleEvent=[%s]",
                    bundleEventToString(aBundleEvent.getType()));

            pUiExecutor.execute(new Runnable() {

                @Override
                public void run() {

                    if (pJPanel != null) {
                        if (BundleEvent.UNINSTALLED == aBundleEvent.getType()) {
                            pJPanel.removeRow(aBundleEvent.getBundle());
                        } else {
                            pJPanel.setRow(aBundleEvent.getBundle());
                        }
                    }
                }
            });

        }

        /**
         * @param aType
         * @return
         */
        private String bundleEventToString(final int aType) {

            switch (aType) {
            case BundleEvent.INSTALLED: // The bundle has been installed.
                return String.format("%d INSTALLED", aType);
            case BundleEvent.LAZY_ACTIVATION: // The bundle will be lazily
                                              // activated.
                return String.format("%d LAZY_ACTIVATION", aType);
            case BundleEvent.RESOLVED: // The bundle has been resolved.
                return String.format("%d RESOLVED", aType);
            case BundleEvent.STARTED: // The bundle has been started.
                return String.format("%d STARTED", aType);
            case BundleEvent.STARTING: // The bundle is about to be activated.
                return String.format("%d STARTING", aType);
            case BundleEvent.STOPPED: // The bundle has been stopped.
                return String.format("%d STOPPED", aType);
            case BundleEvent.STOPPING: // The bundle is about to deactivated.
                return String.format("%d STOPPING", aType);
            case BundleEvent.UNINSTALLED: // The bundle has been uninstalled.
                return String.format("%d UNINSTALLED", aType);
            case BundleEvent.UNRESOLVED: // The bundle has been unresolved.
                return String.format("%d UNRESOLVED", aType);
            case BundleEvent.UPDATED: // The bundle has been updated.
                return String.format("%d UPDATED", aType);
            default:
                return String.format("%d ???", aType);
            }
        }

    }

    /** the listener */
    private CBundleListener pBundleListener = null;

    /** the JPanel **/
    private CJPanelTableBundles pJPanel = null;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** the UiAdminPanel returned by the IUiAdminScv */
    private IUiAdminPanel pUiAdminPanel = null;

    /** the IUiAdminScv */
    @Requires
    IUiAdminSvc pUiAdminSvc;

    /**
     * Service reference managed by iPojo (see metadata.xml)
     */
    @Requires(filter = "(thread=main)")
    private Executor pUiExecutor;

    /**
     * 
     */
    private void initContent() {

        Runnable wRunnable = new Runnable() {
            @Override
            public void run() {

                pJPanel = new CJPanelTableBundles(pUiExecutor, pLogger,
                        pUiAdminPanel.getPanel());

                pLogger.logInfo(this, "initContent");

                // put in place the list of all installed bundles.
                pJPanel.setRows(CBundleUiActivator.getInstance().getContext()
                        .getBundles());

                pUiAdminPanel.pack();

                pBundleListener = new CBundleListener();

                CBundleUiActivator.getInstance().getContext()
                        .addBundleListener(pBundleListener);
            }
        };
        try {
            // gives the runnable to the UIExecutor
            pUiExecutor.execute(wRunnable);
        } catch (Exception e) {
            pLogger.logSevere(this, "init", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        // logs in the bundle output
        pLogger.logInfo(this, "invalidatePojo", "INVALIDATE", toDescription());

        try {
            if (pBundleListener != null) {
                CBundleUiActivator.getInstance().getContext()
                        .removeBundleListener(pBundleListener);
            }

            if (pJPanel != null) {
                pJPanel.destroy();
                pJPanel = null;
            }

            pUiAdminSvc.removeUiAdminPanel(pUiAdminPanel);

        } catch (Exception e) {
            pLogger.logSevere(this, "invalidatePojo", e);
        }
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

        if (pJPanel != null) {
            pJPanel.setTableFont(aUiAdminFont);
            pJPanel.setTextFont(aUiAdminFont);
        }
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
        pLogger.logInfo(this, "invalidatePojo", "VALIDATE", toDescription());

        try {
            pUiAdminPanel = pUiAdminSvc.newUiAdminPanel("Bundles",
                    "Bundles list and managment.", null, this,
                    EUiAdminPanelLocation.FIRST);

            initContent();

        } catch (Exception e) {
            pLogger.logSevere(this, "validatePojo", e);
        }
    }

}
