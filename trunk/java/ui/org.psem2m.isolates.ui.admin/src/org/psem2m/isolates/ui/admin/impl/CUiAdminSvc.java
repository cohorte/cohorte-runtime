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
package org.psem2m.isolates.ui.admin.impl;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceController;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.slave.agent.ISvcAgent;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;
import org.psem2m.isolates.ui.admin.api.EUiAdminPanelLocation;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanel;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanelControler;
import org.psem2m.isolates.ui.admin.api.IUiAdminSvc;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
@Component(name = "psem2m-ui-admin-factory", publicFactory = false)
@Instantiate(name = "psem2m-ui-admin")
@Provides(specifications = IUiAdminSvc.class)
public class CUiAdminSvc extends CPojoBase implements IUiAdminSvc,
        IUiAdminPanelControler {

    private CUiAdminPanels pCUiAdminPanels = null;

    /**
     * The window of the UiAdmin service
     */
    private CFrameMain pFrameMain = null;

    /**
     * True if window of the UiAdmin service is available
     */
    @ServiceController
    private boolean pFrameMainAvailable = false;

    /**
     * Service reference managed by iPojo (see metadata.xml)
     * 
     * This service is the logger of the current bundle
     **/
    @Requires
    private IIsolateLoggerSvc pLogger;
    /**
     * Service reference managed by iPojo (see metadata.xml)
     **/
    @Requires
    private IPlatformDirsSvc pPlatformDirsSvc;

    /**
     * Service reference managed by iPojo (see metadata.xml)
     */
    @Requires
    private ISvcAgent pSvcAgent;

    /**
     * Explicit default constructor
     */
    public CUiAdminSvc() {

        super();
    }

    /**
     * do the creation of the main frame of this UISvc
     */
    private void createFrameMainExec() {

        try {
            pLogger.logInfo(this, "initFrame", "Create the frame [%s]",
                    pPlatformDirsSvc.getIsolateId());
            final CFrameMain wFrameMain = new CFrameMain(this);

            pLogger.logInfo(this, "initFrame", "FrameConfig : %s", wFrameMain
                    .getFrameMainConfig().toDescription());

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

        } catch (final Exception e) {
            pLogger.logSevere(this, "init", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.CXObjectBase#destroy()
     */
    @Override
    public void destroy() {

        pLogger.logInfo(this, "destroy", "hasFrame=[%b]", hasFrameMain());

        if (hasFrameMain()) {
            final CFrameMain main = getFrameMain();
            setFrameMain(null);
            main.destroy();
        }

        if (pCUiAdminPanels != null) {
            pCUiAdminPanels.destroy();
            pCUiAdminPanels = null;
        }
    }

    /**
     * @return the reference of the main frame of the UISvc is available
     */
    private synchronized CFrameMain getFrameMain() {

        return pFrameMain;
    }

    /**
     * @return
     */
    private synchronized boolean getFrameMainAvailable() {

        return pFrameMainAvailable;
    }

    /**
     * @return
     */
    IIsolateLoggerSvc getLogger() {

        return pLogger;
    }

    /**
     * @return true if the main frame of the UISvc is available
     */
    private boolean hasFrameMain() {

        return getFrameMainAvailable();
    }

    /**
     * launch the creation of the main frame of this UISvc
     */
    private void initFramMain() {

        // gives the runnable to the UIExecutor
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                CUiAdminSvc.this.createFrameMainExec();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.osgi.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() {

        // logs in the bundle output
        pLogger.logInfo(this, "invalidatePojo", "INVALIDATE", toDescription());

        try {

            destroy();

        } catch (final Exception e) {
            pLogger.logSevere(this, "invalidatePojo", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.ui.admin.api.IUiAdminSvc#newUiAdminPanel(java.lang
     * .String, org.psem2m.isolates.ui.admin.api.IUiAdminPanelControler)
     */
    @Override
    public IUiAdminPanel newUiAdminPanel(final String aName, final String aTip,
            final Icon aIcon, final IUiAdminPanelControler aControler,
            final EUiAdminPanelLocation aLocation) throws Exception {

        if (!hasFrameMain()) {
            throw new Exception(
                    "Unable to create a new UIAdminpanel. The pFrameMain isn't available");
        }

        // This part doesn't use the UI...
        final CUiAdminPanel wCUiAdminPanel = new CUiAdminPanel(this, aName,
                aTip, aIcon, aControler);

        // Run everything that uses the UI in the Swing thread ...
        final Runnable wRunnable = new Runnable() {

            @Override
            public void run() {

                pCUiAdminPanels.add(wCUiAdminPanel);
                getFrameMain().addUiAdminPanel(wCUiAdminPanel, aLocation);
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            // ... we already are in the UI thread
            wRunnable.run();

        } else {
            // ... we are somewhere else: use a blocking call
            SwingUtilities.invokeAndWait(wRunnable);
        }

        return wCUiAdminPanel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.ui.admin.api.IUiAdminSvc#remove(org.psem2m.isolates
     * .ui.admin.api.IUiAdminPanel)
     */
    @Override
    public void removeUiAdminPanel(final IUiAdminPanel aUiAdminPanel) {

        final Runnable wRunnable = new Runnable() {

            @Override
            public void run() {

                getFrameMain().removeUiAdminPanel(aUiAdminPanel);
                pCUiAdminPanels.remove(aUiAdminPanel);
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            wRunnable.run();

        } else {
            try {
                SwingUtilities.invokeAndWait(wRunnable);

            } catch (final Exception ex) {
                pLogger.logSevere(this, "", "Error :", ex);
            }
        }
    }

    /**
     * @param aFrameMain
     *            store the reference of the main frame of the UISvc
     */
    private synchronized void setFrameMain(final CFrameMain aFrameMain) {

        pFrameMain = aFrameMain;
        pFrameMainAvailable = (pFrameMain != null);

        pLogger.logInfo(this, "setFrameMainAvailable",
                "FrameMainAvailable=[%b]", pFrameMainAvailable);
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

        // gives the runnable to the UIExecutor
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                pCUiAdminPanels.setUiAdminFont(aUiAdminFont);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.osgi.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() {

        // logs in the logger of the isolate
        pLogger.logInfo(this, "validatePojo", "VALIDATE", toDescription());

        initFramMain();

        pCUiAdminPanels = new CUiAdminPanels(this);
    }
}
