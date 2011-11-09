"""
SCA XML OASIS to PSEM2M Composer JSON file converter

Created on 8 nov. 2011

@author: Thomas Calmant
"""

from __init__ import PSEM2M_XMLNS
from scamodel import SCAComposite, SCAComponent
from xml.dom.minidom import parse as xml_parse


XML_NS = "http://www.w3.org/2000/xmlns/"
SCA_XMLNS = "http://docs.oasis-open.org/ns/opencsa/sca/200912"


def getChildrenNS(node, namespace_uri, tag_name):
    """
    Retrieves all elements, children of the given one corresponding to the given
    tag and namespace. Returns None if the node is invalid
    """
    if not node:
        return

    result = []

    for child in node.childNodes:
        if child.nodeType == child.ELEMENT_NODE \
                and child.namespaceURI == namespace_uri \
                and child.localName == tag_name:
            # Found one
            result.append(child)

    return result


class SCAConverter(object):
    """
    Converts XML *.composite SCA files (OASIS schema) into PSEM2M Composer JSON
    configuration files.
    """
    __slots__ = ("_sca_composites")

    def __init__(self):
        """
        Constructor
        """
        self._sca_composites = {}


    def convert(self, files_array, root_composite_ns=None):
        """
        Entry point of the conversion.
        
        @param files_array: An array of valid XML files paths to load
        @param root_composite_ns: The target namespace of the root composite to
        use. If None, creates a root composite. Raises an error if the given
        root is not found.
        @return: The PSEM2M Composer model of the composition
        """
        if not files_array:
            raise "No input files given"

        # Load the SCA model
        for sca_file in files_array:
            self._load_composite(sca_file)

        # Resolve composites internal wires
        for composite in self._sca_composites.values():
            composite.resolve_wires()

        # Resolve hierarchy
        self._compute_hierarchy()

        # Get the root SCA element
        sca_root = self._select_root(root_composite_ns)

        # Compute nodes fully-qualified names
        sca_root.compute_fq_name()

        # Prepare a named wires dictionary (key = fully-qualified source node)
        named_wires = {}
        sca_root.get_fqnamed_wires(named_wires)

        # Convert into the PSEM2M Model
        return sca_root.to_psem2m_model(named_wires)


    def _load_composite(self, sca_file):
        """
        Loads the model described by the given SCA XML file
        
        @param sca_file: The source SCA file
        @param IoError: Invalid file
        """
        # Parse the XML file
        dom = xml_parse(sca_file)
        if not dom:
            raise IOError("Not a valid XML file : " + sca_file)

        root = dom.documentElement
        if root.namespaceURI != SCA_XMLNS:
            raise IOError("Not an OASIS SCA file")

        if root.localName != "composite":
            raise IOError('XML root is not "composite"')

        # Create the composite
        composite = SCAComposite()

        # Parse attributes
        composite.name = root.getAttribute("name")
        composite.namespace = root.getAttribute("targetNamespace")

        # Map imported namespaces and local names
        composite.imported_namespaces = dict((value, key[1]) \
                    for key, value in root.attributes.itemsNS() \
                    if key[0] == XML_NS)

        # Parse components
        for element in root.getElementsByTagNameNS(SCA_XMLNS, "component"):

            component = self._handle_component(element)
            if component.is_valid():
                composite.components[component.name] = component
                component.parent = composite
            else:
                raise IOError("Incomplete component in " + sca_file)

        # Parse wires, services and references
        nodes = getChildrenNS(root, SCA_XMLNS, "wire")
        if nodes:
            composite.wires = [([None, node.getAttribute("source")], \
                                [None, node.getAttribute("target")]) \
                                for node in nodes]

        nodes = getChildrenNS(root, SCA_XMLNS, "reference")
        if nodes:
            composite.references = dict((node.getAttribute("name"), \
                                         node.getAttribute("promote")) \
                                         for node in nodes)

        nodes = getChildrenNS(root, SCA_XMLNS, "service")
        if nodes:
            composite.services = dict((node.getAttribute("name"), \
                                       node.getAttribute("promote")) \
                                       for node in nodes)

        # Test composite validity
        if not composite.is_valid():
            raise IOError("Incomplete composite in " + sca_file)

        # Store it
        self._sca_composites[composite.namespace] = composite


    def _handle_component(self, element):
        """
        Reads a csa:component node

        @param element: A DOM Component element
        """
        # Prepare the object
        component = SCAComponent()
        component.name = element.getAttribute("name")

        # Store the implementation node
        for child in element.childNodes:
            if (child.namespaceURI == SCA_XMLNS \
                    or child.namespaceURI == PSEM2M_XMLNS) \
                    and child.localName.startswith("implementation."):

                implementation_name = child.localName

                if implementation_name == "implementation.composite":
                    # The implementation is a sub-composite
                    component.is_composite = True
                    component.implementation["name"] = child.getAttribute("name")

                elif implementation_name == "implementation.psem2m":
                    # PSEM2M component implementation
                    component.is_composite = False
                    component.implementation["type"] = child.getAttribute("type")
                    component.implementation["isolate"] = child.getAttribute("isolate")

                else:
                    raise ValueError("Unknown implementation type : " \
                                     + implementation_name)

                break

        # Properties
        nodes = getChildrenNS(element, SCA_XMLNS, "property")
        if nodes:
            component.properties = dict((node.getAttribute("name"), \
                                         node.getAttribute("value")) \
                                        for node in nodes)

        # References
        nodes = getChildrenNS(element, SCA_XMLNS, "reference")
        if nodes:
            for node in nodes:
                ref_name = node.getAttribute("name")
                # Store the reference
                component.references[ref_name] = None

                # Store the reference filter
                filters = getChildrenNS(node, PSEM2M_XMLNS, "psem2m.filter")
                if filters:
                    component.filters[ref_name] = filters[0].getAttribute("filter")

        # Services
        nodes = getChildrenNS(element, SCA_XMLNS, "service")
        if nodes:
            component.services = [node.getAttribute("name") for node in nodes]

        return component



    def _compute_hierarchy(self):
        """
        Does the job
        """
        # For each composite...
        for composite in self._sca_composites.values():
            assert isinstance(composite, SCAComposite)

            # Copy components keys
            components_names = composite.components.keys()

            # ... for each component ...
            for old_name in components_names:
                old_component = composite.components[old_name]
                assert isinstance(old_component, SCAComponent)

                # Special treatment for "implementation.composite" only
                if old_component.is_composite:

                    # Get the child composite name
                    local_ns, name = old_component.implementation["name"].split(":")

                    for namespace in composite.imported_namespaces:
                        if local_ns == composite.imported_namespaces[namespace]:
                            child_ns = namespace
                            break
                    else:
                        raise ValueError("Child namespace not found")

                    # Remove the component object
                    del composite.components[old_name]

                    # Insert the composite object
                    new_composite = self._sca_composites[child_ns]
                    composite.components[name] = new_composite
                    new_composite.parent = composite

                    assert isinstance(new_composite, SCAComposite)

                    # Update links to composite services
                    for service_name in old_component.services:
                        # Prepare the current target definition for this service
                        old_target = (old_component, service_name)

                        # Prepare the new one
                        new_target = new_composite.get_promoted_component(service_name)
                        if not new_target:
                            raise Exception("Can't do my job - no new_target for " + str(old_target))

                        # Update wires targets in other composites
                        for other_composite in self._sca_composites.values():

                            # Don't work on new composite, useless
                            if other_composite == new_composite:
                                continue

                            assert isinstance(other_composite, SCAComposite)
                            other_composite.update_wire_target(old_target, new_target)

                    # Update links from composite references
                    for old_reference_name in old_component.references:
                        # Prepare the current source definition for this reference
                        old_source = (old_component, old_reference_name)

                        # Prepare the new one
                        new_source = new_composite.get_promoted_component(old_reference_name)
                        if not new_source:
                            raise Exception("Can't do my job - no new_source for " + str(old_source))

                        # Update the link in the parent composite
                        composite.update_wire_source(old_source, new_source)


    def _select_root(self, root_composite_ns):
        """
        Selects the root composite from the SCA model
        
        @param root_composite_ns: The target namespace of the root composite to
        use. If None, creates a root composite. Raises an error if the given
        root is not found.
        @raise KeyError: The given root composite namespace has not been found
        """
        # Find the possible roots
        roots = [name for name in self._sca_composites \
                 if not self._sca_composites[name].parent]

        # No root, no possibility
        if len(roots) == 0:
            raise Exception("No root found for the composite files. " + \
                            "Maybe the composition is a cycle...")

        # Case 1 : a root composite is given
        if root_composite_ns != None:
            if root_composite_ns in roots:
                # Requested root found
                return self._sca_composites[root_composite_ns]
            else:
                # Unknown root
                raise KeyError("Unknown composite : " + root_composite_ns)

        # Case 2 : get the only, if possible
        elif len(roots) == 1:
            return self._sca_composites[roots[0]]

        # Case 3 : create a new root composite
        root_composite = SCAComposite()
        root_composite.name = "__sca_converter_root__"
        root_composite.namespace = "http://www.psem2m.org/sca/virtual"

        for composite_names in roots:
            composite = self._sca_composites[composite_names]
            composite.parent = root_composite
            root_composite.components[composite.name] = composite

        return root_composite
