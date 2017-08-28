#!/usr/bin/python
# -- Content-Encoding: UTF-8 --
"""
Cohorte quick start module, which grabs the home and base templates from
isandlaTech servers

:author: Thomas Calmant
:copyright: Copyright 2014, isandlaTech
:license: Apache License 2.0
:version: 0.0.1
:status: Alpha

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

# Print compatibility
from __future__ import print_function

# Standard library
import argparse
import json
import os
import shutil
import sys
import tempfile
import zipfile

# Pelix
from pelix.utilities import to_str

try:
    # pylint: disable=F0401
    import urllib2
except ImportError:
    # pylint: disable=F0401,E0611
    import urllib.request as urllib2

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------


class CohorteMaker(object):
    """
    Utility class to construct home and bases folders
    """
    def __init__(self, index_url):
        """
        Sets up members

        :param index_url: URL to the JSON index file
        """
        self.index_url = index_url
        # Name -> ZIP URL
        self._index = {}
        # Name -> TemporaryFile
        self._templates = {}

    @staticmethod
    def _get_file_content(url):
        """
        Returns the content of the given file

        :param url: URL to the file
        :return: The content of the file
        :raise IOError: Error loading the file
        """
        return urllib2.urlopen(url).read()

    def load_index(self, force=False):
        """
        Loads the templates index

        :param force: Force index reload
        :raise IOError: Can't load the index file
        :raise ValueError: Invalid index file
        """
        if force or not self._index:
            data = to_str(self._get_file_content(self.index_url))
            index = json.loads(data)
            for key in ('home', 'base'):
                self._index[key] = \
                    index['snapshots']['cohorte-minimal-{0}-distribution'
                                       .format(key)]['url']

    def load_template(self, name, force=False):
        """
        Loads the template with the given name

        :param name: Name of the template
        :param force: If True, force the template to be reloaded
        :raise IOError: Error loading the file
        :raise KeyError: Unknown template
        """
        if force or name not in self._templates:
            # Download the template
            content = self._get_file_content(self._index[name])
            filep = tempfile.TemporaryFile()
            filep.write(content)
            filep.flush()
            self._templates[name] = filep

    def extract_template(self, name, directory):
        """
        Extract the template to the disk

        :param name: Name of the template
        :param directory: Output directory
        :raise KeyError: Unknown template
        :raise IOError: Error extracting template
        """
        with zipfile.ZipFile(self._templates[name]) as tpl_zip:
            # Get the list of files
            infos = tpl_zip.infolist()

            # Find the template content level
            content_folder = os.path.commonprefix(
                [info.filename for info in infos if info.filename[-1] != '/'])
            content_folder_len = len(content_folder)

            # Extract files, replacing the top level by the target directory
            for info in infos:
                # Remove the common prefix
                output_name = info.filename[content_folder_len:]

                # Extract is inspired from Python's zipfile._extract_member
                # Convert path to system format
                output_name.replace('/', os.path.sep)

                # Forge the target path
                target_path = os.path.join(directory, output_name)
                target_path = os.path.normpath(target_path)

                # Create all upper directories if necessary.
                upperdirs = os.path.dirname(target_path)
                if upperdirs and not os.path.exists(upperdirs):
                    os.makedirs(upperdirs)

                if info.filename[-1] == '/':
                    # Create the directory if the current ZipInfo is one
                    if not os.path.isdir(target_path):
                        os.mkdir(target_path)
                else:
                    # Extract the file
                    with tpl_zip.open(info) as source, \
                            open(target_path, "wb") as target:
                        shutil.copyfileobj(source, target)

    def make_home_directory(self, home_dir):
        """
        Prepares the Cohorte Home directory

        :param home_dir: The absolute path to the home directory
        :raise IOError: Error creating the folder
        """
        if os.path.exists(home_dir):
            raise IOError("Home directory {0} already exist. Abandon."
                          .format(home_dir))

        # Load the template file
        self.load_index(False)
        self.load_template("home", False)

        self.extract_template("home", home_dir)
        print("Home template extracted, you know have to set the COHORTE_HOME "
              "environment variable:")
        print("set COHORTE_HOME=\"{0}\"".format(home_dir))

    def make_base_directory(self, base_dir):
        """
        Prepares the Cohorte Base directory

        :param base_dir: The absolute path to the base directory
        """
        if os.path.exists(base_dir):
            raise IOError("Base directory {0} already exist. Abandon."
                          .format(base_dir))

        # Load the template file
        self.load_index(False)
        self.load_template("base", False)

        # Extract it
        self.extract_template("base", base_dir)
        print("Base template extracted, you know have to set the COHORTE_BASE "
              "environment variable:")
        print("set COHORTE_BASE=\"{0}\"".format(base_dir))


def main(args=None):
    """
    Script entry point
    :param args: Program arguments
    """
    parser = argparse.ArgumentParser("Cohorte Quick start")
    parser.add_argument("--home", dest="home_dir", metavar="HOME_DIR",
                        help="Directory where to store the Cohorte Home "
                        "folder")
    parser.add_argument("-b", "--base", dest="base_dirs", nargs="*",
                        metavar="BASE_DIR",
                        help="Directory where to store the Cohorte Base "
                        "folder")
    parser.add_argument("--url", dest="index_url", metavar="URL",
                        default="http://repo.isandlatech.com/last.json",
                        help="Index JSON file")
    options = parser.parse_args(args)
    if not any((options.home_dir, options.base_dirs)):
        parser.print_help()
        return 1

    # Prepare the maker
    maker = CohorteMaker(options.index_url)
    try:
        if options.home_dir:
            # Extract the template
            target_dir = os.path.abspath(options.home_dir)
            maker.make_home_directory(target_dir)

        if options.base_dirs:
            for base_dir in options.base_dirs:
                # Create the base directory
                target_dir = os.path.abspath(base_dir)
                maker.make_base_directory(target_dir)

    except IOError as ex:
        print(ex)
        return 1

# ------------------------------------------------------------------------------

if __name__ == '__main__':
    sys.exit(main())
