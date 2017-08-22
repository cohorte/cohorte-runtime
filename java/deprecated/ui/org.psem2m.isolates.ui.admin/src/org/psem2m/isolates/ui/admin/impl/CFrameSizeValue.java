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

        final String wValue = aValue.indexOf(',') > -1 ? aValue.replace(',',
                '.') : aValue;

        final int wPosSuffixScreen = wValue.indexOf(UNIT_SCREEN);
        if (wPosSuffixScreen > -1) {
            return new Double(getSizeRef(aDimension)
                    * Double.parseDouble(wValue.substring(0, wPosSuffixScreen)))
                    .intValue();
        }
        final int wPosSuffixPixels = wValue.indexOf(UNIT_PIXELS);
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
