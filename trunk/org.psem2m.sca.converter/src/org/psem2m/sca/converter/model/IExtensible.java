/**
 * File:   IExtensible.java
 * Author: Thomas Calmant
 * Date:   12 janv. 2012
 */
package org.psem2m.sca.converter.model;

import org.w3c.dom.Element;

/**
 * Represents a SCA element that can be extended (Binding, Implementation or
 * Interface)
 * 
 * @author Thomas Calmant
 */
public interface IExtensible {

    /**
     * Returns the kind of extension, i.e. the tag name
     * 
     * @return the kind of extension
     */
    String getKind();

    /**
     * Retrieves the XML DOM element corresponding to this SCA element, to
     * access extension specific values
     * 
     * @return The DOM element of this SCA element
     */
    Element getXmlElement();
}
