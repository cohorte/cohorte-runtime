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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.ui.admin.api.CJPanel;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;

/**
 * @author ogattaz
 *
 */
public class CJPanelConfiguration extends CJPanel {

    private static final long serialVersionUID = -5537324836085851473L;

    private JTextArea pConfigTextArea;

    private JScrollPane pConfigurationScrollPane;

    /**
     *
     */
    public CJPanelConfiguration() {

        super();
        newGUI();
    }

    /**
     * @param aLogger
     */
    public CJPanelConfiguration(final IIsolateLoggerSvc aLogger,
            final JPanel aPanel) {

        super(aLogger);
        aPanel.setLayout(new BorderLayout(0, 0));
        aPanel.add(newGUI(), BorderLayout.CENTER);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.ui.admin.panels.CJPanel#destroy()
     */
    @Override
    public void destroy() {

        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.ui.admin.panels.CJPanel#newGUI()
     */
    @Override
    public JPanel newGUI() {

        setLayout(new BorderLayout(0, 0));
        {
            pConfigurationScrollPane = new JScrollPane();
            this.add(pConfigurationScrollPane, BorderLayout.CENTER);
            {
                pConfigTextArea = new JTextArea();
                setTextFont(EUiAdminFont.NORMAL);

                setText("Config ...");
                pConfigurationScrollPane.setViewportView(pConfigTextArea);
            }
        }

        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.isolates.ui.admin.panels.CJPanel#setText(java.lang.String)
     */
    @Override
    public void setText(final String aText) {

        pConfigTextArea.setText(aText);
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

        pConfigTextArea.setFont(aUiAdminFont.getTextFont());
        return aUiAdminFont.getTextFont();
    }
}
