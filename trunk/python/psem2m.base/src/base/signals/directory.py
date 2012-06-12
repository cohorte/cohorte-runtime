#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
Python implementation of the PSEM2M Signals directory

Created on 12 juin 2012

:author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

import logging
import threading

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate, Instantiate

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

@ComponentFactory("psem2m-signals-directory-factory")
@Instantiate("psem2m-signals-directory")
@Provides("org.psem2m.signals.IDirectory")
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
        self._lock = threading.Lock()

        # Isolate ID -> (host, port)
        self._accesses = {}

        # Group Name -> [isolates]
        self._groups = {}

        # Java API compliance (if possible)
        self.getIsolateId = self.get_isolate_id
        self.registerIsolate = self.register_isolate
        self.unregisterIsolate = self.unregister_isolate


    def get_isolate_id(self):
        """
        Retrieves the current isolate ID
        
        :return: the current isolate ID
        """
        return self._context.get_property('psem2m.isolate.id')


    def get_group_accesses(self, group_name):
        """
        Retrieves an Isolate ID -> (host, port) map, containing all known
        isolates that belong to given group
        
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
                access = self._accesses.get(isolate, None)
                if access is not None:
                    accesses[isolate] = access

            if not accesses:
                # Nothing found, consider the group unknown
                return None

            return accesses


    def get_isolate_access(self, isolate_id):
        """
        Retrieves the (host, port) tuple to access the given isolate, or None
        
        :param isolate_id: An isolate ID
        :return: A (host, port) tuple or None if the isolate is unknown
        """
        if not isolate_id:
            # Empty isolate ID
            return None

        with self._lock:
            # An ID is case-sensitive
            return self._accesses.get(isolate_id, None)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        with self._lock:
            # Use the lock, as some consumer may still be there
            self._accesses.clear()
            self._groups.clear()

        self._context = None


    def register_isolate(self, isolate_id, host, port, *groups):
        """
        Registers an isolate in the directory.
        
        :param isolate_id: The ID of the isolate to register
        :param host: The host of the isolate
        :param port: The port to access the isolate
        :param groups: All groups of the isolate
        :raise ValueError: An argument is invalid
        """
        if not isolate_id:
            raise ValueError("Invalid IsolateID : '%s'" % isolate_id)

        if not host:
            raise ValueError("Invalid host for isolate '%s' : '%s'" \
                             % (isolate_id, host))

        if isolate_id == self.get_current_isolate_id():
            # Ignore our own registration
            return

        with self._lock:
            # Store the isolate access
            self._accesses[isolate_id] = (host, port)

            # Store the isolate in all groups
            if groups:
                for group in groups:
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

            for group in self._groups:
                isolates = self._groups[group]
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

        # Special access for local isolate
        self.accesses[self.get_current_isolate_id()] = "{local}"
