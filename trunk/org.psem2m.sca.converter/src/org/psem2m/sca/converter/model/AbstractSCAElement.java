/**
 * File:   AbstractSCAElement.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.model;

import org.w3c.dom.Element;

/**
 * Abstract class for SCA model element
 * 
 * @author Thomas Calmant
 */
public abstract class AbstractSCAElement {

    /** Indentation */
    public static final String PREFIX_INDENT = "  ";

    /** The element container */
    protected IReferenceContainer pContainer;

    /** The XML element representing the SCA element */
    protected Element pXmlElement;

    /**
     * Top level implementation of the duplication of an SCA element
     * 
     * This method should be overridden by children, and called using
     * <em>super</em>.
     * 
     * @return The new SCA element
     */
    public AbstractSCAElement duplicate() {

        final AbstractSCAElement copy = newInstance();

        // Copy container reference
        copy.pContainer = pContainer;

        // Copy XML element reference
        copy.pXmlElement = pXmlElement;

        return copy;
    }

    /**
     * Retrieves the container of this SCA element
     * 
     * @return the container
     */
    public IReferenceContainer getContainer() {

        return pContainer;
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
     * Creates a new instance of an object of the same type than this one
     * 
     * @return
     */
    protected AbstractSCAElement newInstance() {

        try {
            return getClass().getConstructor((Class<?>[]) null).newInstance(
                    (Object[]) null);

        } catch (final Exception e) {
            e.printStackTrace();
        }

        // Return null on error...
        return null;
    }

    /**
     * Sets the element container
     * 
     * @param aContainer
     *            A container
     */
    protected void setContainer(final IReferenceContainer aContainer) {

        pContainer = aContainer;
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
