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

import javax.swing.JTable;

import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.ui.admin.api.CJPanel;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;

/**
 * @author ogattaz
 * 
 */
public abstract class CJPanelTable<T> extends CJPanel {

    private static final long serialVersionUID = 7749683299927125353L;

    /**
     * 
     */
    public CJPanelTable() {

        super();
    }

    /**
     * @param aLogger
     */
    public CJPanelTable(final IIsolateLoggerSvc aLogger) {

        super(aLogger);
    }

    /**
     * Return true if the row built with the entity must be added
     * 
     * @param aEntity
     *            the source entity
     * @param wDataRow
     *            the row to add
     * @return true if the row must be added
     */
    abstract boolean acceptRow(final T aEntity, final String[] wDataRow);

    /**
     * Add a new row built with the passed entity
     * 
     * @param aEntity
     *            the source entity
     * @return
     */
    abstract boolean addRow(final T aEntity);

    /**
     * Add a new set of row built with the passed entities
     * 
     * @param aEntities
     *            the table of source entities
     */
    abstract void addRows(final T[] aEntities);

    /**
     * @param aFontName
     * @param aSize
     */
    void adjustTableRowHeight(final EUiAdminFont aUiAdminFont) {

        final Font wNewFont = setTableFont(aUiAdminFont);
        getTable().setRowHeight(calcRowHeight(wNewFont));
    }

    /**
     * @param aEntity
     * @return
     */
    abstract String[] buildRowData(final T aEntity);

    /**
     * @param aEntity
     * @return
     */
    abstract String buildRowKey(final T aEntity);

    /**
     * @param aEntity
     * @return
     */
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

}
