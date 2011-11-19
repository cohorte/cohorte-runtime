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

import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.IXDescriber;

/**
 * <p> http://www.w3schools.com/html/html_colornames.asp</p>
 * 
 * 
 * 
 * @author ogattaz
 * 
 */

public enum EHtmlColor implements IXDescriber {
    Azure("0xF0FFFF"), Beige("0xF5F5DC"), Cornsilk("0xFFF8DC"), FloralWhite(
            "0xFFFAF0"), GhostWhite("0xF8F8FF"), LightGoldenRodYellow(
            "0xFAFAD2"), Linen("FAF0E6"), Moccasin("0xFFE4B5"), SeaShell(
            "0xFFF5EE"), SkyBlue("0x87CEEB"), White("0xFFFFFF"), YellowGreen(
            "0x9ACD32");

    // Azure #F0FFFF
    // Beige #F5F5DC
    // Cornsilk #FFF8DC
    // FloralWhite #FFFAF0
    // GhostWhite #F8F8FF
    // LightGoldenRodYellow #FAFAD2
    // Linen #FAF0E6
    // Moccasin #FFE4B5
    // SeaShell #FFF5EE
    // SkyBlue #87CEEB
    // White #FFFFFF
    // YellowGreen #9ACD32

    /**
     * Returns the EHtmlColor corresponding to the name or Black
     * 
     * @param aName
     *            the name of the html color
     * @return an instance of EHtmlColor
     */
    public static EHtmlColor getHtmlColor(final String aName) {

        if (aName == null || aName.isEmpty()) {
            return EHtmlColor.White;
        }
        String wName = aName.toLowerCase();

        EHtmlColor[] wEHtmlColors = EHtmlColor.values();

        for (EHtmlColor wEHtmlColor : wEHtmlColors) {
            if (wEHtmlColor.name().toLowerCase().equals(wName)) {
                return wEHtmlColor;
            }
        }
        return EHtmlColor.White;
    }

    /** **/
    private final String pHexa;

    /**
     * @param aHexa
     */
    EHtmlColor(final String aHexa) {

        pHexa = aHexa;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.utilities.IXDescriber#addDescriptionInBuffer(java.lang.Appendable
     * )
     */
    @Override
    public Appendable addDescriptionInBuffer(final Appendable aBuffer) {

        CXStringUtils.appendKeyValInBuff(aBuffer, "Name", name());
        CXStringUtils.appendKeyValInBuff(aBuffer, "Hexa", pHexa);
        CXStringUtils.appendKeyValInBuff(aBuffer, "RGB",
                Integer.toHexString(getColor().getRGB()));

        return aBuffer;
    }

    /**
     * @return
     */
    public Color getColor() {

        return Color.decode(getHexa());
    }

