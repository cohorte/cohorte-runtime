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
package org.psem2m.isolates.ui.admin.api;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * @author ogattaz
 * 
 */
public interface IUiAdminPanel {

    /**
     * @return
     */
    public IUiAdminPanelControler getConroler();

    /**
     * @return
     */
    public Icon getIcon();

    /**
     * @return
     */
    public String getName();

    /**
     * @return
     */
    public JPanel getPanel();

    /**
     * @return
     */
    public String getTip();

    /**
     * @return
     */
    public boolean hasControler();

    /**
     * @return
     */
    public boolean hasPanel();

    /**
     * 
     */
    public void pack();

}
