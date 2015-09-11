# cohorte.boot.looper

This package contains the main-loop handlers, which let the main thread of the process handle UI events (Qt, Cocoa, ...). By default, the Pelix framework is started in the main thread. By using a looper, it can run in a secondary thread, and let an event-loop run in the main thread, to handle GUI events.

## Existing loopers

* ``cocoa``: Handles the Cocoa event-loop on Mac OS X. It is based on cocoapy (embedded as cohorte.cocoapy);
* ``qt``: Handles the Qt event-loop;
* ``default``: A sample event-loop, which allows to run any method in the main thread.

## API

The module providing a looper must implement the following method:

* ``get_looper() -> Looper``: creates the looper object (singleton).

The looper implementation must provide the following methods:

* ``setup(argv)``: setup the event loop. ``argv`` contains the arguments
  of the application (same as ``sys.argv``);
* ``loop()``: runs the main loop (it must be a blocking method, obviously);
* ``stop()``: stops the main loop and cleans up the UI library (if necessary);
* ``run(method, *args, **kwargs)``: runs the given method, with the given arguments, in the main thread. This method must be blocking (synchronous execution).

### Looper Skeleton

```python
def get_looper():
    """
    Constructs the looper
    """
    return SampleLooper()

class SampleLooper(object):
    """
    Sample looper
    """
    def __init__(self):
        """
        Sets up members
        """
        # The loop control event
        self._stop_event = threading.Event()

    def setup(self, argv=None):
        """
        Configures the looper
        """
        self._stop_event.clear()

    def run(self, method, *args, **kwargs):
        """
        Runs the given method in the main thread

        :param method: The method to call
        :param args: Method arguments
        :param kwargs: Method keyword arguments
        :return: The result of the method
        """
        pass

    def loop(self):
        """
        Main loop
        """
        while not self._stop_event.is_set():
            pass

    def stop(self):
        """
        Stops the loop
        """
        self._stop_event.set()
```