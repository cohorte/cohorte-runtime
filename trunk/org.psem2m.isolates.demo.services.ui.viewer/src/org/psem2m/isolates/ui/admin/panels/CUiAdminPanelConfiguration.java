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
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.conf.IBundleDescr;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;
import org.psem2m.isolates.ui.admin.api.EUiAdminPanelLocation;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanel;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanelControler;
import org.psem2m.isolates.ui.admin.api.IUiAdminSvc;
import org.psem2m.utilities.CXListUtils;
import org.psem2m.utilities.CXStringUtils;

/**
 * @author ogattaz
 * 
 */
@Component(name = "psem2m-ui-admin-panel-configuration-factory", publicFactory = false)
@Instantiate(name = "psem2m-ui-admin-panel-configuration")
public class CUiAdminPanelConfiguration extends CPojoBase implements
        IUiAdminPanelControler {

    /** the JPanel **/
    private CJPanelConfiguration pJPanel = null;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /**
     * Service reference managed by iPojo (see metadata.xml)
     **/
    @Requires
    private IPlatformDirsSvc pPlatformDirsSvc;

    /** Configuration service */
    @Requires
    private ISvcConfig pSvcConfig;

    /** the UiAdminPanel returned by the IUiAdminScv **/
    private IUiAdminPanel pUiAdminPanel = null;

    @Requires
    private IUiAdminSvc pUiAdminSvc;

    /**
     * Service reference managed by iPojo (see metadata.xml)
     */
    @Requires(filter = "(thread=main)")
    private Executor pUiExecutor;

    /**
     * log a dump of the config
     */
    private String buildConfigDump() {

        StringBuilder wSB = new StringBuilder();

        CXStringUtils.appendKeyValInBuff(wSB, "Application", pSvcConfig
                .getApplication().getApplicationId());

        pSvcConfig.getApplication().getIsolate(pPlatformDirsSvc.getIsolateId());

        for (String wIsolateId : pSvcConfig.getApplication().getIsolateIds()) {

            CXStringUtils.appendKeyValInBuff(wSB, "\n - IsolateId", wIsolateId);
        }

        String wCurrentIsolateId = pPlatformDirsSvc.getIsolateId();

        CXStringUtils.appendKeyValInBuff(wSB, "\nCurrentIsolateId",
                wCurrentIsolateId);

        for (IBundleDescr wIBundleDescr : pSvcConfig.getApplication()
                .getIsolate(wCurrentIsolateId).getBundles()) {

            CXStringUtils.appendKeyValInBuff(wSB, "\n  - Bundle",
                    wIBundleDescr.getSymbolicName());
            CXStringUtils.appendKeyValInBuff(wSB, "Optional",
                    wIBundleDescr.getOptional());
            CXStringUtils.appendKeyValInBuff(wSB, "Version",
                    wIBundleDescr.getVersion());

            if (wIBundleDescr.hasProperties()) {
                CXStringUtils.appendKeyValInBuff(wSB, "\n    - Properties",
                        CXListUtils.PropertiesToString(
                                wIBundleDescr.getProperties(),
                                "\n                  "));
            }
        }

        return wSB.toString();
    }

    /**
     * 
     */
    private void initContent() {

        Runnable wRunnable = new Runnable() {
            @Override
            public void run() {

                pLogger.logInfo(this, "initContent");

                // put in place the text of the configuration
                pJPanel.setText(buildConfigDump());

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
            pUiAdminPanel = pUiAdminSvc.newUiAdminPanel("Configuration",
                    "Application config.", null, this,
                    EUiAdminPanelLocation.FIRST);

            pJPanel = new CJPanelConfiguration(pUiExecutor, pLogger,
                    pUiAdminPanel.getPanel());

            initContent();

        } catch (Exception e) {
            pLogger.logSevere(this, "validatePojo", e);
        }
    }

}
