/**
 * File:   SCAParser.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.core;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.psem2m.sca.converter.model.Binding;
import org.psem2m.sca.converter.model.Component;
import org.psem2m.sca.converter.model.Composite;
import org.psem2m.sca.converter.model.INameable;
import org.psem2m.sca.converter.model.IReferenceContainer;
import org.psem2m.sca.converter.model.Implementation;
import org.psem2m.sca.converter.model.Interface;
import org.psem2m.sca.converter.model.Property;
import org.psem2m.sca.converter.model.Reference;
import org.psem2m.sca.converter.model.Service;
import org.psem2m.sca.converter.model.Wire;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

/**
 * Loads a SCA composition model
 * 
 * @author Thomas Calmant
 */
public class SCAParser implements SCAConstants {

    /** The parser */
    private DOM3Parser pParser;

    /**
     * Sets up the DOM 3 parser
     * 
     * @throws Exception
     *             The DOM 3 parser could not be initialized
     */
    public SCAParser() throws Exception {

        pParser = new DOM3Parser();
        pParser.init(null);
        pParser.shutUp();
    }

    /**
     * Connects the wires in the model
     * 
     * @throws InvalidAttributeValueException
     *             A reference or a service is missing
     */
    protected void connectWires(final Composite aRootComposite)
            throws InvalidAttributeValueException {

        // Replace reference targets by wires
        aRootComposite.normalizeWires();

        final Collection<Composite> compositeChildren = aRootComposite
                .getChildren();

        // Add the root composite itself
        compositeChildren.add(aRootComposite);

        for (final Wire wire : aRootComposite.getWires()) {

            Reference ref = (Reference) resolveQName(compositeChildren,
                    aRootComposite, wire.getSourceName(),
                    EElementType.REFERENCE);

            if (ref == null) {
                final StringBuilder builder = new StringBuilder();
                builder.append("In composite '");
                builder.append(aRootComposite.getQualifiedName());
                builder.append("', can't find the wire source '");
                builder.append(wire.getSourceName());
                builder.append("'.");

                // throw new InvalidAttributeValueException(builder.toString());
                System.err.println(builder);
                continue;
            }

            INameable svc = resolveQName(null, aRootComposite,
                    wire.getTargetName(), EElementType.SERVICE);
            if (svc == null) {
                // Second chance, try to find a component
                svc = resolveQName(null, aRootComposite, wire.getTargetName(),
                        EElementType.COMPONENT);
            }

            if (svc == null) {
                // No way...
                final StringBuilder builder = new StringBuilder();
                builder.append("In composite '");
                builder.append(aRootComposite.getQualifiedName());
                builder.append("', can't find the wire target '");
                builder.append(wire.getTargetName());
                builder.append("' (source: ");
                builder.append(wire.getSourceName());
                builder.append(").");

                // throw new InvalidAttributeValueException(builder.toString());
                System.err.println(builder);
                continue;
            }

            // Resolve the reference promotions
            Reference prevRef = ref;
            while (ref.isPromotion()) {

                ref = (Reference) resolveQName(compositeChildren,
                        aRootComposite, ref.getPromotedElementName(),
                        EElementType.REFERENCE);

                if (prevRef.equals(ref)) {
                    throw new InvalidAttributeValueException(
                            "INFINITE LOOP due to reference : "
                                    + ref.getQualifiedName());
                }

                prevRef = ref;
            }

            // Resolve the service promotions
            INameable prevSvc = svc;
            while (svc instanceof Service && ((Service) svc).isPromotion()) {

                final QName promotedName = ((Service) svc)
                        .getPromotedElementName();

                // Only services can be promoted targets
                svc = resolveQName(compositeChildren, aRootComposite,
                        promotedName, EElementType.SERVICE);

                if (prevSvc.equals(svc)) {
                    throw new InvalidAttributeValueException(
                            "INFINITE LOOP due to service : "
                                    + svc.getQualifiedName());
                }

                prevSvc = svc;
            }

            // Set the new source and targets
            wire.setSource(ref);
            wire.setTarget(svc);

            // Set the reference target
            ref.addTarget(svc);
        }
    }

