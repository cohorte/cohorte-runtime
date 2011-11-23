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

import org.psem2m.composer.model.ComponentBean;
import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXStringUtils;

/**
 * Represents a Component defined in the composition
 * 
 * @author ogattaz
 * 
 */
public class ComponentSnapshot extends AbstractSnapshot {

    private final ComponentBean pComponent;

    /**
     * @param aComponent
     */
    public ComponentSnapshot(final ComponentBean aComponent) {

        super(EComponentState.RESOLVED);
        pComponent = aComponent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.AbstractSnapshot#getChild(int)
     */
    @Override
    public AbstractSnapshot getChild(final int aIdx) {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.AbstractSnapshot#getChildCount()
     */
    @Override
    public int getChildCount() {

        return 0;
    }

    /**
     * @return
     */
    public ComponentBean getComponent() {

        return pComponent;
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

        return getComponent().getName();
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

            CXStringUtils.appendFormatStrInBuff(wSB, "component.name=[%s]\n",
                    getQName());
            CXStringUtils.appendFormatStrInBuff(wSB,
                    "component.isolate=[%s]\n", getComponent().getIsolate());

        } catch (Exception e) {
            wSB.append(CXException.eInString(e));
        }
        return wSB.toString();
    }

}
