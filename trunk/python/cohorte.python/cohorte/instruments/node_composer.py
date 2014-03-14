#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Node composer instrument

:author: Thomas Calmant
:copyright: Copyright 2014, isandlaTech
:license: GPLv3
:version: 1.0.0

..

    This file is part of Cohorte.

    Cohorte is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cohorte is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cohorte. If not, see <http://www.gnu.org/licenses/>.
"""

# Module version
__version_info__ = (1, 0, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

# Instruments constants
import cohorte.instruments

# Composer
import cohorte.composer

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Requires, \
    Instantiate, Property, Validate

# Standard library
import json
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)


STORY_PAGE = """<!DOCTYPE html>
<html lang="en">
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<meta charset="UTF-8">
<title>Node Composer Story</title>

<link rel="stylesheet" type="text/css" media="all" href="{statics}/narrative/style.css">
<link rel="stylesheet" type="text/css" media="all" href="{statics}/narrative/narrative.css">

<script src="{statics}/d3.min.js"></script>
<script src="{statics}/narrative/catxml.js"></script>
<script src="{statics}/narrative/narrative.js"></script>
</head>

<body>
<p id="chart" />

<script lang="javascript">
characters_xml = '{characters_xml}';


json_scenes = '{json_scenes}';


draw_chart('Composer Story', 'composer_story',
           characters_xml, json_scenes,
           true, false, false);
</script>
</body>
</html>
"""

ISOLATES_PAGE = """<!DOCTYPE html>
<html lang="en">
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<meta charset="UTF-8">
<title>Node Composer Isolates</title>

<script src="{statics}/d3.min.js"></script>

<link rel="stylesheet" type="text/css" media="all" href="{statics}/circle2/style.css">
<script src="{statics}/circle2/circle.js"></script>
</head>

<body>
<script lang="javascript">
json_distribution = '{json_distribution}';

draw_chart(json_distribution);
</script>
</body>
</html>
"""

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.instruments.SERVICE_INSTRUMENT_UI)
@Requires('_history', cohorte.composer.SERVICE_HISTORY_NODE)
@Property('_name', cohorte.instruments.PROP_INSTRUMENT_NAME, 'node')
@Instantiate('instrument-node')
class NodeComposerInstrument(cohorte.instruments.CommonHttp):
    """
    Prints informations about the node composer
    """
    def __init__(self):
        """
        Sets up members
        """
        # Parent initialization
        super(NodeComposerInstrument, self).__init__()

        # Composer history
        self._history = None

        # Node name
        self._node_name = None

        # Instrument name
        self._name = None

        # Path -> method
        self._paths = {'story': self._send_story,
                       'isolates': self._send_isolates}


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._node_name = context.get_property(cohorte.PROP_NODE_NAME)


    def handle_request(self, base_path, sub_path, request, response):
        """
        Handles a HTTP request

        :param base_path: Path to this instrument
        :param sub_path: Part of the path for this instrument
        :param request: A HTTP request bean
        :param response: A HTTP response bean
        """
        parts = [part for part in sub_path.split('/') if part]
        if not parts:
            # No parameter given
            self.send_index(response)

        else:
            try:
                # Find the method handling this path...
                handler = self._paths[parts[0]]

            except KeyError:
                # ... not found
                self.page_not_found(response,
                                    "Unknown page: {0}".format(parts[0]),
                                    self._name)

            else:
                # ... use it
                handler(response)

    def send_index(self, response):
        """
        Prepares the index page
        """
        # Prepare the lists of links
        items_list = self.make_list(self.make_link(name.title(),
                                                   self._name, name)
                                    for name in sorted(self._paths))

        # Prepare the HTML body
        body = """<h2>Node Composer pages</h2>
{items_list}
""".format(items_list=items_list)

        # Send the page
        response.send_content(200,
                              self.make_page("Cohorte Node Composer", body))


    def _send_story(self, response):
        """
        Sends an XKCD-story-like graph about components
        """
        hist_scenes = self._history.items()
        if not hist_scenes:
            # No history yet
            response.send_content(200, "<p>No history yet...</p>")
            return

        # Get the names of all component names
        all_names = self.__extract_story_characters(hist_scenes)

        # Prepare character -> ID association
        characters_id = dict((name, idx) for idx, name in enumerate(all_names))
        id_characters = dict((idx, name) for name, idx in characters_id.items())

        # Prepare "character.xml"
        characters_xml = """<?xml version="1.0"?>
