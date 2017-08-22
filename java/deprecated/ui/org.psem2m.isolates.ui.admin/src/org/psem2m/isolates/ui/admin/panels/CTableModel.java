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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

/**
 * @author ogattaz
 *
 * @param <T>
 */
class CEntityBean<T> {

    private T pEntity;

    /**
     * @param aEntity
     */
    public CEntityBean(final T aEntity) {

        super();
        pEntity = aEntity;
    }

    /**
     * @return
     */
    public T getEntity() {

        return pEntity;
    }

    /**
     * @param entity
     */
    public void setEntity(final T entity) {

        pEntity = entity;
    }
}

/**
 * @author ogattaz
 *
 */
public abstract class CTableModel<T> extends AbstractTableModel {

    private static final long serialVersionUID = -3434843033520198107L;

    private final int pColKeyIdx;

    private final List<String[]> pList = new ArrayList<String[]>();

    private final Map<String, CEntityBean<T>> pMap = new HashMap<String, CEntityBean<T>>();

    private final CJPanelTable<T> pPanel;

    private final String[] pTitles;

    /**
     * @param aNbCol
     * @param aColKeyIdx
     */
    public CTableModel(final CJPanelTable<T> aPanel, final String[] aTitles,
            final int aColKeyIdx) {

        super();
        pPanel = aPanel;
        pTitles = aTitles;
        pColKeyIdx = aColKeyIdx;

        traceDebug("%15s|  ColKeyIdx=[%d], Titles=[%s]", "<init>", pColKeyIdx,
                Arrays.asList(pTitles));
    }

    /**
     * @param aEntity
     * @return
     */
    private int addEntity(final T aEntity) {

        final String[] wData = pPanel.buildRowData(aEntity);
        if (pPanel.acceptRow(aEntity, wData)) {
            pList.add(wData);
            final String wKey = getRowKey(wData);
            pMap.put(wKey, buildEntityBean(aEntity));

            traceDebug("%15s| adds row=[%s]", "addEntity", wKey);

            return pList.size() - 1;
        }
        return -1;
    }

    /**
     * @param aEntity
     */
    public synchronized boolean addRow(final T aEntity) {

        if (aEntity == null) {
            return false;
        }
        traceDebug("%15s| NbExistingRows=[%d], NbRowsToAdd=[%d]", "addRow",
                pList.size(), 1);

        int wFirstRow = -1;
        int wLastRow = -1;

        synchronized (this) {
            wFirstRow = pList.size();
            wLastRow = addEntity(aEntity);
        }
        // Notifies all listeners that rows in the range [firstRow, lastRow],
        // inclusive, have been inserted.
        if (wLastRow > -1) {
            traceDebug(
                    "%15s| fireTableRowsInserted FirstRow=[%d], wLastRow=[%d]",
                    "addRow", wFirstRow, wLastRow);
            fireTableRowsInserted(wFirstRow, wLastRow);
            return true;
        }
        return false;
    }

    /**
     * @param aEntities
     */
    public synchronized boolean addRows(final T[] aEntities) {

        if (aEntities == null || aEntities.length == 0) {
            return false;
        }

        traceDebug("%15s| NbExistingRows=[%d], NbRowsToAdd=[%d]", "addRows",
                pList.size(), aEntities.length);

        int wFirstRow = -1;
        int wLastRow = -1;
        int wNbAdded = 0;
        synchronized (this) {
            wFirstRow = pList.size();
            for (final T wEntity : aEntities) {
                final int wNewRowIdx = addEntity(wEntity);
                if (wNewRowIdx != -1) {
                    wLastRow = wNewRowIdx;
                    wNbAdded++;
                }
            }
        }
        // Notifies all listeners that rows in the range [firstRow, lastRow],
        // inclusive, have been inserted.
        if (wLastRow > -1) {
            traceDebug(
                    "%15s| fireTableRowsInserted FirstRow=[%d], wLastRow=[%d] wNbAdded=[%d]",
                    "addRows", wFirstRow, wLastRow, wNbAdded);
            fireTableRowsInserted(wFirstRow, wLastRow);
            return true;
        }
        return false;
    }

    /**
     * @param aEntity
     * @return
     */
    CEntityBean<T> buildEntityBean(final T aEntity) {

        return new CEntityBean<T>(aEntity);
    }

    /**
     * @param aRowIdx
     * @return
     */
    String buildTextInfos(final int aRowIdx) {

        if (aRowIdx < 0 || aRowIdx > getRowCount() - 1) {
            return String.format("OutBound row index ! RowIdx=[%d] max=[%d]",
                    aRowIdx, getRowCount() - 1);
        }

        return pPanel.buildTextInfos(getEntity(aRowIdx));
    }

