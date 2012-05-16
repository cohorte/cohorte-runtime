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

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.psem2m.composer.AbstractSnapshot;
import org.psem2m.composer.ComponentSnapshot;
import org.psem2m.composer.ComponentsSetSnapshot;

/**
 * @author ogattaz
 * 
 */
public class CCompositionTreeModel implements TreeModel {

	/** The snapshots list */
	private List<AbstractSnapshot> pSnapshots = new ArrayList<AbstractSnapshot>();

	/**
	 * Sets up the tree model
	 * 
	 * @param aCompositionSnapshot
	 *            A composition snapshot list
	 */
	CCompositionTreeModel(
			final List<ComponentsSetSnapshot> aCompositionSnapshots) {

		super();
		update(aCompositionSnapshots);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.
	 * TreeModelListener)
	 */
	@Override
	public void addTreeModelListener(final TreeModelListener aListener) {
	}

	/**
	 * Cleans up the model
	 */
	void destroy() {

		pSnapshots.clear();
		pSnapshots = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	@Override
	public Object getChild(final Object aParent, final int aIndex) {

		if (aParent instanceof String) {
			// Root
			synchronized (pSnapshots) {
				return pSnapshots.get(aIndex);
			}
		}

		final AbstractSnapshot wCompositionSnapshot = (AbstractSnapshot) aParent;
		return wCompositionSnapshot.getChild(aIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	@Override
	public int getChildCount(final Object aParent) {

		if (aParent instanceof String) {
			// Root
			synchronized (pSnapshots) {
				return pSnapshots.size();
			}
		}

		final AbstractSnapshot wCompositionSnapshot = (AbstractSnapshot) aParent;
		return wCompositionSnapshot.getChildCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public int getIndexOfChild(final Object aParent, final Object aChild) {

		if (aParent instanceof String) {
			// Root
			synchronized (pSnapshots) {
				return pSnapshots.indexOf(aChild);
			}
		}

		final AbstractSnapshot wCompositionSnapshot = (AbstractSnapshot) aParent;
		return wCompositionSnapshot.getIndexOfChild((AbstractSnapshot) aChild);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	@Override
	public Object getRoot() {

		return "Compositions";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	@Override
	public boolean isLeaf(final Object aObject) {

		if (aObject instanceof String) {
			synchronized (pSnapshots) {
				return pSnapshots.isEmpty();
			}
		}

		return aObject instanceof ComponentSnapshot;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.
	 * TreeModelListener)
	 */
	@Override
	public void removeTreeModelListener(final TreeModelListener aListener) {
	}

	/**
	 * Updates the tree model
	 * 
	 * @param aCompositionSnapshots
	 *            New snapshots
	 */
	public void update(final List<ComponentsSetSnapshot> aCompositionSnapshots) {

		synchronized (pSnapshots) {
			pSnapshots.clear();
			if (aCompositionSnapshots != null) {
				pSnapshots.addAll(aCompositionSnapshots);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath,
	 * java.lang.Object)
	 */
	@Override
	public void valueForPathChanged(final TreePath aArg0, final Object aArg1) {
	}
}
