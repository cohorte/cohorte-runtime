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

package org.psem2m.isolates.ui.admin.api;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * @author ogattaz
 *
 */
public interface IUiAdminPanel {

    /**
     * @return
     */
    public IUiAdminPanelControler getControler();

    /**
     * @return
     */
    public Icon getIcon();

    /**
     * @return
     */
    public String getName();

    /**
     * @return
     */
    public JPanel getPanel();

    /**
     * @return
     */
    public String getTip();

    /**
     * @return
     */
    public boolean hasControler();

    /**
     * @return
     */
    public boolean hasPanel();

    /**
     *
     */
    public void pack();

}
