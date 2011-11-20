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

    /**
     * iPOJO Components architectures
     * 
     * @see the methodes "architectureBind" and "architectureUnBind" which are
     *      called each time an architecture appeared or desapeared
     */
    @Requires
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
    IUiAdminSvc pUiAdminSvc;

    /**
     * Service reference managed by iPojo (see metadata.xml)
     */
    @Requires(filter = "(thread=main)")
    private Executor pUiExecutor;

    /**
     * Update the list of components each time an Architecture appears
     * 
     * @param aArch
     */
    @Bind
    void architectureBind(final Architecture aArch) {

        pLogger.logInfo(this, "architectureBind", "component=[%s]", aArch
                .getInstanceDescription().getName());

        pJPanel.setRow(aArch);

    }

    /**
     * Update the list of components each time an Architecture desappears
     * 
     * @param aArch
     */
    @Unbind
    void architectureUnBind(final Architecture aArch) {

        pLogger.logInfo(this, "architectureUnBind", "component=[%s]", aArch
                .getInstanceDescription().getName());

        pJPanel.removeRow(aArch);
    }

    /**
     * 
     */
    private void initContent() {

        Runnable wRunnable = new Runnable() {
            @Override
            public void run() {

                pLogger.logInfo(this, "initContent");

                // put in place the list of all registered services.
                pJPanel.addRows(pArchitectures);

                pUiAdminPanel.pack();
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
            pJPanel.destroy();

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

        pJPanel.setTableFont(aUiAdminFont);
        pJPanel.setTextFont(aUiAdminFont);
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
            pUiAdminPanel = pUiAdminSvc.newUiAdminPanel("Components",
                    "Componnents list and managment.", null, this,
                    EUiAdminPanelLocation.FIRST);

            pJPanel = new CJPanelTableComponents(pUiExecutor, pLogger,
                    pUiAdminPanel.getPanel());

            initContent();

        } catch (Exception e) {
            pLogger.logSevere(this, "validatePojo", e);
        }
    }
}
