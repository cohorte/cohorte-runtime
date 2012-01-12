/**
 * File:   Composite.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.psem2m.sca.converter.utils.QName;

/**
 * Represents an SCA composite
 * 
 * @author Thomas Calmant
 */
public class Composite extends AbstractNameableSCAElement implements
        IReferenceContainer, IAlias {

    /** Name to use in complete name after an inclusion */
    private String pAlias;

    /** Components contained by this composite */
    private final List<Component> pComponents = new ArrayList<Component>();

    /** Composite implementations tracking */
    private final Map<QName, Composite> pCompositeImplementations = new HashMap<QName, Composite>();

    /** Sub-composites (after hierarchy computation) */
    private final List<Composite> pComposites = new ArrayList<Composite>();

    /** Included composites names */
    private final List<QName> pIncludedCompositesNames = new ArrayList<QName>();

    /** Composite-level properties */
    private final List<Property> pProperties = new ArrayList<Property>();

    /** Promoted references */
    private final List<Reference> pReferences = new ArrayList<Reference>();

    /** Promoted services */
    private final List<Service> pServices = new ArrayList<Service>();

    /** Wires */
    private final List<Wire> pWires = new ArrayList<Wire>();

    /**
     * @param aComponent
     *            the component to add
     */
    public void addComponent(final Component aComponent) {

        if (aComponent != null) {
            aComponent.setContainer(this);
            pComponents.add(aComponent);
        }
    }

    /**
     * Adds the included composite reference.
     * 
     * @param aComposite
     *            The included composite reference.
     */
    public void addComposite(final Composite aComposite) {

        if (aComposite != null) {
            aComposite.setContainer(this);
            pComposites.add(aComposite);
        }
    }

    /**
     * 
     * @param aCompositeName
     */
    public void addIncludedComposite(final QName aCompositeName) {

        if (aCompositeName != null) {
            pIncludedCompositesNames.add(aCompositeName);
        }
    }

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

    /**
     * @param aWire
     *            the wire to add
     */
    public void addWire(final Wire aWire) {

        if (aWire != null) {
            aWire.setContainer(this);
            pWires.add(aWire);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.sca.converter.model.AbstractSCAElement#duplicate()
     */
    @Override
    public Composite duplicate() {

        final Composite copy = (Composite) super.duplicate();
        copy.pAlias = pAlias;

        copy.pCompositeImplementations.putAll(pCompositeImplementations);
        copy.pIncludedCompositesNames.addAll(pIncludedCompositesNames);

        for (final Component component : pComponents) {
            copy.addComponent(component.duplicate());
        }

        for (final Composite composite : pComposites) {
            copy.addComposite(composite.duplicate());
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

        for (final Wire wire : pWires) {
            copy.addWire(wire.duplicate());
        }

        return copy;
    }

    /**
     * @return the alias
     */
    @Override
    public String getAlias() {

        if (pAlias == null) {
            return pQName.getLocalName();
        }

        return pAlias;
    }

    /**
     * Makes a plain-collection containing all composite children
     * 
     * @return the children
     */
    public Collection<Composite> getChildren() {

        final Collection<Composite> composites = new ArrayList<Composite>();
        getChildren(composites);
        return composites;
    }

    /**
     * Fills the collection with sub-composites
     * 
     * @param aChildren
     *            A collection to fill
     */
    private void getChildren(final Collection<Composite> aChildren) {

        for (final Composite composite : pComposites) {
            aChildren.add(composite);
            composite.getChildren(aChildren);
        }
    }

    /**
     * Retrieves the complete alias of the current element
     * 
     * @return The complete alias
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
        if (pContainer instanceof Composite) {
            // Another composite (standard case)
            ((Composite) pContainer).getCompleteAlias(aBuilder);
            aBuilder.append('.');

        } else if (pContainer instanceof INameable) {
            // Other kind of container
            aBuilder.append(((INameable) pContainer).getCompleteName());
            aBuilder.append('.');
        }

        if (pAlias != null) {
            // Alias set
            aBuilder.append(pAlias);

        } else {
            // No alias
            aBuilder.append(pQName.getLocalNameLastPart());
        }
    }

    /**
     * Retrieves the first direct component found with the given name. Returns
     * null if no component matches
     * 
     * @param aComponentName
     *            A component name
     * @return The component, or null
     */
    public Component getComponent(final QName aComponentName) {

        for (final Component component : pComponents) {
            if (component.getQualifiedName().equals(aComponentName)) {
                return component;
            }
        }

        return null;
    }

    /**
     * @return the components
     */
    public Component[] getComponents() {

        return pComponents.toArray(new Component[pComponents.size()]);
    }

    /**
     * @return the composites
     */
    public Composite[] getComposites() {

        return pComposites.toArray(new Composite[pComposites.size()]);
    }

    /**
     * @return the includedCompositesNames
     */
    public QName[] getIncludedCompositesNames() {

        return pIncludedCompositesNames
                .toArray(new QName[pIncludedCompositesNames.size()]);
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
     * Retrieves the composite or component with the given name
     * 
     * @param aComponentName
     *            A component name
     * @return The composite or component found, or null
     */
    public IReferenceContainer getReferenceContainer(final QName aComponentName) {

        // Try with components replaced by a composite
        if (pCompositeImplementations.containsKey(aComponentName)) {
            return pCompositeImplementations.get(aComponentName);
        }

        // Try with components
        for (final Component component : pComponents) {
            if (component.getQualifiedName().equals(aComponentName)) {
                return component;
            }
        }

        // Try with composites
        for (final Composite composite : pComposites) {

            final IReferenceContainer container = composite
                    .getReferenceContainer(aComponentName);
            if (container != null) {
                return container;
            }
        }

        // Not found
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
     * @return the wires
     */
    public Wire[] getWires() {

        return pWires.toArray(new Wire[pWires.size()]);
    }

    /**
     * Includes a composite into this one (duplicates its entry)
     * 
     * @param aIncludedName
     *            The name used in the including composite
     * 
     * @param aIncludedComposite
     *            The included composite
     */
    public void includeComposite(final QName aIncludedName,
            final Composite aIncludedComposite) {

        if (aIncludedComposite == null) {
            // Do nothing...
            return;
        }

        // Remove the name from the include list
        pIncludedCompositesNames.remove(aIncludedName);

        // Components
        for (final Component component : aIncludedComposite.pComponents) {
            addComponent(component.duplicate());
        }

        // Sub-composites (if any...)
        for (final Composite composite : aIncludedComposite.pComposites) {
            addComposite(composite.duplicate());
        }

        // Properties
        for (final Property property : aIncludedComposite.pProperties) {
            addProperty(property.duplicate());
        }

        // References
        for (final Reference reference : aIncludedComposite.pReferences) {
            addReference(reference.duplicate());
        }

        // Services
        for (final Service service : aIncludedComposite.pServices) {
            addService(service.duplicate());
        }

        // Wires
        for (final Wire wire : aIncludedComposite.pWires) {
            addWire(wire);
        }
    }

    /**
     * Normalizes wires : replaces reference targets by wires
     */
    public void normalizeWires() {

        // Normalize wires for this composite
        for (final Component component : pComponents) {

            for (final Reference reference : component.getReferences()) {

                // Prepare the reference name
                final QName refName = reference.getQualifiedName();

                for (final QName target : reference.getTargetsNames()) {

                    // Make the new wire
                    final Wire wire = new Wire();
                    wire.setSourceName(refName);
                    wire.setTargetName(target);

                    addWire(wire);
                }

                // Remove the reference targets
                reference.getTargetsNames().clear();
            }
        }

        // Do the same for the whole hierarchy
        for (final Composite composite : pComposites) {
            composite.normalizeWires();
        }
    }

    /**
     * Replace a component by its composite implementation
     * 
     * @param aReplacedComponent
     *            A component
     * @param aComposite
     *            Its implementation
     */
    public void replaceComponentByComposite(final Component aReplacedComponent,
            final Composite aComposite) {

        if (aReplacedComponent == null || aComposite == null) {
            // Do nothing...
            return;
        }

        // Set the alias of the composite
        aComposite.pAlias = aReplacedComponent.getQualifiedName()
                .getLocalName();

        pComponents.remove(aReplacedComponent);
        pComposites.add(aComposite);
        pCompositeImplementations.put(aReplacedComponent.getQualifiedName(),
                aComposite);
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
        aBuilder.append("Composite(name=").append(pQName).append(",\n");

        final Map<String, List<? extends AbstractSCAElement>> toPrint = new LinkedHashMap<String, List<? extends AbstractSCAElement>>();
        toPrint.put("composites", pComposites);
        toPrint.put("components", pComponents);
        toPrint.put("properties", pProperties);
        toPrint.put("references", pReferences);
        toPrint.put("services", pServices);
        toPrint.put("wires", pWires);

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
