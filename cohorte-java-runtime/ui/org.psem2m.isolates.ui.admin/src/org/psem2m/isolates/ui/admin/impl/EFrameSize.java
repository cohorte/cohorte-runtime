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
