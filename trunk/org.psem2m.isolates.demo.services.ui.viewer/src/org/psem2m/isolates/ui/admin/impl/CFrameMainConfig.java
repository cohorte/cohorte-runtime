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

import java.awt.Color;
import java.awt.Dimension;

import org.psem2m.utilities.CXObjectBase;
import org.psem2m.utilities.CXStringUtils;

/**
 * @author ogattaz
 * 
 */
public class CFrameMainConfig extends CXObjectBase {

    public final static String COLOR = "psem2m.demo.ui.viewer.color";
    public final static String DIM_HEIGHT = "psem2m.demo.ui.viewer.height";
    public final static String DIM_LEFT = "psem2m.demo.ui.viewer.left";
    public final static String DIM_TOP = "psem2m.demo.ui.viewer.top";
    public final static String DIM_WIDTH = "psem2m.demo.ui.viewer.width";

    private CFrameSizeValue pHeight;
    private EHtmlColor pHtmlColor;
    private CFrameSizeValue pLeft;
    private CFrameSizeValue pTop;
    private CFrameSizeValue pWidth;

    /**
     * 
     */
    public CFrameMainConfig() {

        super();
        init();
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

        CXStringUtils.appendIXDescriberInBuff(aBuffer, pTop);
        CXStringUtils.appendIXDescriberInBuff(aBuffer, pLeft);
        CXStringUtils.appendIXDescriberInBuff(aBuffer, pHeight);
        CXStringUtils.appendIXDescriberInBuff(aBuffer, pWidth);
        CXStringUtils.appendIXDescriberInBuff(aBuffer, pHtmlColor);
        return aBuffer;
    }

    /**
     * @return
     */
    public Color getColor() {

        return pHtmlColor.getColor();
    }

    /**
     * @return
     */
    public int getHeight() {

        return pHeight.getPixels();
    }

    /**
     * @return
     */
    public int getLeft() {

        return pLeft.getPixels();
    }

    /**
     * @return
     */
    public int getTop() {

        return pTop.getPixels();
    }

    /**
     * @return
     */
    public int getWidth() {

        return pWidth.getPixels();
    }

    /**
     * @return
     */
    public Dimension getWidthHeight() {

        return new java.awt.Dimension(getWidth(), getHeight());
    }

    /**
     * 
     */
    private void init() {

        setHeight(new CFrameSizeValue(EFrameSize.HEIGHT,
                System.getProperty(DIM_HEIGHT)));

        setWidth(new CFrameSizeValue(EFrameSize.WIDTH,
                System.getProperty(DIM_WIDTH)));

        setTop(new CFrameSizeValue(EFrameSize.TOP, System.getProperty(DIM_TOP)));

        setLeft(new CFrameSizeValue(EFrameSize.LEFT,
                System.getProperty(DIM_LEFT)));

        setHtmlColor(EHtmlColor.getHtmlColor(System.getProperty(COLOR)));

    }

    /**
     * @param aHeight
     */
    private void setHeight(final CFrameSizeValue aHeight) {

        this.pHeight = aHeight;
    }

    /**
     * @param aColor
     */
    private void setHtmlColor(final EHtmlColor aColor) {

        pHtmlColor = aColor;
    }

    /**
     * @param aLeft
     */
    private void setLeft(final CFrameSizeValue aLeft) {

        this.pLeft = aLeft;
    }

    /**
     * @param aTop
     */
    private void setTop(final CFrameSizeValue aTop) {

        this.pTop = aTop;
    }

    /**
     * @param aWidth
     */
    private void setWidth(final CFrameSizeValue aWidth) {

        this.pWidth = aWidth;
    }

}
