#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Voting system cartoonist: draws charts describing the votes

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
import cgi
import logging
import numbers
import uuid

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate

# nvd3
import nvd3

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


def _normalize(values):
    """
    Normalizes the given list of values: it must only contain numbers or
    strings
    """
    return [value if isinstance(value, (numbers.Number, str)) else str(value)
            for value in values]


def append_code(html, code, *args):
    """
    Appends the given code to the existing HTML code
    """
    if args:
        # Format added code first
        code = code.format(*args)

    return "{0}\n{1}".format(html, code)


def make_html_list(items, tag="ul"):
    """
    Prepare a list in HTML

    :param items: List of items to show
    :param tag: One of ul or ol
    :return: The list in HTML
    """
    return "<{0}>\n{1}</{0}>".format(
        tag, "".join("\t<li>{0}</li>\n".format(cgi.escape(str(item)))
                     for item in items))


def make_chart(*series, **kwargs):
    """
    Makes a bar chart of the given results using nvd3
    """
    # Generate a chart ID
    chart_id = "chart_{0}".format(str(uuid.uuid4()).replace("-", ""))

    # Options
    required_keys = kwargs.get("required", None)

    # Prepare the chart
    chart = nvd3.discreteBarChart(name=chart_id, height=400, width=800)

    for serie in series:
        if not serie:
            # Ignore empty series
            continue

        if isinstance(serie, dict):
            # If the results are stored as a candidate -> score dictionary
            xdata, ydata = zip(*((key, serie[key]) for key in serie))
        else:
            # Rotate results list((score, candidate)
            # ==> list(candidate), list(scores)
            ydata, xdata = zip(*serie)

        # Add missing keys
        if required_keys:
            to_add = [key for key in required_keys if key not in xdata]
            if to_add:
                # We have to add those keys: make data modifiable
                xdata = list(xdata)
                ydata = list(ydata)

                for key in to_add:
                    xdata.append(key)
                    ydata.append(0)

        # Normalize series
        ydata, xdata = zip(*sorted(zip(_normalize(ydata), _normalize(xdata)),
                                   reverse=True))

        # Add the serie
        chart.add_serie(y=ydata, x=xdata)

    # Generate the HTML
    chart.buildcontainer()
    chart.buildjschart()

    # Return its HTML code
    return "{0}\n{1}\n".format(chart.container, chart.jschart)

# ------------------------------------------------------------------------------


def count_ballots(ballots):
    """
    Counts the "for" and "against" ballots

    :param ballots: Vote ballots
    :return: A tuple of dictionaries: for and against votes
    """
    # Count the number of votes for each candidate
    results_for = {}
    results_against = {}
    blanks = []

    for name, ballot in ballots.items():
        if not ballot['for'] and not ballot['against']:
            # Blank vote
            blanks.append(name)
            continue

        for candidate in ballot['for']:
            results_for[candidate] = results_for.get(candidate, 0) + 1

        for candidate in ballot['against']:
            results_against[candidate] = results_against.get(candidate, 0) - 1

    return results_for, results_against, tuple(sorted(blanks))

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.vote.SERVICE_VOTE_CARTOONIST)
@Instantiate('vote-cartoonist')
class Cartoonist(object):
    """
    Draws charts according to VoteResults beans
    """
    @staticmethod
    def make_chart_html(content):
        """
        Prepares the HTML code to draw a chart

        :param content: A VoteResults bean
        :return: A piece of HTML containing the chart
        """
        # Count electors
        nb_electors = len(content.electors)

        html = """<h2>{info.name}</h2>
<p>Vote of kind <em>{info.kind}</em>, with {count} electors</p>
""".format(info=content, count=nb_electors)

        if nb_electors <= 10:
            # Avoid to print a huge list of electors
            html = append_code(html, make_html_list(content.electors))

        for round_id, round_data in enumerate(content.rounds):
            # Show the round
            html = append_code(html, "<h3>Round {0}</h3>", round_id + 1)

            # List the candidates for this round
            candidates = round_data['candidates']
            html = append_code(html, "<p>{0} candidates:</p>\n{1}\n",
                               len(candidates), make_html_list(candidates))

            # Add the ballots chart
            html = append_code(html, "<h4>Ballots</h4>")
            ballots_for, ballots_against, blanks = \
                count_ballots(round_data['ballots'])
            html = append_code(html, make_chart(ballots_for, ballots_against,
                                                required=candidates))

            if blanks:
                html = append_code(html,
                                   "<p>{0} electors did not vote</p>\n{1}",
                                   len(blanks), make_html_list(blanks))

            # Add the results chart
            html = append_code(html, "<h4>Engine results</h4>")
            html = append_code(html, make_chart(round_data['results'],
                                                required=candidates))

            # Add extra charts
            for extra in round_data['extra']:
                html = append_code(html, "<h4>Extra: {0}</h4>", extra['title'])
                html = append_code(html, make_chart(extra['values']))

        # Print the result
        html = append_code(
            html, "<h4>Results:</h4>\n{0}",
            make_html_list((str(result) for result in content.results), "ol"))
        return html

    def make_page_html(self, votes, title="Vote results",
                       statics="./bower_components"):
        """
        Makes a whole HTML page for the given vote content

        :param votes: A list of VoteResults beans
        :param title: Page title
        :param statics: Relative path to static files (containing d3 and nvd3)
        :return: An HTML page
        """
        html_content = "\n\n".join(self.make_chart_html(vote)
                                   for vote in votes)

        return """<!DOCTYPE html>
<html lang="en">
<head>
<link media="all" href="{statics}/nvd3/src/nv.d3.css" type="text/css"
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
""".format(body=html_content, title=title, statics=statics)
