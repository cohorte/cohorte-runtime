#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Instrumentation servlet

:author: Thomas Calmant
:license: Apache Software License 2.0
:version: 1.1.0

..

    Copyright 2014 isandlaTech

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
"""

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, RequiresMap, \
    Instantiate, Property, Validate, BindField
import pelix.http

# Instruments constants
import cohorte.instruments

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__
# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(pelix.http.HTTP_SERVLET)
@RequiresMap('_instruments', cohorte.instruments.SERVICE_INSTRUMENT_UI,
             key=cohorte.instruments.PROP_INSTRUMENT_NAME,
             optional=True)
@Property('_path', pelix.http.HTTP_SERVLET_PATH, '/instruments')
@Instantiate('instruments-servlet')
class InstrumentsServlet(cohorte.instruments.CommonHttp):
    """
    Aggregates instruments and forwards requests
    """
    def __init__(self):
        """
        Sets up members
        """
        # Parent initialization
        super(InstrumentsServlet, self).__init__()

        # Instruments UIs
        self._instruments = {}

        # Servlet path
        self._path = None

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # Normalize the paths
        self._path = '/' + self.normalize_path(self._path)

        # No starting '/' for this one
        self._statics = self.normalize_path(self._statics)

        # Set the servlet path for bound instruments
        if self._instruments:
            for instrument in self._instruments.values():
                instrument.set_servlet_path(self._path)

    @BindField('_instruments', if_valid=True)
    def bind_instrument(self, field, instrument, reference):
        """
        A new instrument is bound
        """
        instrument.set_servlet_path(self._path)

    def do_GET(self, request, response):
        """
        Handle requests
        """
        path = request.get_path()

        # Remove double '/'
        path = '/' + self.normalize_path(path)

        # Remove path
        path = path[len(self._path):]
        parts = [part for part in path.split('/') if part]

        if not parts:
            # Index only
            self.send_index(response)
        elif path.startswith('/' + self._statics):
            # Static file
            filename = path[len(self._statics) + 1:]
            self.send_static(response, filename)
        else:
            # Name of the instrument
            name = parts[0]

            # Its path
            base_path = '{0}/{1}'.format(self._path, name)

            # Its sub-parts
            sub_path = '/'.join(parts[1:])

            try:
                # Find the matching instrument
                handler = self._instruments[name].handle_request
            except KeyError:
                # Unknown instrument
                self.page_not_found(response,
                                    "Unknown instrument: {0}".format(name))
            else:
                # Use it
                handler(base_path, sub_path, request, response)

    def send_index(self, response):
        """
        Prepares the index page
        """
        # Prepare the lists of links
        items_list = self.make_list(self.make_link(name, name)
                                    for name in sorted(self._instruments))

        # Prepare the HTML body
        body = """<h2>All instruments</h2>
{items_list}
""".format(items_list=items_list)

        # Send the page
        response.send_content(200, self.make_page("Cohorte Instruments", body))
