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

import java.awt.Dimension;
import java.awt.Toolkit;

import org.psem2m.utilities.CXObjectBase;
import org.psem2m.utilities.CXStringUtils;

/**
 * represent a size and its value usable to set a Frame (eg. top, left,
 * width,...)
 * 
 * @author ogattaz
 * 
 */
public class CFrameSizeValue extends CXObjectBase {

    private final static int DEFAULT_HEIGHT = 500;

    private final static int DEFAULT_WIDTH = 550;

    private final static Dimension sScreenSize = Toolkit.getDefaultToolkit()
            .getScreenSize();

    private final static String UNIT_PIXELS = "px";

    private final static String UNIT_SCREEN = "scr";

    private final EFrameSize pDimension;
    private final int pPixels;
    private final String pValue;

    /**
     * @param aFrameSize
     * @param aValue
     */
    public CFrameSizeValue(final EFrameSize aFrameSize, final String aValue) {

        super();
        pDimension = aFrameSize;
        pValue = aValue;
        pPixels = calcPixels(aFrameSize, aValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.utilities.CXObjectBase#addDescriptionInBuffer(java.lang.Appendable
     * )
     */
    @Override
    public Appendable addDescriptionInBuffer(final Appendable aBuffer) {

        CXStringUtils.appendKeyValInBuff(aBuffer, "Dim", pDimension.name());
        CXStringUtils.appendKeyValInBuff(aBuffer, "Pixels", getPixels());
        CXStringUtils.appendKeyValInBuff(aBuffer, "Value", getValue());
        return aBuffer;
    }

    /**
     * @param aDimension
     * @param aValue
     */
    private int calcPixels(final EFrameSize aDimension, final String aValue) {

        if (aValue == null || aValue.isEmpty()) {
            return getDefaultPixel(aDimension);
        }

        String wValue = aValue.indexOf(',') > -1 ? aValue.replace(',', '.')
                : aValue;

        int wPosSuffixScreen = wValue.indexOf(UNIT_SCREEN);
        if (wPosSuffixScreen > -1) {
            return new Double(getSizeRef(aDimension)
                    * Double.parseDouble(wValue.substring(0, wPosSuffixScreen)))
                    .intValue();
        }
        int wPosSuffixPixels = wValue.indexOf(UNIT_PIXELS);
        if (wPosSuffixPixels > -1) {
            return Integer.parseInt(wValue.substring(0, wPosSuffixPixels));
        }
        return getDefaultPixel(aDimension);

    }

    /**
     * @param aDimension
     * @return
     */
    private int getDefaultPixel(final EFrameSize aDimension) {

        if (aDimension.isLeft() || aDimension.isRight()) {
            return (getSizeRef(aDimension) - DEFAULT_WIDTH) / 2;
        }

        if (aDimension.isBottom() || aDimension.isTop()) {
            return (getSizeRef(aDimension) - DEFAULT_HEIGHT) / 2;
        }

        if (aDimension.isHeight()) {
            return DEFAULT_HEIGHT;
        }
        if (aDimension.isWidth()) {
            return DEFAULT_WIDTH;
        }
        return 0;
    }

    /**
     * @return
     */
    public int getPixels() {

        return pPixels;
    }

    /**
     * @param aDimension
     * @return
     */
    private int getSizeRef(final EFrameSize aDimension) {

        return aDimension.isHorizontally() ? sScreenSize.width
                : sScreenSize.height;
    }

    /**
     * @return
     */
    public String getValue() {

        return pValue;
    }
}
