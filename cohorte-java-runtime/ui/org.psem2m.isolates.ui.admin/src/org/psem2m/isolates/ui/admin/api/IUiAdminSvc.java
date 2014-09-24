/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz (isandlaTech) - initial API and implementation
 *******************************************************************************/
package org.psem2m.isolates.ui.admin.api;

import javax.swing.Icon;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public interface IUiAdminSvc {

    /**
     * @param aName
     *            the name of the new UiAdminPanel
     * @param aTip
     *            the tip of the new UiAdminPanel. null is accepted.
     * @param aControler
     * @return the instance of the added UiAdminPanel
     */
    IUiAdminPanel newUiAdminPanel(String aName, String aTip, Icon icon,
            IUiAdminPanelControler aControler, EUiAdminPanelLocation aLocation)
            throws Exception;

    /**
     * @param aUiAdminPanel
     */
    void removeUiAdminPanel(IUiAdminPanel aUiAdminPanel);
}
