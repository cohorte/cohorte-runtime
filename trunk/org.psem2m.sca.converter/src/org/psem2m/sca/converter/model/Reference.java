/**
 * File:   Reference.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.psem2m.sca.converter.core.QName;

/**
 * Represents a SCA reference
 * 
 * @author Thomas Calmant
 */
public class Reference extends AbstractPromotableSCAElement {

    /** Reference bindings */
    private final List<Binding> pBindings = new ArrayList<Binding>();

    /** Referenced interface */
    private Interface pInterface;

    /** The reference multiplicity (default : {@link EMultiplicity#ONE_ONE} */
    private EMultiplicity pMultiplicity = EMultiplicity.ONE_ONE;

    /** Reference targets */
    private final Set<INameable> pTargets = new HashSet<INameable>();

    /** Reference targets names */
    private final Set<QName> pTargetsNames = new HashSet<QName>();

    /**
     * @param aBinding
     */
    public void addBinding(final Binding aBinding) {

        pBindings.add(aBinding);
    }

    /**
     * Adds the target element. It must be a Component or a Service instance.
     * Returns false if the given target is invalid
     * 
     * @param aTarget
     *            A target element.
     * @return True on success
     */
    public boolean addTarget(final INameable aTarget) {

        if (aTarget instanceof Component || aTarget instanceof Service) {
            pTargets.add(aTarget);
            return true;
        }

        return false;
    }

    /**
     * @param aTargetName
     */
    public void addTargetName(final QName aTargetName) {

        if (aTargetName != null) {
            pTargetsNames.add(aTargetName);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.AbstractSCAElement#duplicate()
     */
    @Override
    public Reference duplicate() {

        final Reference copy = (Reference) super.duplicate();

        // Multiplicity
        copy.pMultiplicity = pMultiplicity;

        // Interface
        if (pInterface != null) {
            copy.pInterface = pInterface.duplicate();
        }

        // Same targets
        copy.pTargetsNames.addAll(pTargetsNames);
        copy.pTargets.addAll(pTargets);

        // Bindings
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
     * @return the multiplicity
     */
    public EMultiplicity getMultiplicity() {

        return pMultiplicity;
    }

    /**
     * @return the targets
     */
    public Set<INameable> getTargets() {

        return pTargets;
    }

    /**
     * @return the targets
     */
    public Set<QName> getTargetsNames() {

        return pTargetsNames;
    }

    /**
     * @param aInterface
     *            the interface to set
     */
    public void setInterface(final Interface aInterface) {

        pInterface = aInterface;
    }

    /**
     * @param aMultiplicity
     *            the multiplicity to set
     */
    public void setMultiplicity(final EMultiplicity aMultiplicity) {

        pMultiplicity = aMultiplicity;
    }

    /**
     * Parses the given string to obtain a multiplicity. If the string is
     * invalid, considers the multiplicity as 1..1.
     * 
     * @param aMultiplicity
     *            A SCA string representation of the multiplicity
     */
    public void setMultiplicity(final String aMultiplicity) {

        final EMultiplicity defaultMultiplicity = EMultiplicity.ONE_ONE;

        if (aMultiplicity == null || aMultiplicity.trim().isEmpty()) {
            // Special case...
            setMultiplicity(defaultMultiplicity);
            return;
        }

        final String trimmedStr = aMultiplicity.trim();
        if (trimmedStr.length() < 3) {
            // Insufficient length
            setMultiplicity(defaultMultiplicity);
            return;
        }

        final char firstChar = trimmedStr.charAt(0);
        final char lastChar = trimmedStr.charAt(trimmedStr.length() - 1);

        switch (firstChar) {

        case '0':
            switch (lastChar) {
            case '1':
                setMultiplicity(EMultiplicity.ZERO_ONE);
                return;

            case 'n':
                setMultiplicity(EMultiplicity.ZERO_N);
                return;
            }
            break;

        case '1':
            switch (lastChar) {
            case '1':
                setMultiplicity(EMultiplicity.ONE_ONE);
                return;

            case 'n':
                setMultiplicity(EMultiplicity.ONE_N);
                return;
            }
            break;
        }

        // Default...
        setMultiplicity(defaultMultiplicity);
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
        aBuilder.append("Reference(name=").append(pQName).append(",\n");
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
            pInterface.toString(aBuilder, subSubPrefix);
            aBuilder.append("\n");
        }

        aBuilder.append(subPrefix);
        aBuilder.append("targetsNames=").append(pTargetsNames).append("\n");
        aBuilder.append(subPrefix);
        aBuilder.append("targets=[");
        for (final INameable target : pTargets) {
            aBuilder.append(target.getCompleteName()).append(", ");
        }
        aBuilder.append("]\n");

        aBuilder.append(subPrefix);
        aBuilder.append("bindings=[\n");
        for (final Binding binding : pBindings) {
            binding.toString(aBuilder, subSubPrefix);
            aBuilder.append(",\n");
        }
        aBuilder.append(aPrefix).append("]\n");

        aBuilder.append(aPrefix);
        aBuilder.append(")");
    }
}
