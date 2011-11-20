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
package org.psem2m.isolates.ui.admin.panels;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.InstanceStateListener;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;
import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXStringUtils;

/**
 * @author ogattaz
 * 
 */
public class CJPanelTableComponents extends CJPanelTable<Architecture> {

    class CComponentBean {

        private Architecture pArchitecture;
        private ComponentInstance pComponentInstance;
        private InstanceStateListener pInstanceStateListener;

        /**
         * @return the pArchitecture
         */
        public final Architecture getArchitecture() {

            return pArchitecture;
        }

        /**
         * @return the pComponentInstance
         */
        public final ComponentInstance getComponentInstance() {

            return pComponentInstance;
        }

        /**
         * @return the pInstanceStateListener
         */
        public final InstanceStateListener getInstanceStateListener() {

            return pInstanceStateListener;
        }

        /**
         * @param pArchitecture
         *            the pArchitecture to set
         */
        public final void setArchitecture(final Architecture aArchitecture) {

            this.pArchitecture = aArchitecture;
        }

        /**
         * @param pComponentInstance
         *            the pComponentInstance to set
         */
        public final void setComponentInstance(
                final ComponentInstance aComponentInstance) {

            this.pComponentInstance = aComponentInstance;
        }

        /**
         * @param pInstanceStateListener
         *            the pInstanceStateListener to set
         */
        public final void setInstanceStateListener(
                final InstanceStateListener aInstanceStateListener) {

            this.pInstanceStateListener = aInstanceStateListener;
        }

    }