<characters>
{0}
</characters>
""".format('\n'.join('\t<character group="{0}" id="{1}" name="{2}"/>'\
                     .format(idx % 10, idx, id_characters[idx])
                     for idx in sorted(characters_id.values())))

        # Prepare "narrative.json"
        scenes = self.__make_story_scenes(hist_scenes, characters_id)
        narrative_json = json.dumps({"scenes": scenes}, indent=True)

        # Escape content: must be Javascript a string declaration
        characters_xml = characters_xml.replace("'", "\\'") \
                                       .replace("\n", " \\\n")
        narrative_json = narrative_json.replace("'", "\\'") \
                                       .replace("\n", " \\\n")


        # Generate the page content
        page = STORY_PAGE.format(statics=self.get_statics_path(),
                                 characters_xml=characters_xml,
                                 json_scenes=narrative_json)

        response.send_content(200, page)


    def __extract_story_characters(self, history_items):
        """
        Makes a sorted list of the characters in the whole history

        :param history_items: Result History.items()
        :return: A sorted tuple of names
        """
        # Get the names of all component names
        all_names = set()
        for hist_scene in history_items:
            # hist_scene: tuple(timestamp, {isolate -> tuple(names)})
            for components_group in hist_scene[1].values():
                all_names.update(components_group)

        # Make a sorted tuple out of this set
        return tuple(sorted(all_names))


    def __make_story_scenes(self, history, characters):
        """
        Prepares an array representing the scenes in the given history

        :param history: A sorted history
        :param characters: A Name -> CharacterID dictionary
        :return: A tuple of scene dictionaries
        """
        # Compute starting and ending time stamps
        min_stamp = history[0][0]
        max_stamp = history[-1][0]
        delta = max_stamp - min_stamp
        if delta == 0:
            # Only one panel
            def scale(stamp):
                return 1

        else:
            # Prepare a scaling method to have indices in [0,50]
            def scale(stamp):
                return int(((stamp - min_stamp) * 50) / delta)

        scenes = []
        idx = 0
        for timestamp, distribution in history:
            start = scale(timestamp)
            for components in distribution.values():
                idx += 1
                scenes.append(self.__make_story_scene(idx, start,
                                                      components, characters))

        return tuple(scenes)


    def __make_story_scene(self, idx, start, live_characters, all_characters):
        """
        Prepares the dictionary that represents the given scene

        :param idx: Scene ID
        :param start: Scene start panel
        :param live_characters: List of the names of the characters in this
                                scene
        :param all_characters: A Name -> CharacterID dictionary
        """
        return {
            "id": idx,
            "duration": 1,
            "start": start,
            "chars": [all_characters[name] for name in live_characters],
        }


    def _send_isolates(self, response):
        """
        Sends a circle packing graph about isolates content
        """
        # Prepare the distribution dictionary
        distribution = {}

        # Root: node name
        distribution['name'] = self._node_name

        # Get the status history
        hist_scenes = self._history.items()
        if hist_scenes:
            # History there... only use the last scene (forget the timestamp)
            last_dist = hist_scenes[-1][1]

            distribution['children'] = [{'name': isolate,
                                         'children': [{'name': name,
                                                       'size': 100}
                                                      for name in components]
                                         }
                                        for isolate, components in last_dist.items()]



        # Escape content: must be Javascript a string declaration
        json_distribution = json.dumps(distribution, indent=True)
        json_distribution = json_distribution.replace("'", "\\'") \
                                             .replace("\n", " \\\n")


        # Generate the page content
        page = ISOLATES_PAGE.format(statics=self.get_statics_path(),
                                    json_distribution=json_distribution)

        response.send_content(200, page)
