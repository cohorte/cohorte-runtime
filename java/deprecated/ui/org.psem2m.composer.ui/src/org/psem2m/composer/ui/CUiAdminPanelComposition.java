/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz  (isandlaTech) - 22 nov. 2011 - initial API and implementation
 *******************************************************************************/
package org.psem2m.composer.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.IComposer;
import org.psem2m.composer.ICompositionListener;
import org.psem2m.composer.model.ComponentsSetBean;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.constants.IPlatformProperties;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;
import org.psem2m.isolates.ui.admin.api.EUiAdminPanelLocation;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanel;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanelControler;
import org.psem2m.isolates.ui.admin.api.IUiAdminSvc;

/**
 * @author ogattaz
 * @author Thomas Calmant
 */
@Component(name = "psem2m-composer-ui-admin-factory", publicFactory = false)
@Instantiate(name = "psem2m-composer-ui-admin")
@Provides(specifications = ICompositionListener.class, properties = @StaticServiceProperty(name = "service.exported.interfaces", value = "*", type = "String"))
public class CUiAdminPanelComposition extends CPojoBase implements
        IUiAdminPanelControler, ICompositionListener {

    /** The Composer service */
    @Requires
    private IComposer pComposer;

    /** Composition tree (UI) */
    private CCompositionTreeModel pCompositionTreeModel = null;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** the JPanel **/
    private CJPanelComposition pTreePanel = null;

    /** the UiAdminPanel returned by the IUiAdminScv */
    private IUiAdminPanel pUiAdminPanel = null;

    /** the IUiAdminScv */
    @Requires
    private IUiAdminSvc pUiAdminSvc;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.ICompositionListener#componentsSetRemoved(java.lang
     * .String)
     */
    @Override
    public void componentsSetRemoved(final String aRootName) {

        pCompositionTreeModel.removeSnapshot(aRootName);
        updateTree();
    }

    /**
     * Destroys the panel content
     */
    private void destroyContent() {

        pUiAdminSvc.removeUiAdminPanel(pUiAdminPanel);

        if (pCompositionTreeModel != null) {
            pCompositionTreeModel.destroy();
            pCompositionTreeModel = null;
        }

        if (pTreePanel != null) {
            pTreePanel.destroy();
            pTreePanel = null;
        }
    }

    /**
     * Sets up the panel content
     */
    private void initContent() {

        final Runnable wRunnable = new Runnable() {

            @Override
            public void run() {

                try {
                    /* The parent panel */
                    pUiAdminPanel = pUiAdminSvc.newUiAdminPanel("Composition",
                            "Bundles list and managment.", null,
                            CUiAdminPanelComposition.this,
                            EUiAdminPanelLocation.FIRST);
                    final JPanel parentPanel = pUiAdminPanel.getPanel();

                    /* The tree panel */
                    pCompositionTreeModel = new CCompositionTreeModel();

                    pTreePanel = new CJPanelComposition(pLogger, parentPanel,
                            pCompositionTreeModel);

                    /* The button panel */
                    final JPanel btnPanel = new JPanel();
                    parentPanel.add(btnPanel, BorderLayout.SOUTH);

                    final JButton btnAdd = new JButton("Load");
                    btnPanel.add(btnAdd);
                    btnAdd.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(final ActionEvent aEvent) {

                            loadComposition();
                        }
                    });

                    final JButton btnRemove = new JButton("Remove");
                    btnPanel.add(btnRemove);
                    btnRemove.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(final ActionEvent aEvent) {

                            removeComposition();
                        }
                    });

                } catch (final Exception e) {
                    pLogger.logSevere(this, "initContent", e);
                }
            }
        };

        safeInvokeAndWait(wRunnable);
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
            // remove composer panel
            destroyContent();

        } catch (final Exception e) {
            pLogger.logSevere(this, "invalidatePojo", e);
        }
    }

    /**
     * Pops up a file selection dialog and call the composer to load it
     */
    protected void loadComposition() {

        // Open a file chooser
        final JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(System
                .getProperty(IPlatformProperties.PROP_PLATFORM_BASE)));

        final int returnVal = chooser.showOpenDialog(pTreePanel);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            // Nothing to do
            return;
        }

        // Load the selected file
        final String filename = chooser.getSelectedFile().getAbsolutePath();
        final ComponentsSetBean composet = pComposer
                .loadCompositionFile(filename);

        if (composet == null) {
            JOptionPane.showMessageDialog(pTreePanel,
                    "Error loading composition file :\n" + filename,
                    "Composer", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Instantiate the composet
        try {
            pComposer.instantiateComponentsSet(composet);

            JOptionPane.showMessageDialog(pTreePanel, MessageFormat.format(
                    "Composition ''{0}'' loaded.", composet.getName()),
                    "Composer", JOptionPane.INFORMATION_MESSAGE);

        } catch (final Exception ex) {

            pLogger.logWarn(this, "loadComposition",
                    "Error instantiating composet=", composet.getName(), ex);

            JOptionPane.showMessageDialog(pTreePanel, MessageFormat.format(
                    "Error loading ''{0}'':\n{1}", composet.getName(),
                    ex.getMessage()), "Composer",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Removes the currently selected composition
     */
    protected void removeComposition() {

        // Retrieve the selected composition
        final List<ComponentsSetBean> composets = pTreePanel
                .getSelectedComposets();

        // Tell the composer to stop it
        for (final ComponentsSetBean composet : composets) {
            try {
                pComposer.removeComponentsSet(composet);

            } catch (final Exception ex) {
                JOptionPane.showMessageDialog(pTreePanel,
                        "Error removing composition '" + composet.getName()
                                + "':\n" + ex, "Composer",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Calls {@link SwingUtilities#invokeAndWait(Runnable)} or runs the
     * arguments immediately depending of the current thread
     * 
     * @param aRunnable
     *            A runnable
     */
    private void safeInvokeAndWait(final Runnable aRunnable) {

        if (SwingUtilities.isEventDispatchThread()) {
            try {
                // We already are in the Swing thread
                aRunnable.run();

            } catch (final Exception ex) {
                // Just log
                pLogger.logSevere(this, "initContent",
                        "Error during content initialization", ex);
            }

        } else {
            try {
                // Call the Swing thread
                SwingUtilities.invokeAndWait(aRunnable);

            } catch (final InterruptedException ex) {
                // Ignore...

            } catch (final InvocationTargetException ex) {
                // Just log...
                pLogger.logSevere(this, "initContent",
                        "Error during content initialization", ex.getCause());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.ICompositionListener#setCompositionSnapshots(org.
     * psem2m.composer.ComponentsSetSnapshot[])
     */
    @Override
    public void setCompositionSnapshots(final ComponentsSetBean[] aSnapshots) {

        pCompositionTreeModel.setSnapshots(aSnapshots);
        updateTree();
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

        pTreePanel.setTextFont(aUiAdminFont);
        pTreePanel.setTreeFont(aUiAdminFont);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.ICompositionListener#updateCompositionSnapshot(org
     * .psem2m.composer.ComponentsSetSnapshot)
     */
    @Override
    public void updateCompositionSnapshot(final ComponentsSetBean aSnapshot) {

        pCompositionTreeModel.updateSnapshot(aSnapshot);
        updateTree();
    }

    /**
     * Updates the tree panel in the UI thread
     */
    private void updateTree() {

        safeInvokeAndWait(new Runnable() {

            @Override
            public void run() {

                if (pTreePanel != null) {
                    pTreePanel.updateTree();
                }
            }
        });
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
            // Set up GUI in a thread
            initContent();

        } catch (final Exception e) {
            pLogger.logSevere(this, "validatePojo", e);
        }
    }
}
