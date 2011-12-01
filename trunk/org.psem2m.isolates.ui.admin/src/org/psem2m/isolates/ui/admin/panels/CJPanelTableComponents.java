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
import java.util.List;
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
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Handler;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.InstanceStateListener;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.handlers.dependency.Dependency;
import org.apache.felix.ipojo.handlers.dependency.DependencyHandler;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;
import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXStringUtils;

/**
 * @author ogattaz
 * 
 */
public class CJPanelTableComponents extends CJPanelTable<Architecture> {

    class CEntityBeanComponent extends CEntityBean<Architecture> {

        private ComponentInstance pComponentInstance;
        private InstanceStateListener pInstanceStateListener;

        /**
         * @param aArchitecture
         */
        CEntityBeanComponent(final Architecture aArchitecture) {

            super(aArchitecture);
        }

        public ComponentInstance getComponentInstance() {

            return pComponentInstance;
        }

        public InstanceStateListener getInstanceStateListener() {

            return pInstanceStateListener;
        }

        public void setComponentInstance(
                final ComponentInstance componentInstance) {

            pComponentInstance = componentInstance;
        }

        public void setInstanceStateListener(
                final InstanceStateListener instanceStateListener) {

            pInstanceStateListener = instanceStateListener;
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
        public void stateChanged(final ComponentInstance aInstance,
                final int aNewState) {

            execute(new Runnable() {
                @Override
                public void run() {

                    final String wName = aInstance.getInstanceDescription()
                            .getName();
                    final int wRowIdx = pCTableModelComponents.indexOf(wName);
                    final String wStrState = buildComponentState(aInstance
                            .getInstanceDescription());

                    if (hasLogger()) {
                        getLogger().logInfo(this, "stateChanged",
                                "Name=[%s] RowIdx=[%d] NewState=[%s]", wName,
                                wRowIdx, wStrState);
                    }
                    if (wRowIdx > -1) {
                        pCTableModelComponents.setValueAt(wStrState, wRowIdx,
                                COLUMN_IDX_STATE);

                        try {
                            // if sorted
                            final int wSelectedRowIdx = pComponentsTable
                                    .convertRowIndexToModel(pComponentsTable
                                            .getSelectionModel()
                                            .getLeadSelectionIndex());

                            if (wRowIdx == wSelectedRowIdx) {

                                // set the text info of the service
                                setText(pCTableModelComponents
                                        .buildTextInfos(wSelectedRowIdx));
                            }
                        } catch (final ArrayIndexOutOfBoundsException e1) {
                            if (hasLogger()) {
                                getLogger().logSevere(this, "stateChanged",
                                        "ArrayIndexOutOfBoundsException !");
                            }
                        } catch (final Exception e2) {
                            if (hasLogger()) {
                                getLogger().logSevere(this, "stateChanged", e2);
                            }
                        }
                    }

                }
            });
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

            final String wName = String.valueOf(pCTableModelComponents
                    .getValueAt(aRowIndex, COLUMN_IDX_NAME));

            final Architecture wArchitecture = pCTableModelComponents
                    .getEntity(aRowIndex);
            final int wState = wArchitecture.getInstanceDescription()
                    .getState();

            final boolean wMustValidate = wState != ComponentInstance.VALID;

            final String wAction = wMustValidate ? "Validate" : "Invalidate";

            final String wItemLib1 = String.format("%s %s", wAction, wName);

            final JMenuItem wMenuItem1 = new JMenuItem(wItemLib1);
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

            final String wItemLib2 = String.format("Stop %s", wName);

            final JMenuItem wMenuItem2 = new JMenuItem(wItemLib2);
            wMenuItem2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent actionEvent) {

                    logAction(actionEvent, aRowIndex);

                    doStopComponent(aRowIndex, wName, wArchitecture);
                }
            });

            final JPopupMenu wJPopupMenu = new JPopupMenu();
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

            final int r = pComponentsTable.rowAtPoint(e.getPoint());
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

