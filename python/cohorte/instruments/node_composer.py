#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Node composer instrument

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
import datetime
import json
import logging

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Requires, \
    Instantiate, Property, Validate

# Cohorte
import cohorte
import cohorte.composer
import cohorte.instruments

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

STORY_PAGE = """<!DOCTYPE html>
<html lang="en">
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<meta charset="UTF-8">
<title>Node Composer Story</title>

<link rel="stylesheet" type="text/css" media="all"
    href="{statics}/narrative/narrative.css">

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

SECOND_STORY_PAGE = """<!DOCTYPE html>
<html lang="en">
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<meta charset="UTF-8">
<title>Node Composer Story</title>
<script src="{statics}/d3.min.js"></script>
<script src="{statics}/isolates_story/story.js"></script>
</head>

<body>
<p id="chart" />

<script lang="javascript">
json_data = '{json_data}';
data = JSON.parse(json_data);
draw_chart("#chart", data);
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

<link rel="stylesheet" type="text/css" media="all"
    href="{statics}/circle2/style.css">
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
    Prints information about the node composer
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
                       'story2': self._send_second_story,
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
                self.page_not_found(
                    response, "Unknown page: {0}".format(parts[0]), self._name)
            else:
                # ... use it
                handler(response)

    def send_index(self, response):
        """
        Prepares the index page
        """
        # Prepare the lists of links
        items_list = self.make_list(
            self.make_link(name.title(), self._name, name)
            for name in sorted(self._paths))

        # Prepare the HTML body
        body = "<h2>Node Composer pages</h2>\n{items_list}\n" \
            .format(items_list=items_list)

        # Send the page
        response.send_content(
            200, self.make_page("Cohorte Node Composer", body))

    def _send_second_story(self, response):
        """
        Sends a graph showing the isolate of each component during distribution
        """
        hist_scenes = self._history.items()
        if not hist_scenes:
            # No history yet
            response.send_content(200, "<p>No history yet...</p>")
            return

        # Get the names of all component names
        all_names = self.__extract_story_characters(hist_scenes)

        # Prepare temporary dictionary
        data = {name: {"name": name, "scenes": []} for name in all_names}

        # Store data
        for timestamp, distribution in hist_scenes:
            for isolate, components in distribution.items():
                for name in components:
                    dist_time = datetime.datetime.fromtimestamp(timestamp)
                    data[name]['scenes'].append(
                        {"distribution": dist_time.strftime("%X"),
                         "isolate": isolate})

        # Generate JSON
        data_json = json.dumps(list(data.values()), indent=True)

        # Escape content: must be Javascript a string declaration
        data_json = data_json.replace("'", "\\'").replace("\n", " \\\n")

        # Generate the page content
        page = SECOND_STORY_PAGE.format(statics=self.get_statics_path(),
                                        json_data=data_json)
        response.send_content(200, page)

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
        characters_id = dict((name, idx)
                             for idx, name in enumerate(all_names))
        id_characters = dict((idx, name)
                             for name, idx in characters_id.items())

        # Prepare "character.xml"
        characters_xml = '<?xml version="1.0"?>\n<characters>\n{0}\n' \
                         '</characters>\n'\
            .format('\n'.join('\t<character group="{0}" id="{1}" name="{2}"/>'
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

    @staticmethod
    def __extract_story_characters(history_items):
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
                """
                No need to scale X-axis values
                """
                return 1
        else:
            # Prepare a scaling method to have indices in [0,50]
            def scale(stamp):
                """
                Scales X-axis values (time stamps) in a [0,50] domain
                """
                return int(((stamp - min_stamp) * 50) / delta)

        scenes = []
        idx = 0
        for timestamp, distribution in history:
            start = scale(timestamp)
            for isolate, components in distribution.items():
                idx += 1
                scenes.append(self.__make_story_scene(
                    idx, isolate, start, components, characters))

        return tuple(scenes)

    @staticmethod
    def __make_story_scene(idx, name, start, live_characters, all_characters):
        """
        Prepares the dictionary that represents the given scene

        :param idx: Scene ID
        :param name: Name of the scene
        :param start: Scene start panel
        :param live_characters: List of the names of the characters in this
                                scene
        :param all_characters: A Name -> CharacterID dictionary
        """
        return {
            "id": idx,
            "name": name,
            "duration": 1,
            "start": start,
            "chars": [all_characters[name] for name in live_characters],
        }

    def _send_isolates(self, response):
        """
        Sends a circle packing graph about isolates content
        """
        # Root: node name
        distribution = {'name': self._node_name}

        # Get the status history
        hist_scenes = self._history.items()
        if hist_scenes:
            # History there... only use the last scene (forget the timestamp)
            last_dist = hist_scenes[-1][1]

            distribution['children'] = [
                {'name': isolate,
                 'children': [{'name': name, 'size': 100}
                              for name in components]}
                for isolate, components in last_dist.items()
            ]

        # Escape content: must be Javascript a string declaration
        json_distribution = json.dumps(distribution, indent=True)
        json_distribution = json_distribution.replace("'", "\\'") \
            .replace("\n", " \\\n")

        # Generate the page content
        page = ISOLATES_PAGE.format(statics=self.get_statics_path(),
                                    json_distribution=json_distribution)
        response.send_content(200, page)
