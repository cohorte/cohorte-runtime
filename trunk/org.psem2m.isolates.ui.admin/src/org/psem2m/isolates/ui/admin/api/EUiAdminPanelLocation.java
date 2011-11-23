/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz  (isandlaTech) - 20 nov. 2011 - initial API and implementation
 *******************************************************************************/
package org.psem2m.isolates.ui.admin.api;

/**
 * @author ogattaz
 * 
 */
public enum EUiAdminPanelLocation {
    ASCENDING, DESCENDING, FIRST, LAST;

    public boolean is(final EUiAdminPanelLocation aLocation) {

        return this == aLocation;
    }

    public boolean isFIRST() {

        return is(FIRST);
    }

    public boolean isLAST() {

        return is(LAST);
    }
}
