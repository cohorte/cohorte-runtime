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

/**
 * @author ogattaz
 * 
 */
public abstract class AbstractSnapshot {

    /** the state of the snapshot */
    private EComponentState pState = EComponentState.RESOLVED;

    /**
     * @param aState
     */
    public AbstractSnapshot(final EComponentState aState) {

        super();
        pState = aState;
    }

    /**
     * @return
     */
    public abstract AbstractSnapshot getChild(final int aIdx);

    /**
     * @return
     */
    public abstract int getChildCount();

    /**
     * @param aParent
     * @param aChaild
     * @return
     */
    public abstract int getIndexOfChild(final AbstractSnapshot aChild);

    public String getName() {

        String wQName = getQName();
        int wPos = wQName.lastIndexOf('.');
        if (wPos > -1) {
            wQName = wQName.substring(wPos);
        }
        return wQName;
    }

    public abstract String getQName();

    /**
     * @return
     */
    public EComponentState getState() {

        return pState;
    }

    public abstract String getTextInfo();

    /**
     * @param pState
     */
    public void setState(final EComponentState aState) {

        this.pState = aState;
    }

    @Override
    public String toString() {

        return getName();
    }
}
