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

import java.util.Properties;

import javax.swing.SwingUtilities;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.conf.IStartConfiguration;
import org.psem2m.isolates.services.conf.beans.BundleConf;
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

    @Requires
    private IStartConfiguration pSvcConfig;

    /** the UiAdminPanel returned by the IUiAdminScv **/
    private IUiAdminPanel pUiAdminPanel = null;

    @Requires
    private IUiAdminSvc pUiAdminSvc;

    /**
     * log a dump of the config
     */
    private String buildConfigDump() {

        final StringBuilder wSB = new StringBuilder();

        if (pSvcConfig.getApplication() != null) {
            CXStringUtils.appendKeyValInBuff(wSB, "Application", pSvcConfig
                    .getApplication().getApplicationId());
        }

        // List other isolates
        // for (final String wIsolateId : pSvcConfig.getApplication()
        // .getIsolateIds()) {
        //
        // CXStringUtils.appendKeyValInBuff(wSB, "\n - IsolateId", wIsolateId);
        // }

        // Print current isolate ID
        final String wCurrentIsolateId = pPlatformDirsSvc.getIsolateUID();
        CXStringUtils.appendKeyValInBuff(wSB, "\nCurrentIsolateUID",
                wCurrentIsolateId);

        // Print current isolate Name
        final String wCurrentIsolateName = pPlatformDirsSvc.getIsolateName();
        CXStringUtils.appendKeyValInBuff(wSB, "\nCurrentIsolateName",
                wCurrentIsolateName);

        // Print the bundles of the active isolate configuration
        for (final BundleConf wBundle : pSvcConfig.getConfiguration()
                .getBundles()) {

            CXStringUtils.appendKeyValInBuff(wSB, "\n  - Bundle",
                    wBundle.getName());
            CXStringUtils.appendKeyValInBuff(wSB, "Optional",
                    wBundle.isOptional());
            CXStringUtils.appendKeyValInBuff(wSB, "Version",
                    wBundle.getVersion());

            // Convert map to properties
            final Properties props = new Properties();
            props.putAll(wBundle.getProperties());
            CXStringUtils.appendKeyValInBuff(wSB, "\n    - Properties",
                    CXListUtils.PropertiesToString(props,
                            "\n                  "));
        }

        return wSB.toString();
    }

    /**
     * 
     */
    private void initContent() {

        final Runnable wRunnable = new Runnable() {

            @Override
            public void run() {

                pLogger.logInfo(this, "initContent");

                // put in place the text of the configuration
                pJPanel.setText(buildConfigDump());

                pUiAdminPanel.pack();
            }
        };

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
        pLogger.logInfo(this, "validatePojo", "VALIDATE", toDescription());

        try {
            pUiAdminPanel = pUiAdminSvc.newUiAdminPanel("Configuration",
                    "Application config.", null, this,
                    EUiAdminPanelLocation.FIRST);

            pJPanel = new CJPanelConfiguration(pLogger,
                    pUiAdminPanel.getPanel());

            initContent();

        } catch (final Exception e) {
            pLogger.logSevere(this, "validatePojo", e);
        }
    }

}
