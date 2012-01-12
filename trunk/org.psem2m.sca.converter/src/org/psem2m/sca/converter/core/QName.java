/**
 * File:   QName.java
 * Author: Thomas Calmant
 * Date:   10 janv. 2012
 */
package org.psem2m.sca.converter.core;

import java.net.MalformedURLException;

import org.w3c.dom.Document;

/**
 * Represents a qualified name
 * 
 * @author Thomas Calmant
 */
public class QName {

    /** The local name */
    private String pLocalName;

    /** The name space */
    private String pNamespace;

    /** The parent name */
    private QName pParent;

    /**
     * @param aXmlDocument
     *            A XML document
     * @param aUri
     *            An URI
     * @throws MalformedURLException
     *             The given URI can't be split correctly
     */
    public QName(final Document aXmlDocument, final String aUri)
            throws MalformedURLException {

        // Split the URI
        final String[] parts = aUri.split(":");

        if (parts.length == 1) {
            // No prefix, use target name space
            pLocalName = aUri;
            pNamespace = aXmlDocument.getDocumentElement().getAttribute(
                    "targetNamespace");

        } else if (parts.length == 2) {
            // Prefix found
            pNamespace = aXmlDocument.lookupNamespaceURI(parts[0]);
            pLocalName = parts[1];

        } else {
            throw new MalformedURLException("Malformed URL : " + aUri);
        }
    }

    /**
     * Sets up a qualified name
     * 
     * @param aNamespace
     *            A full name space URI
     * @param aName
     *            A local name
     */
    public QName(final String aNamespace, final String aName) {

        pLocalName = aName;
        pNamespace = aNamespace;
    }

    /**
     * Creates a new QName with the same name space as this one and the given
     * local name
     * 
     * @param aLocalName
     *            A local name
     * @return A new qualified name
     */
    public QName createNSQName(final CharSequence aLocalName) {

        return new QName(pNamespace, aLocalName != null ? aLocalName.toString()
                : null);
    }

    /**
     * Creates a new QName with the same name space as this one and appends the
     * given name to the local name
     * 
     * @param aName
     *            A local name to append
     * @return A new qualified name
     */
    public QName createSubQName(final CharSequence aName) {

        final StringBuilder localName = new StringBuilder();
        if (pLocalName != null) {
            localName.append(pLocalName);
            localName.append('/');
        }
        localName.append(aName);

        final QName result = new QName(pNamespace, localName.toString());
        result.pParent = this;
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object aObj) {

        if (aObj instanceof QName) {
            // Field comparison
            final QName other = (QName) aObj;

            if (!equalsOrNull(pNamespace, other.pNamespace)) {
                // Bad name space
                return false;
            }

            if (equalsOrNull(pLocalName, other.pLocalName)) {
                // Same name : OK
                return true;
            }

            // Compare relative names
            if (equalsOrNull(pLocalName, other.getParentRelativeName())) {
                return true;
            }

            if (equalsOrNull(getParentRelativeName(), other.pLocalName)) {
                return true;
            }

        } else if (aObj instanceof CharSequence) {
            // String form comparison
            return aObj.equals(toString());
        }

        return false;
    }

    /**
     * Tests if both objects are null or equals
     * 
     * @param aObjA
     *            An object
     * @param aObjB
     *            Another object
     * @return True if objects are equals or both null
     */
    protected boolean equalsOrNull(final Object aObjA, final Object aObjB) {

        if (aObjA == null) {
            return aObjB == null;
        }

        return aObjA.equals(aObjB);
    }

    /**
     * @return the local name
     */
    public String getLocalName() {

        return pLocalName;
    }

    /**
     * Retrieves the last part of the local name (after the last '/'). Returns
     * null if the local name is null.
     * 
     * @return The last part of the local name, or null
     */
    public String getLocalNameLastPart() {

        if (pLocalName == null) {
            return null;
        }

        final int index = pLocalName.lastIndexOf('/');
        if (index == -1) {
            // Can't split
            return pLocalName;
        }

        return pLocalName.substring(index + 1);
    }

    /**
     * @return the name space
     */
    public String getNamespace() {

        return pNamespace;
    }

    /**
     * Retrieves the parent qualified name
     * 
     * @return The parent name
     */
    public QName getParentName() {

        return pParent;
    }

    /**
     * Retrieves the part of the local name without the parent name
     * 
     * @return The parent relative name
     */
    protected String getParentRelativeName() {

        if (pParent == null) {
            // Can't work
            return pLocalName;
        }

        if (!pLocalName.startsWith(pParent.pLocalName)) {
            // Should not happen...
            return pLocalName;
        }

        final String relativeName = pLocalName.substring(pParent.pLocalName
                .length());

        if (relativeName.startsWith("/")) {
            // Remove starting slash
            return relativeName.substring(1);
        }

        return relativeName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return toString().hashCode();
    }

    /**
     * Tests if the local name is empty
     * 
     * @return True if the local name is empty
     */
    public boolean isEmpty() {

        if (pLocalName != null) {
            return pLocalName.isEmpty();
        }

        return true;
    }

    /**
     * Splits the last element of the rest of URI. Returns an array with an
     * empty (non null) first entry if the local name can't be split. Returns
     * null if the local name is null.
     * 
     * @return The split URI, null if not possible
     */
    public String[] splitLocalNameLastPart() {

        if (pLocalName == null) {
            return null;
        }

        final String[] result = new String[2];
        final int index = pLocalName.lastIndexOf('/');
        if (index == -1) {
            // Can't split the URI
            result[0] = "";
            result[1] = pLocalName;

        } else {
            // Split the string
            result[0] = pLocalName.substring(0, index);
            result[1] = pLocalName.substring(index + 1);
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder(pNamespace);
        builder.append(':').append(pLocalName);

        return builder.toString();
    }
}
