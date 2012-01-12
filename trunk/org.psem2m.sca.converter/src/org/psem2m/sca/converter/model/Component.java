/**
 * File:   Component.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.psem2m.sca.converter.core.QName;

/**
 * Represents an SCA component
 * 
 * @author Thomas Calmant
 */
public class Component extends AbstractNameableSCAElement implements
        IReferenceContainer {

    /** Component implementation */
    private Implementation pImplementation;

    /** Component properties */
    private final List<Property> pProperties = new ArrayList<Property>();

    /** Component references */
    private final List<Reference> pReferences = new ArrayList<Reference>();

    /** Component services */
    private final List<Service> pServices = new ArrayList<Service>();

    /**
     * @param aProperty
     *            the property to add
     */
    public void addProperty(final Property aProperty) {

        if (aProperty != null) {
            aProperty.setContainer(this);
            pProperties.add(aProperty);
        }
    }

    /**
     * @param aReference
     *            the reference to add
     */
    public void addReference(final Reference aReference) {

        if (aReference != null) {
            aReference.setContainer(this);
            pReferences.add(aReference);
        }
    }

    /**
     * @param aService
     *            the service to add
     */
    public void addService(final Service aService) {

        if (aService != null) {
            aService.setContainer(this);
            pServices.add(aService);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.AbstractSCAElement#duplicate()
     */
    @Override
    public Component duplicate() {

        final Component copy = new Component();
        copy.pQName = pQName;
        copy.pContainer = pContainer;
        copy.pXmlElement = pXmlElement;

        if (pImplementation != null) {
            copy.pImplementation = pImplementation.duplicate();
        }

        for (final Property property : pProperties) {
            copy.addProperty(property.duplicate());
        }

        for (final Reference reference : pReferences) {
            copy.addReference(reference.duplicate());
        }

        for (final Service service : pServices) {
            copy.addService(service.duplicate());
        }

        return copy;
    }

    /**
     * @return the implementation
     */
    public Implementation getImplementation() {

        return pImplementation;
    }

    /**
     * @return the properties
     */
    public Property[] getProperties() {

        return pProperties.toArray(new Property[pProperties.size()]);
    }

    /**
     * Retrieves the first reference with the given name, null if not found
     * 
     * @param aReferenceName
     *            A reference name
     * @return The found reference, or null
     */
    @Override
    public Reference getReference(final QName aReferenceName) {

        for (final Reference ref : pReferences) {
            if (ref.getQualifiedName().equals(aReferenceName)) {
                return ref;
            }
        }

        return null;
    }

    /**
     * @return the references
     */
    public Reference[] getReferences() {

        return pReferences.toArray(new Reference[pReferences.size()]);
    }

    /**
     * Retrieves the first service with the given name, null if not found
     * 
     * @param aServiceName
     *            A service name
     * @return The found service, or null
     */
    @Override
    public Service getService(final QName aServiceName) {

        for (final Service svc : pServices) {
            if (svc.getQualifiedName().equals(aServiceName)) {
                return svc;
            }
        }

        return null;
    }

    /**
     * @return the services
     */
    public Service[] getServices() {

        return pServices.toArray(new Service[pServices.size()]);
    }

    /**
     * Tests if this component is implemented by a composite
     * 
     * @return True if the component is a composite
     */
    public boolean isComposite() {

        if (pImplementation != null) {
            return pImplementation.isComposite();
        }

        return false;
    }

    /**
     * @param aImplementation
     *            the implementation to set
     */
    public void setImplementation(final Implementation aImplementation) {

        aImplementation.setContainer(this);
        pImplementation = aImplementation;
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
        aBuilder.append("Component(name=").append(pQName.getLocalName())
                .append(",\n");

        aBuilder.append(subPrefix);
        aBuilder.append("implementation=");
        if (pImplementation == null) {
            aBuilder.append("<null>\n");

        } else {
            pImplementation.toString(aBuilder, "");
            aBuilder.append("\n");
        }

        final Map<String, List<? extends AbstractSCAElement>> toPrint = new LinkedHashMap<String, List<? extends AbstractSCAElement>>();
        toPrint.put("properties", pProperties);
        toPrint.put("references", pReferences);
        toPrint.put("services", pServices);

        for (final Entry<String, List<? extends AbstractSCAElement>> entry : toPrint
                .entrySet()) {

            aBuilder.append(subPrefix).append(entry.getKey()).append("=[\n");

            for (final AbstractSCAElement element : entry.getValue()) {
                element.toString(aBuilder, subSubPrefix);
                aBuilder.append(",\n");
            }

            aBuilder.append(subPrefix).append("]\n");
        }

        aBuilder.append(aPrefix).append(")");
    }
}
