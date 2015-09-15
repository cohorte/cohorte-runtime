"""
Original code from PyObjC-Cocoa
See: https://bitbucket.org/ronaldoussoren/pyobjc/

AppKit helpers.

Exported functions:
* runEventLoop - run NSApplicationMain in a safer way
* stopEventLoop - stops the event loop or terminates the application
* callAfter - call a function on the main thread (async)
"""

# Use cocoa-python instead of PyObjC
import logging
import sys
import traceback

from cohorte.cocoapy import ObjCClass, ObjCSubclass, ObjCInstance, \
    send_super, PyObjectEncoding

# ------------------------------------------------------------------------------

__all__ = ('runEventLoop', 'stopEventLoop', 'callAfter')

_logger = logging.getLogger(__name__)

# Load Cocoa classes
# pylint: disable=C0103
NSApplication = ObjCClass('NSApplication')
NSRunAlertPanel = ObjCClass('NSRunAlertPanel')
NSApplicationMain = ObjCClass('NSApplicationMain')
NSApplicationDidFinishLaunchingNotification = \
    ObjCClass('NSApplicationDidFinishLaunchingNotification')

NSObject = ObjCClass('NSObject')
NSRunLoop = ObjCClass('NSRunLoop')
NSTimer = ObjCClass('NSTimer')
NSDefaultRunLoopMode = ObjCClass('NSDefaultRunLoopMode')
NSNotificationCenter = ObjCClass('NSNotificationCenter')
NSLog = ObjCClass('NSLog')
NSAutoreleasePool = ObjCClass('NSAutoreleasePool')

# ------------------------------------------------------------------------------


def NSApp():
    """
    Defines a method equivalent to the NSApp() call from PyObjC

    :return: The application instance
    """
    return NSApplication.sharedApplication()

# ------------------------------------------------------------------------------


class PyObjCAppHelperCaller_Implementation(object):
    """
    Implementation of the call of methods in the main-thread
    """
    PyObjCAppHelperCaller = ObjCSubclass('NSObject', 'PyObjCAppHelperCaller')

    def initWithArgs_(self, args):
        """
        Sets up the Objective-C subclass
        """
        new_self = ObjCInstance(send_super(self, 'init'))
        new_self.args = args
        return new_self

    @PyObjCAppHelperCaller.method(b'v' + PyObjectEncoding)
    def callAfter_(self, sender):
        """
        Waits for a call to be done
        """
        self.performSelectorOnMainThread_withObject_waitUntilDone_(
            self.call_, self.args, False)

    @staticmethod
    def call_(func_args_kwargs):
        """
        Where the method call really happens
        """
        (func, args, kwargs) = func_args_kwargs
        func(*args, **kwargs)

PyObjCAppHelperCaller = ObjCClass('PyObjCAppHelperCaller')


def callAfter(func, *args, **kwargs):
    """
    Call the given function on the main thread (async)
    """
    pool = NSAutoreleasePool.alloc().init()
    obj = PyObjCAppHelperCaller.alloc().initWithArgs_((func, args, kwargs))
    obj.callAfter_(None)
    del obj
    del pool

# ------------------------------------------------------------------------------


class PyObjCAppHelperRunLoopStopper(object):
    """
    A helper method to handle multiple run loops and their stopper.
    """
    singletons = {}

    def __init__(self):
        self.shouldStop = False

    @classmethod
    def currentRunLoopStopper(cls):
        """
        Returns the stopper of the current loop stopper
        """
        run_loop = NSRunLoop.currentRunLoop()
        return cls.singletons.get(run_loop)

    def shouldRun(self):
        """
        Checks if the event loop should continue
        """
        return not self.shouldStop

    @classmethod
    def addRunLoopStopper_toRunLoop_(cls, run_loop_stopper, run_loop):
        """
        Adds a loop stopper
        """
        if run_loop in cls.singletons:
            raise ValueError("Stopper already registered for this runLoop")
        cls.singletons[run_loop] = run_loop_stopper

    @classmethod
    def removeRunLoopStopperFromRunLoop_(cls, run_loop):
        """
        Removes the stopper of the given loop
        """
        if run_loop not in cls.singletons:
            raise ValueError("Stopper not registered for this runLoop")
        del cls.singletons[run_loop]

    def stop(self):
        """
        Stops the event loop and terminates the application
        """
        self.shouldStop = True
        # this should go away when/if runEventLoop uses
        # runLoop iteration
        if NSApp() is not None:
            NSApp().terminate_(self)


def stopEventLoop():
    """
    Stop the current event loop if possible
    returns True if it expects that it was successful, False otherwise
    """
    stopper = PyObjCAppHelperRunLoopStopper.currentRunLoopStopper()
    if stopper is None:
        if NSApp() is not None:
            NSApp().terminate_(None)
            return True
        return False

    NSTimer.scheduledTimerWithTimeInterval_target_selector_userInfo_repeats_(
        0.0, stopper, 'performStop:', None, False)
    return True

# ------------------------------------------------------------------------------


def unexpectedErrorAlertPanel():
    """
    Request the user to continue or quit the application

    (untested)
    """
    ex_info = traceback.format_exception_only(*sys.exc_info()[:2])[0].strip()
    return NSRunAlertPanel(
        "An unexpected error has occurred %@",
        "Continue", "Quit", None, "({0})".format(ex_info))


RAISETHESE = (SystemExit, MemoryError, KeyboardInterrupt)


def runEventLoop(argv=None, unexpected_error_alert=None,
                 main=NSApplicationMain):
    """
    Run the event loop, ask the user if we should continue if an
    exception is caught. Use this function instead of NSApplicationMain().

    :param argv: Application arguments
    :param unexpected_error_alert: Method to call in case of unexpected error
    :param main: Main application class
    """
    if argv is None:
        argv = sys.argv

    if unexpected_error_alert is None:
        unexpected_error_alert = unexpectedErrorAlertPanel

    run_loop = NSRunLoop.currentRunLoop()
    stopper = PyObjCAppHelperRunLoopStopper()
    PyObjCAppHelperRunLoopStopper.addRunLoopStopper_toRunLoop_(
        stopper, run_loop)

    first_run = NSApp() is None
    try:
        while stopper.shouldRun():
            try:
                if first_run:
                    first_run = False
                    main(argv)
                else:
                    NSApp().run()
            except RAISETHESE as ex:
                _logger.exception("Special handling of %s: %s",
                                  type(ex).__name__, ex)
                break
            except Exception as ex:
                _logger.exception("An unexpected exception occurred: %s", ex)
                if not unexpected_error_alert():
                    sys.exit(0)
            else:
                break
    finally:
        PyObjCAppHelperRunLoopStopper.removeRunLoopStopperFromRunLoop_(
            run_loop)
