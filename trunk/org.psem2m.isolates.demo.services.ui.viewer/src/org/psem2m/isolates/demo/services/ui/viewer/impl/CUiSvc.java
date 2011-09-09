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

import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.demo.services.ui.viewer.IUiSvc;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CUiSvc extends CPojoBase implements IUiSvc {
    private CFrameMain pFrameMain;

    /**
     * Service reference managed by iPojo (see metadata.xml)
     * 
     * This service is the logger of the current bundle
     **/
    private IIsolateLoggerSvc pIsolateLoggerSvc;

    /** Service reference managed by iPojo (see metadata.xml) **/
    private IPlatformDirsSvc pPlatformDirsSvc;

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
	    pFrameMain.setLocationRelativeTo(null);
	    pFrameMain.setIsolateName(pPlatformDirsSvc.getIsolateId());
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

	init();
    }

}
