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

    /**
     * @return
     */
    public ComponentsSetBean getComponentSet() {

        return pComponentSet;
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
}
