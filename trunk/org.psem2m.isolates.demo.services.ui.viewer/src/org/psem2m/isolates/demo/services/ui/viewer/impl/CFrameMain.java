package org.psem2m.isolates.demo.services.ui.viewer.impl;

import java.awt.BorderLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class CFrameMain extends javax.swing.JFrame {

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
				CFrameMain inst = new CFrameMain();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}

	private JList jList;
	private JPanel jPanelPage;
	private JScrollPane jScrollPaneList;
	private JScrollPane jScrollPanePage;
	private JSplitPane jSplitPaneMain;

	/**
	 *
	 */
	public CFrameMain() {
		super();
		initGUI();
	}

	/**
	 *
	 */
	private void initGUI() {
		try {
			BorderLayout thisLayout = new BorderLayout();
			setTitle("psem2m - isolate view");
			getContentPane().setLayout(thisLayout);
			this.setPreferredSize(new java.awt.Dimension(1000, 800));
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				jSplitPaneMain = new JSplitPane();
				getContentPane().add(jSplitPaneMain, BorderLayout.CENTER);
				{
					jScrollPanePage = new JScrollPane();
					jSplitPaneMain.add(jScrollPanePage, JSplitPane.RIGHT);
					{
						jPanelPage = new JPanel();
						jScrollPanePage.setViewportView(jPanelPage);
					}
				}
				{
					jScrollPaneList = new JScrollPane();
					jSplitPaneMain.add(jScrollPaneList, JSplitPane.LEFT);
					{
						ListModel jListModel = new DefaultComboBoxModel(
								new String[] { "Item One", "Item Two",
										"Item Three", "Item Four" });
						jList = new JList();
						jScrollPaneList.setViewportView(jList);
						jList.setModel(jListModel);
					}
				}
			}
			pack();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void setIsolateName(final String aIsolateName) {
		setTitle(String.format(TITLE_PREFIX, aIsolateName));
	}

}