    /**
     * @author ogattaz
     * 
     */
    class CInstanceStateListener implements InstanceStateListener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.apache.felix.ipojo.InstanceStateListener#stateChanged(org.apache
         * .felix.ipojo.ComponentInstance, int)
         */
        @Override
        public void stateChanged(final ComponentInstance arg0, final int arg1) {

            final String wName = arg0.getInstanceDescription().getName();
            final int wRowIdx = findComponentRow(wName);
            final String wStrState = buildComponentState(arg0
                    .getInstanceDescription());

            if (hasLogger()) {
                getLogger().logInfo(this, "stateChanged",
                        "Name=[%s] RowIdx=[%d] NewState=[%s]", wName, wRowIdx,
                        wStrState);
            }
            if (wRowIdx > -1) {
                pComponentsTableModel.setValueAt(wStrState, wRowIdx,
                        COLUMN_IDX_STATE);

                try {
                    // if sorted
                    final int wSelectedRowIdx = pComponentsTable
                            .convertRowIndexToModel(pComponentsTable
                                    .getSelectionModel()
                                    .getLeadSelectionIndex());

                    if (wRowIdx == wSelectedRowIdx) {
                        execute(new Runnable() {
                            @Override
                            public void run() {

                                // set the text info of the service
                                setText(buildTextInfos(wSelectedRowIdx));
                            }
                        });
                    }
                } catch (Exception e) {
                    if (hasLogger()) {
                        getLogger().logInfo(this, "stateChanged", e);
                    }
                }
            }

        }
    }

    /**
     * 
     * By default a left mouse click on a JTable row will select that row, but
     * it’s not the same for the right mouse button. It takes bit more work to
     * get a JTable to select a row based on right mouse clicks. You might be
     * able to do it by subclassing JTable or ListSelectionModel, but there’s an
     * easier way.
     * 
     * 1. Use a MouseAdapter to detect right clicks on the JTable.
     * 
     * 2. Get the Point (X-Y coordinate) of the click.
     * 
     * 3. Find out what row contains that Point
     * 
     * 4. Get the ListSelectionModel of the JTable and 5. Tell the
     * ListSelectionModel to select that row. Here’s what it looks like:
     * 
     * 
     * @see http://www.stupidjavatricks.com/?p=12
     * @author ogattaz
     * 
     */
    class CMouseListener extends MouseAdapter {

        private JPopupMenu createPopUp(final int aRowIndex) {

            final String wName = String.valueOf(pComponentsTableModel
                    .getValueAt(aRowIndex, COLUMN_IDX_NAME));

            final Architecture wArchitecture = pComponentsMap.get(wName)
                    .getArchitecture();
            final int wState = wArchitecture.getInstanceDescription()
                    .getState();

            final boolean wMustValidate = wState != ComponentInstance.VALID;

            final String wAction = wMustValidate ? "Validate" : "Invalidate";

            String wItemLib1 = String.format("%s %s", wAction, wName);

            JMenuItem wMenuItem1 = new JMenuItem(wItemLib1);
            wMenuItem1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent actionEvent) {

                    logAction(actionEvent, aRowIndex);

                    if (wMustValidate) {
                        doValidateComponent(aRowIndex, wName, wArchitecture);
                    } else {
                        doInvalidateComponent(aRowIndex, wName, wArchitecture);
                    }
                }
            });

            String wItemLib2 = String.format("Stop %s", wName);

            JMenuItem wMenuItem2 = new JMenuItem(wItemLib2);
            wMenuItem2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent actionEvent) {

                    logAction(actionEvent, aRowIndex);

                    doStopComponent(aRowIndex, wName, wArchitecture);
                }
            });

            JPopupMenu wJPopupMenu = new JPopupMenu();
            wJPopupMenu.add(wMenuItem1);
            wJPopupMenu.addSeparator();
            wJPopupMenu.add(wMenuItem2);

            return wJPopupMenu;
        }

        /**
         * @param actionEvent
         * @param aRowIndex
         */
        private void logAction(final ActionEvent actionEvent,
                final int aRowIndex) {

            if (hasLogger()) {
                getLogger().logInfo(this, "actionPerformed",
                        "rowIdx=[%d] action=[%s]", aRowIndex,
                        actionEvent.getActionCommand());
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseClicked(final MouseEvent e) {

            // Left mouse click
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (hasLogger()) {
                    getLogger().logInfo(this, "mouseClicked",
                            "isLeftMouseButton");
                }
            }
            // Right mouse click
            else if (SwingUtilities.isRightMouseButton(e)) {
                if (hasLogger()) {
                    getLogger().logInfo(this, "mouseClicked",
                            "isRightMouseButton");
                }
                rightClik(e);
            }
        }

        public void rightClik(final MouseEvent e) {

            int r = pComponentsTable.rowAtPoint(e.getPoint());
            if (r >= 0 && r < pComponentsTable.getRowCount()) {
                pComponentsTable.setRowSelectionInterval(r, r);
            } else {
                pComponentsTable.clearSelection();
            }

            int wRowIdx = pComponentsTable.getSelectedRow();
            if (wRowIdx < 0) {
                return;
            }

            // if sorted
            wRowIdx = pComponentsTable.convertRowIndexToModel(wRowIdx);

            createPopUp(wRowIdx).show(e.getComponent(), e.getX(), e.getY());
        }
    }

    /**
     * Look at TableSelectionDemo.java from java tutorial to learn how work the
     * JTable selection model
     * 
     * @author ogattaz
     * 
     */
    class CSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(final ListSelectionEvent aListSelectionEvent) {

            int wRowIdx = pComponentsTable.getSelectionModel()
                    .getLeadSelectionIndex();

            if (hasLogger()) {
                getLogger().logInfo(this, "valueChanged", "RowIdx=[%d]",
                        wRowIdx);
            }

            if (wRowIdx > -1 && wRowIdx < pComponentsTableModel.getRowCount()) {
                // if sorted
                final int wRealRowIdx = pComponentsTable
                        .convertRowIndexToModel(wRowIdx);
                execute(new Runnable() {
                    @Override
                    public void run() {

                        if (hasLogger()) {
                            getLogger().logInfo(this, "valueChanged",
                                    "RealRowIdx=[%d]", wRealRowIdx);
                        }
                        // set the text info of the service
                        setText(buildTextInfos(wRealRowIdx));
                    }
                });
            }
        }
    }

    private final static int COLUMN_IDX_BUNDLE = 2;

    private final static int COLUMN_IDX_FACTORY = 1;
    private final static int COLUMN_IDX_NAME = 0;
    private final static int COLUMN_IDX_STATE = 3;
    private static final long serialVersionUID = -6506936458249187873L;

    /**
     * @param aInstanceDescription
     * @return
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    static ComponentInstance getComponentInstance(
            final InstanceDescription aInstanceDescription) {

        ComponentInstance wCI = null;
        try {
            Field wField = InstanceDescription.class
                    .getDeclaredField("m_instance");
            wField.setAccessible(true);
            wCI = (ComponentInstance) wField.get(aInstanceDescription);
        } catch (Exception e) {
            // ...
        }
        return wCI;
    }

    private final boolean[] COLUMNS_EDITABLE = { false, false, false, false };

    private final String[] COLUMNS_TITLE = { "Component name", "Factory name",
            "Bundle", "State" };
    private JPanel pComponentInfoPanel;

    private Map<String, CComponentBean> pComponentsMap = new HashMap<String, CComponentBean>();
    private JSplitPane pComponentsSplitPane;
    private JTable pComponentsTable;
    private DefaultTableModel pComponentsTableModel;
    private JScrollPane pComponentsTablScrollPane;
    private JTextArea pComponentTextArea;
    private JScrollPane pComponentTextAreaScrollPane;
    private CMouseListener pMouseListener = null;
    private CSelectionListener pSelectionListener = null;

    /**
     * 
     */
    public CJPanelTableComponents() {

        super();
        newGUI();
    }

    /**
     * Create the panel.
     */
    public CJPanelTableComponents(final Executor aUiExecutor,
            final IIsolateLoggerSvc aLogger, final JPanel aPanel) {

        super(aUiExecutor, aLogger);
        aPanel.setLayout(new BorderLayout(0, 0));
        aPanel.add(newGUI(), BorderLayout.CENTER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.ui.admin.panels.CJPanelTable#addRow(java.lang.Object)
     */
    @Override
    boolean addRow(final Architecture aArchitecture) {

        pComponentsTableModel.addRow(buildRowData(aArchitecture));

        CComponentBean wComponentBean = new CComponentBean();
        wComponentBean.setArchitecture(aArchitecture);

        wComponentBean.setComponentInstance(getComponentInstance(aArchitecture
                .getInstanceDescription()));
        wComponentBean.setInstanceStateListener(new CInstanceStateListener());

        pComponentsMap.put(aArchitecture.getInstanceDescription().getName(),
                wComponentBean);

        wComponentBean.getComponentInstance().addInstanceStateListener(
                wComponentBean.getInstanceStateListener());

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.admin.panels.CJPanelTable#addRows(T[])
     */
    @Override
    void addRows(final Architecture[] aArchitectures) {

        if (aArchitectures.length > 0) {
            for (Architecture wArchitecture : aArchitectures) {
                addRow(wArchitecture);
            }
            pComponentsTable.setRowSelectionInterval(0, 0);
            fireUpdateTable();
        }
    }

    /**
     * @param aDescription
     * @return
     */
    private String buildComponentState(final InstanceDescription aDescription) {

        return String.format("%1d %s", aDescription.getState(), aDescription
                .getDescription().getAttribute("state"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.ui.admin.panels.CJPanelTable#buildRowData(java.lang
     * .Object)
     */
    @Override
    String[] buildRowData(final Architecture aArchitecture) {

        String[] wRowData = new String[COLUMNS_TITLE.length];
        final InstanceDescription wDescription = aArchitecture
                .getInstanceDescription();

        wRowData[COLUMN_IDX_NAME] = wDescription.getName();
        wRowData[COLUMN_IDX_FACTORY] = wDescription.getComponentDescription()
                .getFactory().getName();

        wRowData[COLUMN_IDX_BUNDLE] = String
                .valueOf(wDescription.getBundleId());
        wRowData[COLUMN_IDX_STATE] = buildComponentState(wDescription);

        return wRowData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.ui.admin.panels.CJPanelTable#buildTextInfos(java.
     * lang.Object)
     */
    @Override
    String buildTextInfos(final Architecture aArchitecture) {

        StringBuilder wSB = new StringBuilder();
        try {
            final InstanceDescription wDescription = aArchitecture
                    .getInstanceDescription();

            CXStringUtils.appendFormatStrInBuff(wSB, "component.name=[%s]\n",
                    wDescription.getName());
            CXStringUtils.appendFormatStrInBuff(wSB,
                    "component.factory=[%s]\n", wDescription
                            .getComponentDescription().getFactory().getName());
            CXStringUtils.appendFormatStrInBuff(wSB, "component.state=[%s]\n",
                    buildComponentState(wDescription));
            CXStringUtils.appendFormatStrInBuff(wSB, "bundle.id=[%d]\n",
                    wDescription.getBundleId());

        } catch (Exception e) {
            wSB.append(CXException.eInString(e));
        }
        // if (hasLogger()) {
        // getLogger().logInfo(this, "buildTextInfos", wSB.toString());
        // }
        return wSB.toString();
    }

    /**
     * @param aRowIdx
     * @return
     */
    private String buildTextInfos(final int aRowIdx) {

        return buildTextInfos(findInList(aRowIdx));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.admin.panels.CJPanel#destroy()
     */
    @Override
    void destroy() {

        super.destroy();

        if (pMouseListener != null) {
            pComponentsTable.addMouseListener(pMouseListener);
        }
        if (pSelectionListener != null) {
            pComponentsTable.getSelectionModel().removeListSelectionListener(
                    pSelectionListener);
        }

        List<CComponentBean> wComponentBeans = new ArrayList<CComponentBean>();
        synchronized (pComponentsMap) {
            Set<Entry<String, CComponentBean>> wEntries = pComponentsMap
                    .entrySet();
            for (Entry<String, CComponentBean> wEntry : wEntries) {
                wComponentBeans.add(wEntry.getValue());
            }
        }
        for (CComponentBean wComponentBean : wComponentBeans) {
            wComponentBean.getComponentInstance().removeInstanceStateListener(
                    wComponentBean.getInstanceStateListener());
        }
    }

    /**
     * @param aRowIdx
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws SecurityException
     */
    void doInvalidateComponent(final int aRowIdx, final String aName,
            final Architecture aArchitecture) {

        try {
            ComponentInstance wComponentInstance = getComponentInstance(aArchitecture
                    .getInstanceDescription());

            if (hasLogger()) {
                getLogger().logInfo(this, "doValidateComponent",
                        "INVALID component=[%s] ",
                        aArchitecture.getInstanceDescription().getName());
            }

            if (wComponentInstance instanceof InstanceManager) {
                ((InstanceManager) wComponentInstance)
                        .setState(ComponentInstance.INVALID);
            }
        } catch (Exception e) {
            if (hasLogger()) {
                getLogger().logInfo(this, "doInvalidateComponent", e);
            }
        }
    }

    void doStopComponent(final int aRowIdx, final String aName,
            final Architecture aArchitecture) {

        try {
            ComponentInstance wComponentInstance = getComponentInstance(aArchitecture
                    .getInstanceDescription());

            if (hasLogger()) {
                getLogger().logInfo(this, "doStopComponent",
                        "STOP component=[%s] ",
                        aArchitecture.getInstanceDescription().getName());
            }

            wComponentInstance.stop();
        } catch (Exception e) {
            if (hasLogger()) {
                getLogger().logInfo(this, "doStopComponent", e);
            }
        }
    }

    /**
     * @param aRowIdx
     */
    void doValidateComponent(final int aRowIdx, final String aName,
            final Architecture aArchitecture) {

        try {
            ComponentInstance wComponentInstance = getComponentInstance(aArchitecture
                    .getInstanceDescription());

            if (hasLogger()) {
                getLogger().logInfo(this, "doValidateComponent",
                        "VALID component=[%s] ",
                        aArchitecture.getInstanceDescription().getName());
            }
            if (wComponentInstance instanceof InstanceManager) {
                ((InstanceManager) wComponentInstance)
                        .setState(ComponentInstance.VALID);
            }
        } catch (Exception e) {
            if (hasLogger()) {
                getLogger().logInfo(this, "doValidateComponent", e);
            }
        }
    }

    /**
     * @param wComponentName
     * @return
     */
    private int findComponentRow(final String wComponentName) {

        for (int wI = 0; wI < pComponentsTableModel.getRowCount(); wI++) {
            if (wComponentName.equals(pComponentsTableModel.getValueAt(wI,
                    COLUMN_IDX_NAME))) {
                return wI;
            }
        }
        return -1;
    }

    /**
     * @param aRowIdx
     * @return
     */
    Architecture findInList(final int aRowIdx) {

        String wName = (String) pComponentsTableModel.getValueAt(aRowIdx,
                COLUMN_IDX_NAME);

        return pComponentsMap.get(wName).getArchitecture();
    }

    /**
     * @param aArchitecture
     * @return
     */
    int findInTable(final Architecture aArchitecture) {

        String wName = aArchitecture.getInstanceDescription().getName();
        int wMax = pComponentsTableModel.getRowCount();
        for (int wI = 0; wI < wMax; wI++) {
            if (wName.equals(pComponentsTableModel.getValueAt(wI,
                    COLUMN_IDX_NAME))) {
                return wI;
            }
        }
        return -1;
    }

    /**
     * 
     */
    private void fireUpdateTable() {

        execute(new Runnable() {
            @Override
            public void run() {

                try {
                    pComponentsTable.tableChanged(new TableModelEvent(
                            pComponentsTableModel));
                    pComponentsTable.updateUI();
                } catch (Exception e) {
                    if (hasLogger()) {
                        getLogger().logInfo(this, "fireUpdateTable", e);
                    }
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.admin.panels.CJPanelTable#getTable()
     */
    @Override
    JTable getTable() {

        return pComponentsTable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.admin.panels.CJPanel#newGUI()
     */
    @Override
    JPanel newGUI() {

        setLayout(new BorderLayout(0, 0));

        pComponentsSplitPane = new JSplitPane();
        pComponentsSplitPane.setResizeWeight(0.5);
        pComponentsSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        add(pComponentsSplitPane, BorderLayout.CENTER);

        {
            pComponentsTablScrollPane = new JScrollPane();
            pComponentsSplitPane.add(pComponentsTablScrollPane, JSplitPane.TOP);

            pComponentsTable = new JTable();
            pComponentsTableModel = new DefaultTableModel(new Object[][] {},
                    COLUMNS_TITLE) {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isCellEditable(final int row, final int column) {

                    return COLUMNS_EDITABLE[column];
                }
            };
            pComponentsTable.setModel(pComponentsTableModel);

            pComponentsTable.getColumnModel().getColumn(COLUMN_IDX_NAME)
                    .setPreferredWidth(200);
            pComponentsTable.getColumnModel().getColumn(COLUMN_IDX_FACTORY)
                    .setPreferredWidth(20);
            pComponentsTable.getColumnModel().getColumn(COLUMN_IDX_STATE)
                    .setPreferredWidth(20);

            TableRowSorter<TableModel> wServicesSorter = new TableRowSorter<TableModel>(
                    pComponentsTableModel);
            pComponentsTable.setRowSorter(wServicesSorter);

            List<SortKey> wSortKeys = new ArrayList<SortKey>();
            wSortKeys.add(new SortKey(COLUMN_IDX_NAME, SortOrder.ASCENDING));
            wServicesSorter.setSortKeys(wSortKeys);

            pComponentsTable
                    .setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            pComponentsTable.setColumnSelectionAllowed(false);
            pComponentsTable.setRowSelectionAllowed(true);

            // Look at TableSelectionDemo.java from java
            // tutorial to learn how work the JTable selection
            // model
            pSelectionListener = new CSelectionListener();
            pComponentsTable.getSelectionModel().addListSelectionListener(
                    pSelectionListener);

            setTableFont(EUiAdminFont.NORMAL);
            pMouseListener = new CMouseListener();
            pComponentsTable.addMouseListener(pMouseListener);

            pComponentsTablScrollPane.setViewportView(pComponentsTable);
        }

        {

            pComponentInfoPanel = new JPanel();
            pComponentsSplitPane.add(pComponentInfoPanel, JSplitPane.BOTTOM);
            pComponentInfoPanel.setLayout(new BorderLayout(0, 0));
            {
                pComponentTextAreaScrollPane = new JScrollPane();
                pComponentInfoPanel.add(pComponentTextAreaScrollPane,
                        BorderLayout.CENTER);

                pComponentTextArea = new JTextArea();
                setText("Info...");
                pComponentTextAreaScrollPane
                        .setViewportView(pComponentTextArea);
                setTextFont(EUiAdminFont.NORMAL);
            }
        }
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.admin.panels.CJPanelTable#removeAllRows()
     */
    @Override
    void removeAllRows() {

        for (int wI = pComponentsTableModel.getRowCount() - 1; wI > -1; wI--) {
            pComponentsTableModel.removeRow(wI);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.ui.admin.panels.CJPanelTable#removeRow(java.lang.
     * Object)
     */
    @Override
    void removeRow(final Architecture aArchitecture) {

        int wRowIdx = findInTable(aArchitecture);
        if (wRowIdx != -1) {
            pComponentsTableModel.removeRow(wRowIdx);
            pComponentsMap.remove(aArchitecture.getInstanceDescription()
                    .getName());
        }

        fireUpdateTable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.ui.admin.panels.CJPanelTable#setRow(java.lang.Object)
     */
    @Override
    void setRow(final Architecture aArchitecture) {

        int wRowIdx = findInTable(aArchitecture);
        if (wRowIdx == -1) {
            addRow(aArchitecture);
        } else {
            updateRow(wRowIdx, aArchitecture);
        }
        fireUpdateTable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.admin.panels.CJPanelTable#setRows(T[])
     */
    @Override
    void setRows(final Architecture[] aArchitectures) {

        removeAllRows();
        addRows(aArchitectures);
        fireUpdateTable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.ui.admin.panels.CJPanelTable#setTableFont(java.lang
     * .String, int)
     */
    @Override
    Font setTableFont(final EUiAdminFont aUiAdminFont) {

        pComponentsTable.setFont(aUiAdminFont.getTableFont());
        return aUiAdminFont.getTableFont();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.ui.admin.panels.CJPanel#setText(java.lang.String)
     */
    @Override
    void setText(final String aText) {

        pComponentTextArea.setText(aText);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.ui.admin.panels.CJPanel#setTextFont(java.lang.String,
     * int)
     */
    @Override
    Font setTextFont(final EUiAdminFont aUiAdminFont) {

        pComponentTextArea.setFont(aUiAdminFont.getTextFont());
        return aUiAdminFont.getTextFont();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.admin.panels.CJPanelTable#updateRow(int,
     * java.lang.Object)
     */
    @Override
    void updateRow(final int aRowIdx, final Architecture aArchitecture) {

        String[] wRowData = buildRowData(aArchitecture);
        int wI = 0;
        for (String wColumnValue : wRowData) {
            pComponentsTableModel.setValueAt(wColumnValue, aRowIdx, wI);
            wI++;
        }
        fireUpdateTable();
    }

}
