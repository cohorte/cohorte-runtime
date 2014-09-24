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

package org.psem2m.isolates.ui.admin.impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.ui.admin.CBundleUiActivator;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;
import org.psem2m.isolates.ui.admin.api.EUiAdminPanelLocation;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanel;

/**
 * The main Frame of the service IUISvc
 * 
 * @author ogattaz
 * 
 */
public class CFrameMain extends javax.swing.JFrame {

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

			final JComboBox wCombo = (JComboBox) aActionEvent.getSource();

			final String wUiAdminFontLib = (String) wCombo.getSelectedItem();

			final EUiAdminFont wUiAdminFont = EUiAdminFont.fontFromLib(wUiAdminFontLib);

			if (hasLogger()) {
				getLogger().logInfo(this, "", "UiAdminFontLib=[%s] UiAdminFont=[%s]",
						wUiAdminFontLib, wUiAdminFont.name());
			}

			if (pUiAdminSvc != null) {
				pUiAdminSvc.setUiAdminFont(wUiAdminFont);
			}

		}
	}

	/** the format of the title of the main Frame **/
	private final static String FRAME_TITLE_FORMAT = "Cohorte viewer - isolate [%s]";

	/** to be serialized ... **/
	private static final long serialVersionUID = -2939661125543649591L;

	/**
	 * Auto-generated main method to display this JFrame
	 */
	public static void main(final String[] args) {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				final CFrameMain inst = new CFrameMain();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);

			}
		});
	}

	private final CFrameMainConfig pFrameConfig;

	private IIsolateLoggerSvc pLogger;

	private JTabbedPane pMainTabbedPane;

	private JLabel pPreferenceLabelReadability;

	private JPanel pPreferencesChoices;

	private JLabel pPreferencesFontLabel;

	private JComboBox pPreferencesFontSizeComboBox;

	private JPanel pPreferencesPanel;

	private CUiAdminSvc pUiAdminSvc;

	/**
     * 
     */
	public CFrameMain() {

		super();

		final CBundleUiActivator activator = CBundleUiActivator.getInstance();
		if (activator != null) {
			pFrameConfig = new CFrameMainConfig(activator.getContext());
		} else {
			pFrameConfig = new CFrameMainConfig(null);
		}

		initGUI();
	}

	/**
	 * @param aCUiAdminSvc
	 * @param aLogger
	 */
	CFrameMain(final CUiAdminSvc aCUiAdminSvc) {

		this();
		pLogger = aCUiAdminSvc.getLogger();
		pUiAdminSvc = aCUiAdminSvc;
		if (hasLogger()) {
			getLogger().logInfo(this, "<init>", "CFrameMain instanciated");
		}
	}

	/**
	 * @param aCUiAdminPanel
	 * @throws Exception
	 */
	void addUiAdminPanel(final CUiAdminPanel aCUiAdminPanel, final EUiAdminPanelLocation aLocation) {

		final JPanel wNewPanel = new JPanel();

		aCUiAdminPanel.setFrameAndPanel(this, wNewPanel);

		int wIdx;

		if (aLocation.isFIRST()) {
			wIdx = 0;
		} else if (aLocation.isLAST()) {
			wIdx = pMainTabbedPane.getTabCount() - 1;
		} else {
			// TODO => ASCENDING & DESCENDING,
			wIdx = 0;
		}

		// public void insertTab(String title, Icon icon,Component
		// component, String tip, int index)
		pMainTabbedPane.insertTab(aCUiAdminPanel.getName(), aCUiAdminPanel.getIcon(), wNewPanel,
				aCUiAdminPanel.getTip(), wIdx);
		pMainTabbedPane.setSelectedIndex(wIdx);
	}

	/**
     * 
     */
	void destroy() {

		dispose();

		pLogger = null;
		pUiAdminSvc = null;
	}

	/**
	 * @return
	 */
	CFrameMainConfig getFrameMainConfig() {

		return pFrameConfig;
	}

	/**
	 * @return
	 */
	IIsolateLoggerSvc getLogger() {

		return pLogger;
	}

	/**
	 * @return
	 */
	boolean hasLogger() {

		return getLogger() != null;
	}

	/**
     * 
     */
	private void initGUI() {

		try {
			setLocation(getFrameMainConfig().getLeft(), getFrameMainConfig().getTop());
			setTitle("Cohorte sdk");

			getContentPane().setBackground(getFrameMainConfig().getColor());

			this.setPreferredSize(getFrameMainConfig().getWidthHeight());
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			getContentPane().setLayout(new BorderLayout(0, 0));
			{
				pMainTabbedPane = new JTabbedPane(SwingConstants.TOP);
				getContentPane().add(pMainTabbedPane);

				{
					pPreferencesPanel = new JPanel();
					pMainTabbedPane.addTab("Preferences", null, pPreferencesPanel, null);
					pPreferencesPanel.setLayout(new BorderLayout(0, 0));
					{
						{
							pPreferencesChoices = new JPanel();
							pPreferencesPanel.add(pPreferencesChoices);
							final GridBagLayout gbl_pPreferencesChoices = new GridBagLayout();
							gbl_pPreferencesChoices.columnWidths = new int[] { 199, 26, 100, 0 };
							gbl_pPreferencesChoices.rowHeights = new int[] { 27, 0, 0, 0 };
							gbl_pPreferencesChoices.columnWeights = new double[] { 0.0, 0.0, 0.0,
									Double.MIN_VALUE };
							gbl_pPreferencesChoices.rowWeights = new double[] { 0.0, 0.0, 0.0,
									Double.MIN_VALUE };
							pPreferencesChoices.setLayout(gbl_pPreferencesChoices);
							{
								pPreferenceLabelReadability = new JLabel("Readability");
								final GridBagConstraints gbc_pPreferenceLabelReadability = new GridBagConstraints();
								gbc_pPreferenceLabelReadability.insets = new Insets(0, 0, 5, 5);
								gbc_pPreferenceLabelReadability.gridx = 0;
								gbc_pPreferenceLabelReadability.gridy = 1;
								pPreferencesChoices.add(pPreferenceLabelReadability,
										gbc_pPreferenceLabelReadability);
							}
							{
								pPreferencesFontLabel = new JLabel("Font");
								final GridBagConstraints gbc_PreferencesFontLabel = new GridBagConstraints();
								gbc_PreferencesFontLabel.anchor = GridBagConstraints.WEST;
								gbc_PreferencesFontLabel.insets = new Insets(0, 0, 0, 5);
								gbc_PreferencesFontLabel.gridx = 1;
								gbc_PreferencesFontLabel.gridy = 2;
								pPreferencesChoices.add(pPreferencesFontLabel,
										gbc_PreferencesFontLabel);
							}
							pPreferencesFontSizeComboBox = new JComboBox();
							final GridBagConstraints gbc_PreferencesFontSizeComboBox = new GridBagConstraints();
							gbc_PreferencesFontSizeComboBox.anchor = GridBagConstraints.NORTHWEST;
							gbc_PreferencesFontSizeComboBox.gridx = 2;
							gbc_PreferencesFontSizeComboBox.gridy = 2;
							pPreferencesChoices.add(pPreferencesFontSizeComboBox,
									gbc_PreferencesFontSizeComboBox);
							pPreferencesFontSizeComboBox.setModel(new DefaultComboBoxModel(
									EUiAdminFont.getLibs()));
							pPreferencesFontSizeComboBox.setSelectedIndex(1);

							pPreferencesFontSizeComboBox
									.addActionListener(new CFontSizeActionListener());

						}
					}
					{
						JPanel pPreferencesNorthPanel = new JPanel();
						pPreferencesPanel.add(pPreferencesNorthPanel, BorderLayout.NORTH);
						pPreferencesNorthPanel.setLayout(new BoxLayout(pPreferencesNorthPanel,
								BoxLayout.Y_AXIS));
						pPreferencesNorthPanel.setBackground(Color.WHITE);

						{
							ImageIcon wIconCohorteLogoText = new ImageIcon(
									CFrameMain.class
											.getResource("/org/psem2m/isolates/ui/admin/impl/COHORTE logo text.png"));
							// 657 × 83 pixels => 327 x 42
							wIconCohorteLogoText = new ImageIcon(wIconCohorteLogoText.getImage()
									.getScaledInstance(327, 42, Image.SCALE_SMOOTH));

							ImageIcon wIconCohorteLogo = new ImageIcon(
									CFrameMain.class
											.getResource("/org/psem2m/isolates/ui/admin/impl/COHORTE logo.png"));

							// 1268 × 1043 pixels => 327 x 42
							wIconCohorteLogo = new ImageIcon(wIconCohorteLogo.getImage()
									.getScaledInstance(200, 190, Image.SCALE_SMOOTH));

							JLabel wlabelCohorteLogoText = new JLabel();
							wlabelCohorteLogoText.setIcon(wIconCohorteLogoText);

							JPanel wPanelCohorteLogoText = new JPanel();
							wPanelCohorteLogoText.setAlignmentX(JComponent.CENTER_ALIGNMENT);
							wPanelCohorteLogoText.add(wlabelCohorteLogoText);

							pPreferencesNorthPanel.add(wPanelCohorteLogoText);

							JLabel wlabelCohorteLogo = new JLabel();
							wlabelCohorteLogo.setIcon(wIconCohorteLogo);

							JPanel wPanelCohorteLogo = new JPanel();
							wPanelCohorteLogo.setAlignmentX(JComponent.CENTER_ALIGNMENT);
							wPanelCohorteLogo.add(wlabelCohorteLogo);

							pPreferencesNorthPanel.add(wPanelCohorteLogo);

						}
					}
					{
						JPanel pPreferencesSouthPanel = new JPanel();
						pPreferencesPanel.add(pPreferencesSouthPanel, BorderLayout.SOUTH);
						pPreferencesSouthPanel.setLayout(new BoxLayout(pPreferencesSouthPanel,
								BoxLayout.Y_AXIS));
						pPreferencesSouthPanel.setBackground(Color.WHITE);

						{
							ImageIcon wIconLig = new ImageIcon(
									CFrameMain.class
											.getResource("/org/psem2m/isolates/ui/admin/impl/LIG_masque.png"));

							// 688 x 263 => 344 x 131
							wIconLig = new ImageIcon(wIconLig.getImage().getScaledInstance(344,
									131, Image.SCALE_SMOOTH));

							ImageIcon wIconErods = new ImageIcon(
									CFrameMain.class
											.getResource("/org/psem2m/isolates/ui/admin/impl/ERODS.png"));

							ImageIcon wIconIsandla = new ImageIcon(
									CFrameMain.class
											.getResource("/org/psem2m/isolates/ui/admin/impl/isandlaTech logo masque.png"));
							// 804 x 119 => 402 x 59
							wIconIsandla = new ImageIcon(wIconIsandla.getImage().getScaledInstance(
									402, 59, Image.SCALE_SMOOTH));

							JPanel wSouthPanelLogos = new JPanel();
							pPreferencesSouthPanel.add(wSouthPanelLogos);
							wSouthPanelLogos.setLayout(new BorderLayout(0, 0));

							JLabel wLabelLogoLig = new JLabel();
							wLabelLogoLig.setIcon(wIconLig);

							JPanel wPanelLogoLig = new JPanel();
							wPanelLogoLig.setAlignmentX(JComponent.CENTER_ALIGNMENT);
							wPanelLogoLig.add(wLabelLogoLig);

							wSouthPanelLogos.add(wPanelLogoLig, BorderLayout.NORTH);

							JLabel wLabelErodsUrl = new JLabel();
							wLabelErodsUrl.setIcon(wIconErods);

							JPanel wPanelErodsUrl = new JPanel();
							wPanelErodsUrl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
							wPanelErodsUrl.add(wLabelErodsUrl);

							wSouthPanelLogos.add(wPanelErodsUrl, BorderLayout.CENTER);

							JLabel wLabelLogoIsandlaTech = new JLabel();
							wLabelLogoIsandlaTech.setIcon(wIconIsandla);

							JPanel wPanelLogoIsandlaTech = new JPanel();
							wPanelLogoIsandlaTech.setAlignmentX(JComponent.CENTER_ALIGNMENT);
							wPanelLogoIsandlaTech.add(wLabelLogoIsandlaTech);

							wSouthPanelLogos.add(wPanelLogoIsandlaTech, BorderLayout.SOUTH);
						}
					}
				}
			}

			pack();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param aCUiAdminPanel
	 * @throws Exception
	 */
	void removeUiAdminPanel(final IUiAdminPanel aCUiAdminPanel) {

		final String panelName = aCUiAdminPanel.getName();
		final int wMax = pMainTabbedPane.getTabCount();
		for (int wI = 0; wI < wMax; wI++) {
			if (pMainTabbedPane.getTitleAt(wI).equals(panelName)) {
				pMainTabbedPane.removeTabAt(wI);
				break;
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
			final int wPos = aIsolateName.lastIndexOf('.');
			wId = wPos > -1 && wPos + 1 < aIsolateName.length() - 1 ? aIsolateName
					.substring(wPos + 1) : aIsolateName;
		}

		super.setTitle(String.format(FRAME_TITLE_FORMAT, wId));
	}

}
