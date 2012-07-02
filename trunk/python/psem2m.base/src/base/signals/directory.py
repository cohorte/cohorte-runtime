#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
Python implementation of the PSEM2M Signals directory

Created on 12 juin 2012

:author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate, Instantiate

# ------------------------------------------------------------------------------

import logging
_logger = logging.getLogger(__name__)

import threading

# ------------------------------------------------------------------------------

ALL = "ALL"
""" All isolates, including the current one """

FORKERS = "FORKERS"
""" All forkers, including the current isolate if it is a forker """

ISOLATES = "FORKERS"
"""
All isolates, including monitors and the current one, excluding forkers.
If the current isolate is a forker, it is excluded.
"""

CURRENT = "CURRENT"
""" Current isolate """

MONITORS = "MONITORS"
""" All monitors, including the current isolate if it is a monitor """

NEIGHBOURS = "NEIGHBOURS"
""" All isolates on the current node, excluding the current one """

OTHERS = "OTHERS"
""" All isolates, with monitors and forkers, but this one """

FORKER_ID_PREFIX = "org.psem2m.internals.isolates.forker"
""" Forkers isolate ID prefix """

MONITOR_ID_PREFIX = "org.psem2m.internals.isolates.monitor"
""" Monitors isolate ID prefix """

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-signals-directory-factory")
@Instantiate("psem2m-signals-directory")
@Provides("org.psem2m.signals.ISignalDirectory")
class SignalsDirectory(object):
    """
    Implementation of the PSEM2M Signals V2 directory
    """
    def __init__(self):
        """
        Constructor
        """
        self._context = None

        # The big lock
        self._lock = threading.RLock()

        # Isolate ID -> (node, port)
        self._accesses = {}

        # Group Name -> [isolates]
        self._groups = {}

        # Node name -> [isolates]
        self._nodes_isolates = {}

        # Node name -> host
        self._nodes_host = {}

        # Current isolate access port
        self._current_isolate_port = -1

        # Java API compliance (if possible)
        self.getAllIsolates = self.get_all_isolates
        self.getHostForNode = self.get_host_for_node
        self.getIsolateAccess = self.get_isolate_access
        self.getIsolateId = self.get_isolate_id
        self.getIsolateNode = self.get_isolate_node
        self.getIsolatesOnNode = self.get_isolates_on_node
        self.getLocalNode = self.get_local_node
        self.registerIsolate = self.register_isolate
        self.unregisterIsolate = self.unregister_isolate


    def dump(self):
        """
        Returns a snapshot of the directory.
        
        The result is a map with 3 entries :
        
        * 'accesses': Isolate ID -> {'node' -> Node Name, 'port' -> Port}
        * 'groups': Group Name -> [Isolates IDs]
        * 'nodes_host': Node Name -> Host Address
        
        :return: A snapshot of the directory
        """
        result = {}
        with self._lock:
            # Isolate accesses, converted into a map
            result["accesses"] = {}
            for isolate_id, access in self._accesses.items():

                if access[0] is None:
                    # Special case: current isolate
                    access = (self.get_local_node(), self._current_isolate_port)

                result["accesses"][isolate_id] = {"node": access[0],
                                                  "port": access[1]}

            for registry in ('groups', 'nodes_host'):
                # Prefix with an underscore to get the member
                result[registry] = getattr(self, '_{0}'.format(registry)).copy()

        return result


    def get_all_isolates(self, prefix, include_current):
        """
        Retrieves all known isolates which ID begins with the given prefix. If 
        the prefix is null or empty, returns all known isolates.
        
        Returns None if no isolate matched the prefix.
        
        :param prefix: An optional prefix filter
        :param include_current: If true, include the current isolate in the
                                result
        :return: A tuple of all known isolates beginning with prefix, or None
        """
        if not self._accesses:
            # Nothing to return
            return None

        with self._lock:
            if not prefix:
                # No prefix, use a copy of the known IDs
                matching = set(self._accesses.keys())

            else:
                # Construct the set
                matching = set()
                for isolate in self._accesses:
                    if isolate.startswith(prefix):
                        matching.add(isolate)

            if not include_current:
                # Remove the current isolate ID
                matching.remove(self.get_isolate_id())

            if not matching:
                # No isolate found
                return None

            # Return a tuple
            return tuple(matching)


    def get_computed_group_accesses(self, group_name):
        """
        Retrieves an Isolate ID -> (host, port) map, containing all known
        isolates that belong to the computed group
        
        Valid group names are:
        * ALL: all isolates, including the current one
        * FORKERS: All forkers, including the current isolate if it is a forker
        * ISOLATES: All isolates, including monitors and the current one,
          excluding forkers. If the current isolate is a forker, it is excluded.
        * CURRENT: Current isolate 
        * MONITORS: All monitors, including the current isolate if it is a
          monitor
        * NEIGHBOURS: All isolates on the current node, excluding the current
          one
        * OTHERS: All isolates, with monitors and forkers, excluding this one
        
        :param group_name: Name of the group to compute
        :return: An ID -> (host, port) map, None if the group is unknown.
        """
        isolate_id = self.get_isolate_id()
        matching = None

        with self._lock:
            if group_name == ALL:
                # Return all isolates, including the current one
                matching = self.get_all_isolates(None, True)

            elif group_name == OTHERS:
                # Return all isolates, excluding the current one
                matching = self.get_all_isolates(None, False)

            elif group_name == CURRENT:
                # Only the current isolate
                matching = [isolate_id]

            elif group_name == FORKERS:
                # Return only forkers, including the current one
                matching = self.get_all_isolates(FORKER_ID_PREFIX, True)

            elif group_name == MONITORS:
                # Return only monitors, including the current one
                matching = self.get_all_isolates(MONITOR_ID_PREFIX, True)

            elif group_name == ISOLATES:
                # Return all isolates but the forkers
                matching = self.get_all_isolates(None, True)
                if matching:
                    # Filter the list
                    matching = [isolate for isolate in matching
                                if not isolate.startswith(FORKER_ID_PREFIX)]

            elif group_name == NEIGHBOURS:
                # Return all isolates but the forkers
                matching = self.get_isolates_on_node(self.get_local_node())
                if matching:
                    matching.remove(isolate_id)

            else:
                # Unknown
                _logger.warning("Unknown directory group '%s'", group_name)
                matching = None

            if not matching:
                # No match found
                return None

            accesses = {}
            for isolate in matching:
                # Compute all accesses
                access = self.get_isolate_access(isolate)
                if access is not None:
                    accesses[isolate] = access

        return accesses


    def get_group_accesses(self, group_name):
        """
        Retrieves an Isolate ID -> (host, port) map, containing all known
        isolates that belong to the given group
        
        :param group_name: A group name
        :return: An ID -> (host, port) map, None if the group is unknown.
        """
        if not group_name:
            # Empty group name
            return None

        with self._lock:
            # Use lower case strings : group names are case insensitive
            group_name = group_name.lower()

            isolates = self._groups.get(group_name, None)
            if not isolates:
                # None or empty group is considered unknown
                return None

            accesses = {}
            for isolate in isolates:
                access = self.get_isolate_access(isolate)
                if access is not None:
                    accesses[isolate] = access

            if not accesses:
                # Nothing found, consider the group unknown
                return None

            return accesses


    def get_host_for_node(self, node):
        """
        Retrieves the host address for the given node
        
        :param node: A node name
        :return: The address of the node, or None
        """
        return self._nodes_host.get(node, None)


    def get_isolate_access(self, isolate_id):
        """
        Retrieves the (host, port) tuple to access the given isolate, or None
        
        :param isolate_id: An isolate ID
        :return: A (host, port) tuple or None if the isolate is unknown
        """
        # An ID is case-sensitive
        node_access = self._accesses.get(isolate_id, None)
        if not node_access:
            return None

        node_host = self._nodes_host.get(node_access[0], None)
        if not node_host:
            # No host for this node
            return None

        return (node_host, node_access[1])


    def get_isolate_id(self):
        """
        Retrieves the current isolate ID
        
        :return: the current isolate ID
        """
        return self._context.get_property('psem2m.isolate.id')


    def get_isolate_node(self, isolate):
        """
        Retrieves the name of the node hosting the given isolate
        
        :param isolate: An isolate ID
        :return: THe node hosting the isolate
        """
        if not isolate:
            return None

        with self._lock:
            for node, isolates in self._nodes_isolates.items():
                if isolate in isolates:
                    return node

        return None


    def get_isolates_on_node(self, node):
        """
        Retrieves the IDs of the isolates on the given node, or None
        
        :param node: The name of a node
        :return: A list of IDs, or None
        """
        with self._lock:
            isolates = self._nodes.get(node, None)
            if isolates is not None:
                # Return a copy, to avoid unwanted modifications
                return isolates[:]

            return None


    def get_local_node(self):
        """
        Retrieves the current isolate ID
        
        :return: the current isolate ID
        """
        return self._context.get_property('psem2m.isolate.node')


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        with self._lock:
            # Use the lock, as some consumer may still be there
            self._accesses.clear()
            self._groups.clear()
            self._nodes_host.clear()
            self._nodes_isolates.clear()

        self._context = None


    def register_isolate(self, isolate_id, node, port, groups):
        """
        Registers an isolate in the directory.
        
        :param isolate_id: The ID of the isolate to register
        :param node: The name of the node the isolate is running on
        :param port: The port to access the isolate
        :param groups: All groups of the isolate
        :return: True if the isolate has been registered
        :raise ValueError: An argument is invalid
        """
        if not isolate_id:
            raise ValueError("Invalid IsolateID : '%s'" % isolate_id)

        if not node:
            raise ValueError("Invalid node name for '%s' : '%s'" \
                             % (isolate_id, node))

        if isolate_id == self.get_isolate_id():
            # Ignore our own registration
            return True

        with self._lock:

            if isolate_id in self._accesses:
                # Already known isolate
                _logger.warning("Isolate already known: '%s'", isolate_id)
                return False

            # Store the isolate access
            self._accesses[isolate_id] = (node, port)

            # Store the node
            node_isolates = self._nodes_isolates.get(node, None)
            if node_isolates is None:
                # Create the node entry
                node_isolates = []
                self._nodes_isolates[node] = node_isolates

            if isolate_id not in node_isolates:
                node_isolates.append(isolate_id)

            # Store the isolate in all groups
            if groups:
                for group in groups:
                    if not group:
                        # Ignore empty names
                        continue

                    # Lower case for case-insensitivity
                    group = group.lower()

                    isolates = self._groups.get(group, None)
                    if isolates is None:
                        # Create the group if needed
                        isolates = []
                        self._groups[group] = isolates

                    if isolate_id not in isolates:
                        # Store the isolate
                        isolates.append(isolate_id)

            else:
                _logger.warning("The isolate '%s' has no group.", isolate_id)

        return True


    def register_local(self, port, groups):
        """
        Registers the local isolate in the registry
        
        :param port: The port to access the signals
        :param groups: The local isolate groups
        """
        isolate_id = self.get_isolate_id()
        node = self.get_local_node()

        with self._lock:
            # Store the isolate access port
            self._current_isolate_port = port

            # Store the node
            node_isolates = self._nodes_isolates.get(node, None)
            if node_isolates is None:
                # Create the node entry
                node_isolates = []
                self._nodes_isolates[node] = node_isolates

            if isolate_id not in node_isolates:
                node_isolates.append(isolate_id)

            # Store the isolate in all groups
            if groups:
                for group in groups:
                    # Empty names are ignored
                    if not group:
                        continue

                    # Lower case for case-insensitivity
                    group = group.lower()

                    isolates = self._groups.get(group, None)
                    if isolates is None:
                        # Create the group if needed
                        isolates = []
                        self._groups[group] = isolates

                    if isolate_id not in isolates:
                        # Store the isolate
                        isolates.append(isolate_id)


    def set_node_address(self, node, address):
        """
        Sets up the address to access the given node. Overrides the previous
        address and returns it.
        
        If the given address is null or empty, only returns the current node
        address.
        
        :param node: A node name
        :param address: The address to the node host
        :return: The previous address
        """
        with self._lock:
            old = self._nodes_host.get(node, None)
            if not address or address == old:
                # Nothing to do
                return old

            # Update the address if neither None nor empty
            self._nodes_host[node] = address
            return old


    def unregister_isolate(self, isolate_id):
        """
        Unregisters the given isolate of the directory
        
        :param isolate_id: The ID of the isolate to unregister
        :return: True if the isolate has been unregistered
        """
        if not isolate_id:
            # Nothing to do
            return False

        with self._lock:
            result = False

            # Remove the isolate access
            if isolate_id in self._accesses:
                del self._accesses[isolate_id]
                result = True

            # Remove references in nodes
            for isolates in self._nodes_isolates.values():
                if isolate_id in isolates:
                    # The isolate belongs to the group
                    isolates.remove(isolate_id)
                    result = True

            # Remove references in groups
            for isolates in self._groups.values():
                if isolate_id in isolates:
                    # The isolate belongs to the group
                    isolates.remove(isolate_id)
                    result = True

            return result


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._context = context

        # Special registration for the current isolate
        self._accesses[self.get_isolate_id()] = (None, -1)
        self._nodes_host[self.get_local_node()] = "localhost"
        self.register_local(-1, None)
