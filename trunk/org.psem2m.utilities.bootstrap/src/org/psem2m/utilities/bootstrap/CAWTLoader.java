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
package org.psem2m.utilities.bootstrap;

import java.awt.Color;

/**
 * To load the AWT jni library : "libawt.jnilib"
 * 
 * @author ogattaz
 * 
 */
public class CAWTLoader {

    /**
     * @return
     */
    public int getBlackRgb() {

        return Color.BLACK.getRGB();
    }
}
