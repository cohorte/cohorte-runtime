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

import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXStringUtils;

/**
 * @author ogattaz
 * 
 */
public class CompositionSnapshot extends AbstractSnapshot {

    private final ComponentsSetSnapshot pComponentsSetSnapshot;
    private final long pCreateTime = System.currentTimeMillis();

    /**
     * @param aComponentsSetSnapshot
     */
    public CompositionSnapshot(
            final ComponentsSetSnapshot aComponentsSetSnapshot) {

        super(EComponentState.RESOLVED);
        pComponentsSetSnapshot = aComponentsSetSnapshot;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.AbstractSnapshot#getChild(int)
     */
    @Override
    public ComponentsSetSnapshot getChild(final int aIdx) {

        return getComponentsSetSnapshot();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.AbstractSnapshot#getChildCount()
     */
    @Override
    public int getChildCount() {

        return 1;
    }

    /**
     * @return the pComponentsSetSnapshot
     */
    public final ComponentsSetSnapshot getComponentsSetSnapshot() {

        return pComponentsSetSnapshot;
    }

    /**
     * @return
     */
    public long getCreateTime() {

        return pCreateTime;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.AbstractSnapshot#getIndexOfChild(org.psem2m.composer
     * .AbstractSnapshot, org.psem2m.composer.AbstractSnapshot)
     */
    @Override
    public int getIndexOfChild(final AbstractSnapshot aChild) {

        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.AbstractSnapshot#getQName()
     */
    @Override
    public String getQName() {

        return "composition test";
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
                    "compositiont.name=[%s]\n", getQName());

        } catch (Exception e) {
            wSB.append(CXException.eInString(e));
        }
        return wSB.toString();
    }
}