    /**
     *
     */
    public synchronized void destroy() {

        pMap.clear();
        pList.clear();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {

        return pTitles.length;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(final int aColIdx) {

        return pTitles[aColIdx];
    }

    /**
     * @param aRowIdx
     * @return
     */
    T getEntity(final int aRowIdx) {

        return getEntityBean(aRowIdx).getEntity();
    }

    /**
     * @param aRowIdx
     * @return
     */
    synchronized CEntityBean<T> getEntityBean(final int aRowIdx) {

        return pMap.get(getRowKey(pList.get(aRowIdx)));
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public synchronized int getRowCount() {

        return pList.size();
    }

    /**
     * @param aRowData
     * @return
     */
    private String getRowKey(final String[] aRowData) {

        return aRowData[pColKeyIdx];
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public synchronized Object getValueAt(final int row, final int col) {

        // traceDebug("%15s| NbExistingRows=[%d] row=[%d] col=[%d]",
        // "getValueAt",
        // pList.size(), row, col);

        if (row > -1 && row < pList.size()) {
            final String[] rowContent = pList.get(row);
            if (rowContent != null && col > -1 && col < rowContent.length) {
                return rowContent[col];
            }
        }

        return null;
    }

    /**
     * @param aKey
     * @return
     */
    synchronized int indexOf(final String aKey) {

        int wRowIdx = -1;
        int wI = 0;
        for (final String[] wRowData : pList) {
            if (wRowData[pColKeyIdx].equals(aKey)) {
                wRowIdx = wI;
                break;
            }
            wI++;
        }
        return wRowIdx;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(final int row, final int col) {

        return false;
    }

    /**
     *
     */
    public synchronized void removeAllRows() {

        traceDebug("%15s| NbExistingRows=[%d]", "removeAllRows", pList.size());
        if (pList.size() > 0) {
            final int wFirstRow = 0;
            final int wLastRow = pList.size() - 1;
            pMap.clear();
            pList.clear();

            traceDebug(
                    "%15s| fireTableRowsDeleted FirstRow=[%d], wLastRow=[%d]",
                    "removeAllRows", wFirstRow, wLastRow);
            fireTableRowsDeleted(wFirstRow, wLastRow);
        }
    }

    /**
     * @param row
     */
    public synchronized void removeRow(final T aEntity) {

        traceDebug("%15s| NbExistingRows=[%d]", "removeRow", pList.size());
        final int wRowIdx;
        synchronized (this) {
            final String wKey = pPanel.buildRowKey(aEntity);
            wRowIdx = indexOf(wKey);
            if (wRowIdx > -1) {
                pMap.remove(wKey);
                pList.remove(wRowIdx);
            }
        }
        if (wRowIdx > -1) {
            traceDebug(
                    "%15s| fireTableRowsDeleted FirstRow=[%d], wLastRow=[%d]",
                    "removeRow", wRowIdx, wRowIdx);
            fireTableRowsDeleted(wRowIdx, wRowIdx);
        }
    }

    /**
     * @param aEntity
     */
    public synchronized void setRow(final T aEntity) {

        traceDebug("%15s| NbExistingRows=[%d]", "setRow", pList.size());

        int wRowIdx = -1;
        synchronized (this) {
            final String[] wData = pPanel.buildRowData(aEntity);
            final String wKey = getRowKey(wData);
            wRowIdx = indexOf(wKey);
            if (wRowIdx > -1) {
                int wI = 0;
                for (final String wCellData : wData) {
                    pList.get(wRowIdx)[wI] = wCellData;
                    wI++;
                }
                pMap.put(wKey, buildEntityBean(aEntity));

                traceDebug("%15s| sets row=[%s]", "setRow", wKey);
            }
        }
        if (wRowIdx > -1) {
            traceDebug(
                    "%15s| fireTableRowsUpdated FirstRow=[%d], wLastRow=[%d]",
                    "setRow", wRowIdx, wRowIdx);
            fireTableRowsUpdated(wRowIdx, wRowIdx);
        } else {
            addRow(aEntity);
        }
    }

    /**
     * @param aEntities
     */
    public synchronized void setRows(final T[] aEntities) {

        traceDebug("%15s| NbExistingRows=[%d]", "setRows", pList.size());

        synchronized (this) {
            removeAllRows();
            addRows(aEntities);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
     * int, int)
     */
    @Override
    public synchronized void setValueAt(final Object value, final int row,
            final int col) {

        final String[] rowContent = pList.get(row);
        if (rowContent != null && col > -1 && col < rowContent.length) {
            pList.get(row)[col] = (String) value;
            fireTableCellUpdated(row, col);
        }
    }

    /**
     * @param aFormat
     * @param aArgs
     */
    private void traceDebug(final String aFormat, final Object... aArgs) {

        // System.out.println(String.format("[%25s] %s", pPanel.getClass()
        // .getSimpleName(), String.format(aFormat, aArgs)));
    }
}
