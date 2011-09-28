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
package org.psem2m.isolates.demo.services.ui.viewer.impl;

/**
 * @author ogattaz
 * 
 */
public enum EFrameSize {
    BOTTOM(EDimension.VERTICAL), HEIGHT(EDimension.VERTICAL), LEFT(
            EDimension.HORIZONTAL), RIGHT(EDimension.HORIZONTAL), TOP(
            EDimension.VERTICAL), WIDTH(EDimension.HORIZONTAL);
    /**
     * 
     */
    private EDimension pDimensionSense;

    EFrameSize(final EDimension aDimensionSense) {

        pDimensionSense = aDimensionSense;
    }

    /**
     * @return true if the FramSize is BOTTOM
     */
    public boolean isBottom() {

        return this == BOTTOM;
    }

    /**
     * @return true if the FramSize is HEIGHT
     */
    public boolean isHeight() {

        return this == HEIGHT;
    }

    /**
     * @return true if the dimension of this FramSize is horizontal
     */
    public boolean isHorizontally() {

        return pDimensionSense.isHorizontal();
    }

    /**
     * @return true if the FramSize is LEFT
     */
    public boolean isLeft() {

        return this == LEFT;
    }

    /**
     * @return true if the FramSize is RIGHT
     */
    public boolean isRight() {

        return this == RIGHT;
    }

    /**
     * @return true if the FramSize is TOP
     */
    public boolean isTop() {

        return this == TOP;
    }

    /**
     * @return true if the dimension of this FramSize is vertical
     */
    public boolean isVertically() {

        return pDimensionSense.isVertical();
    }

    /**
     * @return true if the FramSize is WIDTH
     */
    public boolean isWidth() {

        return this == WIDTH;
    }
}
