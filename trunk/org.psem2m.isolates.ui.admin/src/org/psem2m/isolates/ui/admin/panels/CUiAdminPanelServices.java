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
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
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
@Component(name = "psem2m-ui-admin-panel-services-factory", publicFactory = false)
@Instantiate(name = "psem2m-ui-admin-panel-services")
public class CUiAdminPanelServices extends CPojoBase implements
        IUiAdminPanelControler {

    /**
     * @author ogattaz
     * 
     */
    class CServiceListener implements ServiceListener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework
         * .ServiceEvent)
         */
        @Override
        public void serviceChanged(final ServiceEvent aServiceEvent) {

            pLogger.logInfo(this, "serviceChanged",
                    "ServiceEvent=[%s] service=[%s]",
                    serviceEventToString(aServiceEvent.getType()),
                    aServiceEvent.getServiceReference().toString());

            if (aServiceEvent.getType() == ServiceEvent.REGISTERED) {

                pJPanel.setRow(aServiceEvent.getServiceReference());

            } else if (aServiceEvent.getType() == ServiceEvent.UNREGISTERING) {

                pJPanel.removeRow(aServiceEvent.getServiceReference());
            }
        }

        private String serviceEventToString(final int aType) {

            switch (aType) {
            case ServiceEvent.REGISTERED:
                return String.format("%d REGISTERED", aType);
            case ServiceEvent.UNREGISTERING:
                return String.format("%d UNREGISTERING", aType);
            default:
                return String.format("%d ???", aType);
            }
        }
    }

    /** the JPanel **/
    private CJPanelTableServices pJPanel = null;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    private CServiceListener pServiceListener = null;

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
     * Explicit constructor
     */
    public CUiAdminPanelServices() {

        super();
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
                pJPanel.addRows(CBundleUiActivator.getInstance()
                        .getAllServiceReferences());

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
            if (pServiceListener != null) {
                CBundleUiActivator.getInstance().getContext()
                        .removeServiceListener(pServiceListener);
                pServiceListener = null;
            }
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
            pUiAdminPanel = pUiAdminSvc.newUiAdminPanel("Services",
                    "Services list and managment.", null, this,
                    EUiAdminPanelLocation.FIRST);

            pJPanel = new CJPanelTableServices(pUiExecutor, pLogger,
                    pUiAdminPanel.getPanel());

            initContent();
            pServiceListener = new CServiceListener();
            CBundleUiActivator.getInstance().getContext()
                    .addServiceListener(pServiceListener);

        } catch (Exception e) {
            pLogger.logSevere(this, "validatePojo", e);
        }
    }
}
