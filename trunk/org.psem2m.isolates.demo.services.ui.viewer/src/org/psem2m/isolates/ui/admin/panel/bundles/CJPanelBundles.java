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

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

/**
 * @author ogattaz
 * 
 */
public class CJPanelBundles extends JPanel {

    static int BUNDLE_COLUMN_IDX_ID = 1;
    static int BUNDLE_COLUMN_IDX_NAME = 0;
    static int BUNDLE_COLUMN_IDX_STATE = 2;
    private static final long serialVersionUID = -6506936458249187873L;
    private JTable pBundlesTable;
    private DefaultTableModel pBundlesTableModel;
    private JScrollPane pBundlesTablScrollPane;
    private JScrollPane pBundleTextAreaScrollPane;
    private JTextArea pBundleTtextArea;
    private JSplitPane splitPaneBundles;

    /**
     * 
     */
    public CJPanelBundles() {

        super();
        newGUI();
    }

    /**
     * Create the panel.
     */
    public CJPanelBundles(final JPanel aPanel) {

        super();
        aPanel.setLayout(new BorderLayout(0, 0));
        aPanel.add(newGUI(), BorderLayout.CENTER);
    }

    /**
     * 
     */
    private JPanel newGUI() {

        setLayout(new BorderLayout(0, 0));

        splitPaneBundles = new JSplitPane();
        splitPaneBundles.setOrientation(JSplitPane.VERTICAL_SPLIT);
        add(splitPaneBundles, BorderLayout.CENTER);

        pBundlesTablScrollPane = new JScrollPane();
        splitPaneBundles.setLeftComponent(pBundlesTablScrollPane);

        pBundlesTable = new JTable();
        pBundlesTableModel = new DefaultTableModel(new Object[][] {},
                new String[] { "Bundle name", "Id", "State" }) {
            private static final long serialVersionUID = 1L;
            boolean[] columnEditables = new boolean[] { false, false, false };

            @Override
            public boolean isCellEditable(final int row, final int column) {

                return columnEditables[column];
            }
        };
        pBundlesTable.setModel(pBundlesTableModel);

        pBundlesTable.getColumnModel().getColumn(BUNDLE_COLUMN_IDX_NAME)
                .setPreferredWidth(200);
        pBundlesTable.getColumnModel().getColumn(BUNDLE_COLUMN_IDX_ID)
                .setPreferredWidth(20);
        pBundlesTable.getColumnModel().getColumn(BUNDLE_COLUMN_IDX_STATE)
                .setPreferredWidth(20);

        pBundlesTablScrollPane.setViewportView(pBundlesTable);

        pBundleTextAreaScrollPane = new JScrollPane();
        splitPaneBundles.setRightComponent(pBundleTextAreaScrollPane);

        pBundleTtextArea = new JTextArea();
        pBundleTextAreaScrollPane.add(pBundleTtextArea);

        return this;
    }

}
