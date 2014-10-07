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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
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

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.Utilities;
import org.psem2m.isolates.ui.admin.CBundleUiActivator;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;
import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXStringUtils;

/**
 * @author ogattaz
 *
 */
public class CJPanelTableServices extends CJPanelTable<ServiceReference<?>> {

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
            final JCheckBox wCheckBox = (JCheckBox) aEvt.getSource();

            // use state to set the compaction flag
            setServiceNameCompaction(wCheckBox.isSelected());

            // reload the services
            execute(new Runnable() {

                @Override
                public void run() {

                    if (hasLogger()) {
                        getLogger().logInfo(this, "actionPerformed",
                                "Compaction=[%b]", pServiceNameCompaction);
                    }
                    // reset the list !
                    setRows(CBundleUiActivator.getInstance()
                            .getAllServiceReferences());
                }
            });
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

            final JComboBox wCombo = (JComboBox) aActionEvent.getSource();
            final String wSelectedFilterName = (String) wCombo
                    .getSelectedItem();

            setServicesFilterKind(wSelectedFilterName);

            // reload the services
            execute(new Runnable() {

                @Override
                public void run() {

                    if (hasLogger()) {
                        getLogger().logInfo(this, "actionPerformed",
                                "Filter=[%s]", getServicesFilterKind().name());
                    }
                    // reset the list !
                    setRows(CBundleUiActivator.getInstance()
                            .getAllServiceReferences());
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

        /**
         * @param aRowIndex
         * @return
         */
        private JPopupMenu createPopUp(final int aRowIndex) {

            final JPopupMenu wJPopupMenu = new JPopupMenu();
            try {
                final String wName = String.valueOf(pCTableModelServices
                        .getValueAt(aRowIndex, COLUMN_IDX_NAME));

                // final ServiceReference<?>wServiceReference<?>=
                // pCTableModelServices
                // .getEntity(aRowIndex);

                final JMenuItem wMenuItem1 = new JMenuItem(String.format(
                        "%s %s", "Service", wName));
                wMenuItem1.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent actionEvent) {

                        logAction(actionEvent, aRowIndex);

                        try {

                            // ...
                        } catch (final Exception e) {
                            if (hasLogger()) {
                                getLogger().logSevere(this, "actionPerformed",
                                        "JMenuItem1", e);
                            }
                        }

                    }
                });

                wJPopupMenu.add(wMenuItem1);

            } catch (final Exception e) {
                if (hasLogger()) {
                    getLogger().logSevere(this, "createPopUp", e);
                }
            }

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

            final int r = pServicesTable.rowAtPoint(e.getPoint());
            if (r >= 0 && r < pServicesTable.getRowCount()) {
                pServicesTable.setRowSelectionInterval(r, r);
            } else {
                pServicesTable.clearSelection();
            }

            int wRowIdx = pServicesTable.getSelectedRow();
            if (wRowIdx < 0) {
                return;
            }

            try {
                // if sorted
                wRowIdx = pServicesTable.convertRowIndexToModel(wRowIdx);

                createPopUp(wRowIdx).show(e.getComponent(), e.getX(), e.getY());
            } catch (final ArrayIndexOutOfBoundsException e1) {
                if (hasLogger()) {
                    getLogger().logSevere(this, "rightClik",
                            "ArrayIndexOutOfBoundsException !");
                }
            } catch (final Exception e2) {
                if (hasLogger()) {
                    getLogger().logInfo(this, "rightClik", e2);
                }
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
    class CSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(final ListSelectionEvent aListSelectionEvent) {

            execute(new Runnable() {

                @Override
                public void run() {

                    try {
                        final int wRowIdx = pServicesTable.getSelectionModel()
                                .getLeadSelectionIndex();

                        if (wRowIdx > -1) {

                            if (hasLogger()) {
                                getLogger().logInfo(this, "valueChanged",
                                        "SelectionRowIdx=[%d]", wRowIdx);
                            }
                            // if sorted
                            final int wRealRowIdx = pServicesTable
                                    .convertRowIndexToModel(wRowIdx);

                            if (hasLogger()) {
                                getLogger().logInfo(this, "valueChanged",
                                        "RealRowIdx=[%d]", wRealRowIdx);
                            }
                            // set the text info of the service
                            setText(pCTableModelServices
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
    class CTableModelServices extends CTableModel<ServiceReference<?>> {

        private static final long serialVersionUID = -3735378518754175980L;

        /**
         * @param aNbCol
         * @param aColKeyIdx
         */
        public CTableModelServices(
                final CJPanelTable<ServiceReference<?>> aPanel,
                final String[] aTitles, final int aColKeyIdx) {

            super(aPanel, aTitles, aColKeyIdx);
            if (hasLogger()) {
                getLogger().logInfo(this, "<init>", "OK");
            }
        }

    }

    private final static int COLUMN_IDX_BUNDLE_ID = 4;

    private final static int COLUMN_IDX_INTERFACE = 0;

    private final static int COLUMN_IDX_NAME = 1;

    private final static int COLUMN_IDX_REMOTE_INFO = 2;

    private final static int COLUMN_IDX_SERVICE_ID = 3;

    private final static int COLUMN_KEY_IDX = COLUMN_IDX_SERVICE_ID;

    private final static boolean COMPACTION = true;

    /** Exported service property prefix */
    private static final String PROP_SERVICE_EXPORTED_PREFIX = "service.exported.";

    /** Imported service property */
    private static final String PROP_SERVICE_IMPORTED = "service.imported";

    private static final long serialVersionUID = -6506936458249187873L;

    private final int[] COLUMNS_SIZE = { 150, 150, 5, 5, 5 };

    private final String[] COLUMNS_TIPS = { "Interface of the service.",
            "Name of the service.", "Imported or Exported service.",
            "Id of the service.", "Id of the bundle." };

    private final String[] COLUMNS_TITLE = { "Interface", "Name", "i/e", "Svc",
            "Bndl" };

    private CTableModelServices pCTableModelServices = null;

    private CMouseListener pMouseListener = null;

    private CSelectionListener pSelectionListener = null;

    private JPanel pServiceChoicesPanel;

    private JComboBox pServiceFilterComboBox;

    private JLabel pServiceFilterLabel;

    private JPanel pServiceInfoPanel;

    private JTextArea pServiceInfoTextArea;

    private boolean pServiceNameCompaction = COMPACTION;

    private EFilterKind pServicesFilterKind = EFilterKind.ALL;

    private JCheckBox pServicesNameCompactionCheckBox;

    private JSplitPane pServicesSplitPane;

    private JTable pServicesTable;

    private JScrollPane pServicesTablScrollPane;

    private JScrollPane pServiceTextAreaScrollPane;

    /**
     *
     */
    public CJPanelTableServices() {

        super();
        newGUI();
    }

    /**
     * Create the panel.
     */
    public CJPanelTableServices(final IIsolateLoggerSvc aLogger,
            final JPanel aPanel) {

        super(aLogger);
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
    boolean acceptRow(final ServiceReference<?> aServiceReference,
            final String[] wDataRow) {

        return pServicesFilterKind.isALL()

                || pServicesFilterKind.isPSEM2M()
                && (wDataRow[COLUMN_IDX_INTERFACE].toString().startsWith(
                        "org.psem2m.") || wDataRow[COLUMN_IDX_INTERFACE]
                        .toString().startsWith("o.p."))

                || pServicesFilterKind.isREMOTE()
                && !wDataRow[COLUMN_IDX_REMOTE_INFO].toString().isEmpty();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.isolates.ui.admin.panels.CJPanelTable#addRow(java.lang.Object)
     */
    @Override
    boolean addRow(final ServiceReference<?> aServiceReference) {

        final boolean result = pCTableModelServices.addRow(aServiceReference);
        pServicesTable.updateUI();
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.ui.admin.panels.CJPanelTable#addRows(T[])
     */
    @Override
    void addRows(final ServiceReference<?>[] aServiceReferences) {

        pCTableModelServices.addRows(aServiceReferences);
        pServicesTable.updateUI();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.isolates.ui.admin.panels.CJPanelTable#buildRowData(java.lang
     * .Object)
     */
    @Override
    String[] buildRowData(final ServiceReference<?> aServiceReference) {

        final String[] wRowData = new String[COLUMNS_TITLE.length];
        wRowData[COLUMN_IDX_INTERFACE] = extractServiceInterfaceCleanedCompacted(aServiceReference);
        wRowData[COLUMN_IDX_NAME] = extractServiceNameCleaned(aServiceReference);
        wRowData[COLUMN_IDX_REMOTE_INFO] = extractRemoteInfo(aServiceReference);
        // COLUMN_KEY_IDX
        wRowData[COLUMN_IDX_SERVICE_ID] = buildRowKey(aServiceReference);
        wRowData[COLUMN_IDX_BUNDLE_ID] = extractBundleId(aServiceReference);
        return wRowData;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.isolates.ui.admin.panels.CJPanelTable#buildRowKey(java.lang
     * .Object)
     */
    @Override
    String buildRowKey(final ServiceReference<?> aServiceReference) {

        // COLUMN_KEY_IDX !
        return extractServiceId(aServiceReference);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.isolates.ui.admin.panels.CJPanelTable#buildTextInfos(java.
     * lang.Object)
     */
    @Override
    String buildTextInfos(final ServiceReference<?> aServiceReference) {

        final StringBuilder wSB = new StringBuilder();

        try {

            CXStringUtils.appendKeyValInBuff(wSB, "  NAME",
                    extractServiceInterfaceCleaned(aServiceReference));
            CXStringUtils.appendKeyValInBuff(wSB, "\n SVCID",
                    extractServiceId(aServiceReference));
            CXStringUtils.appendKeyValInBuff(wSB, "\nBNDLID", aServiceReference
                    .getBundle().getBundleId());

            wSB.append(extractServiceFormatedProperties("\n  PROP: %s=[%s]",
                    aServiceReference));

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

        if (pMouseListener != null) {
            pServicesTable.removeMouseListener(pMouseListener);
            pMouseListener = null;
        }

        if (pSelectionListener != null) {
            pServicesTable.getSelectionModel().removeListSelectionListener(
                    pSelectionListener);
            pSelectionListener = null;
        }

        if (pCTableModelServices != null) {
            pCTableModelServices.destroy();
            pCTableModelServices = null;
        }

        super.destroy();
    }

    /**
     * @param aServiceReference
     * @return
     */
    private String extractBundleId(final ServiceReference<?> aServiceReference) {

        if (aServiceReference == null || aServiceReference.getBundle() == null) {
            return "null";
        }
        return CXStringUtils.strAdjustRight(aServiceReference.getBundle()
                .getBundleId(), 3);

    }

    /**
     * return the "remote" attribute of a service
     *
     * => 'E' if exported , 'I' if exported
     *
     * @param aServiceReference
     * @return a string representing the remote attribute
     */
    private String extractRemoteInfo(final ServiceReference<?> aServiceReference) {

        // A special property tells if the service has been imported
        final boolean imported = aServiceReference
                .getProperty(PROP_SERVICE_IMPORTED) != null;

        // It's more complicated for the exported services...
        boolean exported = false;
        for (final String property : aServiceReference.getPropertyKeys()) {

            if (property.startsWith(PROP_SERVICE_EXPORTED_PREFIX)
                    && aServiceReference.getProperty(property) != null) {
                // Exported service
                exported = true;
                break;
            }
        }

        // Construct the result string
        final StringBuilder result = new StringBuilder();
        if (imported) {
            result.append("I");
        }

        if (exported) {
            result.append("E");
        }

        return result.toString();
    }

    /**
     * the values of all the properties of a service
     *
     * @param aServiceReference
     * @return the values separated by a comma in a string
     */
    private String extractServiceFormatedProperties(final String aFormat,
            final ServiceReference<?> aServiceReference) {

        final String[] wKeys = aServiceReference.getPropertyKeys();

        final StringBuilder wSB = new StringBuilder();

        for (int wI = 0; wI < wKeys.length; wI++) {

            final String key = wKeys[wI];
            final Object value = aServiceReference.getProperty(key);
            String valueStr;

            if (value != null && value.getClass().isArray()) {
                // Convert array to Object[]
                Object[] convertedArray;
                if (value instanceof Object[]) {
                    // No conversion needed
                    convertedArray = (Object[]) value;

                } else {
                    // Array conversion
                    convertedArray = Utilities.arrayObjectToArray(value,
                            Object.class);
                }

                // String value
                valueStr = Arrays.toString(convertedArray);

            } else {
                // String value
                valueStr = String.valueOf(value);
            }

            wSB.append(String.format(aFormat, key, valueStr));
        }
        return wSB.toString();
    }

    /**
     * @param aServiceReference
     * @return
     */
    private String extractServiceId(final ServiceReference<?> aServiceReference) {

        return CXStringUtils.strAdjustRight(
                aServiceReference.getProperty(
                        org.osgi.framework.Constants.SERVICE_ID).toString(), 3,
                '0');
    }

    /**
     * remove the square bracket of the service name
     *
     * @param aServiceReference
     * @return a cleaned service name
     */
    private String extractServiceInterfaceCleaned(
            final ServiceReference<?> aServiceReference) {

        final String[] interfaces = (String[]) aServiceReference
                .getProperty(Constants.OBJECTCLASS);

        return CXStringUtils.removeChars("[]", Arrays.toString(interfaces));
    }

    /**
     * @param aServiceReference
     * @return
     */
    private String extractServiceInterfaceCleanedCompacted(
            final ServiceReference<?> aServiceReference) {

        String wName = extractServiceInterfaceCleaned(aServiceReference);
        if (pServiceNameCompaction) {
            final StringBuilder wSB = new StringBuilder();
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
            final ServiceReference<?> aServiceReference) {

        String wName = "";
        final Object wProperty = aServiceReference.getProperty("instance.name");
        if (wProperty != null && wProperty instanceof String) {
            wName = (String) wProperty;
            final int wPos = wName.indexOf('.');
            if (wPos > -1 && wPos + 1 < wName.length() - 1) {
                wName = wName.substring(wPos + 1);
            }
        }
        return wName;
    }

    /**
     * @return
     */
    EFilterKind getServicesFilterKind() {

        return pServicesFilterKind;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.ui.admin.panels.CJPanelTable#getTable()
     */
    @Override
    JTable getTable() {

        return pServicesTable;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.ui.admin.panels.CJPanel#newGUI()
     */
    @Override
    public JPanel newGUI() {

        try {
            setLayout(new BorderLayout(0, 0));

            pServicesSplitPane = new JSplitPane();
            pServicesSplitPane.setResizeWeight(0.5);
            pServicesSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            add(pServicesSplitPane, BorderLayout.CENTER);

            {
                pServicesTablScrollPane = new JScrollPane();
                pServicesSplitPane.add(pServicesTablScrollPane, JSplitPane.TOP);

                pServicesTable = new JTable();
                pCTableModelServices = new CTableModelServices(this,
                        COLUMNS_TITLE, COLUMN_KEY_IDX);
                pServicesTable.setModel(pCTableModelServices);

                for (int wI = 0; wI < COLUMNS_SIZE.length; wI++) {
                    pServicesTable.getColumnModel().getColumn(wI)
                            .setPreferredWidth(COLUMNS_SIZE[wI]);
                }

                final CColumnHeaderTips wColumnHeaderTips = new CColumnHeaderTips();
                pServicesTable.getTableHeader().addMouseMotionListener(
                        wColumnHeaderTips);

                for (int wI = 0; wI < COLUMNS_TIPS.length; wI++) {
                    wColumnHeaderTips.setToolTip(pServicesTable
                            .getColumnModel().getColumn(wI), COLUMNS_TIPS[wI]);
                }

                final TableRowSorter<TableModel> wServicesSorter = new TableRowSorter<TableModel>(
                        pCTableModelServices);
                pServicesTable.setRowSorter(wServicesSorter);

                final List<SortKey> wSortKeys = new ArrayList<SortKey>();
                wSortKeys
                        .add(new SortKey(COLUMN_IDX_NAME, SortOrder.ASCENDING));
                wServicesSorter.setSortKeys(wSortKeys);

                pServicesTable
                        .setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                pServicesTable.setColumnSelectionAllowed(false);
                pServicesTable.setRowSelectionAllowed(true);

                // Look at TableSelectionDemo.java from java
                // tutorial to learn how work the JTable selection
                // model
                pSelectionListener = new CSelectionListener();
                pServicesTable.getSelectionModel().addListSelectionListener(
                        pSelectionListener);
                setTableFont(EUiAdminFont.NORMAL);

                pMouseListener = new CMouseListener();
                pServicesTable.addMouseListener(pMouseListener);
                pServicesTablScrollPane.setViewportView(pServicesTable);
            }
            {
                pServiceInfoPanel = new JPanel();
                pServicesSplitPane.add(pServiceInfoPanel, JSplitPane.BOTTOM);
                pServiceInfoPanel.setLayout(new BorderLayout(0, 0));
                {
                    pServiceTextAreaScrollPane = new JScrollPane();
                    pServiceInfoPanel.add(pServiceTextAreaScrollPane,
                            BorderLayout.CENTER);
                    {
                        pServiceInfoTextArea = new JTextArea();
                        setText("Info...");
                        pServiceTextAreaScrollPane
                                .setViewportView(pServiceInfoTextArea);
                        setTextFont(EUiAdminFont.NORMAL);
                    }
                }
                {
                    pServiceChoicesPanel = new JPanel();
                    pServiceInfoPanel.add(pServiceChoicesPanel,
                            BorderLayout.NORTH);
                    {
                        pServiceFilterLabel = new JLabel("filter");
                        pServiceChoicesPanel.add(pServiceFilterLabel);
                    }
                    {
                        pServiceFilterComboBox = new JComboBox();
                        pServiceChoicesPanel.add(pServiceFilterComboBox);
                        pServiceFilterComboBox
                                .addActionListener(new CFilterActionListener());
                        pServiceFilterComboBox
                                .setModel(new DefaultComboBoxModel(EFilterKind
                                        .getLibs()));

                        pServiceFilterComboBox
                                .setSelectedIndex(EFilterKind.PSEM2M.getIdx());
                    }
                    {
                        pServicesNameCompactionCheckBox = new JCheckBox(
                                "compact");
                        pServicesNameCompactionCheckBox
                                .addActionListener(new CCompactionActionListener());
                        pServicesNameCompactionCheckBox.setSelected(COMPACTION);
                        pServiceChoicesPanel
                                .add(pServicesNameCompactionCheckBox);
                    }
                }
            }
        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

        pCTableModelServices.removeAllRows();
        pServicesTable.updateUI();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.isolates.ui.admin.panels.CJPanelTable#removeRow(java.lang.
     * Object)
     */
    @Override
    void removeRow(final ServiceReference<?> aServiceReference) {

        pCTableModelServices.removeRow(aServiceReference);
        pServicesTable.updateUI();
    }

    /**
     * @param aBundle
     */
    @Override
    void setRow(final ServiceReference<?> aServiceReference) {

        pCTableModelServices.setRow(aServiceReference);
        pServicesTable.updateUI();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.ui.admin.panels.CJPanelTable#setRows(T[])
     */
    @Override
    void setRows(final ServiceReference<?>[] aServiceReferences) {

        pCTableModelServices.setRows(aServiceReferences);
        pServicesTable.updateUI();
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

        pServicesFilterKind = EFilterKind.kindFromLib(aFilterName);
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

        pServicesTable.setFont(aUiAdminFont.getTableFont());
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

        pServiceInfoTextArea.setText(aText);
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

        pServiceInfoTextArea.setFont(aUiAdminFont.getTextFont());
        return aUiAdminFont.getTextFont();
    }

}

/**
 * @author ogattaz
 *
 */
enum EFilterKind {
    ALL("all services"), PSEM2M("psem2m services"), REMOTE("remote services");

    /**
     * @return
     */
    static String[] getLibs() {

        final String[] wLibs = new String[values().length];
        int wI = 0;
        for (final EFilterKind wKind : values()) {
            wLibs[wI] = wKind.getLib();
            wI++;
        }
        return wLibs;
    }

    /**
     * @param aLib
     * @return
     */
    static EFilterKind kindFromLib(final String aLib) {

        for (final EFilterKind wKind : values()) {
            if (wKind.pLib.equals(aLib)) {
                return wKind;
            }
        }
        return null;
    }

    private String pLib;

    /**
     * @param aLib
     */
    EFilterKind(final String aLib) {

        pLib = aLib;
    }

    /**
     * @return
     */
    int getIdx() {

        int wIdx = 0;
        for (final EFilterKind wKind : values()) {
            if (is(wKind)) {
                return wIdx;
            }
            wIdx++;
        }
        return -1;
    }

    /**
     * @return
     */
    String getLib() {

        return pLib;
    }

    /**
     * @param aKind
     * @return
     */
    boolean is(final EFilterKind aKind) {

        return this == aKind;
    }

    /**
     * @return
     */
    boolean isALL() {

        return is(ALL);
    }

    /**
     * @return
     */
    boolean isPSEM2M() {

        return is(PSEM2M);
    }

    /**
     * @return
     */
    boolean isREMOTE() {

        return is(REMOTE);
    }
}
