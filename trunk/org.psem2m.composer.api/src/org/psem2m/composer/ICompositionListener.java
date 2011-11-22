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
import org.psem2m.composer.model.ComponentsSetBean;

/**
 * Define a composition listener which will be called each time the state of a
 * component or of a componentsSet will change and each time the composition
 * will change (eg. when a componentsSet will start or stop or will be removed).
 * 
 * @author ogattaz
 * 
 */
public interface ICompositionListener {

    /**
     * @param aComponentBean
     *            the instance of ComponentBean target of the change
     * @param aState
     *            the new state
     */
    void componentStateChanged(final ComponentBean aComponentBean,
            final EComponentState aState);

    /**
     * @param aComponentsSetBean
     *            the instance of ComponentsSetBean target of the change
     * @param aState
     *            the new state
     */
    void conponentsSetStateChanged(final ComponentsSetBean aComponentsSetBean,
            final EComponentState aState);

    /**
     * @param aCompositionEvent
     *            the event to be applied on the composition.
     */
    void conpositionChanged(final CompositionEvent aCompositionEvent);

}