    /**
     * Finds the first composite with the given name in the given collection
     * 
     * @param aComposites
     *            A collection of composites
     * @param aName
     *            A composite name
     * @return The composite with the given name, or null
     */
    protected Composite findComposite(final Collection<Composite> aComposites,
            final QName aName) {

        for (final Composite composite : aComposites) {
            if (composite.getQualifiedName().equals(aName)) {
                return composite;
            }
        }

        return null;
    }

    /**
     * Finds the first component matching the given URI in the given composites
     * 
     * @param aComposites
     *            Possible parents
     * @param aUri
     *            A URI to the component
     * @return The first component found
     */
    protected IReferenceContainer findContainer(
            final Collection<Composite> aComposites, final QName aQName) {

        // Split the URI
        final String[] uriParts = aQName.getLocalName().split("/");

        if (uriParts.length == 1) {
            // Only got the component name (one level)
            for (final Composite composite : aComposites) {
                final IReferenceContainer container = composite
                        .getReferenceContainer(aQName);
                if (container != null) {
                    // Found !
                    return container;
                }
            }

        } else {
            // More than one part
            final List<Composite> matching = new ArrayList<Composite>(
                    aComposites);

            final List<Composite> secondMatch = new ArrayList<Composite>(
                    aComposites.size());

            // Find the possible parents
            for (int i = 0; i < uriParts.length - 1; i++) {

                final QName compositeName = aQName.createNSQName(uriParts[i]);
                for (final Composite composite : matching) {

                    if (composite.getQualifiedName().equals(compositeName)) {
                        // All children will be tested
                        secondMatch.add(composite);
                    }
                }

                if (secondMatch.isEmpty()) {

                    // No composite matches the given name...
                    return null;
                }

                // Prepare the next loop
                matching.clear();

                if (i == uriParts.length - 2) {
                    // Last loop : keep current level
                    matching.addAll(secondMatch);

                } else {
                    // Prepare next loop : get to the next level
                    for (final Composite composite : secondMatch) {
                        matching.addAll(Arrays.asList(composite.getComposites()));
                    }
                }

                // Clear the second match list
                secondMatch.clear();
            }

            // Try to find the component
            final QName componentName = aQName
                    .createNSQName(uriParts[uriParts.length - 1]);

            for (final Composite composite : matching) {
                final IReferenceContainer container = composite
                        .getReferenceContainer(componentName);
                if (container != null) {
                    return container;
                }
            }
        }

        // No match
        return null;
    }

    /**
     * Reads the given XML SCA attribute
     * 
     * @param aElement
     *            The DOM element containing the attribute
     * @param aAttributeName
     *            Name of the attribute
     * @param aMandatory
     *            If true, empty values are rejected
     * @return The attribute value
     * @throws InvalidAttributeValueException
     *             If the value is empty and mandatory
     */
    protected String getScaAttribute(final Element aElement,
            final String aAttributeName, final boolean aMandatory)
            throws InvalidAttributeValueException {

        String value = aElement.getAttributeNS(SCA_NS, aAttributeName).trim();
        if (value.isEmpty() && aElement.getNamespaceURI().equals(SCA_NS)) {
            // Small trick, if the default name space is SCA
            value = aElement.getAttribute(aAttributeName).trim();
        }

        if (aMandatory && value.isEmpty()) {

            final StringBuilder builder = new StringBuilder(
                    "Invalid empty attribute value for ");
            builder.append('"');
            builder.append(aElement.getNamespaceURI());
            builder.append(':');
            builder.append(aElement.getLocalName());
            builder.append('@');
            builder.append(aAttributeName);
            builder.append('"');

            throw new InvalidAttributeValueException(builder.toString());
        }

        return value;
    }

    /**
     * Reads the given XML SCA boolean attribute
     * 
     * @param aElement
     *            The DOM element containing the attribute
     * @param aAttributeName
     *            Name of the attribute
     * @param aMandatory
     *            If true, empty values are rejected
     * @param aDefault
     *            Default value (if the value is empty)
     * @return The boolean value of the attribute
     * @throws InvalidAttributeValueException
     *             If the value is empty and mandatory
     */
    protected boolean getScaAttributeBoolean(final Element aElement,
            final String aAttributeName, final boolean aMandatory,
            final boolean aDefault) throws InvalidAttributeValueException {

        final String strValue = getScaAttribute(aElement, aAttributeName,
                aMandatory);
        if (strValue.trim().isEmpty()) {
            // Not found
            return aDefault;
        }

        return Boolean.parseBoolean(strValue);
    }