    /**
     * @return
     */
    public String getHexa() {

        return pHexa;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.IXDescriber#toDescription()
     */
    @Override
    public String toDescription() {

        return addDescriptionInBuffer(new StringBuilder(128)).toString();
    }

    /**
     * 
     * <p> the html color table availabe here :
     * http://www.w3schools.com/html/html_colornames.asp</p>
     * 
     * <ul> <li>AliceBlue #F0F8FF</li> <li>AntiqueWhite #FAEBD7</li> <li>Aqua
     * #00FFFF</li> <li>Aquamarine #7FFFD4</li> <li>Azure #F0FFFF</li> <li>Beige
     * #F5F5DC</li> <li>Bisque #FFE4C4</li> <li>Black #000000</li>
     * <li>BlanchedAlmond #FFEBCD</li> <li>Blue #0000FF</li> <li>BlueViolet
     * #8A2BE2</li> <li>Brown #A52A2A</li> <li>BurlyWood #DEB887</li>
     * <li>CadetBlue #5F9EA0</li> <li>Chartreuse #7FFF00</li> <li>Chocolate
     * #D2691E</li> <li>Coral #FF7F50</li> <li>CornflowerBlue #6495ED</li>
     * <li>Cornsilk #FFF8DC</li> <li>Crimson #DC143C</li> <li>Cyan #00FFFF</li>
     * <li>DarkBlue #00008B</li> <li>DarkCyan #008B8B</li> <li>DarkGoldenRod
     * #B8860B</li> <li>DarkGray #A9A9A9</li> <li>DarkGrey #A9A9A9</li>
     * <li>DarkGreen #006400</li> <li>DarkKhaki #BDB76B</li> <li>DarkMagenta
     * #8B008B</li> <li>DarkOliveGreen #556B2F</li> <li>Darkorange #FF8C00</li>
     * <li>DarkOrchid #9932CC</li> <li>DarkRed #8B0000</li> <li>DarkSalmon
     * #E9967A</li> <li>DarkSeaGreen #8FBC8F</li> <li>DarkSlateBlue #483D8B</li>
     * <li>DarkSlateGray #2F4F4F</li> <li>DarkSlateGrey #2F4F4F</li>
     * <li>DarkTurquoise #00CED1</li> <li>DarkViolet #9400D3</li> <li>DeepPink
     * #FF1493</li> <li>DeepSkyBlue #00BFFF</li> <li>DimGray #696969</li>
     * <li>DimGrey #696969</li> <li>DodgerBlue #1E90FF</li> <li>FireBrick
     * #B22222</li> <li>FloralWhite #FFFAF0</li> <li>ForestGreen #228B22</li>
     * <li>Fuchsia #FF00FF</li> <li>Gainsboro #DCDCDC</li> <li>GhostWhite
     * #F8F8FF</li> <li>Gold #FFD700</li> <li>GoldenRod #DAA520</li> <li>Gray
     * #808080</li> <li>Grey #808080</li> <li>Green #008000</li> <li>GreenYellow
     * #ADFF2F</li> <li>HoneyDew #F0FFF0</li> <li>HotPink #FF69B4</li>
     * <li>IndianRed #CD5C5C</li> <li>Indigo #4B0082</li> <li>Ivory #FFFFF0</li>
     * <li>Khaki #F0E68C</li> <li>Lavender #E6E6FA</li> <li>LavenderBlush
     * #FFF0F5</li> <li>LawnGreen #7CFC00</li> <li>LemonChiffon #FFFACD</li>
     * <li>LightBlue #ADD8E6</li> <li>LightCoral #F08080</li> <li>LightCyan
     * #E0FFFF</li> <li>LightGoldenRodYellow #FAFAD2</li> <li>LightGray
     * #D3D3D3</li> <li>LightGrey #D3D3D3</li> <li>LightGreen #90EE90</li>
     * <li>LightPink #FFB6C1</li> <li>LightSalmon #FFA07A</li> <li>LightSeaGreen
     * #20B2AA</li> <li>LightSkyBlue #87CEFA</li> <li>LightSlateGray
     * #778899</li> <li>LightSlateGrey #778899</li> <li>LightSteelBlue
     * #B0C4DE</li> <li>LightYellow #FFFFE0</li> <li>Lime #00FF00</li>
     * <li>LimeGreen #32CD32</li> <li>Linen #FAF0E6</li> <li>Magenta
     * #FF00FF</li> <li>Maroon #800000</li> <li>MediumAquaMarine #66CDAA</li>
     * <li>MediumBlue #0000CD</li> <li>MediumOrchid #BA55D3</li>
     * <li>MediumPurple #9370D8</li> <li>MediumSeaGreen #3CB371</li>
     * <li>MediumSlateBlue #7B68EE</li> <li>MediumSpringGreen #00FA9A</li>
     * <li>MediumTurquoise #48D1CC</li> <li>MediumVioletRed #C71585</li>
     * <li>MidnightBlue #191970</li> <li>MintCream #F5FFFA</li> <li>MistyRose
     * #FFE4E1</li> <li>Moccasin #FFE4B5</li> <li>NavajoWhite #FFDEAD</li>
     * <li>Navy #000080</li> <li>OldLace #FDF5E6</li> <li>Olive #808000</li>
     * <li>OliveDrab #6B8E23</li> <li>Orange #FFA500</li> <li>OrangeRed
     * #FF4500</li> <li>Orchid #DA70D6</li> <li>PaleGoldenRod #EEE8AA</li>
     * <li>PaleGreen #98FB98</li> <li>PaleTurquoise #AFEEEE</li>
     * <li>PaleVioletRed #D87093</li> <li>PapayaWhip #FFEFD5</li> <li>PeachPuff
     * #FFDAB9</li> <li>Peru #CD853F</li> <li>Pink #FFC0CB</li> <li>Plum
     * #DDA0DD</li> <li>PowderBlue #B0E0E6</li> <li>Purple #800080</li> <li>Red
     * #FF0000</li> <li>RosyBrown #BC8F8F</li> <li>RoyalBlue #4169E1</li>
     * <li>SaddleBrown #8B4513</li> <li>Salmon #FA8072</li> <li>SandyBrown
     * #F4A460</li> <li>SeaGreen #2E8B57</li> <li>SeaShell #FFF5EE</li>
     * <li>Sienna #A0522D</li> <li>Silver #C0C0C0</li> <li>SkyBlue #87CEEB</li>
     * <li>SlateBlue #6A5ACD</li> <li>SlateGray #708090</li> <li>SlateGrey
     * #708090</li> <li>Snow #FFFAFA</li> <li>SpringGreen #00FF7F</li>
     * <li>SteelBlue #4682B4</li> <li>Tan #D2B48C</li> <li>Teal #008080</li>
     * <li>Thistle #D8BFD8</li> <li>Tomato #FF6347</li> <li>Turquoise
     * #40E0D0</li> <li>Violet #EE82EE</li> <li>Wheat #F5DEB3</li> <li>White
     * #FFFFFF</li> <li>WhiteSmoke #F5F5F5</li> <li>Yellow #FFFF00</li>
     * <li>YellowGreen #9ACD32</li> </ul>
     * 
     */
}
