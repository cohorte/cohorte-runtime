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
package org.psem2m.isolates.ui.admin.panel.bundles;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
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

    private CJPanelBundles pJPanelBundles = null;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    private IUiAdminPanel pUiAdminPanel = null;

    // @Requires(filter = "(FrameMainAvailable=true)")
    @Requires
    IUiAdminSvc pUiAdminSvc;

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

            pJPanelBundles = null;
        } catch (Exception e) {
            pLogger.logSevere(this, "invalidatePojo", e);
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
                    "Bundles list and managment.", null, 0, this);

            pJPanelBundles = new CJPanelBundles(pUiAdminPanel.getPanel());

            pUiAdminPanel.pack();
        } catch (Exception e) {
            pLogger.logSevere(this, "validatePojo", e);
        }
    }
}
