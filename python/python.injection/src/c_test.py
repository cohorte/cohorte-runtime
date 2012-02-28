#!/usr/bin/python3
#-- Content-Encoding: UTF-8 --

from psem2m.component.constants import IPOPO_INSTANCE_NAME
from psem2m.component.decorators import ComponentFactory, Property, Validate, \
    Invalidate, Provides, Requires
import ctypes
import logging
import threading
import time
from psem2m.component import constants
from psem2m.services.pelix import BundleException

# ------------------------------------------------------------------------------

SENDER_FACTORY = "C_Sender_Factory"
SENDER_SERVICE = "org.psem2m.ipopo.test.Sender"

SERVER_FACTORY = "C_Server_Factory"
SERVER_SERVICE = "org.psem2m.ipopo.test.Server"

STAT_FACTORY = "C_Stat_Factory"

_logger = logging.getLogger("ipopo.test")

# ------------------------------------------------------------------------------

def dlclose(handle):
    """
    Closes the shared library, using DL
    """
    libdl = ctypes.CDLL("libdl.so")
    libdl.dlclose(handle)

# ------------------------------------------------------------------------------

class CTest(object):
    """
    Common class for wrappers
    """

    def __init__(self):
        """
        Constructor
        """
        self.lib = None
        self.name = None
        self.pointer = None


    def load_library(self, lib_path):
        """
        Loads the C library with ctypes
        """
        self.lib = ctypes.CDLL(lib_path)
        return self.lib


    def close_library(self):
        """
        Closes the C library
        """
        if self.lib is not None:
            dlclose(self.lib._handle)
            self.lib = None


    def getName(self):
        return self.name


    def getTransfered(self):
        if self.lib is None:
            return -1

        return self.lib.get_transfered(self.pointer)

# ------------------------------------------------------------------------------

@ComponentFactory(name=SENDER_FACTORY)
@Provides(specifications=SENDER_SERVICE)
@Property(field="name", name=IPOPO_INSTANCE_NAME)
@Property(field="lib_path", name="lib.path")
@Property(field="port", name="sender.port", value=9000)
class Sender(CTest):
    """
    Wraps the sender API
    """

    def __init__(self):
        """
        Constructor
        """
        CTest.__init__(self)
        self.lib_path = None
        self.name = None
        self.running = False
        self.lock = threading.Lock()


    @Validate
    def start(self, context):
        """
        Component starts
        """
        self.running = True
        self.load_library(self.lib_path)

        # Indicate that the result is a pointer
        maker = self.lib.make_sender
        maker.restype = ctypes.c_void_p
        self.pointer = ctypes.c_void_p(maker(self.port))
        threading.Thread(target=self.run).start()


    @Invalidate
    def stop(self, context):
        """
        Component stops
        """
        self.running = False

        with self.lock:
            self.lib.free_compo(self.pointer)
            self.pointer = 0
            self.close_library()


    def send(self, data):
        """
        Send data
        """
        buffer = bytes(data, "UTF-8")

        with self.lock:
            if self.lib is None:
                # Library gone
                return

            return self.lib.send_data(self.pointer, buffer, len(buffer))


    def run(self):
        """
        Sender thread method
        """
        while self.running:
            time.sleep(1)
            self.send("I AM %s\n" % self.name)

# ------------------------------------------------------------------------------

@ComponentFactory(name=SERVER_FACTORY)
@Provides(specifications=SERVER_SERVICE)
@Property(field="name", name=IPOPO_INSTANCE_NAME)
@Property(field="lib_path", name="lib.path")
@Property(field="port", name="server.port", value=9000)
class Server(CTest):
    """
    Wraps the server API
    """

    def __init__(self):
        """
        Constructor
        """
        CTest.__init__(self)
        self.lib_path = None
        self.name = None


    @Validate
    def start(self, context):
        """
        Component starts
        """
        self.load_library(self.lib_path)

        # Indicate that the result is a pointer
        maker = self.lib.make_server
        maker.restype = ctypes.c_void_p
        self.pointer = ctypes.c_void_p(maker(self.port))


    @Invalidate
    def stop(self, context):
        """
        Component stops
        """
        self.lib.free_compo(self.pointer)
        self.pointer = 0
        self.close_library()


    def recv(self):
        """
        Receive data
        """
        size = 1024
        buffer = ctypes.create_string_buffer("", size)
        self.lib.recv_data(self.pointer, buffer, size)

        return buffer.value

# ------------------------------------------------------------------------------

@ComponentFactory(name=STAT_FACTORY)
@Provides(specifications="org.pelix.test.stat")
@Property(field="name", name=IPOPO_INSTANCE_NAME)
@Requires(field="compos", specification=[SENDER_SERVICE, SERVER_SERVICE], aggregate=True)
class StatsCompo(object):

    def __init__(self):
        """
        Constructor
        """
        self.lock = threading.RLock()
        self.running = False
        self.compos = None


    @Validate
    def start(self, context):
        """
        Component starts
        """
        self.running = True
        threading.Thread(target=self.run).start()


    @Invalidate
    def stop(self, context):
        """
        Component stops
        """
        self.running = False


    def run(self):
        """
        Run...
        """
        while self.running:

            if not self.compos:
                _logger.warning("No component bound")
                continue

            data = {}
            total = 0

            start = time.time()

            with self.lock:

                lock = time.time()

                if not self.running or self.compos is None:
                    # Injected services have been removed
                    break

                pre_loop = time.time()

                for compo in self.compos:
                    tx = compo.getTransfered()
                    data[compo.getName()] = tx
                    total += tx

                post_loop = time.time()

            end = time.time()

            _logger.info("%d components" % len(data))
            _logger.info("Timing - Lock: %.2f - Loop: %.2f - Total: %.2f",
                         lock - start, post_loop - pre_loop, end - start)

            time.sleep(2)

# ------------------------------------------------------------------------------

class BundleActivator:
    """
    The C test bundle activator
    """

    def start(self, context):
        """
        Called when the bundle is started
        """
        _logger.info("Starting bundle %s",
                     context.get_bundle().get_symbolic_name())

        lib_path = "/home/tcalmant/programmation/workspaces/psem2m/TestLib/Debug/libTestLib.so"

        # Get the iPOPO service
        self.ref = context.get_service_reference(
                                        constants.IPOPO_SERVICE_SPECIFICATION)
        if not self.ref:
            raise BundleException("MISSING iPOPO")

        ipopo = context.get_service(self.ref)

        # Instantiate components
        nb_compos = 0
        start = time.time()
        _logger.info("-- Instantiate server(s) --")
        for i in range(1, 64):
            name = "_server_%d_" % i
            ipopo.instantiate(SERVER_FACTORY, name, \
                              {"server.port": 9000 + i, "lib.path": lib_path})
            nb_compos += 1


        _logger.info("-- Instantiate sender(s) --")
        for i in range(64):
            name = "SENDER_%d" % i
            ipopo.instantiate(SENDER_FACTORY, name, \
                              {"sender.port": 9000 + i, "lib.path": lib_path})
            nb_compos += 1

        _logger.info("-- Instantiate stats --")
        ipopo.instantiate(STAT_FACTORY, "stats")
        nb_compos += 1

        end = time.time()

        _logger.info("Bundle %s started %d components in %.3f s", \
                     context.get_bundle().get_symbolic_name(), nb_compos,
                     (end - start))


    def stop(self, context):
        """
        Called when the bundle is stopped
        """
        # Release the iPOPO service
        if self.ref:
            context.unget_service(self.ref)

        _logger.info("Stopping bundle %s", context.get_bundle().get_symbolic_name())


# The activator instance
activator = BundleActivator()
