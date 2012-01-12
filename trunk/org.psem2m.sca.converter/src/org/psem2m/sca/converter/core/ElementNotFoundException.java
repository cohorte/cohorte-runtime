/**
 * File:   ElementNotFoundException.java
 * Author: Thomas Calmant
 * Date:   10 janv. 2012
 */
package org.psem2m.sca.converter.core;

import org.psem2m.sca.converter.model.Composite;

/**
 * @author Thomas Calmant
 * 
 */
public class ElementNotFoundException extends Exception {

    /** Serial version URI */
    private static final long serialVersionUID = 4245019831235121505L;

    /**
     * Formats the exception message
     * 
     * @param aComposite
     *            A composite
     * @param aIncludedName
     *            The composite to be included
     * @return The exception message
     */
    private static String formatMessage(final Composite aComposite,
            final QName aIncludedName) {

        final StringBuilder builder = new StringBuilder();

        builder.append("Composite '");
        if (aComposite == null) {
            builder.append("<null>");
        } else {
            builder.append(aComposite.getQualifiedName());
        }

        builder.append("' can't find the composite to include : '");
        builder.append(aIncludedName).append("'");
        return builder.toString();
    }

    /**
     * Sets up the exception
     * 
     * @param aComposite
     *            A composite
     * @param aIncludedName
     *            The composite to be included
     */
    public ElementNotFoundException(final Composite aComposite,
            final QName aIncludedName) {

        super(formatMessage(aComposite, aIncludedName));
    }
}