    /**
     * Adds included composites components in the importer
     * 
     * @param aComposites
     *            Composites to work on
     * 
     * @throws ElementNotFoundException
     *             An included composite couldn't be resolved
     */
    protected void handleInclusions(final Collection<Composite> aComposites)
            throws ElementNotFoundException {

        for (final Composite composite : aComposites) {

            for (final QName includedName : composite
                    .getIncludedCompositesNames()) {

                // Resolve the included composite
                final Composite includedComposite = (Composite) resolveQName(
                        aComposites, composite, includedName,
                        EElementType.COMPOSITE);
                if (includedComposite == null) {
                    // Can't find the included composite
                    throw new ElementNotFoundException(composite, includedName);
                }

                // Do the inclusion...
                composite.includeComposite(includedName, includedComposite);
            }
        }
    }

    /**
     * Tests if the given DOM document is a SCA composite, according to its root
     * node
     * 
     * @param aDocument
     *            A DOM document
     * @return True if the document is an SCA composite
     */
    public boolean isComposite(final Document aDocument) {

        if (aDocument == null) {
            return false;
        }

        final Element root = aDocument.getDocumentElement();

        if (!root.getLocalName().equals(COMPOSITE_ELEMENT)) {
            // The root name is not "composite"
            return false;
        }

        if (!root.getNamespaceURI().equals(SCA_NS)) {
            // Not using SCA name space
            return false;
        }

        // Clear to go
        return true;
    }

    /**
     * Tests if the given element is a SCA tag with the given name
     * 
     * @param aElement
     *            A DOM element
     * @param aName
     *            A tag name
     * @return True if the element is the tested SCA element
     */
    protected boolean isScaElement(final Element aElement, final String aName) {

        return aElement.getNamespaceURI().equals(SCA_NS)
                && aElement.getLocalName().equals(aName);
    }

    /**
     * Tests if the given element extends the given SCA type
     * 
     * @param aElement
     *            A DOM element
     * @param aScaType
     *            A SCA type
     * @return True if the given element extends the SCA type
     */
    protected boolean isScaExtension(final Element aElement,
            final String aScaType) {

        return aElement.getSchemaTypeInfo().isDerivedFrom(SCA_NS, aScaType,
                TypeInfo.DERIVATION_EXTENSION);
    }

    /**
     * Transform the SCA plain-model to a hierarchized one
     * 
     * @param aKnownComposites
     *            The known composites
     * @param aComposite
     *            The composite to work on
     * @throws InvalidAttributeValueException
     *             An attribute is missing
     */
    protected void makeHierarchy(final Collection<Composite> aKnownComposites,
            final Composite aComposite) throws InvalidAttributeValueException {

        for (final Component component : aComposite.getComponents()) {

            if (component.isComposite()) {
                // Component is a composite : get its name
                final String name = component.getImplementation()
                        .getXmlAttribute("name");
                if (name == null || name.isEmpty()) {
                    throw new InvalidAttributeValueException(
                            "No composite name given for the implementation of "
                                    + component.getQualifiedName());
                }

                final QName qName;
                try {
                    qName = new QName(component.getXmlElement()
                            .getOwnerDocument(), name);

                } catch (final MalformedURLException e) {
                    throw new InvalidAttributeValueException(
                            "Invalid component implementation.composite name : "
                                    + name, e);
                }

                // Resolve it
                final Composite composite = (Composite) resolveQName(
                        aKnownComposites, aComposite, qName,
                        EElementType.COMPOSITE);
                if (composite == null) {
                    throw new InvalidAttributeValueException(
                            "Unknown composite '" + qName + "' to implement '"
                                    + component.getQualifiedName() + "'");
                }

                // Work on its children
                makeHierarchy(aKnownComposites, composite);

                // Replace it
                aComposite.replaceComponentByComposite(component,
                        composite.duplicate());
            }
        }
    }

