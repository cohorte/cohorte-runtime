/**
 * File:   AbstractNameableSCAElement.java
 * Author: Thomas Calmant
 * Date:   12 janv. 2012
 */
package org.psem2m.sca.converter.model;

import org.psem2m.sca.converter.utils.QName;

/**
 * Basic implementation of a nameable SCA element
 * 
 * @author Thomas Calmant
 */
public abstract class AbstractNameableSCAElement extends AbstractSCAElement
        implements INameable {

    /** The element qualified name */
    protected QName pQName;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.AbstractSCAElement#duplicate()
     */
    @Override
    public AbstractNameableSCAElement duplicate() {

        final AbstractNameableSCAElement copy = (AbstractNameableSCAElement) super
                .duplicate();

        copy.pQName = pQName;
        return copy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.INameable#getCompleteAlias()
     */
    @Override
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
        if (pContainer instanceof INameable) {
            // Other kind of container
            aBuilder.append(((INameable) pContainer).getCompleteAlias());
            aBuilder.append('.');
        }

        if (this instanceof IAlias) {
            // Use the alias
            aBuilder.append(((IAlias) this).getAlias());

        } else {
            // Use the local name
            aBuilder.append(pQName.getLocalNameLastPart());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.INameable#getCompleteName()
     */
    @Override
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

        if (pContainer instanceof AbstractNameableSCAElement) {
            // Abstract nameable (use same string builder)
            ((AbstractNameableSCAElement) pContainer).getCompleteName(aBuilder);
            aBuilder.append('.');

        } else if (pContainer instanceof INameable) {
            // Other nameable
            aBuilder.append(((INameable) pContainer).getCompleteName());
            aBuilder.append('.');
        }

        final String localName = pQName.getLocalNameLastPart();
        if (localName != null) {
            aBuilder.append(localName);

        } else {
            aBuilder.append("<null>");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.INameable#getQualifiedName()
     */
    @Override
    public QName getQualifiedName() {

        return pQName;
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
}
