/**
 * Copyright 2014 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    private final List<CUiAdminPanel> pUiAdminPanels = new ArrayList<CUiAdminPanel>();

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

        for (final CUiAdminPanel wUiAdminPanel : pUiAdminPanels) {
            wUiAdminPanel.setUiAdminFont(aUiAdminFont);
        }
    }

}
