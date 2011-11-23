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
import java.awt.Font;
import java.util.concurrent.Executor;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.psem2m.composer.AbstractSnapshot;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;

/**
 * @author ogattaz
 * 
 */
public class CJPanelComposition extends CJPanelTree {

    class CTreeSelectionListener implements TreeSelectionListener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.
         * event.TreeSelectionEvent)
         */
        @Override
        public void valueChanged(final TreeSelectionEvent aArg0) {

            /* retrieve the node that was selected */
            AbstractSnapshot wAbstractSnapshot = (AbstractSnapshot) pTree
                    .getLastSelectedPathComponent();

            /* if nothing is selected */
            if (wAbstractSnapshot == null) {
                return;
            }

            /* React to the node selection. */

            CJPanelComposition.this.setText(wAbstractSnapshot.getTextInfo());

        }
    }

    private static final long serialVersionUID = -7912303688959719480L;

    private JTextArea pComponentsSetText;
    private CCompositionTreeModel pCompositionTreeModel;
    private JSplitPane pMainSplitPane;
    private JScrollPane pTextScrollPane;
    private JTree pTree;
    private JScrollPane pTreeScrollPane;

    /**
 * 
 */
    public CJPanelComposition() {

        super();
        pCompositionTreeModel = new CCompositionTreeModel(null);
        newGUI();
    }

    /**
     * @param aUiExecutor
     * @param aLogger
     */
    public CJPanelComposition(final Executor aUiExecutor,
            final IIsolateLoggerSvc aLogger, final JPanel aPanel,
            final CCompositionTreeModel aCCompositionTreeModel) {

        super(aUiExecutor, aLogger);
        pCompositionTreeModel = aCCompositionTreeModel;
        aPanel.setLayout(new BorderLayout(0, 0));
        aPanel.add(newGUI(), BorderLayout.CENTER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.admin.panels.CJPanel#destroy()
     */
    @Override
    public void destroy() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.admin.panels.CJPanel#newGUI()
     */
    @Override
    public JPanel newGUI() {

        setLayout(new BorderLayout(0, 0));

        pMainSplitPane = new JSplitPane();
        pMainSplitPane.setResizeWeight(0.5);
        pMainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        add(pMainSplitPane, BorderLayout.CENTER);

        pTreeScrollPane = new JScrollPane();
        pMainSplitPane.setLeftComponent(pTreeScrollPane);

        pTree = new JTree(pCompositionTreeModel);
        pTree.addTreeSelectionListener(new CTreeSelectionListener());
        pTreeScrollPane.setViewportView(pTree);

        JPanel panel = new JPanel();
        pMainSplitPane.setRightComponent(panel);
        panel.setLayout(new BorderLayout(0, 0));

        pTextScrollPane = new JScrollPane();
        panel.add(pTextScrollPane, BorderLayout.CENTER);

        pComponentsSetText = new JTextArea();
        pComponentsSetText.setText("ComponentsSet...");
        pTextScrollPane.setViewportView(pComponentsSetText);
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.admin.api.CJPanel#setText(java.lang.String)
     */
    @Override
    public void setText(final String aText) {

        pComponentsSetText.setText(aText);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.ui.admin.api.CJPanel#setTextFont(org.psem2m.isolates
     * .ui.admin.api.EUiAdminFont)
     */
    @Override
    public Font setTextFont(final EUiAdminFont aUiAdminFont) {

        pComponentsSetText.setFont(aUiAdminFont.getTextFont());
        return aUiAdminFont.getTextFont();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.ui.CJPanel#setTextFont(org.psem2m.isolates.ui.admin
     * .api.EUiAdminFont)
     */
    @Override
    public Font setTreeFont(final EUiAdminFont aUiAdminFont) {

        pTree.setFont(aUiAdminFont.getTableFont());
        return aUiAdminFont.getTextFont();
    }
}
