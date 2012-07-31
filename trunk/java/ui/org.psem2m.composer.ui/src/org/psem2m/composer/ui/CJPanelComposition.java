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
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.psem2m.composer.model.ComponentBean;
import org.psem2m.composer.model.ComponentsSetBean;
import org.psem2m.composer.model.IModelBean;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;
import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXStringUtils;

/**
 * @author ogattaz
 * 
 */
public class CJPanelComposition extends CJPanelTree {

    /**
     * @see http
     *      ://docs.oracle.com/javase/tutorial/uiswing/components/tree.html#
     *      display
     * @author ogattaz
     * 
     */
    private class CTreeCellRenderer extends DefaultTreeCellRenderer {

        private static final String ICON_PATH = "/org/psem2m/composer/ui/";

        private static final String NAME_ICON_COMPLETE = ICON_PATH
                + "task-complete.png";

        // private static final String NAME_ICON_ERROR = ICON_PATH
        // + "dialog-error.png";
        private static final String NAME_ICON_IMPORTANT = ICON_PATH
                + "important.png";
        // private static final String NAME_ICON_INFO = ICON_PATH + "info.png";
        private static final String NAME_ICON_QUESTION = ICON_PATH
                + "dialog-question.png";
        private static final String NAME_ICON_WARNING = ICON_PATH
                + "dialog-warning.png";
        private static final long serialVersionUID = -2838261694472584039L;

        private final Icon pIconComplete;
        private final Icon pIconInstantiating;
        private final Icon pIconResolved;
        private final Icon pIconWaiting;

        /**
         * 
         */
        public CTreeCellRenderer() {

            pIconComplete = new ImageIcon(
                    CJPanelComposition.class.getResource(NAME_ICON_COMPLETE));
            pIconInstantiating = new ImageIcon(
                    CJPanelComposition.class.getResource(NAME_ICON_QUESTION));
            pIconResolved = new ImageIcon(
                    CJPanelComposition.class.getResource(NAME_ICON_IMPORTANT));
            pIconWaiting = new ImageIcon(
                    CJPanelComposition.class.getResource(NAME_ICON_WARNING));

            setIcon(pIconResolved);
        }

        @Override
        public Component getTreeCellRendererComponent(final JTree tree,
                final Object value, final boolean sel, final boolean expanded,
                final boolean leaf, final int row, final boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel, expanded,
                    leaf, row, hasFocus);

            if (value instanceof String) {
                // Simple string
                setIcon(pIconInstantiating);
                return this;
            }

            if (value instanceof IModelBean) {

                final IModelBean wSnapshot = (IModelBean) value;

                // Show the bean name
                String name = wSnapshot.getName();
                if (name == null) {
                    name = wSnapshot.toString();
                }
                setText(name);

                if (leaf) {
                    // Show component state icon
                    switch (wSnapshot.getState()) {
                    case COMPLETE:
                        setIcon(pIconComplete);
                        break;

                    case INSTANTIATING:
                        setIcon(pIconInstantiating);
                        break;

                    case RESOLVED:
                        setIcon(pIconResolved);
                        break;

                    case WAITING:
                        setIcon(pIconWaiting);

                    default:
                        break;
                    }
                }
            }

