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
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * The main Frame of the service IUISvc
 * 
 * @author ogattaz
 * 
 */
public class CFrameMain extends javax.swing.JFrame {

    /**
     * A list which can change
     * 
     * @author ogattaz
     * 
     */
    class MutableList extends JList {

        private static final long serialVersionUID = 4547840131868565534L;

        /**
         * 
         */
        MutableList() {

            super(new DefaultListModel());
        }

        /**
         * @return
         */
        DefaultListModel getContents() {

            return (DefaultListModel) getModel();
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = -2939661125543649591L;

    /**
     * 
     */
    private final static String TITLE_PREFIX = "psem2m - isolate view  - [%s]";

    /**
     * Auto-generated main method to display this JFrame
     */
    public static void main(final String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                System.getProperties().put("psem2m.demo.ui.viewer.top", "0scr");
                System.getProperties().put("psem2m.demo.ui.viewer.left",
                        "0.5scr");
                System.getProperties().put("psem2m.demo.ui.viewer.width",
                        "0.5scr");
                System.getProperties().put("psem2m.demo.ui.viewer.height",
                        "300px");
                System.getProperties().put("psem2m.demo.ui.viewer.color",
                        "blue");

                CFrameMain inst = new CFrameMain();
                inst.setLocationRelativeTo(null);
                inst.setVisible(true);

            }
        });
    }

    private MutableList jList;
    private JPanel jPanelPage;
    private JScrollPane jScrollPaneList;
    private JScrollPane jScrollPanePage;
    private JSplitPane jSplitPaneMain;
    private final CFrameConfig pFrameConfig;
    private JScrollPane scrollPane;
    private JTabbedPane tabbedPane;
    private JTextArea textArea;

    /**
     * 
     */
    public CFrameMain() {

        super();
        pFrameConfig = new CFrameConfig();
        initGUI();
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
    private void initGUI() {

        try {
            setLocation(getFrameConfig().getLeft(), getFrameConfig().getTop());
            BorderLayout thisLayout = new BorderLayout();
            setTitle("psem2m - isolate view");

            getContentPane().setLayout(thisLayout);

            getContentPane().setBackground(getFrameConfig().getColor());

            this.setPreferredSize(getFrameConfig().getWidthHeight());
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            {
                tabbedPane = new JTabbedPane(SwingConstants.TOP);
                getContentPane().add(tabbedPane, BorderLayout.CENTER);
                jSplitPaneMain = new JSplitPane();
                jSplitPaneMain.setOneTouchExpandable(true);
                tabbedPane.addTab("Services", null, jSplitPaneMain, null);
                {
                    jScrollPanePage = new JScrollPane();
                    jSplitPaneMain.add(jScrollPanePage, JSplitPane.RIGHT);
                    {
                        jPanelPage = new JPanel();
                        jScrollPanePage.setViewportView(jPanelPage);
                    }
                }
                jScrollPaneList = new JScrollPane();
                jSplitPaneMain.add(jScrollPaneList, JSplitPane.LEFT);
                jScrollPaneList.setViewportView(jList);
                {
                    scrollPane = new JScrollPane();
                    tabbedPane.addTab("Log", null, scrollPane, null);
                }
                {
                    textArea = new JTextArea();
                    tabbedPane.addTab("Console", null, textArea, null);
                }
                jList = new MutableList();
            }
            {
                jList.getContents().addElement("Item One");
                // jList.getContents().addElement("Item One'");
                // jList.getContents().removeElement("Item One'");
            }

            pack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param aIsolateName
     */
    void setIsolateName(final String aIsolateName) {

        setTitle(String.format(TITLE_PREFIX, aIsolateName));
    }

    /**
     * 
     */
    private void setPos() {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();

        int windowX = Math.max(0, (screenSize.width - windowSize.width) / 2);
        int windowY = Math.max(0, (screenSize.height - windowSize.height) / 2);

        setLocation(windowX, windowY);
    }

}
