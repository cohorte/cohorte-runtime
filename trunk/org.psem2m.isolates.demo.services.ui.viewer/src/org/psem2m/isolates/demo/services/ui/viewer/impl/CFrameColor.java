package org.psem2m.isolates.demo.services.ui.viewer.impl;

import java.awt.Color;

public class CFrameColor {

    // PaleGoldenRod #EEE8AA
    public static CFrameColor toto = new CFrameColor("");

    // Color bColor = Color.decode("FF0096");

    private final Color pColor;

    public CFrameColor(final String aValue) {

        super();
        pColor = calcColor(aValue);
    }

    /**
     * @param aValue
     * @return
     */
    private Color calcColor(final String aValue) {

        return Color.decode(aValue);
    }

    /**
     * @return
     */
    Color getColor() {

        return pColor;
    }

}
