/**
 * File:   Reference.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Calmant
 * 
 */
public class Service extends AbstractPromotableSCAElement<Service> {

    /** Service bindings */
    private final List<Binding> pBindings = new ArrayList<Binding>();

    /** The service interface */
    private Interface pInterface;

    /**
     * @param aBinding
     */
    public void addBinding(final Binding aBinding) {

        pBindings.add(aBinding);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.AbstractSCAElement#duplicate()
     */
    @Override
    public Service duplicate() {

        final Service copy = new Service();
        copy.pQName = pQName;
        copy.pContainer = pContainer;
        copy.pPromotes = pPromotes;
        copy.pPromotedElementName = pPromotedElementName;
        copy.pXmlElement = pXmlElement;

        if (pInterface != null) {
            copy.pInterface = pInterface.duplicate();
        }

        for (final Binding binding : pBindings) {
            copy.addBinding(binding.duplicate());
        }

        return copy;
    }

    /**
     * @return the bindings
     */
    public List<Binding> getBindings() {

        return pBindings;
    }

    /**
     * @return the interface
     */
    public Interface getInterface() {

        return pInterface;
    }

    /**
     * @param aInterface
     *            the interface to set
     */
    public void setInterface(final Interface aInterface) {

        pInterface = aInterface;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.sca.converter.model.AbstractSCAElement#toString(java.lang.
     * StringBuilder, java.lang.String)
     */
    @Override
    public void toString(final StringBuilder aBuilder, final String aPrefix) {

        final String subPrefix = aPrefix + PREFIX_INDENT;
        final String subSubPrefix = subPrefix + PREFIX_INDENT;

        aBuilder.append(aPrefix);
        aBuilder.append("Service(name=").append(pQName.getLocalName())
                .append(",\n");
        aBuilder.append(subPrefix);
        aBuilder.append("promoted=").append(pPromotes).append(",\n");
        if (pPromotes) {
            aBuilder.append(subPrefix);
            aBuilder.append("promotes=").append(pPromotedElementName)
                    .append(",\n");
        }

        aBuilder.append(subPrefix);
        aBuilder.append("interface=");
        if (pInterface == null) {
            aBuilder.append("<null>\n");

        } else {
            aBuilder.append("\n");
            pInterface.toString(aBuilder, subSubPrefix);
        }

        aBuilder.append(subPrefix);
        aBuilder.append("bindings=[\n");
        for (final Binding binding : pBindings) {
            binding.toString(aBuilder, subSubPrefix);
        }
        aBuilder.append("\n").append(aPrefix).append("]\n");
        aBuilder.append(aPrefix).append(")");
    }
}
