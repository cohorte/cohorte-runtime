/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz  (isandlaTech) - 20 nov. 2011 - initial API and implementation
 *******************************************************************************/
package org.psem2m.isolates.ui.admin.api;

import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.psem2m.isolates.base.IIsolateLoggerSvc;

/**
 * @author ogattaz
 * 
 */
public abstract class CJPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -7983409430415340261L;
    private IIsolateLoggerSvc pLogger = null;

    /**
     * Explicit constructor
     */
    public CJPanel() {

        super();
    }

    /**
     * Create the panel.
     */
    public CJPanel(final IIsolateLoggerSvc aLogger) {

        super();
        pLogger = aLogger;

    }

    /**
     * 
     */
    public void destroy() {

        pLogger = null;
    }

    /**
     * @param aRunnable
     */
    public void execute(final Runnable aRunnable) {

        // gives the runnable to the UIExecutor
        SwingUtilities.invokeLater(aRunnable);
    }

    /**
     * @return
     */
    public IIsolateLoggerSvc getLogger() {

        return pLogger;
    }

    /**
     * @return
     */
    public boolean hasLogger() {

        return getLogger() != null;
    }

    /**
     * @return
     */
    public abstract JPanel newGUI();

    /**
     * @param aText
     */
    public abstract void setText(final String aText);

    /**
     * Sets the font of the text
     * 
     * @param aFontName
     * @param aSize
     * @return the new Font
     */
    public abstract Font setTextFont(final EUiAdminFont aFont);

}
