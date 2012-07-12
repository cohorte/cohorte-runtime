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
 * Defines a composition listener which will be called each time the state of a
 * component or of a componentsSet will change and each time the composition
 * will change (e.g. when a componentsSet will start or stop or will be
 * removed).
 * 
 * @author ogattaz
 */
public interface ICompositionListener {

    /**
     * Notifies the listener that a composition has been removed
     * 
     * @param aRootName
     *            Name of the root components set of the removed composition
     */
    void componentsSetRemoved(String aRootName);

    /**
     * Sets the current components set state.
     * 
     * Called when the listener is bound to the composer
     * 
     * @param aSnapshots
     *            Current components set snapshots
     */
    void setCompositionSnapshots(ComponentsSetBean[] aSnapshots);

    /**
     * Updates a components set snapshot
     * 
     * @param aSnapshot
     *            Snapshot of the modified components set
     */
    void updateCompositionSnapshot(ComponentsSetBean aSnapshot);
}