            return this;
        }
    }

    private class CTreeSelectionListener implements TreeSelectionListener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.
         * event.TreeSelectionEvent)
         */
        @Override
        public void valueChanged(final TreeSelectionEvent aArg0) {

            final Object lastSelected = pTree.getLastSelectedPathComponent();

            if (!(lastSelected instanceof IModelBean)) {
                // Nothing selected, or root selected
                return;
            }

            String text;
            if (lastSelected instanceof ComponentBean) {
                // Component
                text = makeComponentTextInfo((ComponentBean) lastSelected);

            } else if (lastSelected instanceof ComponentsSetBean) {
                // Components set
                text = makeComponentsSetTextInfo((ComponentsSetBean) lastSelected);

            } else {
                // Unhandled type
                text = String.format("Unhandled bean type: %s", lastSelected
                        .getClass().getName());
            }

            /* React to the node selection. */
            CJPanelComposition.this.setText(text);
        }
    }

    private static final long serialVersionUID = -7912303688959719480L;

    private JTextArea pComponentsSetText;

    private CCompositionTreeModel pCompositionTreeModel = null;

    private JTree pTree;

    /**
	 * 
	 */
    public CJPanelComposition() {

        super();
        newGUI();
    }

    /**
     * @param aLogger
     */
    public CJPanelComposition(final IIsolateLoggerSvc aLogger,
            final JPanel aPanel,
            final CCompositionTreeModel aCCompositionTreeModel) {

        super(aLogger);
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

    /**
     * Retrieves all the selected component sets
     * 
     * @return A list of composets
     */
    public List<ComponentsSetBean> getSelectedComposets() {

        final List<ComponentsSetBean> result = new ArrayList<ComponentsSetBean>();

        final TreePath[] paths = pTree.getSelectionPaths();
        if (paths != null) {

            for (final TreePath path : paths) {
                if (path.getPathCount() > 1) {
                    final Object root = path.getPathComponent(1);
                    if (root instanceof ComponentsSetBean) {
                        result.add((ComponentsSetBean) root);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Prepare the description of a components set bean
     * 
     * @param aComponentBean
     *            The components set to describe
     * @return A string description
     */
    protected String makeComponentsSetTextInfo(
            final ComponentsSetBean aComponentsSetBean) {

        final StringBuilder wSB = new StringBuilder();
        try {
            CXStringUtils.appendFormatStrInBuff(wSB,
                    "componentsSet.name=[%s]\n", aComponentsSetBean.getName());

            CXStringUtils.appendFormatStrInBuff(wSB,
                    "componentsSet.isRoot=[%b]\n", aComponentsSetBean.isRoot());

            CXStringUtils
                    .appendFormatStrInBuff(wSB, "componentsSet.state=[%s]\n",
                            aComponentsSetBean.getState());

        } catch (final Exception e) {
            wSB.append(CXException.eInString(e));
        }
        return wSB.toString();
    }

    /**
     * Prepare the description of a component bean
     * 
     * @param aComponentBean
     *            The component to describe
     * @return A string description
     */
    protected String makeComponentTextInfo(final ComponentBean aComponentBean) {

        final StringBuilder wSB = new StringBuilder();
        try {

            CXStringUtils.appendFormatStrInBuff(wSB, "component.name=[%s]\n",
                    aComponentBean.getName());
            CXStringUtils.appendFormatStrInBuff(wSB, "component.parent=[%s]\n",
                    aComponentBean.getParentName());
            CXStringUtils.appendFormatStrInBuff(wSB,
                    "component.isolate=[%s]\n", aComponentBean.getIsolate());
            CXStringUtils.appendFormatStrInBuff(wSB, "component.state=[%s]\n",
                    aComponentBean.getState());

            for (final Entry<String, String> wEntry : aComponentBean
                    .getFieldsFilters().entrySet()) {

                CXStringUtils.appendFormatStrInBuff(wSB,
                        "Filter   '%15s'=[%s]\n", wEntry.getKey(),
                        wEntry.getValue());
            }

            for (final Entry<String, String> wEntry : aComponentBean
                    .getProperties().entrySet()) {

                CXStringUtils.appendFormatStrInBuff(wSB,
                        "Property '%15s'=[%s]\n", wEntry.getKey(),
                        wEntry.getValue());
            }
            for (final Entry<String, String> wEntry : aComponentBean.getWires()
                    .entrySet()) {

                CXStringUtils.appendFormatStrInBuff(wSB,
                        "Wire     '%15s'=[%s]\n", wEntry.getKey(),
                        wEntry.getValue());
            }

        } catch (final Exception e) {
            wSB.append(CXException.eInString(e));
        }
        return wSB.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.admin.panels.CJPanel#newGUI()
     */
    @Override
    public JPanel newGUI() {

        try {
            setLayout(new BorderLayout(0, 0));

            final JSplitPane mainSplitPane = new JSplitPane();
            mainSplitPane.setResizeWeight(0.5);
            mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            add(mainSplitPane, BorderLayout.CENTER);

            final JScrollPane treeScrollPane = new JScrollPane();
            mainSplitPane.setLeftComponent(treeScrollPane);

            pTree = pCompositionTreeModel != null ? new JTree(
                    pCompositionTreeModel) : new JTree();

            setTreeFont(EUiAdminFont.NORMAL);
            pTree.addTreeSelectionListener(new CTreeSelectionListener());
            pTree.setCellRenderer(new CTreeCellRenderer());
            treeScrollPane.setViewportView(pTree);

            final JPanel panel = new JPanel();
            mainSplitPane.setRightComponent(panel);
            panel.setLayout(new BorderLayout(0, 0));

            final JScrollPane textScrollPane = new JScrollPane();
            panel.add(textScrollPane, BorderLayout.CENTER);

            pComponentsSetText = new JTextArea();
            pComponentsSetText.setText("ComponentsSet...");
            setTextFont(EUiAdminFont.NORMAL);
            textScrollPane.setViewportView(pComponentsSetText);
        } catch (final Exception e) {
            if (hasLogger()) {
                getLogger().logSevere(this, "newGUI", e);
            }
        }
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

    /**
     * Forces an UI update on the tree
     */
    public void updateTree() {

        if (pTree != null) {
            // Expand rows
            for (int i = 0; i < pTree.getRowCount(); i++) {
                pTree.expandRow(i);
            }

            pTree.updateUI();
            updateUI();
        }
    }
}
