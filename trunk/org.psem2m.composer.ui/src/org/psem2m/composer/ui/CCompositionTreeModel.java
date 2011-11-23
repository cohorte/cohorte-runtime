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

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.psem2m.composer.AbstractSnapshot;
import org.psem2m.composer.ComponentSnapshot;
import org.psem2m.composer.CompositionSnapshot;

/**
 * @author ogattaz
 * 
 */
public class CCompositionTreeModel implements TreeModel {

    private AbstractSnapshot pAbstractSnapshot;

    /**
     * @param aCompositionSnapshot
     */
    CCompositionTreeModel(final CompositionSnapshot aCompositionSnapshot) {

        super();
        pAbstractSnapshot = aCompositionSnapshot;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.
     * TreeModelListener)
     */
    @Override
    public void addTreeModelListener(final TreeModelListener aArg0) {

        // TODO Auto-generated method stub

    }

    /**
     * 
     */
    void destroy() {

        pAbstractSnapshot = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    @Override
    public Object getChild(final Object aArg0, final int aArg1) {

        AbstractSnapshot wAbstractSnapshot = (AbstractSnapshot) aArg0;
        return wAbstractSnapshot.getChild(aArg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    @Override
    public int getChildCount(final Object aArg0) {

        AbstractSnapshot wAbstractSnapshot = (AbstractSnapshot) aArg0;
        return wAbstractSnapshot.getChildCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public int getIndexOfChild(final Object aArg0, final Object aArg1) {

        AbstractSnapshot wAbstractSnapshot = (AbstractSnapshot) aArg0;
        return wAbstractSnapshot.getIndexOfChild((AbstractSnapshot) aArg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeModel#getRoot()
     */
    @Override
    public Object getRoot() {

        // TODO Auto-generaeratedthod stub
        return pAbstractSnapshot;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
     */
    @Override
    public boolean isLeaf(final Object aArg0) {

        return aArg0 instanceof ComponentSnapshot;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.
     * TreeModelListener)
     */
    @Override
    public void removeTreeModelListener(final TreeModelListener aArg0) {

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

        // TODO Auto-generated method stub

    }

}
