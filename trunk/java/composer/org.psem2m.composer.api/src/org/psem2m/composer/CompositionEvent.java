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

import org.psem2m.composer.model.ComponentsSetBean;

/**
 * @author ogattaz
 * 
 */
public class CompositionEvent {

    private final ComponentsSetBean pComponentsSetBean;

    private final ComponentsSetBean pComponentsSetBeanParent;

    private final EComponentState pCompositionEvent;

    /**
     * @param aECompositionEvent
     * @param aComponentsSetSnapshot
     */
    public CompositionEvent(final EComponentState aECompositionEvent,
            final ComponentsSetBean aComponentsSetBean) {

        this(aECompositionEvent, aComponentsSetBean, null);
    }

    /**
     * @param aECompositionEvent
     * @param aComponentsSetSnapshot
     * @param aComponentsSetSnapshotParent
     */
    public CompositionEvent(final EComponentState aECompositionEvent,
            final ComponentsSetBean aComponentsSetBean,
            final ComponentsSetBean aComponentsSetBeantParent) {

        super();
        pCompositionEvent = aECompositionEvent;
        pComponentsSetBean = aComponentsSetBean;
        pComponentsSetBeanParent = aComponentsSetBeantParent;
    }

    /**
     * @return
     */
    public ComponentsSetBean getComponentsSetBean() {

        return pComponentsSetBean;
    }

    /**
     * @return
     */
    public ComponentsSetBean getComponentsSetBeanParent() {

        return pComponentsSetBeanParent;
    }

    /**
     * @return
     */
    public EComponentState getCompositionEvent() {

        return pCompositionEvent;
    }

    /**
     * @return
     */
    public boolean hasComponentsSetBeanParent() {

        return getComponentsSetBeanParent() != null;
    }
}
