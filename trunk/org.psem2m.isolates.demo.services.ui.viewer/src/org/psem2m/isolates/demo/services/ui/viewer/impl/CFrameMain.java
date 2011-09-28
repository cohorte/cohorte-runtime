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

    private final static String FILTER_All = "all services";

    private final static String FILTER_PSEM2M = "psem2m services";

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

    private final CFrameConfig pFrameConfig;
    private JScrollPane pLoggerScrollPane;
    private JTabbedPane pMainTabbedPane;
    private JScrollPane pServiceInfosScrollPane;
    private JTextArea pServiceInfosTextArea;
    private JPanel pServicesControlPanel;
    private JComboBox pServicesFilterComboBox;
    private boolean pServicesFilterPsem2m = true;
    private JScrollPane pServicesScrollPane;
    private JSplitPane pServicesSplitPane;
    private JTable pServicesTable;
    private DefaultTableModel pServicesTableModel;
    private JScrollPane scrollPane;
    private JTextArea txtrPconsoletextarea;
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

        if (pServicesFilterPsem2m
                && !aDataRow[0].toString().startsWith("org.psem2m")) {
            return false;
        }
        pServicesTableModel.addRow(aDataRow);
        return true;
    }

    /**
     * Build the row data with a ServiceReference
     * 
     * @param aServiceReference
     * @return
     */
    private Object[] buildRowData(final ServiceReference aServiceReference) {

        Object[] wRowData = new Object[3];
        wRowData[0] = extractServiceNameCleaned(aServiceReference);
        wRowData[1] = extractServiceId(aServiceReference);
        wRowData[2] = aServiceReference.getBundle().getBundleId();
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
                    aRowIdx, 1).toString());

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
                                pServicesTable.setModel(new DefaultTableModel(
                                        new Object[][] {},
                                        new String[] { "Name", "ServiceId",
                                                "BundleId" }) {
                                    boolean[] columnEditables = new boolean[] {
                                            false, false, false };

                                    @Override
                                    public boolean isCellEditable(
                                            final int row, final int column) {

                                        return columnEditables[column];
                                    }
                                });
                                pServicesTable.getColumnModel().getColumn(0)
                                        .setPreferredWidth(300);
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

                            // see TableSelectionDemo.java from java tutorial
                            pServicesTable.getSelectionModel()
                                    .addListSelectionListener(
                                            new ListSelectionListener() {

                                                @Override
                                                public void valueChanged(
                                                        final ListSelectionEvent aListSelectionEvent) {

                                                    int wRowIdx = pServicesTable
                                                            .getSelectionModel()
                                                            .getLeadSelectionIndex();
                                                    if (wRowIdx > -1
                                                            && wRowIdx < pServicesTableModel
                                                                    .getRowCount()) {
                                                        // if sorted
                                                        wRowIdx = pServicesTable
                                                                .convertRowIndexToModel(wRowIdx);
                                                        // set the text info of
                                                        // the
                                                        // service
                                                        pServiceInfosTextArea
                                                                .setText(buildServiceInfosText(wRowIdx));
                                                    }
                                                }
                                            });

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
                                pServicesFilterComboBox = new JComboBox();
                                pServicesFilterComboBox
                                        .addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(
                                                    final ActionEvent aActionEvent) {

                                                JComboBox wCombo = (JComboBox) aActionEvent
                                                        .getSource();
                                                String wFilter = (String) wCombo
                                                        .getSelectedItem();
                                                pServicesFilterPsem2m = FILTER_PSEM2M
                                                        .equals(wFilter);
                                                setServiceTable(CBundleUiActivator
                                                        .getInstance()
                                                        .getAllServiceReferences());
                                            }
                                        });
                                pServicesFilterComboBox
                                        .setModel(new DefaultComboBoxModel(
                                                new String[] { FILTER_PSEM2M,
                                                        FILTER_All }));

                                pServicesControlPanel.add(
                                        pServicesFilterComboBox,
                                        BorderLayout.SOUTH);
                            }
                            {
                                {
                                    pServiceInfosTextArea = new JTextArea();
                                    pServiceInfosScrollPane = new JScrollPane(
                                            pServiceInfosTextArea);
                                    pServiceInfosTextArea
                                            .setForeground(Color.BLACK);
                                    pServiceInfosTextArea
                                            .setText("Info...\n dfqsdfg qqlf qsfdu.\n dfqsdfg qqlf hqs.\n dfqsdfg qqlffdu hqosdu ");
                                    pServiceInfosTextArea.setFont(new Font(
                                            "Courier New", Font.PLAIN, 11));

                                }
                                pServicesControlPanel.add(
                                        pServiceInfosScrollPane,
                                        BorderLayout.CENTER);

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
                    pMainTabbedPane.addTab("Console", null, scrollPane, null);
                    {
                        txtrPconsoletextarea = new JTextArea();
                        txtrPconsoletextarea.setText("pConsoletextArea");
                        scrollPane.setViewportView(txtrPconsoletextarea);
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
