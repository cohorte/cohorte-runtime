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

    /**
     * @return
     */
    public ComponentBean getComponent() {

        return pComponent;
    }

}
