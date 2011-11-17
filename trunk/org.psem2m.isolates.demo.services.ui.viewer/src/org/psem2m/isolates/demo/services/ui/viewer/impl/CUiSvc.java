/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz (isandlaTech) - initial API and implementation
 *******************************************************************************/
package org.psem2m.isolates.demo.services.ui.viewer.impl;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.felix.ipojo.architecture.Architecture;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.demo.services.ui.viewer.CBundleUiActivator;
import org.psem2m.isolates.demo.services.ui.viewer.IUiSvc;
import org.psem2m.isolates.services.conf.IBundleDescr;
import org.psem2m.isolates.services.conf.ISvcConfig;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.slave.agent.ISvcAgent;
import org.psem2m.utilities.CXListUtils;
import org.psem2m.utilities.CXStringUtils;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CUiSvc extends CPojoBase implements IUiSvc {

    /**
     * The listener used to track all the service events
     * 
     * @author isandlatech (www.isandlatech.com) - ogattaz
     * 
     */
    class CAllServicesListner implements ServiceListener {

        /**
         * Explicit default constructor
         */
        CAllServicesListner() {

            super();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework
         * .ServiceEvent)
         */
        @Override
        public void serviceChanged(final ServiceEvent event) {

            if (event.getType() == ServiceEvent.REGISTERED
                    || event.getType() == ServiceEvent.UNREGISTERING) {

                pIsolateLoggerSvc.logInfo(this, "serviceChanged",
                        "service=[%s]", event.getServiceReference().toString());

                updateServicsTable(event.getServiceReference(), event.getType());
            }
        }

    }

    /** iPOJO Components architectures */
    private Architecture[] pArchitectures;

    /**
     * The window of the bundle
     */
    private CFrameMain pFrameMain = null;

    /**
     * Service reference managed by iPojo (see metadata.xml)
     * 
     * This service is the logger of the current bundle
     **/
    private IIsolateLoggerSvc pIsolateLoggerSvc;

    /**
     * Service reference managed by iPojo (see metadata.xml)
     **/
    private IPlatformDirsSvc pPlatformDirsSvc;

    /**
     * Service reference managed by iPojo (see metadata.xml)
     */
    private ISvcAgent pSvcAgent;

    /** Configuration service */
    private ISvcConfig pSvcConfig;

    /**
     * Service reference managed by iPojo (see metadata.xml)
     */
    private Executor pUiExecutor;

    /**
     * Explicit default constructor
     */
    public CUiSvc() {

        super();
    }

    /**
     * Update the list of components each time an Architecture appears
     * 
     * @param aArch
     */
    void architectureBind(final Architecture aArch) {

        pIsolateLoggerSvc.logInfo(this, "architectureBind", "component=[%s]",
                aArch.getInstanceDescription().getName());

        updateComponentsTable(aArch, ServiceEvent.REGISTERED);

    }

    /**
     * Update the list of components each time an Architecture desappears
     * 
     * @param aArch
     */
    void architectureUnBind(final Architecture aArch) {

        pIsolateLoggerSvc.logInfo(this, "architectureUnBind", "component=[%s]",
                aArch.getInstanceDescription().getName());

        updateComponentsTable(aArch, ServiceEvent.UNREGISTERING);
    }

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
     * do the creation of the main frame of this UISvc
     */
    private void createFrameMainExec() {

        try {
            pIsolateLoggerSvc.logInfo(this, "initFrame",
                    "Create the frame [%s]", pPlatformDirsSvc.getIsolateId());
            CFrameMain wFrameMain = new CFrameMain();

            pIsolateLoggerSvc.logInfo(this, "initFrame", "FrameConfig : %s",
                    wFrameMain.getFrameConfig().toDescription());

            wFrameMain.setTitle(pPlatformDirsSvc.getIsolateId());

            wFrameMain.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent we) {

                    pSvcAgent.killIsolate();
                }
            });
            wFrameMain.setVisible(true);

            // store the reference of the FrameMain (synchronized)
            setFrameMain(wFrameMain);

        } catch (Exception e) {
            pIsolateLoggerSvc.logSevere(this, "init", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.CXObjectBase#destroy()
     */
    @Override
    public void destroy() {

        pIsolateLoggerSvc.logInfo(this, "destroy", "hasFrame=[%b]",
                hasFrameMain());
        if (hasFrameMain()) {
            getFrameMain().dispose();
            setFrameMain(null);
        }
    }

    /**
     * @return the reference of the main frame of the UISvc is available
     */
    private synchronized CFrameMain getFrameMain() {

        return pFrameMain;
    }

    /**
     * @return true if the main frame of the UISvc is available
     */
    private boolean hasFrameMain() {

        return getFrameMain() != null;
    }

    /**
     * launch the initialization of the content of the table of services and the
     * configuration
     */
    private void initFrameMainContent() {

        Runnable wRunnable = new Runnable() {
            @Override
            public void run() {

                CUiSvc.this.initFrameMainContentExec();
            }
        };
        try {
            // gives the runnable to the UIExecutor
            pUiExecutor.execute(wRunnable);
        } catch (Exception e) {
            pIsolateLoggerSvc.logSevere(this, "init", e);
        }
    }

    /**
     * do the initialization of the content of the table o services and the
     * configuration
     */
    private void initFrameMainContentExec() {

        if (hasFrameMain()) {

            try {

                pFrameMain.setConfigextArea(buildConfigDump());

                List<ServiceReference> wListOfServiceRef = CBundleUiActivator
                        .getInstance().getAllServiceReferences();

                pFrameMain.setServicesTable(wListOfServiceRef);

                pFrameMain.setComponentsTable(pArchitectures);

                pIsolateLoggerSvc.logInfo(this, "initListOfService",
                        "add [%d] services : [%s]", wListOfServiceRef.size(),
                        wListOfServiceRef.toString());

            } catch (Exception e) {
                pIsolateLoggerSvc.logSevere(this, "initListOfService", e);

            }
        }
    }

    /**
     * launch the creation of the main frame of this UISvc
     */
    private void initFramMain() {

        Runnable wRunnable = new Runnable() {
            @Override
            public void run() {

                CUiSvc.this.createFrameMainExec();
            }
        };
        try {
            // gives the runnable to the UIExecutor
            pUiExecutor.execute(wRunnable);
        } catch (Exception e) {
            pIsolateLoggerSvc.logSevere(this, "init", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.osgi.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() {

        // logs in the bundle output
        pIsolateLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
                toDescription());

        destroy();
    }

    /**
     * @param aFrameMain
     *            store the reference of the main frame of the UISvc
     */
    private synchronized void setFrameMain(final CFrameMain aFrameMain) {

        pFrameMain = aFrameMain;
    }

    /**
     * Updates the UI with the new components state
     */
    private void updateComponentsTable(final Architecture aArchitecture,
            final int aComponentEvent) {

        if (hasFrameMain()) {

            Runnable wRunnable = new Runnable() {
                @Override
                public void run() {

                    pIsolateLoggerSvc
                            .logInfo(
                                    this,
                                    "updateComponentsTable",
                                    "%s Component=[%s]",
                                    aComponentEvent == ServiceEvent.REGISTERED ? "REGISTERED"
                                            : "UNREGISTERING", aArchitecture
                                            .getInstanceDescription().getName());

                    pFrameMain
                            .setComponentTable(aArchitecture, aComponentEvent);
                }
            };
            try {
                // gives the runnable to the UIExecutor
                pUiExecutor.execute(wRunnable);
            } catch (Exception e) {
                pIsolateLoggerSvc.logSevere(this, "init", e);
            }
        }
    }

    /**
     * Launch the update the table of service with a service reference and a
     * service event (REGISTERED or UNREGISTERING)
     * 
     * @param aServiceReference
     * @param aServiceEvent
     */
    private void updateServicsTable(final ServiceReference aServiceReference,
            final int aServiceEvent) {

        if (hasFrameMain()) {

            Runnable wRunnable = new Runnable() {
                @Override
                public void run() {

                    pIsolateLoggerSvc
                            .logInfo(
                                    this,
                                    "updateServicsTable",
                                    "%s ServiceReference=[%s]",
                                    aServiceEvent == ServiceEvent.REGISTERED ? "REGISTERED"
                                            : "UNREGISTERING",
                                    aServiceReference.toString());

                    pFrameMain
                            .setServiceTable(aServiceReference, aServiceEvent);
                }
            };
            try {
                // gives the runnable to the UIExecutor
                pUiExecutor.execute(wRunnable);
            } catch (Exception e) {
                pIsolateLoggerSvc.logSevere(this, "init", e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.osgi.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() {

        // logs in the logger of the isolate
        pIsolateLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",
                toDescription());

        initFramMain();

        initFrameMainContent();

        // put in place a service listener to setup the list of service
        try {
            CBundleUiActivator.getInstance().getContext()
                    .addServiceListener(new CAllServicesListner(), null);
        } catch (Exception e) {
            pIsolateLoggerSvc.logSevere(this, "initListOfService", e);

        }

    }
}
