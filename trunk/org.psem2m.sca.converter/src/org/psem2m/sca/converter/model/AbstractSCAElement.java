/**
 * File:   AbstractSCAElement.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.model;

import org.psem2m.sca.converter.core.QName;
import org.w3c.dom.Element;

/**
 * Abstract class for SCA model element
 * 
 * @author Thomas Calmant
 */
public abstract class AbstractSCAElement<T> {

    /** Indentation */
    public static final String PREFIX_INDENT = "  ";

    /** The element container */
    protected IElementContainer pContainer;

    /** The element qualified name */
    protected QName pQName;

    /** The XML element representing the SCA element */
    protected Element pXmlElement;

    /** Clones the current object */
    public abstract T duplicate();

    /**
     * Retrieves the complete alias of the current element
     * 
     * @return The complete alias
     */
    public String getCompleteAlias() {

        final StringBuilder builder = new StringBuilder();
        getCompleteAlias(builder);
        return builder.toString();
    }

    /**
     * Prepares the complete name of the current element, using aliases if
     * possible
     * 
     * @param aBuilder
     *            The complete name string builder
     */
    private void getCompleteAlias(final StringBuilder aBuilder) {

        // Make parent name
        if (pContainer instanceof AbstractSCAElement) {
            ((AbstractSCAElement<?>) pContainer).getCompleteAlias(aBuilder);
            aBuilder.append('.');
        }

        // Get the alias
        final String alias;
        if (this instanceof IAlias) {
            alias = ((IAlias) this).getAlias();

        } else {
            alias = pQName.getLocalNameLastPart();
        }

        if (alias != null) {
            aBuilder.append(alias);

        } else {
            // Can't tell
            aBuilder.append("<null>");
        }
    }

    /**
     * Retrieves the complete name of the current element
     * 
     * @return The complete name
     */
    public String getCompleteName() {

        final StringBuilder builder = new StringBuilder();
        getCompleteName(builder);
        return builder.toString();
    }

    /**
     * Fills the builder with the complete name of the element
     * 
     * @param aBuilder
     *            A string builder
     */
    private void getCompleteName(final StringBuilder aBuilder) {

        if (pContainer instanceof AbstractSCAElement) {
            ((AbstractSCAElement<?>) pContainer).getCompleteName(aBuilder);
            aBuilder.append('.');
        }

        final String localName = pQName.getLocalNameLastPart();
        if (localName != null) {
            aBuilder.append(localName);

        } else {
            aBuilder.append("<null>");
        }
    }

    /**
     * Retrieves the element container
     * 
     * @return the element container
     */
    public IElementContainer getContainer() {

        return pContainer;
    }

    /**
     * Retrieves the qualified name of this element
     * 
     * @return A qualified name, or null
     */
    public QName getQualifiedName() {

        return pQName;
    }

    /**
     * Retrieves the XML attribute value of this SCA element.
     * 
     * Returns null if the element as no XML information.
     * 
     * @param aAttributeName
     *            The XML attribute name
     * @return The attribute value
     */
    public String getXmlAttribute(final String aAttributeName) {

        if (pXmlElement == null) {
            return null;
        }

        return pXmlElement.getAttribute(aAttributeName);
    }

    /**
     * Retrieves the XML attribute value of this SCA element
     * 
     * Returns null if the element as no XML information.
     * 
     * @param aNamespace
     *            The XML attribute name space
     * @param aAttributeName
     *            The XML attribute name
     * @return The attribute value
     */
    public String getXmlAttributeNS(final String aNamespace,
            final String aAttributeName) {

        if (pXmlElement == null) {
            return null;
        }

        return pXmlElement.getAttributeNS(aNamespace, aAttributeName);
    }

    /**
     * Retrieves the XML element used to load this SCA element
     * 
     * @return The XML element representing this SCA node
     */
    public Element getXmlElement() {

        return pXmlElement;
    }

    /**
     * Sets the element container
     * 
     * @param aContainer
     *            A container
     */
    protected void setContainer(final IElementContainer aContainer) {

        pContainer = aContainer;
    }

    /**
     * Sets the qualified name of this object
     * 
     * @param aQualifiedName
     *            A qualified name
     */
    public void setQualifiedName(final QName aQualifiedName) {

        pQName = aQualifiedName;
    }

    /**
     * Sets the XML element representing the SCA element
     * 
     * @param aXmlElement
     *            A XML DOM element
     */
    public void setXmlElement(final Element aXmlElement) {

        pXmlElement = aXmlElement;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();
        toString(builder, "");
        return builder.toString();
    }

    /**
     * Writes a representation of the element in the given string builder
     * 
     * @param aBuilder
     *            A string builder
     * @param aPrefix
     *            A prefix for each line
     */
    public abstract void toString(final StringBuilder aBuilder,
            final String aPrefix);
}
