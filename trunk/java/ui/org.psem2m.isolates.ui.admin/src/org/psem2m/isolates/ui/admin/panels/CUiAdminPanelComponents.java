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

import javax.swing.SwingUtilities;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.architecture.Architecture;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;
import org.psem2m.isolates.ui.admin.api.EUiAdminPanelLocation;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanel;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanelControler;
import org.psem2m.isolates.ui.admin.api.IUiAdminSvc;

/**
 * @author ogattaz
 * 
 */
@Component(name = "psem2m-ui-admin-panel-components-factory", publicFactory = false)
@Instantiate(name = "psem2m-ui-admin-panel-components")
public class CUiAdminPanelComponents extends CPojoBase implements
        IUiAdminPanelControler {

    private static final String IPOJO_ID_ARCHITECTURES = "architectures";

    /**
     * iPOJO Components architectures
     * 
     * @see the methodes "architectureBind" and "architectureUnBind" which are
     *      called each time an architecture appeared or disappears
     */
    @Requires(id = IPOJO_ID_ARCHITECTURES)
    private Architecture[] pArchitectures;

    /** the JPanel **/
    private CJPanelTableComponents pJPanel = null;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** the UiAdminPanel returned by the IUiAdminScv **/
    private IUiAdminPanel pUiAdminPanel = null;

    /** the IUiAdminScv **/
    @Requires
    private IUiAdminSvc pUiAdminSvc;

    /**
     * Update the list of components each time an Architecture appears
     * 
     * @param aArch
     */
    @Bind(id = IPOJO_ID_ARCHITECTURES, aggregate = true)
    protected void architectureBind(final Architecture aArch) {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                if (pJPanel != null) {
                    pJPanel.setRow(aArch);
                }
            }
        });
    }

    /**
     * Update the list of components each time an Architecture disappears
     * 
     * @param aArch
     */
    @Unbind(id = IPOJO_ID_ARCHITECTURES, aggregate = true)
    protected void architectureUnBind(final Architecture aArch) {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                if (pJPanel != null) {
                    pJPanel.removeRow(aArch);
                }
            }
        });
    }

    /**
     * 
     */
    private void initContent() {

        final Runnable wRunnable = new Runnable() {

            @Override
            public void run() {

                pLogger.logInfo(this, "initContent");

                pJPanel = new CJPanelTableComponents(pLogger,
                        pUiAdminPanel.getPanel());

                // put in place the list of all registered services.
                pJPanel.setRows(pArchitectures);

                pUiAdminPanel.pack();
            }
        };

        // gives the runnable to the UIExecutor
        SwingUtilities.invokeLater(wRunnable);
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
            pUiAdminSvc.removeUiAdminPanel(pUiAdminPanel);

            if (pJPanel != null) {
                pJPanel.destroy();
                pJPanel = null;
            }

        } catch (final Exception e) {
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
        pLogger.logInfo(this, "validatePojo", "VALIDATE", toDescription());

        try {
            pUiAdminPanel = pUiAdminSvc.newUiAdminPanel("Components",
                    "Componnents list and managment.", null, this,
                    EUiAdminPanelLocation.FIRST);

            initContent();

        } catch (final Exception e) {
            pLogger.logSevere(this, "validatePojo", e);
        }
    }
}
