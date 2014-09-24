/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz  (isandlaTech) - 2 févr. 2012 - initial API and implementation
 *******************************************************************************/
package org.psem2m.utilities.logging;

import java.util.logging.Level;

/**
 * @author ogattaz
 * 
 */
public class CActivityLevel extends Level {

    // same as Level.FINE
    public static final Level DEBUG = new CActivityLevel("DEBUG",
            Level.FINE.intValue());

    // same as Level.SEVERE
    public static final Level ERROR = new CActivityLevel("ERROR",
            Level.SEVERE.intValue());

    // all the known levels

    // Level.ALL => "ALL", Integer.MIN_VALUE
    // Same as Level.SEVERE
    // Level.FINEST => "FINEST", 300
    // Level.FINER => "FINER", 400
    // Level.FINE => "FINE", 500
    // CActivityLevel.DEBUG => "DEBUG", 500
    // Level.CONFIG => "CONFIG", 700,
    // Level.INFO => "INFO", 800
    // Level.WARNING => "WARNING", 900
    // Level.SEVERE => "SEVERE", 1000
    // Level.ERROR => "ERROR", 1000
    // Level.OFF => "OFF", Integer.MAX_VALUE

    private static final Level[] LEVELS = { CActivityLevel.ALL,
            CActivityLevel.FINEST, CActivityLevel.FINER, CActivityLevel.FINE,
            CActivityLevel.DEBUG, CActivityLevel.INFO, CActivityLevel.WARNING,
            CActivityLevel.CONFIG, CActivityLevel.SEVERE, CActivityLevel.ERROR,
            CActivityLevel.OFF };

    // generated serial Version UID
    private static final long serialVersionUID = -6159335203765378024L;

    /**
     * @return
     */
    public static Level[] getSortedLevels() {

        return LEVELS;
    }

    /**
     * @param name
     * @param value
     */
    protected CActivityLevel(String name, int value) {

        super(name, value);
    }
}
