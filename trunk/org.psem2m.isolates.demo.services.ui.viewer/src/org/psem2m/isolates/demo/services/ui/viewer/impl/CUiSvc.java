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

import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.demo.services.ui.viewer.IUiSvc;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.slave.agent.ISvcAgent;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CUiSvc extends CPojoBase implements IUiSvc {

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
     * Explicit default constructor
     */
    public CUiSvc() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.CXObjectBase#destroy()
     */
    @Override
    public void destroy() {

        pIsolateLoggerSvc.logInfo(this, "destroy", "hasFrame=[%b]", hasFrame());
        if (hasFrame()) {
            pFrameMain.dispose();
            pFrameMain = null;
        }
    }

    private boolean hasFrame() {

        return pFrameMain != null;
    }

    /**
       *
       */
    private void init() {

        Runnable wRunnable = new Runnable() {
            /*
             * (non-Javadoc)
             * 
             * @see java.lang.Runnable#run()
             */
            @Override
            public void run() {

                initFrame();
            }
        };
        new Thread(wRunnable, "initUiSvc").start();
        // SwingUtilities.invokeLater(wRunnable);

    }

    /**
       *
       */
    private void initFrame() {

        try {
            pIsolateLoggerSvc.logInfo(this, "initFrame",
                    "Create the frame [%s]", pPlatformDirsSvc.getIsolateId());
            pFrameMain = new CFrameMain();
            pFrameMain.setIsolateName(pPlatformDirsSvc.getIsolateId());

            pIsolateLoggerSvc.logInfo(this, "initFrame", "FrameConfig : %s",
                    pFrameMain.getFrameConfig().toDescription());

            pFrameMain.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent we) {

                    pSvcAgent.killIsolate();
                }
            });

            pFrameMain.setVisible(true);
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

        pIsolateLoggerSvc.logInfo(this, "validatePojo",
                "psem2m.demo.ui.viewer.color=",
                System.getProperty("psem2m.demo.ui.viewer.color"));

        init();
    }

}
