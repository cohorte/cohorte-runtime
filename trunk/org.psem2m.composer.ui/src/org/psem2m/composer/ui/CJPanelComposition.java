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
import java.util.concurrent.Executor;

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

import org.psem2m.composer.AbstractSnapshot;
import org.psem2m.composer.ComponentsSetSnapshot;
import org.psem2m.composer.model.ComponentsSetBean;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;

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
	class CTreeCellRenderer extends DefaultTreeCellRenderer {

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
		private final Icon pIconInstanciating;
		private final Icon pIconResolved;
		private final Icon pIconWaiting;

		/**
         * 
         */
		public CTreeCellRenderer() {

			pIconComplete = new ImageIcon(
					CJPanelComposition.class.getResource(NAME_ICON_COMPLETE));
			pIconInstanciating = new ImageIcon(
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
				setIcon(pIconInstanciating);
				return this;
			}

			final AbstractSnapshot wAbstractSnapshot = (AbstractSnapshot) value;

			if (leaf) {
				if (wAbstractSnapshot.getState().isComplete()) {
					setIcon(pIconComplete);
				} else if (wAbstractSnapshot.getState().isInstanciating()) {
					setIcon(pIconInstanciating);
				} else if (wAbstractSnapshot.getState().isResolved()) {
					setIcon(pIconResolved);
				} else if (wAbstractSnapshot.getState().isWaiting()) {
					setIcon(pIconWaiting);
				}
			}
			return this;
		}

	}

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

			final Object lastSelected = pTree.getLastSelectedPathComponent();

			if (!(lastSelected instanceof ComponentsSetSnapshot)) {
				// Nothing selected, or root selected
				return;
			}

			/* retrieve the node that was selected */
			final AbstractSnapshot wAbstractSnapshot = (AbstractSnapshot) lastSelected;

			/* React to the node selection. */
			CJPanelComposition.this.setText(wAbstractSnapshot.getTextInfo());

		}
	}

	private static final long serialVersionUID = -7912303688959719480L;

	private JTextArea pComponentsSetText;
	private CCompositionTreeModel pCompositionTreeModel = null;
	private JSplitPane pMainSplitPane;
	private JScrollPane pTextScrollPane;
	private JTree pTree;
	private JScrollPane pTreeScrollPane;

	/**
	 * 
	 */
	public CJPanelComposition() {

		super();
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

					if (root instanceof ComponentsSetSnapshot) {
						final ComponentsSetSnapshot snapshot = (ComponentsSetSnapshot) root;
						result.add(snapshot.getComponentSet());
					}
				}
			}
		}

		return result;
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

			pMainSplitPane = new JSplitPane();
			pMainSplitPane.setResizeWeight(0.5);
			pMainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			add(pMainSplitPane, BorderLayout.CENTER);

			pTreeScrollPane = new JScrollPane();
			pMainSplitPane.setLeftComponent(pTreeScrollPane);

			pTree = pCompositionTreeModel != null ? new JTree(
					pCompositionTreeModel) : new JTree();

			setTreeFont(EUiAdminFont.NORMAL);
			pTree.addTreeSelectionListener(new CTreeSelectionListener());
			pTree.setCellRenderer(new CTreeCellRenderer());
			pTreeScrollPane.setViewportView(pTree);

			final JPanel panel = new JPanel();
			pMainSplitPane.setRightComponent(panel);
			panel.setLayout(new BorderLayout(0, 0));

			pTextScrollPane = new JScrollPane();
			panel.add(pTextScrollPane, BorderLayout.CENTER);

			pComponentsSetText = new JTextArea();
			pComponentsSetText.setText("ComponentsSet...");
			setTextFont(EUiAdminFont.NORMAL);
			pTextScrollPane.setViewportView(pComponentsSetText);
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
			pTree.updateUI();
		}
	}
}