    /**
     * Parses the given SCA composition
     * 
     * FIXME replace aFolder by something like the "contributions" in Tuscany
     * 
     * @param aFolder
     *            Composite files folder
     * @param aRootCompositeFile
     *            The composition root file
     * 
     * @return The root composite representation
     * 
     * @throws ElementNotFoundException
     *             An included composite couldn't be found
     * @throws FileNotFoundException
     *             The root composite file couldn't be parsed
     * @throws InvalidAttributeValueException
     *             A parsed attribute has an invalid value
     */
    public Composite parse(final File aFolder, final File aRootCompositeFile)
            throws ElementNotFoundException, FileNotFoundException,
            InvalidAttributeValueException {

        // Parse files and store results
        // FIXME maybe use strings instead of File objects as keys
        final Map<File, Composite> compositesMap = parseFolder(aFolder);
        if (compositesMap == null) {
            System.err.println("Error parsing folder : " + aFolder);
            return null;
        }

        // Find the root composite
        final Composite rootComposite = compositesMap.get(aRootCompositeFile);
        if (rootComposite == null) {
            throw new FileNotFoundException(
                    "The root composite file has not been found or parsed ("
                            + aRootCompositeFile + ")");
        }

        final Collection<Composite> composites = compositesMap.values();

        // Handle inclusions
        handleInclusions(composites);

        // Make the hierarchy
        makeHierarchy(composites, rootComposite);

        // We don't need the composites list anymore
        composites.clear();

        // Connect the wires in the entire hierarchy
        connectWires(rootComposite);

        // Propagate properties values
        final Map<String, String> properties = new HashMap<String, String>();
        propagateProperties(rootComposite, properties);

        return rootComposite;
    }

    /**
     * Parses the given SCA component
     * 
     * @param aQualifiedName
     *            The qualified name of the component container
     * @param aElement
     *            The component DOM element
     * 
     * @return The component representation
     * 
     * @throws InvalidAttributeValueException
     *             The XML contains an invalid attribute value
     */
    protected Component parseComponent(final QName aQualifiedName,
            final Element aElement) throws InvalidAttributeValueException {

        final Component component = new Component();
        final String name = getScaAttribute(aElement, "name", true);
        final QName componentQName = aQualifiedName.createNSQName(name);
        component.setQualifiedName(componentQName);
        component.setXmlElement(aElement);

        for (Node childNode = aElement.getFirstChild(); childNode != null; childNode = childNode
                .getNextSibling()) {

            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                // Ignore non-element nodes
                continue;
            }

            final Element element = (Element) childNode;

            if (isScaExtension(element, TYPE_IMPLEMENTATION)) {
                // Implementation...
                final Implementation implementation = new Implementation();
                implementation.setXmlElement(element);
                component.setImplementation(implementation);

            } else if (isScaElement(element, "service")) {
                // Service
                component.addService(parseService(componentQName, element));

            } else if (isScaElement(element, "property")) {
                // Property
                component.addProperty(parseProperty(componentQName, element));

            } else if (isScaElement(element, "reference")) {
                // Reference
                component.addReference(parseReference(componentQName, element));
            }

            // Ignore other nodes
        }

