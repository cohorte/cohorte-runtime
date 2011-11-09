'''
Representation of the SCA model

Created on 8 nov. 2011

@author: Thomas Calmant
'''

from __init__ import CompoSet, Component

class SCAModelNode(object):
    """
    Common code for SCA nodes
    """

    __slots__ = ("name", "fqname", "parent", "__type")

    def __init__(self, node_type):
        """
        Constructor
        """
        self.name = None
        self.fqname = None
        self.parent = None
        self.__type = node_type


    def compute_fq_name(self):
        """
        Sets up the node fully-qualified name
        """
        if self.parent is None:
            self.fqname = self.name

        else:
            self.fqname = self.parent.fqname + "." + self.name


    def is_valid(self):
        """
        Tests if the component has a valid content
        """
        raise NotImplementedError


    def to_psem2m_model(self, named_wires):
        """
        Converts the current objects and its children to the PSEM2M Composer
        model
        """
        raise NotImplementedError


    def to_str(self, prefix=""):
        """
        Returns a nice representation of the model hierarchy
        
        @param prefix: The output string prefix
        """
        raise NotImplementedError


    def __repr__(self):
        """
        Representation is like the string form
        """
        return self.__str__()


    def __str__(self):
        """
        Returns a string representing the node
        """
        result = self.__type + "(" + str(self.name)

        for entry in ("namespace"):
            if hasattr(self, entry):
                result += ", " + entry + "=" + str(getattr(self, entry))

        if hasattr(self, "wires"):
            wires = getattr(self, "wires")

            if len(wires) > 0:
                result += ", wires=["

                for source, target in wires:
                    result += source[0].name + "/" + source[1]
                    result += " -> "
                    result += target[0].name + "/" + target[1]
                    result += ", "

                result = result[:-2] + "]"

        return result + ")"


class SCAComposite(SCAModelNode):
    """
    "Useful" informations to represent a SCA Composite
    """

    __slots__ = ("namespace", "imported_namespaces", "components", \
                 "references", "services", "wires")

    def __init__(self):
        """
        Constructor
        """
        SCAModelNode.__init__(self, "SCAComposite")

        self.namespace = None
        self.imported_namespaces = {}
        self.components = {}
        self.references = {}
        self.services = {}
        self.wires = []


    def compute_fq_name(self):
        """
        Sets up the node fully-qualified name, recursively
        """
        # Do it for current object
        SCAModelNode.compute_fq_name(self)

        # Then for children
        for component in self.components.values():
            component.compute_fq_name()


    def is_valid(self):
        """
        Tests if the component has a valid content
        """
        return not (not self.name or not self.namespace or not self.components)


    def update_wire_source(self, old_source, new_source):
        """
        Updates a wire source, replacing the old one by the new one...
        
        @param old_source: Current wire source : (component, name) tuple
        @param new_source: New wire source : (component, name) tuple
        """
        # Source is the first element in a wire array
        self.__update_wire_part(0, old_source, new_source)


    def update_wire_target(self, old_target, new_target):
        """
        Updates a wire target, replacing the old one by the new one...
        
        @param old_target: Current wire target : (component, name) tuple
        @param new_target: New wire target : (component, name) tuple
        """
        # Target is the second element in a wire array
        self.__update_wire_part(1, old_target, new_target)


    def __update_wire_part(self, part_index, old_part, new_part):
        """
        Updates a wire extremity (part)
        
        @param old_part: Current wire part : (component, name) tuple
        @param new_part: New wire part : (component, name) tuple
        """
        for wire in self.wires:

            wire_part = wire[part_index]

            if wire_part[0] == old_part[0] and wire_part[1] == old_part[1]:
                # Target match, update the wire extremity
                wire_part[0], wire_part[1] = new_part[0], new_part[1]


    def resolve_wires(self):
        """
        Resolves internal components wires
        """
        # For each wire...
        for wire in self.wires:
            # ... for each wire extremity...
            for extremity in wire:
                # ... test if a component uses this extremity
                for component_name in self.components:
                    # Add a slash, to have a secure match (a/ vs. aa/)
                    name_with_slash = component_name + "/"

                    if not extremity[0] and extremity[1].startswith(name_with_slash):
                        # Unresolved wire source name matches component name
                        ref_name = extremity[1][len(name_with_slash):]

                        # Update the link
                        extremity[0] = self.components[component_name]
                        extremity[1] = ref_name


    def get_fqnamed_wires(self, named_wires):
        """
        Fills the given dictionary recursively
        """
        # For each wire...
        for wire in self.wires:

            # Get the source component fully-qualified name
            source_fqname = wire[0][0].fqname

            if source_fqname not in named_wires:
                named_wires[source_fqname] = {}

            # Sub-key : link name, sub-value = link target, as is
            named_wires[source_fqname][wire[0][1]] = wire[1]

        # Work on SCA sub-composites only
        for component in self.components.values():
            if isinstance(component, SCAComposite):
                component.get_fqnamed_wires(named_wires)


    def get_promoted_component(self, ref):
        """
        Retrieves the component service/reference promoted as "ref"
        
        @param ref: The promoted reference/service name
        """

        if ref in self.services:
            # Service referenced here
            promoted = self.services[ref]

        elif ref in self.references:
            # Reference referenced here
            promoted = self.references[ref]

        else:
            # No luck
            return None

        for name in self.components:
            # Extract the component and service name
            if promoted.startswith(name + "/"):
                return (self.components[name], promoted[len(name) + 1:])

        return None


    def to_str(self, prefix=""):
        """
        Returns a nice representation of the model hierarchy
        
        @param prefix: The output string prefix
        """
        result = prefix + "Composite(" + self.name + ", components = {\n"

        for component in self.components.values():
            result += component.to_str(prefix + "  ")

        result += prefix + "}\n"

        return result


    def to_psem2m_model(self, named_wires):
        """
        Converts the current SCA composite and its children to the
        PSEM2M Composer model
        """
        composet = CompoSet()
        composet.name = self.name
        composet.fqname = self.fqname
        composet.components = []

        for sca_component in self.components.values():
            # Convert all components/composites
            composet.components.append(sca_component.to_psem2m_model(named_wires))

        return composet


class SCAComponent(SCAModelNode):
    """
    "Useful" informations to represent a SCA Component
    """

    __slots__ = ("implementation", "is_composite", "filters", \
                 "properties", "services", "references")

    def __init__(self):
        """
        Constructor
        """
        SCAModelNode.__init__(self, "SCAComponent")

        # A dictionary with known implementation attributes
        self.implementation = {}
        self.is_composite = False

        self.properties = {}
        self.services = {}
        self.references = {}
        self.filters = {}


    def is_valid(self):
        """
        Tests if the component has a valid content
        """
        return not (not self.name or not self.implementation)


    def to_psem2m_model(self, named_wires):
        """
        Converts the current SCA component to the PSEM2M Composer model
        """
        component = Component()

        # Known properties
        component.name = self.name
        component.fqname = self.fqname
        component.type = self.implementation["type"]
        component.isolate = self.implementation["isolate"]
        component.properties = self.properties
        component.filters = self.filters

        if self.fqname in named_wires:
            component_wires = named_wires[self.fqname]

            # Wires found, only get the target fully-qualified name
            for ref_name in component_wires:
                component.wires[ref_name] = component_wires[ref_name][0].fqname

        return component


    def to_str(self, prefix=""):
        """
        Returns a nice representation of the model hierarchy
        
        @param prefix: The output string prefix
        """
        return prefix + "Component(name=" + self.name + ")\n"
