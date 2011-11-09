"""
PSEM2M Composer model

@author: Thomas Calmant
"""

PSEM2M_XMLNS = "http://www.psem2m.org/ns/psem2m-sca"

class ModelNode(object):
    """
    Common code for PSEM2M model nodes
    """
    __slots__ = ("name", "fqname", "__node_type")

    def __init__(self, node_type):
        """
        Constructor
        """
        self.name = None
        self.fqname = None
        self.__node_type = node_type


    def is_valid(self):
        """
        Tests if the component has a valid content
        """
        raise NotImplementedError


    def to_bean(self):
        """
        Converts the Node into its bean representation
        """
        raise NotImplementedError


    def __str__(self):
        """
        Simple node representation
        """
        return str(self.__node_type) + "(" + str(self.name) + ")"


class Component(ModelNode):
    """
    Represents a component
    """
    __slots__ = ("type", "isolate", "properties", "filters", "wires")

    def __init__(self):
        """
        Constructor
        """
        ModelNode.__init__(self, "Component")
        self.type = None
        self.isolate = None
        self.properties = {}
        self.filters = {}
        self.wires = {}


    def is_valid(self):
        """
        Tests if the component has a valid content
        """
        return not ((not self.name) and (not self.type))


    def to_bean(self):
        """
        Converts the Node into its bean representation
        """
        return dict((entry, getattr(self, entry)) \
                    for entry in ("name", "type", "isolate", \
                                  "properties", "wires", "filters") \
                    if getattr(self, entry))


class CompoSet(ModelNode):
    """
    Represents a components set
    """
    __slots__ = ("components")

    def __init__(self):
        """
        Constructor
        """
        ModelNode.__init__(self, "ComponentsSet")
        self.components = []


    def is_valid(self):
        """
        Tests if the component has a valid content
        """
        return not(not self.name and not self.components)


    def to_bean(self):
        """
        Converts the Node into its bean representation
        """
        composets = []
        components = []

        for component in self.components:
            if isinstance(component, CompoSet):
                # Composet found
                composets.append(component.to_bean())

            elif isinstance(component, Component):
                # Component found
                components.append(component.to_bean())

        bean = {"name": self.name}

        # Add entries only if needed
        if composets:
            bean["composets"] = composets

        if components:
            bean["components"] = components

        return bean
