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
import java.util.List;
import java.util.concurrent.Executor;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.ComponentsSetSnapshot;
import org.psem2m.composer.CompositionEvent;
import org.psem2m.composer.EComponentState;
import org.psem2m.composer.IComposer;
import org.psem2m.composer.ICompositionListener;
import org.psem2m.composer.model.ComponentBean;
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

    /**
     * Service reference managed by iPojo (see metadata.xml)
     */
    @Requires(filter = "(thread=main)")
    private Executor pUiExecutor;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.ICompositionListener#componentsSetStateChanged(org
     * .psem2m.composer.model.ComponentsSetBean,
     * org.psem2m.composer.EComponentState)
     */
    @Override
    public void componentsSetStateChanged(
            final ComponentsSetBean aComponentsSetBean,
            final EComponentState aState) {

        updateModel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.ICompositionListener#componentStateChanged(org.psem2m
     * .composer.model.ComponentBean, org.psem2m.composer.EComponentState)
     */
    @Override
    public void componentStateChanged(final ComponentBean aComponentBean,
            final EComponentState aState) {

        updateModel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.ICompositionListener#compositionChanged(org.psem2m
     * .composer.CompositionEvent)
     */
    @Override
    public void compositionChanged(final CompositionEvent aCompositionEvent) {

        updateModel();
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

        // Unregister listener
        pComposer.unregisterCompositionListener(this);

        try {
            if (pCompositionTreeModel != null) {
                pCompositionTreeModel.destroy();
                pCompositionTreeModel = null;
            }
            if (pTreePanel != null) {

                pTreePanel.destroy();
                pTreePanel = null;
            }
            pUiAdminSvc.removeUiAdminPanel(pUiAdminPanel);

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
        pComposer.instantiateComponentsSet(composet);
        JOptionPane.showMessageDialog(pTreePanel,
                "Composition '" + composet.getName() + "' loaded.", "Composer",
                JOptionPane.INFORMATION_MESSAGE);
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

    /**
     * Updates the tree model (changes it)
     */
    protected void updateModel() {

        final List<ComponentsSetSnapshot> compositionSnapshots = pComposer
                .getCompositionSnapshot();

        pCompositionTreeModel.update(compositionSnapshots);
        pTreePanel.updateTree();
        pTreePanel.updateUI();

        System.out.println("Model updated...");
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
            /* The parent panel */
            pUiAdminPanel = pUiAdminSvc.newUiAdminPanel("Composition",
                    "Bundles list and managment.", null, this,
                    EUiAdminPanelLocation.FIRST);
            final JPanel parentPanel = pUiAdminPanel.getPanel();

            /* The tree panel */
            pCompositionTreeModel = new CCompositionTreeModel(
                    pComposer.getCompositionSnapshot());

            pTreePanel = new CJPanelComposition(pUiExecutor, pLogger,
                    parentPanel, pCompositionTreeModel);

            pComposer.registerCompositionListener(this, 0);

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
            pLogger.logSevere(this, "validatePojo", e);
        }
    }
}
