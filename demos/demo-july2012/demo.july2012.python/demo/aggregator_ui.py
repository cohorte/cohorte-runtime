#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
An aggregator of sensors values

Created on 10 juil. 2012

:author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

from pelix.ipopo import constants

from pelix.ipopo.decorators import ComponentFactory, Requires, \
    Validate, Invalidate, Property

# ------------------------------------------------------------------------------

import logging
_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

import sys
if sys.version_info[0] == 3:
    # Python 3
    def _to_string(data, encoding="UTF-8"):
        """
        Converts the given bytes array to a string
        """
        if type(data) is str:
            # Nothing to do
            return data

        return str(data, encoding)

else:
    # Python 2
    def _to_string(data, encoding="UTF-8"):
        """
        Converts the given bytes array to a string
        """
        if type(data) is str:
            # Nothing to do
            return data

        return data.encode(encoding)

# ------------------------------------------------------------------------------

@ComponentFactory("demo-sensor-aggregator-ui-factory")
@Property("_name", constants.IPOPO_INSTANCE_NAME)
@Property("_path", "servlet.path", "/sensors")
@Requires("_aggregator", "org.psem2m.demo.sensors.IAggregator", optional=True)
@Requires("_http", "HttpService")
class AggregatorServlet(object):
    """
    Temperature sensor
    """
    def __init__(self):
        """
        Constructor
        """
        self._name = ""

        self._aggregator = None
        self._http = None


    def _make_sensor_part(self, name, history):
        """
        Prepares a HTML title and a table containing sensor history
        
        :param name: Sensor name
        :param history: Sensor history
        :return: A HTML paragraph
        """
        table_rows = ("""
<tr>
<td>{time}</td>
<td>{value:.2f}</td>
<td>{unit}</td>
</tr>""".format(time=entry["time"], value=entry["value"],
                unit=_to_string(entry["unit"]))
        for entry in history)

        return """
<h2>Sensor {name}</h2>
<table>
<tr>
<th>Time Stamp</th>
<th>Value</th>
<th>Unit</th>
</tr>{rows}
</table>""".format(name=name, rows=''.join(table_rows))


    def do_GET(self, handler):
        """
        Handle GET requests
        """
        output = """<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="refresh" content="2">
<title>Sensor Aggregator - {name}</title>
</head>
<body>
<h1>Sensor Aggregator - {name}</h1>
""".format(name=self._name)

        if not self._aggregator:
            output += "<p>Aggregator service unavailable</p>"

        else:
            output += "<p>Aggregator found</p>"
            history = self._aggregator.get_history()
            output += '\n'.join((self._make_sensor_part(name, history[name])
                                 for name in history))

        output += """
</body>
</html>
"""

        # Send headers
        handler.send_response(200)
        handler.send_header("content-type", "text/html")
        handler.send_header("content-length", str(len(output)))
        handler.end_headers()

        # Send content
        handler.wfile.write(output)


    @Validate
    def validate(self, context):
        """
        Component validation
        """
        # Register the servlet
        self._http.register_servlet(self._path, self)

        _logger.info("Component %s validated", self._name)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidation
        """
        # Unregister the servlet
        self._http.unregister_servlet(self)

        _logger.info("Component %s invalidated", self._name)
