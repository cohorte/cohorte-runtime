"""
Created on 25 janv. 2012

@author: Thomas Calmant
"""

import threading

# ------------------------------------------------------------------------------

class Synchronized:
    """
    A synchronizer for global methods
    """
    def __init__(self, lock):
        """
        Sets up the decorator. If 'lock' is None, an RLock() is created for
        this decorator.
        
        @param lock: The lock to be used for synchronization (can be None)
        """
        if not is_lock(lock):
            self.__lock = threading.RLock()

        else:
            self.__lock = lock


    def __call__(self, method):
        """
        Sets up the decorated method
        
        @param method: The decorated method
        @return: The wrapped method
        """

        def wrapped(*args, **kwargs):
            """
            The wrapping method
            """
            with self.__lock:
                return method(*args, **kwargs)

        return wrapped


def SynchronizedClassMethod(lock_attr_name):
    """
    A synchronizer decorator for class methods. An AttributeError can be raised
    at runtime if the given lock attribute doesn't exist or if it is None.
    
    @param lock_attr_name: The lock attribute name to be used for
    synchronization
    @return: The decorator method, surrounded with the lock 
    """
    if not lock_attr_name:
        raise ValueError("The lock name can't be empty")

    def wrapped(method):
        """
        The wrapping method
        
        @param method: The wrapped method
        @return: The wrapped method
        @raise AttributeError: The given attribute name doesn't exist
        """
        def synchronized(self, *args, **kwargs):
            """
            Calls the wrapped method with a lock
            """
            # Raises an AttributeError if needed
            lock = getattr(self, lock_attr_name)

            if lock is None:
                # No lock...
                raise AttributeError("Lock '%s' can't be None in class %s" \
                                     % (lock_attr_name, type(self).__name__))

            with lock:
                # Use it
                return method(self, *args, **kwargs)

        return synchronized

    # Return the wrapped method
    return wrapped


def is_lock(lock):
    """
    Tests if the given lock is an instance of a lock class
    """
    if lock is None:
        # Don't do useless tests
        return False

    lock_types = (threading.Lock, threading.RLock, threading.Semaphore,
                  threading.Condition)

    for lock_type in lock_types:
        if isinstance(lock, lock_type):
            # Known type
            return True

    lock_api = ('acquire', 'release', '__enter__', '__exit__')

    for attr in lock_api:
        if hasattr(lock, attr):
            # Same API as a lock
            return True

    return False

# ------------------------------------------------------------------------------

def read_only_property(value):
    """
    Makes a read-only property that always returns the given value
    """
    return property(lambda cls: value)

# ------------------------------------------------------------------------------

def remove_all_occurrences(sequence, item):
    """
    Removes all occurrences of item in the given sequence
    
    @param sequence: The items list
    @param item: The item to be removed
    """
    if sequence is None:
        return

    while item in sequence:
        sequence.remove(item)
