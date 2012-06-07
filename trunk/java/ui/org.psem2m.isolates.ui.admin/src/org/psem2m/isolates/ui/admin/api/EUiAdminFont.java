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

import java.awt.Font;

/**
 * @author ogattaz
 * 
 */
public enum EUiAdminFont {
    LARGE("large", 16), NORMAL("normal", 12), SMALL("small", 10);

    /**
     * @param aLib
     * @return
     */
    public static EUiAdminFont fontFromLib(final String aLib) {

        for (final EUiAdminFont wKind : values()) {
            if (wKind.pLib.equals(aLib)) {
                return wKind;
            }
        }
        return null;
    }

    /**
     * @return
     */
    public static String[] getLibs() {

        final String[] wLibs = new String[values().length];
        int wI = 0;
        for (final EUiAdminFont wKind : values()) {
            wLibs[wI] = wKind.getLib();
            wI++;
        }
        return wLibs;
    }

    private final String pLib;
    private final int pSize;
    private final Font pTableFont;
    private final Font pTextFont;

    /**
     * @param aLib
     */
    EUiAdminFont(final String aLib, final int aSize) {

        pLib = aLib;
        pSize = aSize;

        /**
         * The Java Platform defines five logical font names that every
         * implementation must support: Serif, SansSerif, Monospaced, Dialog,
         * and DialogInput. These logical font names are mapped to physical
         * fonts in implementation dependent ways.
         * 
         * @see http 
         *      ://docs.oracle.com/javase/6/docs/technotes/guides/intl/fontconfig
         *      .html
         * 
         * @see http
         *      ://stackoverflow.com/questions/221568/swt-os-agnostic-way-to-
         *      get-monospaced-font
         */
        pTableFont = new Font(Font.SANS_SERIF, Font.PLAIN, getSize());
        pTextFont = new Font(Font.MONOSPACED, Font.PLAIN, getSize());
    }

    /**
     * @return
     */
    public String getLib() {

        return pLib;
    }

    /**
     * @return
     */
    public int getSize() {

        return pSize;
    }

    /**
     * @return
     */
    public Font getTableFont() {

        return pTableFont;
    }

    /**
     * @return
     */
    public Font getTextFont() {

        return pTextFont;
    }

    /**
     * @param aFont
     * @return
     */
    public boolean is(final EUiAdminFont aFont) {

        return this == aFont;
    }
}
