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

import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.demo.services.ui.viewer.CBundleUiActivator;
import org.psem2m.isolates.demo.services.ui.viewer.IUiSvc;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.slave.agent.ISvcAgent;

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

            if (hasFrameMain()
                    && (event.getType() == ServiceEvent.REGISTERED || event
                            .getType() == ServiceEvent.UNREGISTERING)) {

                updateTableOfService(event.getServiceReference(),
                        event.getType());
            }
        }

    }

    /**
     * The window of the bundle
     */
    private CFrameMain pFrameMain;

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

    /**
     * launch the initialization of the content of the table of services
     */
    private void initTableOfService() {

        Runnable wRunnable = new Runnable() {
            @Override
            public void run() {

                CUiSvc.this.initTableOfServiceExec();
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
     * do the initialization of the content of the table o services
     */
    private void initTableOfServiceExec() {

        if (hasFrameMain()) {

            try {

                List<ServiceReference> wListOfServiceRef = CBundleUiActivator
                        .getInstance().getAllServiceReferences();

                pFrameMain.setServiceTable(wListOfServiceRef);

                pIsolateLoggerSvc.logInfo(this, "initListOfService",
                        "add [%d] services : [%s]", wListOfServiceRef.size(),
                        wListOfServiceRef.toString());

            } catch (Exception e) {
                pIsolateLoggerSvc.logSevere(this, "initListOfService", e);

            }
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
     * Launch the update the table of service with a service reference and a
     * service event (REGISTERED or UNREGISTERING)
     * 
     * @param aServiceReference
     * @param aServiceEvent
     */
    private void updateTableOfService(final ServiceReference aServiceReference,
            final int aServiceEvent) {

        Runnable wRunnable = new Runnable() {
            @Override
            public void run() {

                CUiSvc.this.updateTableOfServiceExec(aServiceReference,
                        aServiceEvent);
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
     * do the update the table of service with a service reference and a service
     * event (REGISTERED or UNREGISTERING)
     * 
     * @param aServiceReference
     * @param aEvent
     */
    private void updateTableOfServiceExec(
            final ServiceReference aServiceReference, final int aEvent) {

        pIsolateLoggerSvc.logInfo(this, "updateListOfServiceExec",
                "%s ServiceReference=[%s]",
                aEvent == ServiceEvent.REGISTERED ? "REGISTERED"
                        : "UNREGISTERING", aServiceReference.toString());

        pFrameMain.setServiceTable(aServiceReference, aEvent);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.osgi.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() {

        // logs in the bundle output
        pIsolateLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",
                toDescription());

        initFramMain();

        initTableOfService();

        // put in place a service listener to setup the list of service
        try {
            CBundleUiActivator.getInstance().getContext()
                    .addServiceListener(new CAllServicesListner(), null);
        } catch (Exception e) {
            pIsolateLoggerSvc.logSevere(this, "initListOfService", e);

        }
    }

}