        return component;
    }

    /**
     * Parses a SCA Composite document
     * 
     * @param aDocument
     *            A DOM document
     * @return The parsed composite element
     * @throws InvalidAttributeValueException
     *             The XML contains an invalid attribute value
     */
    protected Composite parseComposite(final Document aDocument)
            throws InvalidAttributeValueException {

        // The composite bean
        final Composite compositeBean = new Composite();

        // Set the associated element
        final Element rootElement = aDocument.getDocumentElement();
        compositeBean.setXmlElement(rootElement);

        // Prepare the composite qualified name
        final QName qName = new QName(
                rootElement.getAttribute(COMPOSITE_ATTR_NAMESPACE),
                rootElement.getAttribute(COMPOSITE_ATTR_NAME));

        // .. qualified name
        compositeBean.setQualifiedName(qName);

        // .. parse children
        for (Node childNode = rootElement.getFirstChild(); childNode != null; childNode = childNode
                .getNextSibling()) {

            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                // Not an element, ignore
                continue;
            }

            if (!childNode.getNamespaceURI().equals(SCA_NS)) {
                // Unknown node...
                System.out
                        .println("Ignoring node : " + childNode.getNodeName());
                continue;
            }

            // Cast to a DOM element
            final Element childElement = (Element) childNode;

            final String nodeName = childNode.getLocalName();
            if (nodeName.equals(COMPONENT_ELEMENT)) {
                // .. component
                compositeBean.addComponent(parseComponent(qName, childElement));

            } else if (nodeName.equals(PROPERTY_ELEMENT)) {
                // .. property
                compositeBean.addProperty(parseProperty(qName, childElement));

            } else if (nodeName.equals(REFERENCE_ELEMENT)) {
                // .. reference
                compositeBean.addReference(parseReference(qName, childElement));

            } else if (nodeName.equals(SERVICE_ELEMENT)) {
                // .. service
                compositeBean.addService(parseService(qName, childElement));

            } else if (nodeName.equals(WIRE_ELEMENT)) {
                // .. wire
                compositeBean.addWire(parseWire(childElement));
            }
        }

        return compositeBean;
    }

    /**
     * Parses the composite XML files in the given folder and returns their
     * parsed representation
     * 
     * @param aFolder
     *            Folder to look into (not recursive)
     * @return A File -&gt; Composite mapping of read files
     * @throws InvalidAttributeValueException
     *             An attribute has an invalid value
     * @throws FileNotFoundException
     *             A file disappeared in the folder
     */
    protected Map<File, Composite> parseFolder(final File aFolder)
            throws InvalidAttributeValueException, FileNotFoundException {

        if (aFolder == null || !aFolder.isDirectory()) {
            return null;
        }

        // Prepare the filter
        final FileFilter filter = new SimpleFileFilter("*.composite", false);

        // Store results
        final Map<File, Composite> parsedFiles = new HashMap<File, Composite>();

        // Parse all files in the folder
        for (final File compositeFile : aFolder.listFiles(filter)) {

            final Document readDoc = pParser.parseFile(compositeFile);
            if (isComposite(readDoc)) {
                parsedFiles.put(compositeFile, parseComposite(readDoc));
            }
        }

        return parsedFiles;
    }

    /**
     * Parses a SCA property XML representation
     * 
     * @param aQualifiedName
     *            The qualified name of the service container
     * @param aElement
     *            The property XML element
     * @return The parsed property
     * @throws InvalidAttributeValueException
     *             An invalid attribute value was found
     */
    protected Property parseProperty(final QName aQualifiedName,
            final Element aElement) throws InvalidAttributeValueException {

        final Property property = new Property();
        final String name = getScaAttribute(aElement, "name", true);
        property.setQualifiedName(aQualifiedName.createSubQName(name));
        property.setMustSupply(getScaAttributeBoolean(aElement, "mustSupply",
                false, false));
        property.setXmlElement(aElement);

        final String value;
        final String attrValue = getScaAttribute(aElement, "value", false);
        final String attrFile = getScaAttribute(aElement, "file", false);
        if (!attrValue.isEmpty()) {
            // Try with value attribute
            value = attrValue;

        } else if (!attrFile.isEmpty()) {
            // Try reading the given file
            value = readFile(attrFile);

        } else {
            // Try with node content
            value = aElement.getTextContent();
        }
        property.setValue(value);

        return property;
    }

    /**
     * Parses the given SCA reference XML representation
     * 
     * @param aQualifiedName
     *            The qualified name of the reference container
     * @param aElement
     *            A DOM element
     * @return The parsed reference
     * @throws InvalidAttributeValueException
     *             An attribute is missing
     */
    protected Reference parseReference(final QName aQualifiedName,
            final Element aElement) throws InvalidAttributeValueException {

        final Reference reference = new Reference();

        final String name = getScaAttribute(aElement, "name", true);
        reference.setQualifiedName(aQualifiedName.createSubQName(name));
        reference.setMultiplicity(getScaAttribute(aElement, "multiplicity",
                false));

        final Document xmlDocument = aElement.getOwnerDocument();
        reference.setXmlElement(aElement);

        // Target(s)...
        final String targets = getScaAttribute(aElement, "target", false);
        if (!targets.isEmpty()) {

            final String[] targetsArray = targets.split(" ");
            for (final String target : targetsArray) {
                try {
                    reference.addTargetName(new QName(xmlDocument, target));

                } catch (final MalformedURLException e) {
                    throw new InvalidAttributeValueException(
                            "Can't parse the reference target : " + target, e);
                }
            }
        }

        // Promotion...
        final String promote = getScaAttribute(aElement, "promote", false);
        if (!promote.isEmpty()) {
            // Service promotion
            reference.setPromotes(true);
            try {
                reference
                        .setPromotedElementName(new QName(xmlDocument, promote));

            } catch (final MalformedURLException e) {
                throw new InvalidAttributeValueException(
                        "Can't parse the reference promotion : " + promote, e);
            }

        } else {
            // Component service
            reference.setPromotes(false);
        }

        for (Node childNode = aElement.getFirstChild(); childNode != null; childNode = childNode
                .getNextSibling()) {

            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                // Ignore non-element nodes
                continue;
            }

            final Element element = (Element) childNode;
            if (isScaExtension(element, TYPE_INTERFACE)) {
                // Interface... (single)
                final Interface svcInterface = new Interface();
                svcInterface.setXmlElement(element);
                reference.setInterface(svcInterface);

            } else if (isScaExtension(element, TYPE_BINDING)) {
                // Binding... (multiple)
                final Binding binding = new Binding();
                binding.setXmlElement(element);
                reference.addBinding(binding);
            }

            // Ignore other nodes
        }

        return reference;
    }

    /**
     * Parses the given SCA service XML representation
     * 
     * @param aQualifiedName
     *            The qualified name of the service container
     * @param aElement
     *            A DOM element
     * @return The parsed service
     * @throws InvalidAttributeValueException
     *             An attribute is missing
     */
    protected Service parseService(final QName aQualifiedName,
            final Element aElement) throws InvalidAttributeValueException {

        final Service service = new Service();
        final String name = getScaAttribute(aElement, "name", true);
        service.setQualifiedName(aQualifiedName.createSubQName(name));

        final Document xmlDocument = aElement.getOwnerDocument();
        service.setXmlElement(aElement);

        // Promotion...
        final String promote = getScaAttribute(aElement, "promote", false);
        if (!promote.isEmpty()) {
            // Service promotion
            service.setPromotes(true);
            try {
                service.setPromotedElementName(new QName(xmlDocument, promote));

            } catch (final MalformedURLException e) {
                throw new InvalidAttributeValueException(
                        "Can't parse the reference promotion : " + promote, e);
            }

        } else {
            // Component service
            service.setPromotes(false);
        }

        for (Node childNode = aElement.getFirstChild(); childNode != null; childNode = childNode
                .getNextSibling()) {

            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                // Ignore non-element nodes
                continue;
            }

            final Element element = (Element) childNode;
            if (isScaExtension(element, TYPE_INTERFACE)) {
                // Interface... (single)
                final Interface svcInterface = new Interface();
                svcInterface.setXmlElement(element);
                service.setInterface(svcInterface);

            } else if (isScaExtension(element, TYPE_BINDING)) {
                // Binding... (multiple)
                final Binding binding = new Binding();
                binding.setXmlElement(element);
                service.addBinding(binding);
            }

            // Ignore other nodes
        }

        return service;
    }

    /**
     * Parses the given SCA wire XML representation
     * 
     * @param aElement
     *            A DOM element
     * @return The parsed wire
     * @throws InvalidAttributeValueException
     *             A wire attribute is missing
     */
    protected Wire parseWire(final Element aElement)
            throws InvalidAttributeValueException {

        final Document xmlDocument = aElement.getOwnerDocument();
        final QName source;
        final QName target;

        try {
            source = new QName(xmlDocument, getScaAttribute(aElement, "source",
                    true));

            target = new QName(xmlDocument, getScaAttribute(aElement, "target",
                    true));

        } catch (final MalformedURLException e) {
            throw new InvalidAttributeValueException(
                    "Can't read the wire source or target", e);
        }

        final Wire wire = new Wire();
        wire.setSourceName(source);
        wire.setTargetName(target);
        wire.setXmlElement(aElement);

        return wire;
    }

    /**
     * Propagates properties values, from top composites to down components
     * 
     * @param aRootComposite
     *            Base composite
     * @param aProperties
     *            Actual properties values (name -> value)
     */
    protected void propagateProperties(final Composite aRootComposite,
            final Map<String, String> aProperties) {

        // Use a local copy of the properties (avoid mixing values between
        // siblings)
        final Map<String, String> compositeProperties = new HashMap<String, String>(
                aProperties);

        // Composite level properties
        for (final Property property : aRootComposite.getProperties()) {

            final String propertyName = property.getQualifiedName()
                    .getLocalName();

            if (compositeProperties.containsKey(propertyName)) {
                // Overridden property value
                property.setValue(compositeProperties.get(propertyName));

            } else {
                // First time seen
                final String propertyValue = property.getValue();
                if (propertyValue != null && !propertyValue.isEmpty()) {
                    // Valid value found
                    compositeProperties.put(propertyName, propertyValue);
                }
            }
        }

        // Component level properties
        for (final Component component : aRootComposite.getComponents()) {

            for (final Property property : component.getProperties()) {

                final String propertyName = property.getQualifiedName()
                        .getLocalName();

                if (compositeProperties.containsKey(propertyName)) {
                    // Property value found
                    property.setValue(compositeProperties.get(propertyName));
                }
            }
        }

        // Children
        for (final Composite composite : aRootComposite.getComposites()) {
            propagateProperties(composite, compositeProperties);
        }
    }

    /**
     * Reads the given file if possible. Returns null if the file is not found
     * 
     * @param aFileName
     *            A file name
     * @return The file content
     */
    protected String readFile(final String aFileName) {

        final Scanner scanner;
        try {
            scanner = new Scanner(new File(aFileName));
            scanner.useDelimiter("\\Z");

        } catch (final FileNotFoundException e) {
            // File not found...
            return null;
        }

        final String data = scanner.next();
        scanner.close();
        return data;
    }

    /**
     * Resolves the given qualified name, targeting a SCA element of the given
     * type
     * 
     * @param aKnownComposites
     *            Known composites (can be null, direct children will be used)
     * @param aComposite
     *            Composite referring to the qualified name
     * @param aQName
     *            The qualified name to resolve
     * @param aType
     *            The type of SCA element to look for
     * @return The found SCA element, or null
     */
    protected INameable resolveQName(
            final Collection<Composite> aKnownComposites,
            final Composite aComposite, final QName aQName,
            final EElementType aType) {

        if (aComposite == null || aQName == null) {
            return null;
        }

        final Collection<Composite> knownComposites;
        if (aKnownComposites != null) {
            // Use given composites
            knownComposites = aKnownComposites;

        } else {
            // Use the composite and its direct children
            knownComposites = new ArrayList<Composite>();
            knownComposites.add(aComposite);
            knownComposites.addAll(Arrays.asList(aComposite.getComposites()));
        }

        // Find composites with the given target name space
        final String namespace = aQName.getNamespace();
        final List<Composite> composites = new ArrayList<Composite>();
        for (final Composite composite : knownComposites) {
            if (namespace.equals(composite.getQualifiedName().getNamespace())) {
                composites.add(composite);
            }
        }

        // Find possible element
        switch (aType) {
        case COMPOSITE:
            // Looking for a composite
            return findComposite(composites, aQName);

        case COMPONENT:
            // Looking for a component or its composite implementation
            return findContainer(composites, aQName);

        case REFERENCE:
        case SERVICE:
            final String[] uriParts = aQName.splitLocalNameLastPart();
            if (uriParts == null) {
                // Invalid URI ?
                System.out.println("NO PARTS in " + aQName);
                return null;
            }

            // Find the host component or composite
            QName parentName = aQName.getParentName();
            if (parentName == null) {
                parentName = aQName.createNSQName(uriParts[0]);
            }

            if (parentName == null || parentName.isEmpty()) {
                // No parent, we are referring to a top level component
                return findContainer(knownComposites, aQName);
            }

            final IReferenceContainer container = findContainer(composites,
                    parentName);
            if (container == null) {
                System.out.println("NO CONTAINER found for " + aType.toString()
                        + " - " + aQName + " => " + parentName);
                return null;
            }

            final QName containerName = container.getQualifiedName();
            final QName refName = containerName.createSubQName(uriParts[1]);

            if (aType == EElementType.REFERENCE) {
                // Retrieve the reference
                return container.getReference(refName);

            } else if (aType == EElementType.SERVICE) {
                // Retrieve the service
                return container.getService(refName);
            }

            break;
        }

        // No match or unknown element type
        return null;
    }
}
