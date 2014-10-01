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

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * @author ogattaz
 *
 */
public class CColumnHeaderTips extends MouseMotionAdapter {

    // Current column whose tooltip is being displayed.
    // This variable is used to minimize the calls to setToolTipText().
    TableColumn curCol;

    // Maps TableColumn objects to tooltips
    Map<TableColumn, String> tips = new HashMap<TableColumn, String>();

    @Override
    public void mouseMoved(final MouseEvent evt) {

        TableColumn col = null;
        final JTableHeader header = (JTableHeader) evt.getSource();
        final JTable table = header.getTable();
        final TableColumnModel colModel = table.getColumnModel();
        final int vColIndex = colModel.getColumnIndexAtX(evt.getX());

        // Return if not clicked on any column header
        if (vColIndex >= 0) {
            col = colModel.getColumn(vColIndex);
        }

        if (col != curCol) {
            header.setToolTipText(tips.get(col));
            curCol = col;
        }
    }

    /**
     * If tooltip is null, removes any tooltip text.
     *
     * @param col
     * @param tooltip
     */
    public void setToolTip(final TableColumn col, final String tooltip) {

        if (tooltip == null) {
            tips.remove(col);
        } else {
            tips.put(col, tooltip);
        }
    }

}
