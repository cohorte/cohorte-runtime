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
public enum EDimension {
    HORIZONTAL, VERTICAL;

    /**
     * @return true if this dimension is horizontal
     */
    public boolean isHorizontal() {

        return this == HORIZONTAL;
    }

    /**
     * @return true if this dimension is vertical
     */
    public boolean isVertical() {

        return this == VERTICAL;
    }

}
