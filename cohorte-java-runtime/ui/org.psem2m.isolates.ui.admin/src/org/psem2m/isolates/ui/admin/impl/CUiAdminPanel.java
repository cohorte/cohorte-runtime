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

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.psem2m.isolates.ui.admin.api.EUiAdminFont;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanel;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanelControler;
import org.psem2m.utilities.CXObjectBase;
import org.psem2m.utilities.IXObjectBase;

/**
 * @author ogattaz
 *
 */
public class CUiAdminPanel extends CXObjectBase implements IUiAdminPanel,
        IUiAdminPanelControler {

    private final IUiAdminPanelControler pControler;

    private JFrame pFrame;

    private final Icon pIcon;

    private final String pName;

    private JPanel pPanel = null;

    private final String pTip;

    /**
     * @param aParent
     */
    CUiAdminPanel(final IXObjectBase aParent, final String aName,
            final String aTip, final Icon aIcon,
            final IUiAdminPanelControler aControler) {

        super(aParent);
        pName = aName;
        pTip = aTip;
        pIcon = aIcon;
        pControler = aControler;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.ui.admin.api.IUiAdminPanel#getConroler()
     */
    @Override
    public IUiAdminPanelControler getControler() {

        return pControler;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.ui.admin.api.IUiAdminPanel#getIcon()
     */
    @Override
    public Icon getIcon() {

        return pIcon;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.ui.admin.api.IUiAdminPanel#getName()
     */
    @Override
    public String getName() {

        return pName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.ui.admin.api.IUiAdminPanel#getPanel()
     */
    @Override
    public JPanel getPanel() {

        return pPanel;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.ui.admin.api.IUiAdminPanel#getTip()
     */
    @Override
    public String getTip() {

        return pTip;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.ui.admin.api.IUiAdminPanel#hasControler()
     */
    @Override
    public boolean hasControler() {

        return getControler() != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.ui.admin.api.IUiAdminPanel#hasPanel()
     */
    @Override
    public boolean hasPanel() {

        return getPanel() != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.ui.admin.api.IUiAdminPanel#pack()
     */
    @Override
    public void pack() {

        pFrame.pack();
    }

    /**
     * @param aPanel
     */
    void setFrameAndPanel(final JFrame aFrame, final JPanel aPanel) {

        pPanel = aPanel;
        pFrame = aFrame;
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

        getControler().setUiAdminFont(aUiAdminFont);
    }

}
