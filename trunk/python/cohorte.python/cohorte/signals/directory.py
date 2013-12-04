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

# Module version
__version__ = "1.0.1"

# ------------------------------------------------------------------------------

# COHORTE constants
import cohorte.forker
import cohorte.monitor
import cohorte.signals

# iPOPO
from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate, Requires, BindField

# Standard library
import logging
import threading

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-signals-directory-factory")
@Requires("_listeners", cohorte.signals.SERVICE_ISOLATE_PRESENCE_LISTENER,
          aggregate=True, optional=True)
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

        # Current isolate access port
        self._current_isolate_port = -1

        # Isolate UID -> (node UID, port)
        self._accesses = {}

        # Isolate UID -> Isolate Name
        self._names = {}

        # Node UID -> Host name/address
        self._nodes_host = {}

        # Node UID -> set(Isolate UID)
        self._nodes_isolates = {}

        # Node name -> set(Node UID)
        self._nodes_names = {}

        # Isolates waiting for validation
        self._waiting_isolates = set()


    @BindField('_listeners')
    def _bind_listener(self, field, svc, svc_ref):
        """
        Notifies newly bound listener of known isolates presence
        """
        with self._lock:
            for isolate_id in self.get_all_isolates(None, False):
                isolate_node = self.get_isolate_node(isolate_id)
                svc.handle_isolate_presence(isolate_id, isolate_node,
                                            cohorte.signals.ISOLATE_REGISTERED)


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

                result["accesses"][isolate_id] = {
                                             "node_uid": access[0],
                                             "port": access[1],
                                             "name": self._names[isolate_id]
                                             }

            # Copy the node UID -> host Name association
            result['nodes_host'] = self._nodes_host.copy()

            # Copy the node Name -> node UIDs registry
            result['nodes_names'] = self._nodes_names.copy()

        return result


    def store_dump(self, dump, ignored_nodes=None, ignored_isolates=None):
        """
        Stores the result of a dump

        :param dump: A dictionary, result of dump()
        :param ignored_nodes: A list of ignored nodes UIDs
        :param ignored_isolates: A list of ignored isolate UIDs
        """
        with self._lock:
            # 0. Always ignore the current isolate and the current node
            local_uid = self.get_isolate_uid()
            local_node_uid = self.get_local_node()

            # Always ignore the local node UID
            if ignored_nodes is None:
                ignored_nodes = set()
            else:
                ignored_nodes = set(ignored_nodes)

            ignored_nodes.add(local_node_uid)

            # Always ignore the local UIDs
            if ignored_isolates is None:
                ignored_isolates = set()
            else:
                ignored_isolates = set(ignored_isolates)

            ignored_isolates.add(local_uid)

            # 1. Setup nodes hosts
            for node_uid, dumped_host in dump["nodes_host"].items():
                if node_uid not in ignored_nodes:
                    self.set_node_address(node_uid, dumped_host)

            # 2. Setup nodes names
            for node_name, node_uids in dump["nodes_names"].items():
                node_uids = set(node_uids).difference(ignored_nodes)
                self._nodes_names.setdefault(node_name, set()).update(node_uids)

            # 3. Prepare isolates information
            filtered_isolates = {}
            for isolate_uid, info in dump["accesses"].items():
                # Access URL
                if isolate_uid not in ignored_isolates:
                    filtered_isolates[isolate_uid] = info

            # 3. Register all new isolates
            new_isolates = []
            for isolate_id, info in filtered_isolates.items():
                try:
                    if self.register_isolate(isolate_id, info["name"],
                                             info["node_uid"], info["port"]):
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
                try:
                    matching.remove(self.get_isolate_uid())

                except KeyError:
                    # The current isolate ID wasn't in the computed list
                    pass

            if not matching:
                # No isolate found
                return tuple()

            # Return a tuple
            return tuple(matching)


    def get_all_nodes(self):
        """
        Retrieves all known nodes UID. Returns None if no nodes are known

        :return: All known nodes UID, or None
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
            if group_name == cohorte.signals.GROUP_ALL:
                # Return all isolates, including the current one
                matching = self.get_all_isolates(None, True)

            elif group_name == cohorte.signals.GROUP_OTHERS:
                # Return all isolates, excluding the current one
                matching = self.get_all_isolates(None, False)

            elif group_name == cohorte.signals.GROUP_CURRENT:
                # Only the current isolate
                matching = [isolate_id]

            elif group_name == cohorte.signals.GROUP_FORKERS:
                # Return only forkers, including the current one
                matching = self.get_all_isolates(cohorte.forker.FORKER_NAME,
                                                 True)

            elif group_name == cohorte.signals.GROUP_MONITORS:
                # Return only monitors, including the current one
                matching = self.get_all_isolates(cohorte.monitor.MONITOR_NAME,
                                                 True)

            elif group_name == cohorte.signals.GROUP_ISOLATES:
                # Return all isolates but the forkers
                matching = self.get_all_isolates(None, True)
                if matching:
                    # Filter the list
                    matching = [isolate for isolate in matching
                                if not isolate.startswith(\
                                                  cohorte.forker.FORKER_NAME)]

            elif group_name == cohorte.signals.GROUP_NEIGHBOURS:
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


    def get_host_for_node(self, node_uid):
        """
        Retrieves the host address for the given node UID

        :param node: A node UID
        :return: The address of the node, or None
        """
        return self._nodes_host.get(node_uid, None)


    def get_isolate_access(self, isolate_uid):
        """
        Retrieves the (host, port) tuple to access the given isolate, or None

        :param isolate_uid: An isolate UID
        :return: A (host, port) tuple or None if the isolate is unknown
        """
        try:
            node_access = self._accesses[isolate_uid]
            node_host = self._nodes_host[node_access[0]]

        except KeyError:
            # Unknown isolate or node
            return None

        else:
            return (node_host, node_access[1])


    def get_isolate_uid(self):
        """
        Retrieves the current isolate ID

        :return: the current isolate ID
        """
        return self._context.get_property(cohorte.PROP_UID)


    def get_isolate_name(self, isolate_uid=None):
        """
        Retrieves the name of the given isolate

        :param isolate_uid: An isolate UID
        :return: The name of the isolate, or None
        """
        if isolate_uid is None:
            return self._context.get_property(cohorte.PROP_NAME)

        return self._names.get(isolate_uid)


    def get_isolate_node(self, isolate_uid=None):
        """
        Retrieves the node hosting the given isolate, or None

        :param isolate_uid: An isolate UID
        :return: The UID of the node hosting the isolate, or the current node
                 UID if isolate_uid is empty, or None
        """
        if not isolate_uid:
            return self._context.get_property(cohorte.PROP_NODE_UID)

        with self._lock:
            for node, isolates in self._nodes_isolates.items():
                if isolate_uid in isolates:
                    return node


    def get_isolates_on_node(self, node_uid):
        """
        Retrieves the UIDs of the isolates on the given node, or None

        :param node_uid: The UID of a node
        :return: A set of isolate UIDs (can be empty)
        """
        with self._lock:
            try:
                return self._nodes_isolates[node_uid].copy()

            except KeyError:
                return set()


    def get_local_node(self):
        """
        Retrieves the current node UID

        :return: the current node UID
        """
        return self._context.get_property(cohorte.PROP_NODE_UID)


    def get_name_uids(self, name):
        """
        Generator that retrieves the UIDs of the isolate having the given name

        :param name: An isolate name
        :return: A list of isolate UIDs associated to that name
        """
        uids = set()
        for isolate_uid, isolate_name in self._names.items():
            if isolate_name == name and isolate_uid not in uids:
                uids.add(isolate_uid)
                yield isolate_uid


    def get_node_name(self, node_uid=None):
        """
        Returns the name of the node with the given UID

        :param node_uid: The UID of a node
        :return: The node name, or None
        """
        if node_uid is None:
            return self._context.get_property(cohorte.PROP_NODE_NAME)

        with self._lock:
            for name, uids in self._nodes_names.items():
                if node_uid in uids:
                    return name


    def get_node_uids(self, node_name):
        """
        Returns the UIDs of the nodes with the given name

        :param node_name: The name of a (set of) node(s)
        :return: A set of node UIDs (can be empty)
        """
        with self._lock:
            try:
                return self._nodes_names[node_name].copy()

            except KeyError:
                return set()


    def get_uids(self, isolate_name_or_uid=None):
        """
        Retrieves a list of isolate UIDs corresponding to the given isolate
        name or UID.
        Returns the list of all known UIDs if the parameter is None.

        :param isolate_name_or_uid: The name or UID of an isolate (or None)
        :return: A list of UIDs
        :raise KeyError: Unknown isolate
        """
        if isolate_name_or_uid is None:
            # Return every thing (except us)
            uids = list(self.get_all_isolates(None, False))

        elif self.is_registered(isolate_name_or_uid):
            # Consider the argument as a UID
            uids = [isolate_name_or_uid]

        else:
            # Consider the given argument as a name, i.e. multiple UIDs
            uids = list(self.get_name_uids(isolate_name_or_uid))

        if not uids:
            # No matching UID
            raise KeyError("Unknown UID/Name: {0}".format(isolate_name_or_uid))

        # Sort names, to always have the same result
        uids.sort()
        return uids


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


    def register_isolate(self, uid, name, node_uid, port, validated=False):
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

        if not node_uid:
            raise ValueError("Empty node UID for isolate {0} ({1})" \
                             .format(uid, name))

        with self._lock:
            # Prepare the new access tuple
            new_access = (node_uid, port)

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

                old_node_uid = old_access[0]
                if node_uid != old_node_uid:
                    # Isolate moved to another node -> remove the old entry
                    _logger.info("Isolate '%s' moved from %s to %s",
                                 uid, old_node_uid, node_uid)

                    try:
                        node_isolates = self._nodes_isolates[old_node_uid]
                        node_isolates.remove(uid)
                        if not node_isolates:
                            del self._nodes_isolates[old_node_uid]

                    except KeyError:
                        # Unknown UID
                        pass

                    # Notify about the removal
                    self._notify_listeners(uid, old_node_uid,
                                           cohorte.signals.ISOLATE_UNREGISTERED)

            # Store the isolate access
            self._accesses[uid] = new_access

            # Store the name
            self._names[uid] = name

            # Store the node
            self._nodes_isolates.setdefault(node_uid, set()).add(uid)

            _logger.debug("Registered isolate UID=%s, Name=%s, Access=%s",
                          uid, name, new_access)

            if not validated:
                # Isolate waits for validation
                self._waiting_isolates.add(uid)

            else:
                # Notify about the registration
                self._notify_listeners(uid, node_uid,
                                       cohorte.signals.ISOLATE_REGISTERED)

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


    def set_node_name(self, node_uid, node_name):
        """
        Associates the given node UID to a node name

        :param node_uid: UID of the node
        :param node_name: Name of the node
        """
        with self._lock:
            for name, uids in self._nodes_names.items():
                if node_uid in uids:
                    if name == node_name:
                        # Nothing to do
                        return

                    else:
                        # UID was known for another name
                        _logger.warning("Node %s renamed from %s to %s",
                                        node_uid, name, node_name)
                        uids.remove(node_uid)
                        break

            self._nodes_names.setdefault(node_name, set()).add(node_uid)


    def remove_node(self, node_uid):
        """
        Removes the references to the given node UID, including all of its
        isolates.

        :param node_uid: UID of the node to remove
        """
        with self._lock:
            # Remove access (to avoid sending signals there)
            del self._nodes_host[node_uid]

            # Unregister corresponding isolates
            for isolate_id in self._nodes_isolates[node_uid].copy():
                self.unregister_isolate(isolate_id)

            # Remove node name reference
            for name, uids in self._nodes_names.items():
                try:
                    uids.remove(node_uid)
                    if not uids:
                        # Full clean up
                        del self._nodes_names[name]

                    break

                except KeyError:
                    # Not there
                    pass

            # Final clean up
            del self._nodes_isolates[node_uid]


    def unregister_isolate(self, isolate_uid):
        """
        Unregisters the given isolate of the directory

        :param isolate_uid: The UID of the isolate to unregister
        :return: True if the isolate has been unregistered
        """
        with self._lock:
            if not isolate_uid or isolate_uid not in self._accesses:
                # Nothing to do
                return False

            # Remove the isolate access
            del self._accesses[isolate_uid]

            # Remove isolate reference in its node
            node_uid = None
            for node, node_isolates in self._nodes_isolates.items():
                try:
                    node_isolates.remove(isolate_uid)
                    # No exception: UID was here
                    node_uid = node
                    break

                except KeyError:
                    # Not on this node
                    pass

            # Remove references in names
            del self._names[isolate_uid]

            try:
                # Remove from the waiting set
                self._waiting_isolates.remove(isolate_uid)

            except KeyError:
                # Wasn't in the waiting set, notify listeners
                self._notify_listeners(isolate_uid, node_uid,
                                       cohorte.signals.ISOLATE_UNREGISTERED)

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
                                       cohorte.signals.ISOLATE_REGISTERED)


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
