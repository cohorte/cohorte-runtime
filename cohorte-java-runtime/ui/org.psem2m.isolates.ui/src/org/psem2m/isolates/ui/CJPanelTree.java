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

package org.psem2m.isolates.ui;

import java.awt.Font;

import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.ui.admin.api.CJPanel;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;

/**
 * @author ogattaz
 *
 */
public abstract class CJPanelTree extends CJPanel {

    private static final long serialVersionUID = 2090135987172885272L;

    /**
     * Explicit default constructor
     */
    public CJPanelTree() {

        super();
    }

    /**
     * @param aLogger
     */
    public CJPanelTree(final IIsolateLoggerSvc aLogger) {

        super(aLogger);

    }

    /**
     * @param aFont
     * @return
     */
    public abstract Font setTreeFont(final EUiAdminFont aFont);

}
