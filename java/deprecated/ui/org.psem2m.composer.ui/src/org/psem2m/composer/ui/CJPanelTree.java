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
package org.psem2m.composer.ui;

import java.awt.Font;

import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.ui.admin.api.CJPanel;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;

/**
 * @author ogattaz
 * 
 */
public abstract class CJPanelTree extends CJPanel {

    private static final long serialVersionUID = 2090135987172885272L;

    /**
     * Explicit default constructor
     */
    public CJPanelTree() {

        super();
    }

    /**
     * @param aLogger
     */
    public CJPanelTree(final IIsolateLoggerSvc aLogger) {

        super(aLogger);

    }

    /**
     * @param aFont
     * @return
     */
    public abstract Font setTreeFont(final EUiAdminFont aFont);

}
