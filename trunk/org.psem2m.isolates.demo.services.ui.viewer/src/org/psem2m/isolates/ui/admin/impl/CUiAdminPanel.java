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

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.psem2m.isolates.ui.admin.api.IUiAdminPanel;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanelControler;
import org.psem2m.utilities.CXObjectBase;
import org.psem2m.utilities.IXObjectBase;

/**
 * @author ogattaz
 * 
 */
public class CUiAdminPanel extends CXObjectBase implements IUiAdminPanel {

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
    public IUiAdminPanelControler getConroler() {

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

        return getConroler() != null;
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

}
