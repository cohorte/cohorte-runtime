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
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

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
     * @author ogattaz
     * 
     */
    class CFilterActionListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent aActionEvent) {

            JComboBox wCombo = (JComboBox) aActionEvent.getSource();
            String wSelectedFilterName = (String) wCombo.getSelectedItem();
            setServicesFilterKind(wSelectedFilterName);
            setServiceTable(CBundleUiActivator.getInstance()
                    .getAllServiceReferences());
        }
    }

    /**
     * 
     * @author ogattaz
     * 
     */
    class CFontSizeActionListener implements ActionListener {

        @Override
        public void actionPerformed(final ActionEvent aActionEvent) {

            JComboBox wCombo = (JComboBox) aActionEvent.getSource();
            String wSize = (String) wCombo.getSelectedItem();
            if (FONT_SIZE_SMALL.equals(wSize)) {

                pServicesTable.setFont(new Font("Lucida Grande", Font.PLAIN,
                        FONT_SIZE_SMALL_PT));
                pServiceInfosTextArea.setFont(new Font("Courier New",
                        Font.PLAIN, FONT_SIZE_SMALL_PT));

            } else if (FONT_SIZE_NORMAL.equals(wSize)) {

                pServicesTable.setFont(new Font("Lucida Grande", Font.PLAIN,
                        FONT_SIZE_NORMAL_PT));
                pServiceInfosTextArea.setFont(new Font("Courier New",
                        Font.PLAIN, FONT_SIZE_NORMAL_PT));

            } else if (FONT_SIZE_LARGE.equals(wSize)) {

                pServicesTable.setFont(new Font("Lucida Grande", Font.PLAIN,
                        FONT_SIZE_LARGE_PT));
                pServiceInfosTextArea.setFont(new Font("Courier New",
                        Font.PLAIN, FONT_SIZE_LARGE_PT));

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
    class CServicesSelectionListener implements ListSelectionListener {

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

    private final static String FILTER_All = "all services";
    private final static int FILTER_All_KIND = 2;
    private final static String FILTER_PSEM2M = "psem2m services";
    private final static int FILTER_PSEM2M_KIND = 1;
    private final static String FILTER_REMOTE = "remote services";
    private final static int FILTER_REMOTE_KIND = 0;

    private final static String FONT_SIZE_LARGE = "large";
    private final static int FONT_SIZE_LARGE_PT = 14;
    private final static String FONT_SIZE_NORMAL = "normal";
    private final static int FONT_SIZE_NORMAL_PT = 12;
    private final static String FONT_SIZE_SMALL = "small";
    private final static int FONT_SIZE_SMALL_PT = 10;

    /** the format of the title of the main Frame **/
    private final static String FRAME_TITLE_FORMAT = "psem2m - viewer - isolate [%s]";

    /** to be serialized ... **/
    private static final long serialVersionUID = -2939661125543649591L;

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

    private int COLUMN_IDX_BUNDLE_ID = 3;
    private int COLUMN_IDX_NAME = 0;
    private int COLUMN_IDX_REMOTE_INFO = 1;
    private int COLUMN_IDX_SERVICE_ID = 2;

    private JLabel lblNewLabel;
    private JLabel lblNewLabel_1;
    private JPanel panel;
    private JTextArea pConfigTextArea;
    private final CFrameConfig pFrameConfig;
    private JScrollPane pLoggerScrollPane;
    private JTabbedPane pMainTabbedPane;
    private JScrollPane pServiceInfosScrollPane;
    private JTextArea pServiceInfosTextArea;

    private JPanel pServicesControlPanel;

    private JComboBox pServicesFilterComboBox;

    private int pServicesFilterKind = FILTER_REMOTE_KIND;

    private JComboBox pServicesFontSizeComboBox;

    private JScrollPane pServicesScrollPane;

    private JSplitPane pServicesSplitPane;

    private JTable pServicesTable;

    private DefaultTableModel pServicesTableModel;

    private JScrollPane scrollPane;

    private JTextArea txtrPlogtextarea;

    /**
     * 
     */
    public CFrameMain() {

        super();
        pFrameConfig = new CFrameConfig();
        initGUI();
    }

    /**
     * @param aDataRow
     * @return
     */
    private boolean addOneServiceRow(final Object[] aDataRow) {

        if (pServicesFilterKind == FILTER_All_KIND

                || pServicesFilterKind == FILTER_PSEM2M_KIND
                && (aDataRow[COLUMN_IDX_NAME].toString().startsWith(
                        "org.psem2m.") || aDataRow[COLUMN_IDX_NAME].toString()
                        .startsWith("o.p."))

                || pServicesFilterKind == FILTER_REMOTE_KIND
                && !aDataRow[COLUMN_IDX_REMOTE_INFO].toString().isEmpty()

        ) {
            pServicesTableModel.addRow(aDataRow);
            return true;
        }
        return false;
    }

    /**
     * Build the row data with a ServiceReference
     * 
     * @param aServiceReference
     * @return
     */
    private Object[] buildRowData(final ServiceReference aServiceReference) {

        Object[] wRowData = new Object[4];
        wRowData[COLUMN_IDX_NAME] = extractServiceNameCleaned(aServiceReference);
        wRowData[COLUMN_IDX_REMOTE_INFO] = extractRemoteInfo(aServiceReference);
        wRowData[COLUMN_IDX_SERVICE_ID] = extractServiceId(aServiceReference);
        wRowData[COLUMN_IDX_BUNDLE_ID] = aServiceReference.getBundle()
                .getBundleId();
        return wRowData;
    }

    /**
     * @param aRowIdx
     * @return
     */
    private String buildServiceInfosText(final int aRowIdx) {

        StringBuilder wSB = new StringBuilder();

        try {
            Long wServiceId = Long.parseLong(pServicesTableModel.getValueAt(
                    aRowIdx, COLUMN_IDX_SERVICE_ID).toString());

            ServiceReference wServiceReference = CBundleUiActivator
                    .getInstance().getServiceReference(wServiceId);

            CXStringUtils.appendKeyValInBuff(wSB, "  NAME",
                    extractServiceNameCleaned(wServiceReference));
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
     * clear the table of the services
     */
    void clearServiceTable() {

        removeAllRow();
        fireUpdateTable();
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
    private String extractServiceNameCleaned(
            final ServiceReference aServiceReference) {

        return removeChars("[]", aServiceReference.toString());
    }

    /**
     * @param aServiceReference
     * @return
     */
    private int findServiceRow(final ServiceReference aServiceReference) {

        String wServiceName = extractServiceNameCleaned(aServiceReference);
        for (int wI = 0; wI < pServicesTableModel.getRowCount(); wI++) {
            if (wServiceName.equals(pServicesTableModel.getValueAt(wI, 0))) {
                return wI;
            }
        }
        return -1;
    }

    /**
 * 
 */
    private void fireUpdateTable() {

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
                    pServicesSplitPane.setResizeWeight(0.75);
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
                                pServicesTable.setFont(new Font(
                                        "Lucida Grande", Font.PLAIN,
                                        FONT_SIZE_NORMAL_PT));
                                pServicesTable.setModel(new DefaultTableModel(
                                        new Object[][] {}, new String[] {
                                                "Name", "i/e", "Service",
                                                "Bundle" }) {
                                    boolean[] columnEditables = new boolean[] {
                                            false, false, false, false };

                                    @Override
                                    public boolean isCellEditable(
                                            final int row, final int column) {

                                        return columnEditables[column];
                                    }
                                });
                                pServicesTable.getColumnModel().getColumn(0)
                                        .setPreferredWidth(300);
                                pServicesTable.getColumnModel().getColumn(1)
                                        .setPreferredWidth(10);
                                pServicesTable.getColumnModel().getColumn(2)
                                        .setPreferredWidth(30);
                                pServicesTable.getColumnModel().getColumn(3)
                                        .setPreferredWidth(30);
                            }
                            pServicesTableModel = (DefaultTableModel) pServicesTable
                                    .getModel();
                            TableRowSorter<TableModel> wServicesSorter = new TableRowSorter<TableModel>(
                                    pServicesTableModel);
                            pServicesTable.setRowSorter(wServicesSorter);

                            pServicesTable
                                    .setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                            pServicesTable.setColumnSelectionAllowed(false);
                            pServicesTable.setRowSelectionAllowed(true);

                            // Look at TableSelectionDemo.java from java
                            // tutorial to learn how work the JTable selection
                            // model
                            pServicesTable.getSelectionModel()
                                    .addListSelectionListener(
                                            new CServicesSelectionListener());

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
                                    pServiceInfosTextArea.setFont(new Font(
                                            "Courier New", Font.PLAIN,
                                            FONT_SIZE_NORMAL_PT));

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
                                    lblNewLabel = new JLabel("Filter");
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
                                    lblNewLabel_1 = new JLabel("Font");
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
                            }
                        }
                    }
                }

                {
                    pLoggerScrollPane = new JScrollPane();
                    pMainTabbedPane
                            .addTab("Log", null, pLoggerScrollPane, null);
                    {
                        txtrPlogtextarea = new JTextArea();
                        txtrPlogtextarea.setText("pLogTextArea");
                        pLoggerScrollPane.setViewportView(txtrPlogtextarea);
                    }
                }
                {
                    scrollPane = new JScrollPane();
                    pMainTabbedPane.addTab("Configuration", null, scrollPane,
                            null);
                    {
                        pConfigTextArea = new JTextArea();
                        pConfigTextArea.setFont(new Font("Courier New",
                                Font.PLAIN, FONT_SIZE_NORMAL_PT));
                        pConfigTextArea.setText("XXX ...");
                        scrollPane.setViewportView(pConfigTextArea);
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
    private void removeAllRow() {

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
     * @param aText
     */
    void setConfigextArea(final String aText) {

        pConfigTextArea.setText(aText);
    }

    /**
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
     * @param aListOfServiceRef
     */
    void setServiceTable(final List<ServiceReference> aListOfServiceRef) {

        removeAllRow();
        boolean wAdded = false;
        for (ServiceReference aServiceReference : aListOfServiceRef) {
            wAdded = wAdded | addOneServiceRow(buildRowData(aServiceReference));
        }
        if (wAdded) {
            pServicesTable.setRowSelectionInterval(0, 0);
            fireUpdateTable();
        }
    }

    /**
     * @param aServiceReference
     * @param aEvent
     */
    void setServiceTable(final ServiceReference aServiceReference,
            final int aEvent) {

        if (aEvent == ServiceEvent.REGISTERED) {
            boolean wAdded = addOneServiceRow(buildRowData(aServiceReference));
            if (wAdded) {
                fireUpdateTable();
            }
        } else {
            int wRow = findServiceRow(aServiceReference);
            if (wRow > -1) {
                pServicesTableModel.removeRow(wRow);
                fireUpdateTable();
            }
        }
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