            execute(new Runnable() {
                @Override
                public void run() {

                    try {
                        final int wRowIdx = pComponentsTable
                                .getSelectionModel().getLeadSelectionIndex();

                        if (wRowIdx > -1) {

                            if (hasLogger()) {
                                getLogger().logInfo(this, "valueChanged",
                                        "RowIdx=[%d]", wRowIdx);
                            }

                            // if sorted
                            final int wRealRowIdx = pComponentsTable
                                    .convertRowIndexToModel(wRowIdx);

                            if (hasLogger()) {
                                getLogger().logInfo(this, "valueChanged",
                                        "RealRowIdx=[%d]", wRealRowIdx);
                            }
                            // set the text info of the service
                            setText(pCTableModelComponents
                                    .buildTextInfos(wRealRowIdx));
                        }
                    } catch (final ArrayIndexOutOfBoundsException e1) {
                        if (hasLogger()) {
                            getLogger().logSevere(this, "valueChanged",
                                    "ArrayIndexOutOfBoundsException !");
                        }
                    } catch (final Exception e2) {
                        if (hasLogger()) {
                            getLogger().logSevere(this, "valueChanged", e2);
                        }
                    }

                }
            });
        }
    }

    /**
     * @author ogattaz
     * 
     */
    class CTableModelComponents extends CTableModel<Architecture> {

        private static final long serialVersionUID = -3735378518754175980L;

        /**
         * @param aNbCol
         * @param aColKeyIdx
         */
        public CTableModelComponents(final CJPanelTable<Architecture> aPanel,
                final String[] aTitles, final int aColKeyIdx) {

            super(aPanel, aTitles, aColKeyIdx);
        }

        @Override
        CEntityBean<Architecture> buildEntityBean(final Architecture aEntity) {

            CEntityBeanComponent wCEntityBeanComponent = new CEntityBeanComponent(
                    aEntity);

            wCEntityBeanComponent
                    .setComponentInstance(getComponentInstance(aEntity
                            .getInstanceDescription()));
            wCEntityBeanComponent
                    .setInstanceStateListener(new CInstanceStateListener());
            wCEntityBeanComponent.getComponentInstance()
                    .addInstanceStateListener(
                            wCEntityBeanComponent.getInstanceStateListener());

            return wCEntityBeanComponent;
        }
    }

    private final static int COLUMN_IDX_BUNDLE = 3;
    private final static int COLUMN_IDX_FACTORY = 1;
    private final static int COLUMN_IDX_NAME = 0;
    private final static int COLUMN_IDX_STATE = 2;
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
            final Field wField = InstanceDescription.class
                    .getDeclaredField("m_instance");
            wField.setAccessible(true);
            wCI = (ComponentInstance) wField.get(aInstanceDescription);
        } catch (final Exception e) {
            // ...
        }
        return wCI;
    }

    private final boolean[] COLUMNS_EDITABLE = { false, false, false, false };
    private final int[] COLUMNS_SIZE = { 150, 150, 20, 5 };
    private final String[] COLUMNS_TIPS = { "Name of the component.",
            "Factory of the component.", "component of the component.",
            "Id of the bundle." };
    private final String[] COLUMNS_TITLE = { "Component name", "Factory name",
            "State", "Bndl" };

    private JPanel pComponentInfoPanel;
    private JSplitPane pComponentsSplitPane;
    private JTable pComponentsTable;
    private JScrollPane pComponentsTablScrollPane;
    private JTextArea pComponentTextArea;
    private JScrollPane pComponentTextAreaScrollPane;
    private CTableModelComponents pCTableModelComponents = new CTableModelComponents(
            this, COLUMNS_TITLE, COLUMN_IDX_NAME);
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
     * org.psem2m.isolates.ui.admin.panels.CJPanelTable#acceptRow(java.lang.
     * Object, java.lang.String[])
     */
    @Override
    boolean acceptRow(final Architecture aEntity, final String[] aWDataRow) {

        // all are accepted
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.ui.admin.panels.CJPanelTable#addRow(java.lang.Object)
     */
    @Override
    boolean addRow(final Architecture aArchitecture) {

        return pCTableModelComponents.addRow(aArchitecture);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.admin.panels.CJPanelTable#addRows(T[])
     */
    @Override
    void addRows(final Architecture[] aArchitectures) {

        pCTableModelComponents.addRows(aArchitectures);
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

        final String[] wRowData = new String[COLUMNS_TITLE.length];
        final InstanceDescription wDescription = aArchitecture
                .getInstanceDescription();

        wRowData[COLUMN_IDX_NAME] = wDescription.getName();
        wRowData[COLUMN_IDX_FACTORY] = wDescription.getComponentDescription()
                .getFactory().getName();

        wRowData[COLUMN_IDX_BUNDLE] = String.valueOf(CXStringUtils
                .strAdjustRight(wDescription.getBundleId(), 3));
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

        final StringBuilder wSB = new StringBuilder();
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

            final HandlerDescription[] wHandlerDescriptions = aArchitecture
                    .getInstanceDescription().getHandlers();

            final ComponentInstance wComponentInstance = getComponentInstance(aArchitecture
                    .getInstanceDescription());

            for (final HandlerDescription wHandlerDescription : wHandlerDescriptions) {

                final Handler wHandler = ((InstanceManager) wComponentInstance)
                        .getHandler(wHandlerDescription.getHandlerName());

                CXStringUtils.appendFormatStrInBuff(wSB, "handler %s=[%b]\n",
                        wHandlerDescription.getHandlerName(),
                        wHandler.getValidity());

                if (wHandler instanceof DependencyHandler) {
                    final Dependency[] wDependencies = ((DependencyHandler) wHandler)
                            .getDependencies();

                    for (final Dependency wDependency : wDependencies) {

                        CXStringUtils.appendFormatStrInBuff(wSB,
                                " Dependency %s/%s=[%d]\n",
                                wDependency.getField(), wDependency.getId(),
                                wDependency.getState());
                    }

                }

            }

        } catch (final Exception e) {
            wSB.append(CXException.eInString(e));
        }
        // if (hasLogger()) {
        // getLogger().logInfo(this, "buildTextInfos", wSB.toString());
        // }
        return wSB.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.admin.panels.CJPanel#destroy()
     */
    @Override
    public void destroy() {

        super.destroy();

        if (pMouseListener != null) {
            pComponentsTable.addMouseListener(pMouseListener);
        }
        if (pSelectionListener != null) {
            pComponentsTable.getSelectionModel().removeListSelectionListener(
                    pSelectionListener);
        }
        if (pCTableModelComponents != null) {
            pCTableModelComponents.destroy();
            pCTableModelComponents = null;
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
            final ComponentInstance wComponentInstance = getComponentInstance(aArchitecture
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
        } catch (final Exception e) {
            if (hasLogger()) {
                getLogger().logInfo(this, "doInvalidateComponent", e);
            }
        }
    }

    void doStopComponent(final int aRowIdx, final String aName,
            final Architecture aArchitecture) {

        try {
            final ComponentInstance wComponentInstance = getComponentInstance(aArchitecture
                    .getInstanceDescription());

            if (hasLogger()) {
                getLogger().logInfo(this, "doStopComponent",
                        "STOP component=[%s] ",
                        aArchitecture.getInstanceDescription().getName());
            }

            wComponentInstance.stop();
        } catch (final Exception e) {
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
            final ComponentInstance wComponentInstance = getComponentInstance(aArchitecture
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
        } catch (final Exception e) {
            if (hasLogger()) {
                getLogger().logInfo(this, "doValidateComponent", e);
            }
        }
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
    public JPanel newGUI() {

        setLayout(new BorderLayout(0, 0));

        pComponentsSplitPane = new JSplitPane();
        pComponentsSplitPane.setResizeWeight(0.5);
        pComponentsSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        add(pComponentsSplitPane, BorderLayout.CENTER);

        {
            pComponentsTablScrollPane = new JScrollPane();
            pComponentsSplitPane.add(pComponentsTablScrollPane, JSplitPane.TOP);

            pComponentsTable = new JTable();

            pComponentsTable.setModel(pCTableModelComponents);

            for (int wI = 0; wI < COLUMNS_SIZE.length; wI++) {
                pComponentsTable.getColumnModel().getColumn(wI)
                        .setPreferredWidth(COLUMNS_SIZE[wI]);
            }

            CColumnHeaderTips wColumnHeaderTips = new CColumnHeaderTips();
            pComponentsTable.getTableHeader().addMouseMotionListener(
                    wColumnHeaderTips);

            for (int wI = 0; wI < COLUMNS_TIPS.length; wI++) {
                wColumnHeaderTips.setToolTip(pComponentsTable.getColumnModel()
                        .getColumn(wI), COLUMNS_TIPS[wI]);
            }

            final TableRowSorter<TableModel> wServicesSorter = new TableRowSorter<TableModel>(
                    pCTableModelComponents);
            pComponentsTable.setRowSorter(wServicesSorter);

            final List<SortKey> wSortKeys = new ArrayList<SortKey>();
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

        pCTableModelComponents.removeAllRows();
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

        pCTableModelComponents.removeRow(aArchitecture);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.ui.admin.panels.CJPanelTable#setRow(java.lang.Object)
     */
    @Override
    void setRow(final Architecture aArchitecture) {

        pCTableModelComponents.setRow(aArchitecture);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.admin.panels.CJPanelTable#setRows(T[])
     */
    @Override
    void setRows(final Architecture[] aArchitectures) {

        pCTableModelComponents.setRows(aArchitectures);

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
    public void setText(final String aText) {

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
    public Font setTextFont(final EUiAdminFont aUiAdminFont) {

        pComponentTextArea.setFont(aUiAdminFont.getTextFont());
        return aUiAdminFont.getTextFont();
    }

}
