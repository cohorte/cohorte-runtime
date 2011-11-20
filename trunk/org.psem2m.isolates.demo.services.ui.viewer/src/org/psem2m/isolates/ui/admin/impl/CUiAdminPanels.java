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

import org.psem2m.isolates.ui.admin.api.EUiAdminFont;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanel;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanelControler;
import org.psem2m.utilities.CXObjectBase;
import org.psem2m.utilities.IXObjectBase;

/**
 * @author ogattaz
 * 
 */
public class CUiAdminPanels extends CXObjectBase implements
        IUiAdminPanelControler {

    private List<CUiAdminPanel> pUiAdminPanels = new ArrayList<CUiAdminPanel>();

    /**
     * @param aParent
     */
    CUiAdminPanels(final IXObjectBase aParent) {

        super(aParent);
    }

    /**
     * @param aUiAdminPanel
     */
    void add(final CUiAdminPanel aUiAdminPanel) {

        pUiAdminPanels.add(aUiAdminPanel);
    }

    @Override
    public void destroy() {

        pUiAdminPanels.clear();
    }

    /**
     * @param aUiAdminPanel
     */
    void remove(final IUiAdminPanel aUiAdminPanel) {

        pUiAdminPanels.remove(aUiAdminPanel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.ui.admin.api.IUiAdminPanelControler#setUiAdminFont
     * (org.psem2m.isolates.ui.admin.api.EUiAdminFont)
     */
    @Override
    public void setUiAdminFont(final EUiAdminFont aUiAdminFont) {

        for (CUiAdminPanel wUiAdminPanel : pUiAdminPanels) {
            wUiAdminPanel.setUiAdminFont(aUiAdminFont);
        }
    }

}
