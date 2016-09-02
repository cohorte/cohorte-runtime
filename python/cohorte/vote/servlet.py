#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Voting system servlet: shows the charts made by the cartoonist

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

# Standard library
import logging
import os.path

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Requires, \
    Instantiate, Property, Invalidate
import pelix.http

# Voting system
import cohorte.vote

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.vote.SERVICE_VOTE_STORE)
@Provides(pelix.http.HTTP_SERVLET)
@Requires('_cartoonist', cohorte.vote.SERVICE_VOTE_CARTOONIST)
@Property('_path', pelix.http.HTTP_SERVLET_PATH, '/votes')
@Property('_statics', 'html.statics.virtual', '_static')
@Property('_real_statics', 'html.statics.physical', './_html_statics')
@Instantiate('vote-servlet')
class VoteChartServlet(object):
    """
    Prints HTML charts draws by the cartoonist
    """
    def __init__(self):
        """
        Sets up members
        """
        # Chart cartoonist
        self._cartoonist = None

        # Servlet path
        self._path = None

        # Static files
        self._statics = None
        self._real_statics = None

        # Store all vote results
        self._all_votes = []

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Clean up
        del self._all_votes[:]

    def store_vote(self, vote):
        """
        Store a vote to chart

        :param vote: A VoteContent bean
        """
        self._all_votes.append(vote)

    def do_GET(self, request, response):
        """
        Handle requests
        """
        path = request.get_path()
        if path == self._path:
            # Root: print an index page
            return self.send_index(response)
        else:
            # Remove the servlet path
            path = path[len(self._path):]

            # Remove double '/'
            parts = [part for part in path.split('/') if part]
            path = '/'.join(parts)

            if not parts:
                # Index only
                return self.send_index(response)
            elif path.startswith(self._statics):
                # Static file
                filename = path[len(self._statics):]
                return self.send_static(response, filename)
            elif parts[-1] == 'all':
                # Print all charts in a single page
                return self.send_all(response)
            elif parts[-2] == 'chart':
                # Print the given chart
                vote = self._all_votes[int(parts[-1])]
                # Let the cartoonist make the chart
                page = self._cartoonist.make_page_html(
                    [vote], vote.name, self._get_statics_path())

                # Send it
                return response.send_content(200, page)

        # Unknown path: redirect to the index
        self._redirect_to_index(response)

    def _get_statics_path(self):
        """
        Returns the path to the static files virtual folder
        """
        return '/'.join((self._path, self._statics))

    def _redirect_to_index(self, response, code=404):
        """
        Redirects the browser to the index
        """
        response.set_response(code)
        response.set_header("Location", self._path)
        response.end_headers()
        response.write("")

    def __make_link(self, text, *parts):
        """
        Prepares a link
        """
        return '<a href="{0}/{1}">{2}</a>' \
            .format(self._path, '/'.join(str(part) for part in parts), text)

    def __make_page(self, title, body):
        """
        Makes an HTML page
        """
        return """<!DOCTYPE html>
<html lang="en">
<head>
<link media="all" href="{statics}/nv.d3.css" type="text/css"
    rel="stylesheet" />
<script src="{statics}/d3.min.js" type="text/javascript"></script>
<script src="{statics}/nv.d3.min.js" type="text/javascript"></script>
<title>{title}</title>
</head>
<body>
<h1>{title}</h1>
{body}
</body>
</html>
""".format(title=title, body=body, statics=self._get_statics_path())

    def send_all(self, response):
        """
        Sends a page containing all charts
        """
        body = '\n\n'.join(self._cartoonist.make_chart_html(vote)
                           for vote in self._all_votes)

        # Send the page
        page = self.__make_page("All votes", body)
        response.send_content(200, page)

    def send_index(self, response):
        """
        Prepares the index page
        """
        # Prepare the lists of links
        all_items = ('\t<li>{0}</li>'.format(self.__make_link(vote.name or idx,
                                                              "chart", idx))
                     for idx, vote in enumerate(self._all_votes))

        all_charts = "<ul>\n{0}</ul>".format("".join(all_items))

        # Prepare the HTML body
        body = """<h2>All charts (in addition order)</h2>
{all_charts}
""".format(all_charts=all_charts)

        # Send the page
        response.send_content(
            200, self.__make_page("Cohorte Vote System", body))

    def send_static(self, response, filename):
        """
        Sends the given static file
        """
        # Ensure it is a relative path
        if filename[0] == '/':
            filename = filename[1:]

        # Get the filename
        filename = os.path.join(self._real_statics, filename)

        try:
            with open(filename) as filep:
                response.send_content(200, filep.read(), "")
        except:
            response.send_content(404, "File not found: {0}".format(filename),
                                  "text/plain")
