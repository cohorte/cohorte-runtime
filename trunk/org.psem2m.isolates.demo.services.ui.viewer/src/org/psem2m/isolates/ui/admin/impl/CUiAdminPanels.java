/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz  (isandlaTech) - 18 nov. 2011 - initial API and implementation
 *******************************************************************************/
package org.psem2m.isolates.ui.admin.impl;

import java.util.ArrayList;
import java.util.List;

import org.psem2m.utilities.CXObjectBase;
import org.psem2m.utilities.IXObjectBase;

/**
 * @author ogattaz
 * 
 */
public class CUiAdminPanels extends CXObjectBase {

    private List<CUiAdminPanel> pUiAdminPanels = new ArrayList<CUiAdminPanel>();

    /**
     * @param aParent
     */
    CUiAdminPanels(final IXObjectBase aParent) {

        super(aParent);
    }

    @Override
    public void destroy() {

        pUiAdminPanels.clear();
    }

}
