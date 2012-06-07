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
package org.psem2m.composer;

import java.util.ArrayList;
import java.util.List;

import org.psem2m.composer.model.ComponentsSetBean;
import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXStringUtils;

/**
 * Represents a ComponentsSet defined in the composition
 * 
 * @author ogattaz
 * 
 */
public class ComponentsSetSnapshot extends AbstractSnapshot {

    final List<ComponentsSetSnapshot> pChidren = new ArrayList<ComponentsSetSnapshot>();

    final List<ComponentSnapshot> pComponents = new ArrayList<ComponentSnapshot>();

    private final ComponentsSetBean pComponentSet;

    /**
     * @param aComponentSet
     */
    public ComponentsSetSnapshot(final ComponentsSetBean aComponentSet) {

        super(EComponentState.RESOLVED);
        pComponentSet = aComponentSet;
    }

    /**
     * @param aComponentSetsSnapshot
     */
    public void addChild(final ComponentsSetSnapshot aComponentSetsSnapshot) {

        pChidren.add(aComponentSetsSnapshot);
    }

    /**
     * @param aComponentSetsSnapshot
     */
    public void addComponent(final ComponentSnapshot aComponentSnapshot) {

        pComponents.add(aComponentSnapshot);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.AbstractSnapshot#getChild(int)
     */
    @Override
    public AbstractSnapshot getChild(final int aIdx) {

        if (aIdx < pChidren.size()) {
            return pChidren.get(aIdx);
        } else {
            return pComponents.get(aIdx - pChidren.size());
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.AbstractSnapshot#getChildCount()
     */
    @Override
    public int getChildCount() {

        return pChidren.size() + pComponents.size();
    }

    /**
     * @return
     */
    public ComponentsSetBean getComponentSet() {

        return pComponentSet;
    }

    /**
     * @return
     */
    public String getComponentsNames() {

        StringBuilder wSB = new StringBuilder();
        for (ComponentSnapshot wComponent : pComponents) {
            CXStringUtils.appendFormatStrInBuff(wSB, " %s",
                    wComponent.getName());
        }
        return wSB.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.AbstractSnapshot#getIndexOfChild(org.psem2m.composer
     * .AbstractSnapshot)
     */
    @Override
    public int getIndexOfChild(final AbstractSnapshot aChild) {

        int wIdx = 0;
        for (ComponentsSetSnapshot wChild : pChidren) {
            if (wChild.equals(aChild)) {
                return wIdx;
            }
            wIdx++;
        }
        for (ComponentSnapshot wComponent : pComponents) {
            if (wComponent.equals(aChild)) {
                return wIdx;
            }
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.AbstractSnapshot#getQName()
     */
    @Override
    public String getQName() {

        return getComponentSet().getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.AbstractSnapshot#getTextInfo()
     */
    @Override
    public String getTextInfo() {

        StringBuilder wSB = new StringBuilder();
        try {

            CXStringUtils.appendFormatStrInBuff(wSB,
                    "componentsSet.name=[%s]\n", getQName());
            CXStringUtils.appendFormatStrInBuff(wSB,
                    "componentsSet.isRoot=[%b]\n", getComponentSet().isRoot());

        } catch (Exception e) {
            wSB.append(CXException.eInString(e));
        }
        return wSB.toString();
    }

    /**
     * @param aComponentSetsSnapshot
     */
    public void removeChild(final ComponentsSetSnapshot aComponentSetsSnapshot) {

        pChidren.remove(aComponentSetsSnapshot);
    }

    /**
     * @param aComponentSetsSnapshot
     */
    public void removeComponent(final ComponentSnapshot aComponentSnapshot) {

        pComponents.remove(aComponentSnapshot);
    }

    @Override
    public String toString() {

        return getName();
    }
}
