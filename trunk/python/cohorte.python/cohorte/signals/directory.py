#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Python implementation of the PSEM2M Signals directory

Created on 12 juin 2012

**TODO:**
* Update
* Review

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# COHORTE constants
import cohorte

# Pelix/iPOPO
from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate, Requires, Bind

import pelix.framework as pelix

# ------------------------------------------------------------------------------

import logging
_logger = logging.getLogger(__name__)

import threading

# ------------------------------------------------------------------------------

ALL = "ALL"
""" All isolates, including the current one """

FORKERS = "FORKERS"
""" All forkers, including the current isolate if it is a forker """

ISOLATES = "ISOLATES"
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

FORKER_NAME = "org.psem2m.internals.isolates.forker"
""" Forkers isolate ID prefix """

MONITOR_NAME = "org.psem2m.internals.isolates.monitor"
""" Monitors isolate ID prefix """

# ------------------------------------------------------------------------------

SPEC_LISTENER = "org.psem2m.isolates.services.monitoring." \
                "IIsolatePresenceListener"

# FIXME: use Jabsorb enumeration dictionaries
REGISTERED = 0
""" Isolate presence event: Isolate registered """

UNREGISTERED = 1
""" Isolate presence event: Isolate unregistered or lost """

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-signals-directory-factory")
@Requires("_listeners", SPEC_LISTENER, aggregate=True, optional=True)
@Provides(cohorte.SERVICE_SIGNALS_DIRECTORY)
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

        # Listeners
        self._listeners = []

        # Isolate UID -> (node, port)
        self._accesses = {}

        # Isolate UID -> Isolate Name
        self._names = {}

        # Current isolate access port
        self._current_isolate_port = -1

        # Node name -> [isolates]
        self._nodes_isolates = {}

        # Node name -> host
        self._nodes_host = {}

        # Isolates waiting for validation
        self._waiting_isolates = set()


    @Bind
    def _bind(self, svc, svc_ref):
        """
        Notifies newly bound listener of known isolates presence
        """
        specs = svc_ref.get_property(pelix.OBJECTCLASS)
        if SPEC_LISTENER in specs:
            # New listener bound
            with self._lock:
                for isolate_id in self.get_all_isolates(None, False):
                    isolate_node = self.get_isolate_node(isolate_id)
                    svc.handle_isolate_presence(isolate_id, isolate_node,
                                                REGISTERED)


    def _notify_listeners(self, isolate_id, isolate_node, event):
        """
        Notifies listeners of an isolate presence event
        
        :param isolate_id: ID of the isolate
        :param isolate_node: Node of the isolate
        :param event: Kind of event
        """
        if not self._listeners:
            # No listeners
            return

        def notification_loop(listeners):
            """
            Notifies isolate presence listeners (should be ran in a thread)
            """
            # Use a copy of the listeners
            for listener in listeners:
                try:
                    listener.handle_isolate_presence(isolate_id, isolate_node,
                                                     event)
                except:
                    # Just log...
                    _logger.exception("Error notifying a presence listener")

        # Notify in another thread, with a copy of the listeners list
        listeners = self._listeners[:]
        threading.Thread(target=notification_loop, args=[listeners]).start()


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
                                                  "port": access[1],
                                                  "name": self._names[isolate_id]
                                                  }

            # Copy the node -> host name association
            result['nodes_host'] = self._nodes_host.copy()

        return result


    def store_dump(self, dump, ignored_nodes=None, ignored_ids=None):
        """
        Stores the result of a dump
        
        :param dump: A dictionary, result of dump()
        :param ignored_nodes: A list of ignored nodes
        :param ignored_ids: A list of ignored IDs
        """
        with self._lock:
            # 0. Always ignore the current isolate and the current node
            local_uid = self.get_isolate_uid()
            local_node = self.get_local_node()

            if ignored_nodes is None:
                ignored_nodes = [local_node]
            elif local_node not in ignored_nodes:
                ignored_nodes.append(local_node)

            if ignored_ids is None:
                ignored_ids = [local_uid]
            elif local_uid not in ignored_ids:
                ignored_ids.append(local_uid)

            # 1. Setup nodes hosts
            for node, dumped_host in dump["nodes_host"].items():
                if node not in ignored_nodes:
                    self.set_node_address(node, dumped_host)

            # 2. Prepare isolates information
            filtered_isolates = {}
            for isolate_id, access in dump["accesses"].items():
                # Access URL
                if isolate_id not in ignored_ids:
                    filtered_isolates[isolate_id] = access

            # 3. Register all new isolates
            new_isolates = []
            for isolate_id, info in filtered_isolates.items():
                try:
                    if self.register_isolate(isolate_id, info["name"],
                                             info["node"], info["port"]):
                        new_isolates.append(isolate_id)

                except KeyError:
                    _logger.warning("Missing information to register '%s': %s",
                                    isolate_id, info)

            # Return the list of newly registered isolates
            if not new_isolates:
                return None

            return new_isolates


    def get_all_isolates(self, prefix, include_current):
        """
        Retrieves all known isolates which ID begins with the given prefix. If 
        the prefix is null or empty, returns all known isolates.
        
        Returns None if no isolate matched the prefix.
        
        :param prefix: An optional prefix filter
        :param include_current: If true, include the current isolate in the
                                result
        :return: A tuple of all known isolates beginning with prefix, or
                 an empty one
        """
        if not self._accesses:
            # Nothing to return
            return tuple()

        with self._lock:
            if not prefix:
                # No prefix, use a copy of the known IDs
                matching = set(self._accesses.keys())

            else:
                # Construct the set
                matching = set()
                for isolate, name in self._names.items():
                    if name.startswith(prefix):
                        matching.add(isolate)

            if not include_current:
                # Remove the current isolate ID
                matching.remove(self.get_isolate_uid())

            if not matching:
                # No isolate found
                return tuple()

            # Return a tuple
            return tuple(matching)


    def get_all_nodes(self):
        """
        Retrieves all known nodes. Returns None if no nodes are known
        
        :return: All known nodes, or None
        """
        with self._lock:
            if not self._nodes_isolates:
                return None

            return tuple(self._nodes_isolates.keys())


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
        isolate_id = self.get_isolate_uid()
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
                matching = self.get_all_isolates(FORKER_NAME, True)

            elif group_name == MONITORS:
                # Return only monitors, including the current one
                matching = self.get_all_isolates(MONITOR_NAME, True)

            elif group_name == ISOLATES:
                # Return all isolates but the forkers
                matching = self.get_all_isolates(None, True)
                if matching:
                    # Filter the list
                    matching = [isolate for isolate in matching
                                if not isolate.startswith(FORKER_NAME)]

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


    def get_isolate_uid(self):
        """
        Retrieves the current isolate ID
        
        :return: the current isolate ID
        """
        return self._context.get_property(cohorte.PROP_UID)


    def get_isolate_name(self, uid):
        """
        Retrieves the name of the given isolate
        
        :param UID: An isolate UID
        :return: The name of the isolate, or None
        """
        return self._names.get(uid)


    def get_isolate_node(self, uid):
        """
        Retrieves the node hosting the given isolate
        
        :param uid: An isolate UID
        :return: THe node hosting the isolate
        """
        if not uid:
            return None

        with self._lock:
            for node, isolates in self._nodes_isolates.items():
                if uid in isolates:
                    return node

        return None


    def get_isolates_on_node(self, node):
        """
        Retrieves the IDs of the isolates on the given node, or None
        
        :param node: The name of a node
        :return: A list of IDs, or None
        """
        with self._lock:
            isolates = self._nodes_isolates.get(node, None)
            if isolates is not None:
                # Return a copy, to avoid unwanted modifications
                return isolates[:]

            return None


    def get_local_node(self):
        """
        Retrieves the current isolate ID
        
        :return: the current isolate ID
        """
        return self._context.get_property(cohorte.PROP_NODE)


    def get_name_uids(self, name):
        """
        Retrieves the UIDs of the isolate having the given name
        
        :param name: An isolate name
        :return: A list of isolate UIDs associated to that name
        """
        if not name:
            return

        uids = set()
        for isolate_uid, isolate_name in self._names.items():
            if isolate_name == name:
                uids.add(isolate_uid)

        return list(uids)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        with self._lock:
            # Use the lock, as some consumer may still be there
            self._accesses.clear()
            self._names.clear()
            self._nodes_host.clear()
            self._nodes_isolates.clear()

        self._context = None


    def is_registered(self, isolate_id):
        """
        Tests if the given isolate ID is registered in the directory
        
        :param isolate_id: An isolate ID
        :return: True if the ID is known, else false
        """
        with self._lock:
            return isolate_id in self._accesses


    def register_isolate(self, uid, name, node, port, validated=False):
        """
        Registers an isolate in the directory.
        
        :param uid: The UID of the isolate to register
        :param name: The name of the isolate to register
        :param node: The node the isolate is running on
        :param port: The port to access the isolate
        :param validated: If True, the isolate doesn't need the validation
        :return: True if the isolate has been registered
        :raise ValueError: An argument is invalid
        """
        if not uid:
            raise ValueError("Empty UID: {0} ({1})".format(uid, name))

        if not name:
            raise ValueError("Empty name: {0} ({1})".format(uid, name))

        if not node:
            raise ValueError("Empty node name for isolate {0} ({1})" \
                             .format(uid, name))

        with self._lock:
            # Prepare the new access tuple
            new_access = (node, port)

            # Get the previous access, if any
            old_access = self._accesses.get(uid, None)
            if old_access is not None:
                # Already known isolate
                if old_access == new_access:
                    # No update needed
                    return False

                else:
                    # Log the update
                    _logger.debug("Already known isolate '%s'"
                                  " - Updated from %s to %s",
                                  uid, self._accesses[uid], new_access)

                old_node = old_access[0]
                if node != old_node:
                    # Isolate moved to another node -> remove the old entry
                    _logger.info("Isolate '%s' moved from %s to %s",
                                 uid, old_node, node)

                    node_isolates = self._nodes_isolates.get(old_node, None)
                    if node_isolates is not None:
                        node_isolates.remove(uid)

                    # Notify the unregistration
                    self._notify_listeners(uid, old_node, UNREGISTERED)

            # Store the isolate access
            self._accesses[uid] = new_access

            # Store the name
            self._names[uid] = name

            # Store the node
            node_isolates = self._nodes_isolates.get(node, None)
            if node_isolates is None:
                # Create the node entry
                node_isolates = []
                self._nodes_isolates[node] = node_isolates

            if uid not in node_isolates:
                node_isolates.append(uid)

            _logger.debug("Registered isolate ID=%s, Name=%s, Access=%s",
                          uid, name, new_access)

            if not validated:
                # Isolate waits for validation
                self._waiting_isolates.add(uid)

            else:
                # Notify registration
                self._notify_listeners(uid, node, REGISTERED)

        return True


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
        with self._lock:
            if not isolate_id or isolate_id not in self._accesses:
                # Nothing to do
                return False

            # Remove the isolate access
            del self._accesses[isolate_id]

            # Remove isolate reference in its node
            isolate_node = None
            for node, isolates in self._nodes_isolates.items():
                if isolate_id in isolates:
                    # Found the isolate node
                    isolates.remove(isolate_id)
                    isolate_node = node
                    break

            # Remove references in names
            del self._names[isolate_id]

            if isolate_id not in self._waiting_isolates:
                # Notify listeners
                self._notify_listeners(isolate_id, isolate_node, UNREGISTERED)

            else:
                self._waiting_isolates.remove(isolate_id)

            return True


    def validate_isolate_presence(self, isolate_id):
        """
        Notifies the directory that an isolate has acknowledged the registration
        of the current isolate.
        
        :param aIsolateId: An isolate ID
        """
        with self._lock:
            if isolate_id in self._waiting_isolates:
                self._waiting_isolates.remove(isolate_id)

                _logger.debug("Isolate %s validated", isolate_id)
                self._notify_listeners(isolate_id,
                                       self.get_isolate_node(isolate_id),
                                       REGISTERED)


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._context = context

        # Special registration for the current isolate
        self._nodes_host[self.get_local_node()] = "localhost"
        self.register_isolate(self.get_isolate_uid(),
                              self._context.get_property(cohorte.PROP_NAME),
                              self.get_local_node(), -1, True)
