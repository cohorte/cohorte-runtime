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

import org.osgi.framework.BundleContext;
import org.psem2m.utilities.CXObjectBase;
import org.psem2m.utilities.CXStringUtils;

/**
 * @author ogattaz
 * 
 */
public class CFrameMainConfig extends CXObjectBase {

    private final static String COLOR = "psem2m.demo.ui.viewer.color";

    private final static String DIM_HEIGHT = "psem2m.demo.ui.viewer.height";

    private final static String DIM_LEFT = "psem2m.demo.ui.viewer.left";

    private final static String DIM_TOP = "psem2m.demo.ui.viewer.top";

    private final static String DIM_WIDTH = "psem2m.demo.ui.viewer.width";

    /** The bundle context (to access properties) */
    private final BundleContext pContext;

    private CFrameSizeValue pHeight;

    private EHtmlColor pHtmlColor;

    private CFrameSizeValue pLeft;

    private CFrameSizeValue pTop;

    private CFrameSizeValue pWidth;

    /**
     * 
     */
    public CFrameMainConfig(final BundleContext aContext) {

        super();
        pContext = aContext;
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
     * Retrieves the value of the given property from the framework or the
     * system.
     * 
     * @param aKey
     *            Property name
     * @return Property value (or null)
     */
    private String getProperty(final String aKey) {

        String value = null;

        if (pContext != null) {
            // Try the framework property
            value = pContext.getProperty(aKey);
        }

        if (value == null) {
            // Try the system property
            value = System.getProperty(aKey);
        }

        return value;
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
                getProperty(DIM_HEIGHT)));
        setWidth(new CFrameSizeValue(EFrameSize.WIDTH, getProperty(DIM_WIDTH)));
        setTop(new CFrameSizeValue(EFrameSize.TOP, getProperty(DIM_TOP)));
        setLeft(new CFrameSizeValue(EFrameSize.LEFT, getProperty(DIM_LEFT)));

        setHtmlColor(EHtmlColor.getHtmlColor(getProperty(COLOR)));

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
