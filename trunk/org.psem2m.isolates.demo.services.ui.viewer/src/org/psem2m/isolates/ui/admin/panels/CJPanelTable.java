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
package org.psem2m.isolates.ui.admin.panels;

import java.awt.Font;
import java.util.concurrent.Executor;

import javax.swing.JTable;

import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;

/**
 * @author ogattaz
 * 
 */
public abstract class CJPanelTable<T> extends CJPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 7749683299927125353L;

    protected JTable pTable;

    /**
     * 
     */
    public CJPanelTable() {

        super();
    }

    /**
     * @param aUiExecutor
     * @param aLogger
     */
    public CJPanelTable(final Executor aUiExecutor,
            final IIsolateLoggerSvc aLogger) {

        super(aUiExecutor, aLogger);
    }

    /**
     * @param aEntity
     * @return
     */
    abstract boolean addRow(final T aEntity);

    /**
     * @param aEntity
     */
    abstract void addRows(final T[] aEntity);

    /**
     * @param aFontName
     * @param aSize
     */
    void adjustTableRowHeight(final EUiAdminFont aUiAdminFont) {

        Font wNewFont = setTableFont(aUiAdminFont);
        getTable().setRowHeight(calcRowHeight(wNewFont));
    }

    /**
     * @param aEntity
     * @return
     */
    abstract String[] buildRowData(final T aEntity);

    abstract String buildTextInfos(final T aEntity);

    /**
     * @param aFont
     * @return
     */
    int calcFontHeight(final Font aFont) {

        return getGraphics().getFontMetrics(aFont).getHeight();
    }

    /**
     * @param aFont
     * @return
     */
    int calcRowHeight(final Font aFont) {

        return calcFontHeight(aFont) + 2;
    }

    abstract JTable getTable();

    /**
     * 
     */
    abstract void removeAllRows();

    /**
     * @param aEntity
     */
    abstract void removeRow(final T aEntity);

    /**
     * @param aEntity
     */
    abstract void setRow(final T aEntity);

    /**
     * @param aEntity
     */
    abstract void setRows(final T[] aEntity);

    /**
     * Sets the font of the table
     * 
     * @param aFontName
     * @param aSize
     * @return the new Font
     */
    abstract Font setTableFont(final EUiAdminFont aFont);

    /**
     * @param aRowIdx
     * @param aEntity
     */
    abstract void updateRow(final int aRowIdx, final T aEntity);

}
