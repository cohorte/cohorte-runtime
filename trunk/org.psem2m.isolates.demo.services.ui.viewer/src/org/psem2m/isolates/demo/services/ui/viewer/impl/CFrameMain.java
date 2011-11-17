/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz (isandlaTech) - initial API and implementation
 *******************************************************************************/

package org.psem2m.isolates.demo.services.ui.viewer.impl;

import java.awt.BorderLayout;
import java.awt.Color;
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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
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
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.psem2m.isolates.demo.services.ui.viewer.CBundleUiActivator;
import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXStringUtils;

/**
 * The main Frame of the service IUISvc
 * 
 * @author ogattaz
 * 
 */
/**
 * @author ogattaz
 * 
 */
public class CFrameMain extends javax.swing.JFrame {

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
    class CClickComponentListener extends MouseAdapter {

        private JPopupMenu createPopUp(final int aRowIndex) {

            final String wName = String.valueOf(pComponentsTableModel
                    .getValueAt(aRowIndex, COMPONENT_COLUMN_IDX_NAME));

            final Architecture wArchitecture = pComponentsMap.get(wName);
            final int wState = wArchitecture.getInstanceDescription()
                    .getState();

            final boolean wMustValidate = wState != ComponentInstance.VALID;

            final String wAction = wMustValidate ? "Validate" : "Invalidate";

            String wItemLib1 = String.format("%s %s", wAction, wName);

            JMenuItem wMenuItem1 = new JMenuItem(wItemLib1);
            wMenuItem1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent actionEvent) {

                    if (CBundleUiActivator.getInstance().hasIsolateLoggerSvc()) {
                        CBundleUiActivator
                                .getInstance()
                                .getIsolateLoggerSvc()
                                .logInfo(this, "actionPerformed",
                                        "rowIdx=[%d] action=[%s]", aRowIndex,
                                        actionEvent.getActionCommand());
                    }
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

                    if (CBundleUiActivator.getInstance().hasIsolateLoggerSvc()) {
                        CBundleUiActivator
                                .getInstance()
                                .getIsolateLoggerSvc()
                                .logInfo(this, "actionPerformed",
                                        "rowIdx=[%d] action=[%s]", aRowIndex,
                                        actionEvent.getActionCommand());
                    }
                    doStopComponent(aRowIndex, wName, wArchitecture);
                }
            });

            JPopupMenu wJPopupMenu = new JPopupMenu();
            wJPopupMenu.add(wMenuItem1);
            wJPopupMenu.addSeparator();
            wJPopupMenu.add(wMenuItem2);

            return wJPopupMenu;
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
                if (CBundleUiActivator.getInstance().hasIsolateLoggerSvc()) {
                    CBundleUiActivator.getInstance().getIsolateLoggerSvc()
                            .logInfo(this, "mouseClicked", "isLeftMouseButton");
                }
            }
            // Right mouse click
            else if (SwingUtilities.isRightMouseButton(e)) {
                if (CBundleUiActivator.getInstance().hasIsolateLoggerSvc()) {
                    CBundleUiActivator
                            .getInstance()
                            .getIsolateLoggerSvc()
                            .logInfo(this, "mouseClicked", "isRightMouseButton");
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
     * 
     * @author ogattaz
     * 
     */
    class CCompactionActionListener implements ActionListener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
         * )
         */
        @Override
        public void actionPerformed(final ActionEvent aEvt) {

            // Perform action
            JCheckBox wCheckBox = (JCheckBox) aEvt.getSource();

            // use state to set the comptaion flag
            setServiceNameCompaction(wCheckBox.isSelected());

            // reload the services
            setServicesTable(CBundleUiActivator.getInstance()
                    .getAllServiceReferences());
        }

    }

    /**
     * @author ogattaz
     * 
     */
    class CComponentInstanceStateListener implements InstanceStateListener {

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

            if (CBundleUiActivator.getInstance().hasIsolateLoggerSvc()) {
                CBundleUiActivator
                        .getInstance()
                        .getIsolateLoggerSvc()
                        .logInfo(this, "stateChanged",
                                "Name=[%s] RowIdx=[%d] NewState=[%s]", wName,
                                wRowIdx, wStrState);
            }
            if (wRowIdx > -1) {
                pComponentsTableModel.setValueAt(wStrState, wRowIdx,
                        COMPONENT_COLUMN_IDX_STATE);
            }

        }
    }

    /**
     * 
     * @author ogattaz
     * 
     */
    class CFilterActionListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent aActionEvent) {

            JComboBox wCombo = (JComboBox) aActionEvent.getSource();
            String wSelectedFilterName = (String) wCombo.getSelectedItem();

            setServicesFilterKind(wSelectedFilterName);

            // reload the services
            setServicesTable(CBundleUiActivator.getInstance()
                    .getAllServiceReferences());
        }
    }

    /**
     * 
     * @author ogattaz
     * 
     */
    class CFontSizeActionListener implements ActionListener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
         * )
         */
        @Override
        public void actionPerformed(final ActionEvent aActionEvent) {

            JComboBox wCombo = (JComboBox) aActionEvent.getSource();
            String wSize = (String) wCombo.getSelectedItem();

            if (FONT_SIZE_SMALL.equals(wSize)) {
                adjustServicesTableRow(FONT_NAME_TABLE, FONT_SIZE_SMALL_PT);
                adjustComponentsTableRow(FONT_NAME_TABLE, FONT_SIZE_SMALL_PT);
                setTextServiceFont(FONT_NAME_TEXTAREA, FONT_SIZE_SMALL_PT);
                setTextConfigFont(FONT_NAME_TEXTAREA, FONT_SIZE_SMALL_PT);

            } else if (FONT_SIZE_NORMAL.equals(wSize)) {
                adjustServicesTableRow(FONT_NAME_TABLE, FONT_SIZE_NORMAL_PT);
                adjustComponentsTableRow(FONT_NAME_TABLE, FONT_SIZE_NORMAL_PT);
                setTextServiceFont(FONT_NAME_TEXTAREA, FONT_SIZE_NORMAL_PT);
                setTextConfigFont(FONT_NAME_TEXTAREA, FONT_SIZE_NORMAL_PT);

            } else if (FONT_SIZE_LARGE.equals(wSize)) {
                adjustServicesTableRow(FONT_NAME_TABLE, FONT_SIZE_LARGE_PT);
                adjustComponentsTableRow(FONT_NAME_TABLE, FONT_SIZE_LARGE_PT);
                setTextServiceFont(FONT_NAME_TEXTAREA, FONT_SIZE_LARGE_PT);
                setTextConfigFont(FONT_NAME_TEXTAREA, FONT_SIZE_LARGE_PT);
            }
        }

        /**
         * @param aFontName
         * @param aSize
         */
        private void adjustComponentsTableRow(final String aFontName,
                final int aSize) {

            Font wNewFont = setComponentsTableFont(aFontName, aSize);
            pComponentsTable.setRowHeight(calcRowHeight(wNewFont));
        }

        /**
         * @param aFontName
         * @param aSize
         */
        private void adjustServicesTableRow(final String aFontName,
                final int aSize) {

            Font wNewFont = setServicesTableFont(aFontName, aSize);
            pServicesTable.setRowHeight(calcRowHeight(wNewFont));
        }

        /**
         * @param aFont
         * @return
         */
        private int calcFontHeight(final Font aFont) {

            return getGraphics().getFontMetrics(aFont).getHeight();
        }

        /**
         * @param aFont
         * @return
         */
        private int calcRowHeight(final Font aFont) {

            return calcFontHeight(aFont) + 2;
        }
    }

    /**
     * Look at TableSelectionDemo.java from java tutorial to learn how work the
     * JTable selection model
     * 
     * @author ogattaz
     * 
     */
    class CSelectComponnentListener implements ListSelectionListener {

        @Override
        public void valueChanged(final ListSelectionEvent aListSelectionEvent) {

            int wRowIdx = pComponentsTable.getSelectionModel()
                    .getLeadSelectionIndex();
            if (wRowIdx > -1 && wRowIdx < pComponentsTableModel.getRowCount()) {
                // if sorted
                wRowIdx = pComponentsTable.convertRowIndexToModel(wRowIdx);
                // set the text info of the service
            }
        }
    }

    /**
     * Look at TableSelectionDemo.java from java tutorial to learn how work the
     * JTable selection model
     * 
     * @author ogattaz
     * 
     */
    class CSelectServiceListener implements ListSelectionListener {

        @Override
        public void valueChanged(final ListSelectionEvent aListSelectionEvent) {

            int wRowIdx = pServicesTable.getSelectionModel()
                    .getLeadSelectionIndex();
            if (wRowIdx > -1 && wRowIdx < pServicesTableModel.getRowCount()) {
                // if sorted
                wRowIdx = pServicesTable.convertRowIndexToModel(wRowIdx);

                // set the text info of the service
                pServiceInfosTextArea.setText(buildServiceInfosText(wRowIdx));
            }
        }
    }

    private final static boolean COMPACTION = true;
    final static int COMPONENT_COLUMN_IDX_BUNDLE = 2;
    final static int COMPONENT_COLUMN_IDX_FACTORY = 1;
    final static int COMPONENT_COLUMN_IDX_NAME = 0;

    final static int COMPONENT_COLUMN_IDX_STATE = 3;

    private final static String EMPTY = "";
    private final static String FILTER_All = "all services";
    private final static int FILTER_All_KIND = 2;
    private final static String FILTER_PSEM2M = "psem2m services";
    private final static int FILTER_PSEM2M_KIND = 1;
    private final static String FILTER_REMOTE = "remote services";
    private final static int FILTER_REMOTE_KIND = 0;
    private final static String FONT_NAME_TABLE = "Lucida Grande";
    private final static String FONT_NAME_TEXTAREA = "Courier New";
    private final static String FONT_SIZE_LARGE = "large";
    private final static int FONT_SIZE_LARGE_PT = 16;
    private final static String FONT_SIZE_NORMAL = "normal";
    private final static int FONT_SIZE_NORMAL_PT = 12;
    private final static String FONT_SIZE_SMALL = "small";

    private final static int FONT_SIZE_SMALL_PT = 10;

    /** the format of the title of the main Frame **/
    private final static String FRAME_TITLE_FORMAT = "psem2m - viewer - isolate [%s]";

    /** to be serialized ... **/
    private static final long serialVersionUID = -2939661125543649591L;
    final static int SERVICE_COLUMN_IDX_BUNDLE_ID = 4;
    final static int SERVICE_COLUMN_IDX_INTERFACE = 0;
    final static int SERVICE_COLUMN_IDX_NAME = 1;
    final static int SERVICE_COLUMN_IDX_REMOTE_INFO = 2;

    final static int SERVICE_COLUMN_IDX_SERVICE_ID = 3;

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

    /**
     * Auto-generated main method to display this JFrame
     */
    public static void main(final String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                CFrameMain inst = new CFrameMain();
                inst.setLocationRelativeTo(null);
                inst.setVisible(true);

            }
        });
    }

    private JLabel lblNewLabel;
    private JLabel lblNewLabel_1;
    private JPanel panel;
    private Map<String, Architecture> pComponentsMap = new HashMap<String, Architecture>();
    private JScrollPane pComponentsScrollPane;
    private JTable pComponentsTable;
    private DefaultTableModel pComponentsTableModel;
    private JTextArea pConfigTextArea;
    private JScrollPane pConfigurationScrollPane;
    private final CFrameConfig pFrameConfig;
    private JTabbedPane pMainTabbedPane;
    private JScrollPane pServiceInfosScrollPane;
    private JTextArea pServiceInfosTextArea;
    private boolean pServiceNameCompaction = COMPACTION;
    private JPanel pServicesControlPanel;
    private JComboBox pServicesFilterComboBox;
    private int pServicesFilterKind = FILTER_REMOTE_KIND;
    private JComboBox pServicesFontSizeComboBox;
    private JCheckBox pServicesNameCompactionCheckBox;
    private JScrollPane pServicesScrollPane;
    private JSplitPane pServicesSplitPane;

    private JTable pServicesTable;

    private DefaultTableModel pServicesTableModel;

    /**
     * 
     */
    public CFrameMain() {

        super();
        pFrameConfig = new CFrameConfig();
        initGUI();

        if (CBundleUiActivator.getInstance().hasIsolateLoggerSvc()) {
            CBundleUiActivator.getInstance().getIsolateLoggerSvc()
                    .logInfo(this, "<init>", "CFrameMain instanciated");
        }
    }

    /**
     * @param aDataRow
     * @return
     */
    private boolean addOneComponentRow(final Architecture aArchitecture) {

        pComponentsMap.put(aArchitecture.getInstanceDescription().getName(),
                aArchitecture);

        if (CBundleUiActivator.getInstance().hasIsolateLoggerSvc()) {
            CBundleUiActivator
                    .getInstance()
                    .getIsolateLoggerSvc()
                    .logInfo(
                            this,
                            "addOneComponentRow",
                            "Architecture.class=[%s] InstanceDescription.class=[%s]",
                            aArchitecture.getClass().getSimpleName(),
                            aArchitecture.getInstanceDescription().getClass()
                                    .getSimpleName());
        }

        pComponentsTableModel.addRow(buildComponentRowData(aArchitecture));

        getComponentInstance(aArchitecture.getInstanceDescription())
                .addInstanceStateListener(new CComponentInstanceStateListener());

        return true;
    }

    /**
     * @param aDataRow
     * @return
     */
    private boolean addOneServiceRow(final Object[] aDataRow) {

        if (pServicesFilterKind == FILTER_All_KIND

                || pServicesFilterKind == FILTER_PSEM2M_KIND
                && (aDataRow[SERVICE_COLUMN_IDX_INTERFACE].toString()
                        .startsWith("org.psem2m.") || aDataRow[SERVICE_COLUMN_IDX_INTERFACE]
                        .toString().startsWith("o.p."))

                || pServicesFilterKind == FILTER_REMOTE_KIND
                && !aDataRow[SERVICE_COLUMN_IDX_REMOTE_INFO].toString()
                        .isEmpty()

        ) {
            pServicesTableModel.addRow(aDataRow);
            return true;
        }
        return false;
    }

    /**
     * @param arch
     * @return
     */
    private Object[] buildComponentRowData(final Architecture arch) {

        final InstanceDescription wDescription = arch.getInstanceDescription();

        String wStrState = buildComponentState(wDescription);

        Object[] wRowData = new Object[4];
        wRowData[CFrameMain.COMPONENT_COLUMN_IDX_NAME] = wDescription.getName();
        wRowData[CFrameMain.COMPONENT_COLUMN_IDX_FACTORY] = wDescription
                .getComponentDescription().getFactory().getName();
        wRowData[CFrameMain.COMPONENT_COLUMN_IDX_BUNDLE] = String
                .valueOf(wDescription.getBundleId());
        wRowData[CFrameMain.COMPONENT_COLUMN_IDX_STATE] = wStrState;
        return wRowData;
    }

    /**
     * @param aDescription
     * @return
     */
    private String buildComponentState(final InstanceDescription aDescription) {

        return String.format("%1d %s", aDescription.getState(), aDescription
                .getDescription().getAttribute("state"));
    }

    /**
     * @param aRowIdx
     * @return
     */
    private String buildServiceInfosText(final int aRowIdx) {

        StringBuilder wSB = new StringBuilder();

        try {
            Long wServiceId = Long.parseLong(pServicesTableModel.getValueAt(
                    aRowIdx, SERVICE_COLUMN_IDX_SERVICE_ID).toString());

            ServiceReference wServiceReference = CBundleUiActivator
                    .getInstance().getServiceReference(wServiceId);

            CXStringUtils.appendKeyValInBuff(wSB, "  NAME",
                    extractServiceInterfaceCleaned(wServiceReference));
            CXStringUtils.appendKeyValInBuff(wSB, "\n SVCID",
                    extractServiceId(wServiceReference));
            CXStringUtils.appendKeyValInBuff(wSB, "\nBNDLID", wServiceReference
                    .getBundle().getBundleId());

            wSB.append(extractServiceFormatedProperties("\n  PROP: %s=[%s]",
                    wServiceReference));

        } catch (Exception e) {
            wSB.append(CXException.eInString(e));
        }
        return wSB.toString();
    }

    /**
     * Build the row data with a ServiceReference
     * 
     * @param aServiceReference
     * @return
     */
    private Object[] buildServiceRowData(
            final ServiceReference aServiceReference) {

        Object[] wRowData = new Object[5];
        wRowData[SERVICE_COLUMN_IDX_INTERFACE] = extractServiceInterfaceCleanedCompacted(aServiceReference);
        wRowData[SERVICE_COLUMN_IDX_NAME] = extractServiceNameCleaned(aServiceReference);
        wRowData[SERVICE_COLUMN_IDX_REMOTE_INFO] = extractRemoteInfo(aServiceReference);
        wRowData[SERVICE_COLUMN_IDX_SERVICE_ID] = extractServiceId(aServiceReference);
        wRowData[SERVICE_COLUMN_IDX_BUNDLE_ID] = aServiceReference.getBundle()
                .getBundleId();
        return wRowData;
    }

    /**
     * clear the table of the services
     */
    void clearServiceTable() {

        removeAllServicesRows();
        fireUpdateServicesTable();
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

        ComponentInstance wComponentInstance = getComponentInstance(aArchitecture
                .getInstanceDescription());

        if (CBundleUiActivator.getInstance().hasIsolateLoggerSvc()) {
            CBundleUiActivator
                    .getInstance()
                    .getIsolateLoggerSvc()
                    .logInfo(this, "doValidateComponent",
                            "INVALID component=[%s] ",
                            aArchitecture.getInstanceDescription().getName());
        }

        if (wComponentInstance instanceof InstanceManager) {
            ((InstanceManager) wComponentInstance)
                    .setState(ComponentInstance.INVALID);
        }
        // wComponentInstance.stop();
    }

    void doStopComponent(final int aRowIdx, final String aName,
            final Architecture aArchitecture) {

        ComponentInstance wComponentInstance = getComponentInstance(aArchitecture
                .getInstanceDescription());

        if (CBundleUiActivator.getInstance().hasIsolateLoggerSvc()) {
            CBundleUiActivator
                    .getInstance()
                    .getIsolateLoggerSvc()
                    .logInfo(this, "doStopComponent", "STOP component=[%s] ",
                            aArchitecture.getInstanceDescription().getName());
        }

        wComponentInstance.stop();
    }

    /**
     * @param aRowIdx
     */
    void doValidateComponent(final int aRowIdx, final String aName,
            final Architecture aArchitecture) {

        ComponentInstance wComponentInstance = getComponentInstance(aArchitecture
                .getInstanceDescription());

        if (CBundleUiActivator.getInstance().hasIsolateLoggerSvc()) {
            CBundleUiActivator
                    .getInstance()
                    .getIsolateLoggerSvc()
                    .logInfo(this, "doValidateComponent",
                            "VALID component=[%s] ",
                            aArchitecture.getInstanceDescription().getName());
        }
        if (wComponentInstance instanceof InstanceManager) {
            ((InstanceManager) wComponentInstance)
                    .setState(ComponentInstance.VALID);
        }
        // wComponentInstance.start();
    }

    /**
     * return the "remote" attribute of a service
     * 
     * => 'E' if exported , 'I' if exported
     * 
     * @param aServiceReference
     * @return a string representing the remote attribute
     */
    private String extractRemoteInfo(final ServiceReference aServiceReference) {

        if (aServiceReference.getProperty("service.exported.configs") != null) {
            return "E";
        }

        if (aServiceReference.getProperty("service.imported") != null) {
            return "I";
        }

        return "";

    }

    /**
     * the values of all the properties of a service
     * 
     * @param aServiceReference
     * @return the values separated by a comma in a string
     */
    private String extractServiceFormatedProperties(final String aFormat,
            final ServiceReference aServiceReference) {

        String[] wKeys = aServiceReference.getPropertyKeys();

        StringBuilder wSB = new StringBuilder();

        for (int wI = 0; wI < wKeys.length; wI++) {
            wSB.append(String.format(aFormat, wKeys[wI], aServiceReference
                    .getProperty(wKeys[wI]).toString()));
        }
        return wSB.toString();
    }

    /**
     * @param aServiceReference
     * @return
     */
    private String extractServiceId(final ServiceReference aServiceReference) {

        return aServiceReference.getProperty(
                org.osgi.framework.Constants.SERVICE_ID).toString();
    }

    /**
     * remove the square bracket of the service name
     * 
     * @param aServiceReference
     * @return a cleaned service name
     */
    private String extractServiceInterfaceCleaned(
            final ServiceReference aServiceReference) {

        return removeChars("[]", aServiceReference.toString());
    }

    /**
     * @param aServiceReference
     * @return
     */
    private String extractServiceInterfaceCleanedCompacted(
            final ServiceReference aServiceReference) {

        String wName = extractServiceInterfaceCleaned(aServiceReference);
        if (pServiceNameCompaction) {
            StringBuilder wSB = new StringBuilder();
            int wI = 0;
            for (String wPart : wName.split("\\.")) {
                if (wI > 0) {
                    wSB.append('.');
                }
                if (wI < 3) {
                    wPart = wPart.substring(0, 1);
                }
                wSB.append(wPart);
                wI++;
            }
            wName = wSB.toString();
        }
        return wName;
    }

    /**
     * 
     * 
     * @param aServiceReference
     * @return the component instance name whithour the first part which is
     *         always the name of the composition
     */
    private String extractServiceNameCleaned(
            final ServiceReference aServiceReference) {

        String wName = EMPTY;
        Object wProperty = aServiceReference.getProperty("instance.name");
        if (wProperty != null && wProperty instanceof String) {
            wName = (String) wProperty;
            int wPos = wName.indexOf('.');
            if (wPos > -1 && wPos + 1 < wName.length() - 1) {
                wName = wName.substring(wPos + 1);
            }
        }
        return wName;
    }

    /**
     * @param aArchitecture
     * @return
     */
    private int findComponentRow(final Architecture aArchitecture) {

        return findComponentRow(aArchitecture.getInstanceDescription()
                .getName());
    }

    /**
     * @param wComponentName
     * @return
     */
    private int findComponentRow(final String wComponentName) {

        for (int wI = 0; wI < pComponentsTableModel.getRowCount(); wI++) {
            if (wComponentName.equals(pComponentsTableModel.getValueAt(wI,
                    COMPONENT_COLUMN_IDX_NAME))) {
                return wI;
            }
        }
        return -1;
    }

    /**
     * @param aServiceReference
     * @return
     */
    private int findServiceRow(final ServiceReference aServiceReference) {

        String wServiceId = extractServiceId(aServiceReference);
        for (int wI = 0; wI < pServicesTableModel.getRowCount(); wI++) {
            if (wServiceId.equals(pServicesTableModel.getValueAt(wI,
                    SERVICE_COLUMN_IDX_SERVICE_ID))) {
                return wI;
            }
        }
        return -1;
    }

    /**
     * 
     */
    private void fireUpdateComponentsTable() {

        pComponentsTable
                .tableChanged(new TableModelEvent(pComponentsTableModel));
        pComponentsTable.updateUI();
    }

    /**
     * 
     */
    private void fireUpdateServicesTable() {

        pServicesTable.tableChanged(new TableModelEvent(pServicesTableModel));
        pServicesTable.updateUI();
    }

    /**
     * @return
     */
    CFrameConfig getFrameConfig() {

        return pFrameConfig;
    }

    /**
 * 
 */
    @SuppressWarnings("serial")
    private void initGUI() {

        try {
            setLocation(getFrameConfig().getLeft(), getFrameConfig().getTop());
            setTitle("psem2m - isolate view");

            getContentPane().setBackground(getFrameConfig().getColor());

            this.setPreferredSize(getFrameConfig().getWidthHeight());
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            getContentPane().setLayout(new BorderLayout(0, 0));
            {
                pMainTabbedPane = new JTabbedPane(SwingConstants.TOP);
                getContentPane().add(pMainTabbedPane);
                {
                    pServicesSplitPane = new JSplitPane();
                    pServicesSplitPane.setResizeWeight(0.6);
                    pServicesSplitPane
                            .setOrientation(JSplitPane.VERTICAL_SPLIT);
                    pMainTabbedPane.addTab("Services", null,
                            pServicesSplitPane, null);

                    {
                        // pServicesPanel = new JPanel();
                        // pMainTabbedPane.addTab("Services", null,
                        // pServicesPanel, null);
                        // pServicesPanel.setLayout(new BorderLayout(0, 0));
                        {
                            pServicesScrollPane = new JScrollPane();

                            {
                                pServicesTable = new JTable();

                                pServicesTable.setModel(new DefaultTableModel(
                                        new Object[][] {}, new String[] {
                                                "Interface", "Name", "i/e",
                                                "Service", "Bundle" }) {
                                    boolean[] columnEditables = new boolean[] {
                                            false, false, false, false, false };

                                    @Override
                                    public boolean isCellEditable(
                                            final int row, final int column) {

                                        return columnEditables[column];
                                    }
                                });
                                pServicesTable
                                        .getColumnModel()
                                        .getColumn(SERVICE_COLUMN_IDX_INTERFACE)
                                        .setPreferredWidth(150);
                                pServicesTable.getColumnModel()
                                        .getColumn(SERVICE_COLUMN_IDX_NAME)
                                        .setPreferredWidth(200);
                                pServicesTable
                                        .getColumnModel()
                                        .getColumn(
                                                SERVICE_COLUMN_IDX_REMOTE_INFO)
                                        .setPreferredWidth(10);
                                pServicesTable
                                        .getColumnModel()
                                        .getColumn(
                                                SERVICE_COLUMN_IDX_SERVICE_ID)
                                        .setPreferredWidth(30);
                                pServicesTable
                                        .getColumnModel()
                                        .getColumn(SERVICE_COLUMN_IDX_BUNDLE_ID)
                                        .setPreferredWidth(30);
                            }
                            pServicesTableModel = (DefaultTableModel) pServicesTable
                                    .getModel();
                            TableRowSorter<TableModel> wServicesSorter = new TableRowSorter<TableModel>(
                                    pServicesTableModel);
                            pServicesTable.setRowSorter(wServicesSorter);

                            List<SortKey> wSortKeys = new ArrayList<SortKey>();
                            wSortKeys.add(new SortKey(
                                    SERVICE_COLUMN_IDX_INTERFACE,
                                    SortOrder.ASCENDING));
                            wServicesSorter.setSortKeys(wSortKeys);

                            pServicesTable
                                    .setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                            pServicesTable.setColumnSelectionAllowed(false);
                            pServicesTable.setRowSelectionAllowed(true);

                            // Look at TableSelectionDemo.java from java
                            // tutorial to learn how work the JTable selection
                            // model
                            pServicesTable.getSelectionModel()
                                    .addListSelectionListener(
                                            new CSelectServiceListener());

                            setServicesTableFont(FONT_NAME_TABLE,
                                    FONT_SIZE_NORMAL_PT);

                            pServicesScrollPane.setViewportView(pServicesTable);
                            pServicesSplitPane.add(pServicesScrollPane,
                                    JSplitPane.TOP);
                        }

                        {
                            pServicesControlPanel = new JPanel();
                            // pServicesPanel.add(pServicesControlPanel,
                            // BorderLayout.SOUTH);
                            pServicesSplitPane.add(pServicesControlPanel,
                                    JSplitPane.BOTTOM);
                            pServicesControlPanel.setLayout(new BorderLayout(0,
                                    0));
                            {
                                {
                                    pServiceInfosTextArea = new JTextArea();
                                    pServiceInfosScrollPane = new JScrollPane(
                                            pServiceInfosTextArea);
                                    pServiceInfosTextArea
                                            .setForeground(Color.BLACK);
                                    pServiceInfosTextArea.setText("Info...");

                                    setTextServiceFont(FONT_NAME_TEXTAREA,
                                            FONT_SIZE_NORMAL_PT);

                                }
                                pServicesControlPanel.add(
                                        pServiceInfosScrollPane,
                                        BorderLayout.CENTER);

                            }
                            {
                                panel = new JPanel();
                                pServicesControlPanel.add(panel,
                                        BorderLayout.NORTH);
                                {
                                    lblNewLabel = new JLabel("filter");
                                    panel.add(lblNewLabel);
                                }
                                {
                                    pServicesFilterComboBox = new JComboBox();
                                    panel.add(pServicesFilterComboBox);
                                    pServicesFilterComboBox
                                            .addActionListener(new CFilterActionListener());
                                    pServicesFilterComboBox
                                            .setModel(new DefaultComboBoxModel(
                                                    new String[] {
                                                            FILTER_REMOTE,
                                                            FILTER_PSEM2M,
                                                            FILTER_All }));
                                }
                                {
                                    lblNewLabel_1 = new JLabel("font");
                                    panel.add(lblNewLabel_1);
                                }
                                {
                                    pServicesFontSizeComboBox = new JComboBox();
                                    pServicesFontSizeComboBox
                                            .setModel(new DefaultComboBoxModel(
                                                    new String[] {
                                                            FONT_SIZE_SMALL,
                                                            FONT_SIZE_NORMAL,
                                                            FONT_SIZE_LARGE }));
                                    pServicesFontSizeComboBox
                                            .setSelectedIndex(1);// normal
                                    panel.add(pServicesFontSizeComboBox);

                                    pServicesFontSizeComboBox
                                            .addActionListener(new CFontSizeActionListener());
                                }
                                {
                                    pServicesNameCompactionCheckBox = new JCheckBox(
                                            "compact");
                                    pServicesNameCompactionCheckBox
                                            .addActionListener(new CCompactionActionListener());
                                    pServicesNameCompactionCheckBox
                                            .setSelected(COMPACTION);
                                    panel.add(pServicesNameCompactionCheckBox);
                                }
                            }
                        }
                    }
                }
                {
                    pComponentsScrollPane = new JScrollPane();
                    {
                        pComponentsTable = new JTable();

                        pComponentsTable.setModel(new DefaultTableModel(
                                new Object[][] {}, new String[] {
                                        "Component name", "Factory name",
                                        "Bundle", "State" }) {
                            boolean[] columnEditables = new boolean[] { false,
                                    false, false, false };

                            @Override
                            public boolean isCellEditable(final int row,
                                    final int column) {

                                return columnEditables[column];
                            }
                        });

                        pComponentsTable.getColumnModel()
                                .getColumn(COMPONENT_COLUMN_IDX_NAME)
                                .setPreferredWidth(200);
                        pComponentsTable.getColumnModel()
                                .getColumn(COMPONENT_COLUMN_IDX_FACTORY)
                                .setPreferredWidth(200);
                        pComponentsTable.getColumnModel()
                                .getColumn(COMPONENT_COLUMN_IDX_BUNDLE)
                                .setPreferredWidth(20);
                        pComponentsTable.getColumnModel()
                                .getColumn(COMPONENT_COLUMN_IDX_STATE)
                                .setPreferredWidth(20);

                        pComponentsTableModel = (DefaultTableModel) pComponentsTable
                                .getModel();
                        TableRowSorter<TableModel> wComponentsSorter = new TableRowSorter<TableModel>(
                                pComponentsTableModel);
                        pComponentsTable.setRowSorter(wComponentsSorter);

                        List<SortKey> wSortKeys = new ArrayList<SortKey>();
                        wSortKeys.add(new SortKey(COMPONENT_COLUMN_IDX_NAME,
                                SortOrder.ASCENDING));
                        wComponentsSorter.setSortKeys(wSortKeys);

                        pComponentsTable
                                .setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                        pComponentsTable.setColumnSelectionAllowed(false);
                        pComponentsTable.setRowSelectionAllowed(true);

                        setComponentsTableFont(FONT_NAME_TABLE,
                                FONT_SIZE_NORMAL_PT);

                        pComponentsTable
                                .setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                        pComponentsTable.setColumnSelectionAllowed(false);
                        pComponentsTable.setRowSelectionAllowed(true);

                        // Look at TableSelectionDemo.java from java
                        // tutorial to learn how work the JTable selection
                        // model
                        pComponentsTable.getSelectionModel()
                                .addListSelectionListener(
                                        new CSelectComponnentListener());

                        pComponentsTable
                                .addMouseListener(new CClickComponentListener());

                        pComponentsScrollPane.setViewportView(pComponentsTable);

                        pMainTabbedPane.addTab("Components", null,
                                pComponentsScrollPane, null);
                    }
                }

                {
                    pConfigurationScrollPane = new JScrollPane();
                    pMainTabbedPane.addTab("Configuration", null,
                            pConfigurationScrollPane, null);
                    {
                        pConfigTextArea = new JTextArea();
                        setTextConfigFont(FONT_NAME_TEXTAREA,
                                FONT_SIZE_NORMAL_PT);

                        pConfigTextArea.setText("XXX ...");
                        pConfigurationScrollPane
                                .setViewportView(pConfigTextArea);
                    }
                }
            }

            pack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    private void removeAllComponentsRows() {

        for (int wI = pComponentsTableModel.getRowCount() - 1; wI > -1; wI--) {
            pComponentsTableModel.removeRow(wI);
        }
        pComponentsMap.clear();
    }

    /**
     * 
     */
    private void removeAllServicesRows() {

        for (int wI = pServicesTableModel.getRowCount() - 1; wI > -1; wI--) {
            pServicesTableModel.removeRow(wI);
        }
    }

    /**
     * @param aChars
     * @param aValue
     * @return
     */
    private String removeChars(final String aChars, final String aValue) {

        if (aValue == null || aValue.isEmpty() || aChars == null
                || aChars.isEmpty()) {
            return aValue;
        }

        StringBuilder wSB = new StringBuilder();

        int wMaxVal = aValue.length();
        char wCharVal;
        for (int wI = 0; wI < wMaxVal; wI++) {
            wCharVal = aValue.charAt(wI);
            if (aChars.indexOf(wCharVal) == -1) {
                wSB.append(wCharVal);
            }
        }
        return wSB.toString();
    }

    /**
     * Sets (replaces) the components table content
     * 
     * @param aComponentsDescription
     *            The new content
     */
    void setComponentsTable(final Architecture[] aArchitectures) {

        removeAllComponentsRows();

        boolean wAdded = false;
        for (Architecture wArchitecture : aArchitectures) {
            wAdded = wAdded | addOneComponentRow(wArchitecture);
        }
        if (wAdded) {
            pComponentsTable.setRowSelectionInterval(0, 0);
            fireUpdateComponentsTable();
        }

    }

    /**
     * Sets the font of the components table
     * 
     * @param aFontName
     * @param aSize
     */
    private Font setComponentsTableFont(final String aFontName, final int aSize) {

        Font wNewFont = new Font(aFontName, Font.PLAIN, aSize);
        pComponentsTable.setFont(wNewFont);
        return wNewFont;
    }

    /**
     * Adds or removes one component in the components table content
     * 
     * @param aArchitecture
     * @param aComponentEvent
     */
    void setComponentTable(final Architecture aArchitecture,
            final int aComponentEvent) {

        if (aComponentEvent == ServiceEvent.REGISTERED) {
            boolean wAdded = addOneComponentRow(aArchitecture);
            if (wAdded) {
                fireUpdateComponentsTable();
            }
        } else {
            int wRow = findComponentRow(aArchitecture);
            if (wRow > -1) {
                pComponentsTableModel.removeRow(wRow);
                pComponentsMap.remove(aArchitecture.getInstanceDescription()
                        .getName());
                fireUpdateComponentsTable();
            }
        }
    }

    /**
     * Sets (replaces) the configuration text area content
     * 
     * @param aText
     */
    void setConfigextArea(final String aText) {

        pConfigTextArea.setText(aText);
    }

    /**
     * Sets the flag which control the compaction of the name of the service
     * interface
     * 
     * @param aCompaction
     */
    private void setServiceNameCompaction(final boolean aCompaction) {

        pServiceNameCompaction = aCompaction;
    }

    /**
     * Sets the filter applied on the row of the services table
     * 
     * @param aFilterName
     *            the name of the selected filter
     */
    private void setServicesFilterKind(final String aFilterName) {

        if (FILTER_REMOTE.equals(aFilterName)) {
            pServicesFilterKind = FILTER_REMOTE_KIND;
        } else if (FILTER_PSEM2M.equals(aFilterName)) {
            pServicesFilterKind = FILTER_PSEM2M_KIND;
        } else {
            pServicesFilterKind = FILTER_All_KIND;
        }
    }

    /**
     * Sets (replaces) the services table content
     * 
     * @param aListOfServiceRef
     */
    void setServicesTable(final List<ServiceReference> aListOfServiceRef) {

        removeAllServicesRows();
        boolean wAdded = false;
        for (ServiceReference aServiceReference : aListOfServiceRef) {
            wAdded = wAdded
                    | addOneServiceRow(buildServiceRowData(aServiceReference));
        }
        if (wAdded) {
            pServicesTable.setRowSelectionInterval(0, 0);
            fireUpdateServicesTable();
        }
    }

    /**
     * Sets the services table font
     * 
     * @param aFontName
     * @param aSize
     */
    private Font setServicesTableFont(final String aFontName, final int aSize) {

        Font wNewFont = new Font(aFontName, Font.PLAIN, aSize);
        pServicesTable.setFont(wNewFont);
        return wNewFont;
    }

    /**
     * Adds or removes one service in the services table content
     * 
     * @param aServiceReference
     * @param aEvent
     */
    void setServiceTable(final ServiceReference aServiceReference,
            final int aEvent) {

        if (aEvent == ServiceEvent.REGISTERED) {
            boolean wAdded = addOneServiceRow(buildServiceRowData(aServiceReference));
            if (wAdded) {
                fireUpdateServicesTable();
            }
        } else {
            int wRow = findServiceRow(aServiceReference);
            if (wRow > -1) {
                pServicesTableModel.removeRow(wRow);
                fireUpdateServicesTable();
            }
        }
    }

    /**
     * @param aFontName
     * @param aSize
     * @return
     */
    private Font setTextConfigFont(final String aFontName, final int aSize) {

        Font wNewFont = new Font(aFontName, Font.PLAIN, aSize);
        pConfigTextArea.setFont(wNewFont);
        return wNewFont;
    }

    /**
     * @param aFontName
     * @param aSize
     */
    private Font setTextServiceFont(final String aFontName, final int aSize) {

        Font wNewFont = new Font(aFontName, Font.PLAIN, aSize);
        pServiceInfosTextArea.setFont(wNewFont);
        return wNewFont;
    }

    /**
     * @param aIsolateName
     */
    @Override
    public void setTitle(final String aIsolateName) {

        String wId;
        if (aIsolateName == null || aIsolateName.isEmpty()) {
            wId = "no id";
        } else {
            int wPos = aIsolateName.lastIndexOf('.');
            wId = wPos > -1 && wPos + 1 < aIsolateName.length() - 1 ? aIsolateName
                    .substring(wPos + 1) : aIsolateName;
        }

        super.setTitle(String.format(FRAME_TITLE_FORMAT, wId));
    }

}
