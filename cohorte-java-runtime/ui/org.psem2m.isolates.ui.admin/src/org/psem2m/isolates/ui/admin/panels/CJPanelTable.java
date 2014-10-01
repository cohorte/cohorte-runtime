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
