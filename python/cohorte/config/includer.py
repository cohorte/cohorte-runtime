#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE file includer

component that allow the resolve a file that contain multiple includes. 
this allow to split a composition file in severals file and get a full resolved file 

:author: Aurélien PISU
:license: Apache Software License 2.0

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

# COHORTE constants
import glob
import json
import logging
import os
import re

import cohorte
from cohorte.config import common
import cohorte.version
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate, \
    Validate, Invalidate, Requires
from simpleeval import simple_eval

try:
    # Python 3
    # pylint: disable=F0401,E0611
    import urllib.parse as urlparse
except ImportError:
    # Python 2
    # pylint: disable=F0401
    import urlparse

# iPOPO Decorators
# ------------------------------------------------------------------------------

# Bundle version
__version__ = cohorte.version.__version__

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------
#
#   Expression parser and evaluator 
# 
# ------------------------------------------------------------------------------


class CBadResourceException(Exception):

    def __init__(self, message):
        self._message = message

    def __str__(self):
        return self._message


class CResource(object):

    def get_tag(self):
        return self.tag

    def get_params(self):
        return self.parms

    def get_path(self):
        return self.path

    def getdirpath(self):
        return self.dirpath

    def get_contents(self):
        return self.contents

    def set_contents(self, contents):
        self.contents = contents

    def readall(self):
        """
        return the merge content of all files that correspond to the resource
        """
        join_list = []
        if self.contents != None:
            for content in self.contents:
                if self.tag != None:
                    tag_elem = None
                    json_obj = json.loads(content);
                    idx_obraces = self.tag.find("[")
                    idx_cbraces = self.tag.find("]")
                    tag_key = self.tag
                    if idx_obraces != -1:
                        tag_key = self.tag[:idx_obraces]
                       
                    if not tag_key in json_obj.keys():
                        mess = "include property [{0}]  defined in file [{1}] doesn't exists in file [{2}]".format(
                            tag_key, self.resource_parent.filename, self.filename);
                        raise CBadResourceException(mess)
                    else:
                        arr_elem = json_obj[tag_key]
                    # manage a tag can identified a array or a object and we want a elem of the array or all elemnet or the array
                    if (idx_obraces != -1 and idx_cbraces != -1):
                        if isinstance(arr_elem, list):
                            # want something from the array and not all the array
                            range_idx = self.tag[idx_obraces + 1:idx_cbraces]
                            if range_idx == None:
                                # nothin specify raise an error
                                raise CBadResourceException("include : No index but array ask")

                            elif range_idx.isdigit():
                                # want a specific element
                                idx = int(range_idx)
                                if idx > len(arr_elem):
                                    raise CBadResourceException(
                                        "include : Bad index expect=[{0}] size=[{1}]".format(idx, len(arr_elem)))
                                else:
                                    tag_elem = json.dumps(arr_elem[idx])
                            elif range_idx == "*":
                                tag_elem = ",".join([json.dumps(elem) for elem in arr_elem])
                            else:
                                idxs = range_idx.split(":")
                                first = None
                                last = None
                                if idx[0].isdigit():
                                    first = int(idx[0])
                                if idx[1].isdigit():
                                    last = int(idx[1])
                                if last != None and first != None:
                                    tag_elem = ",".join([json.dumps(elem) for elem in arr_elem[first:last]])
                                elif last != None:
                                    tag_elem = ",".join([json.dumps(elem) for elem in arr_elem[:last]])
                                else:
                                    tag_elem = ",".join([json.dumps(elem) for elem in arr_elem[first:]])

                                # want a specific range
                                pass
                        else:
                            raise CBadResourceException(
                                "include : expect for json array and get a json object urlinclude=[{0}]".format(
                                    self.fullpath))
                    else:
                        tag_elem = json.dumps(json.loads(content)[self.tag])

                    join_list.append(tag_elem)
                else:
                    join_list.append(content)

            return ",".join(join_list)
        return None

    """
    describe a resource that can be file, a http or memory or any kind
    """

    def __init__(self, filename, includer, finder, parent_resource=None):
        """
        construct a resource from a string e.g file:///mypath/foo.txt?k1=v1&#mytag
        @param resource : parent resource , use if path is relative
        @param alternive_dirs : alternative dirs to locate  the file content to include
        @param path : path of the current resource to create
        """
        self.fullpath = filename
        self.protocol_idx = 0
        # use to know the keep the generator due to the manage of import as it is today
        self._include = includer
        # contain the paths possible of location of the file
        self.filename = None
        # list of directory data and base (and home) where the file can be located
        self._finder = finder
        self.params = {}
        self.type = "file"
        # contain the subpath of the directory not an absolute path
        self.dirpath = None
        self.contents = None
        self.read_files_name = None  # list of file name read that correspond to the contents . use for error managment

        self.tag = None
        self.resource_parent = parent_resource;
        self._set_filename(filename, parent_resource)

    def get_full_filename(self):
        return self.dirpath + os.sep + self.filename if self.dirpath != None else self.filename

    def _set_filename(self, filename, parent_resource=None):
        """
        @param path : path of the files to load. can identified one file by multiple file via a regexp or wildchar
        """
        if filename != None:
            self.query_idx = filename.find("?")
            self.tag_idx = filename.find("#")
            filename = self._init_type(filename)
            filename = self._init_filename(filename, parent_resource)
            filename = self._init_query(filename)
            if self.tag_idx != -1:
                self.tag = filename[self.tag_idx + 1:]
                _logger.info("property tag {0}".format(self.tag))

            self.contents = self._read_contents()
        else:
            raise CBadResourceException("path parameter has 'None' value")

    def _init_type(self, filename):
        """
        return the protocol to use for resolving the url content
        @param path : a string that start with protocol://
        @return : a type that identified which protocol to use : can be file, http or memory
        """
        if filename != None:
            self.type = "file"
            # prefix that identify the protocol e.g file://
            if filename.startswith("memory://"):
                self.type = "memory"
                self.protocol_idx = 9
            elif filename.startswith("http://"):
                self.type = "http"
                self.protocol_idx = 7
            elif filename.startswith("file://"):
                self.protocol_idx = 7

        return filename

    def _init_param_run_config(self, a_json_config, a_current_path=""):
        if isinstance(a_json_config, dict):
            for key in a_json_config.keys():
                self._init_param_run_config(a_json_config[key], a_current_path + "." + key if a_current_path != "" else key)
        else:
            _logger.debug("add run variable key={} value={}".format(a_current_path, a_json_config))
            self.params["run:{}".format(a_current_path)] = [a_json_config]

    def _init_query(self, filename):
        """
        return the query string parameter key=value that can be in the url. the substring starts with '#' will be ignored
        @param filename : a string that can contain key value a.g foo?k=v&p=c
        @return : a param dictionnary that contain the key value
        """
        if self.query_idx != -1:

            # set query if exists
            if self.tag_idx != -1 and self.tag_idx > self.query_idx:
                query = filename[self.query_idx + 1:self.tag_idx]
           
            else:
                query = filename[self.query_idx + 1:]

            self.params = urlparse.parse_qs(query)
            _logger.info("replace params passed in query ={0}".format(self.params))

        # add environment variable as parameter
        for en_key in os.environ:
            self.params[en_key] = [os.environ[en_key]]
            if not en_key.startswith("run:"):
                self.params["env:" + en_key] = self.params[en_key]  # set env namespace 
        
        # create variable for namespace run 
        if "run" in os.environ:
            w_json = json.loads(os.environ["run"])
            self._init_param_run_config(w_json)
        
        _logger.debug("replace params passed in query ={0}".format(self.params))

        return filename

    def _read_contents_file(self, a_file):
        contents = []
        _logger.info("read file {0}".format(a_file))
        lines = []
        if a_file != None:
            with open(a_file) as obj_file:
                comment_line = False
                self.read_files_name.append(a_file)
                for line in obj_file:
                    # remove /* */  comment, the // is manage by the regexp
                    # TODO manage "http://" /*
                    idx_last_str = line.rfind("\"")
                    idx_start = line.find("/*")
                    idx_end = line.find("*/")
                    if idx_start != -1 and idx_end != -1:
                        if not idx_start + 1 < idx_end:
                            lines.append(line)
                        else:
                            lines.append(line[:idx_start] + line[idx_end + 2:])
                    elif idx_start != -1 and idx_last_str < idx_start:
                        comment_line = True
                        lines.append(line[:idx_start])
                    elif idx_end != -1 and comment_line:
                        comment_line = False
                        lines.append(line[idx_end + 2:])
                    elif not comment_line:
                        lines.append(line)
                contents.append("\n".join(lines));
            if len(contents) > 0:
                return contents
        return None

    def _read_contents_files(self):
        """
        if the path contain a wildChar we read o all files else only the first one (compatibilty with the current way to manage import)
        return the contents of the files identified by the path
        @return : a list of String
        """
        contents = []
        path = self.filename

        self.read_files_name = []
        w_readed_file = []
        for file in self._finder.find_rel(path, self.dirpath):
            content = self._read_contents_file(file)
            if content != None and not file in w_readed_file:
                contents.append("\n".join(content));
                w_readed_file.append(file)
            
        if len(contents) > 0:
            return contents
        # no content found in list of directory
        return None

    def _read_contents(self):
        """
        return the content of the url with the variable replace
        @return : a  list of String
        """
        if self.type == "file":
            self.contents = self._read_contents_files()
        else:
            # not manager
            self.content = None

        if self.params != None:
            self.contents = common.replace_vars(self.params, self.contents)

        return self.contents

    def _init_filename(self, filename, parent_resource=None):
        """
        return the  filename of the resource to read
        @param filename : path of the resource to read
        @param parent_resource : a parent resource that include the current in order to resolve the relative path if it's necessary
        @return : a String
        """
        if filename != None:

            res_file_name = None
            if self.query_idx != -1 and self.tag_idx != -1:
                end_idx = self.tag_idx if self.query_idx > self.tag_idx else self.query_idx
                res_file_name = filename[self.protocol_idx:end_idx]
            elif self.query_idx != -1 :
                res_file_name = filename[self.protocol_idx:self.query_idx]
            elif self.tag_idx != -1:
                res_file_name = filename[self.protocol_idx:self.tag_idx]
            else:
                res_file_name = filename[self.protocol_idx:]

            # compute dir path and filename
            if res_file_name.find(os.sep) != -1:
                split_file = res_file_name.split(os.sep)
                self.dirpath = os.sep.join(split_file[:-1])
                self.filename = split_file[-1]

            else:
                self.dirpath = ""
                self.filename = res_file_name

            # check if parent resource has dirpath
            if parent_resource != None and parent_resource.dirpath != "":
                self.dirpath = parent_resource.dirpath + os.sep + self.dirpath

        _logger.info("filename {0}".format(filename))

        return filename


class FileIncluderAbs(object):
    """
    Simple component that resolve a comment json that can include using { $include : "path"} a subfile json content
    """

    def __init__(self):
        # regexp that manage multiline comment and // comment
        self._check = re.compile("(.*//.*)", re.MULTILINE)
        self._check_slash = re.compile("(\".*//.*\")", re.MULTILINE)
        # _simple_include = "((\\n)*\\{(\\n)*\\s*\"\\$include\"\\s*:(\\s*\"[^\\}]*\"\\s*)\\})"
        # _complex_include = "((\n)*\\{(\\s|\n|\t)*\"\$include\"(\\s|\n|\t)*:(\\s|\n|\t)*\\{((\\s|\n|\t)*\"[^\\}]*\"(\\s|\n|\t)*:(\\s|\n|\t)*.*(\\s|\n|\t)*,{0,1}(\\s|\n|\t)*)*\\}(\\s|\n|\t)*)\\}"
        # self._include = re.compile("(" + _simple_include + ")|(" + _complex_include + ")", re.MULTILINE)
        _simple_merge = "(,{0,1}(\n|\s|\t)*\"\$merge\"(\n|\s|\t)*:((\n|\s|\t)*\[(\n|\s|\t)*\"[^\}]*\"(\n|\s|\t)*\](\n|\s|\t)*,{0,1}))"
        self._merge = re.compile(_simple_merge, re.MULTILINE)

        self._file_generator = {}  # list of generator by file

    def _get_finder(self):
        """
            return the finder component instance abstract method
        """
        pass

    def _get_content(self, filepath, parent_resource=None):
        """
        return a resolved content json string without commentary
        """

        if filepath != None:
            _logger.info("_getContent {0}".format(filepath))

            # return a resolve content json
            resource = CResource(filepath, self, self._get_finder(), parent_resource)

            # remove comment and content of the resource
            self._remove_comment(resource)

            # resolve content
            self._resolve_content(resource);

            return resource.readall()

        return None

    # for using it from jython without osgi and ipopo resolution
    def set_finder(self, finder):
        self._finder = finder

    def get_content(self, filename, want_json=False):
        """
        @param filename: path we want the content. the filename can be a absolute path, a path with key value params or tag to specify what content we want.
        e.g :
            -    file://mydir/myfile.js => this just get the content of the file myfile
            -    file://mydir.myfile.js?k1=v1&k2=v2 => this will get the content of the file myfile but it will replace all variable ${k1} and ${k2} by v1 and v2
            -    file://mydir.myfile.js?k1=v1&k2=v2#myprop => this will get the only the property myprop of the content file myfile wth replace variable

        @param wantJson : boolean to defined if we want a json object as a result or a string
        return a resolved content json string without commentary
        """

        # multi path asked if the filename contains ; separator
        if filename.find(";") != -1:
            content = ",".join([self._get_content(name) for name in filename.split(";")])
        else:
            content = self._get_content(filename)

        merge_content = None
        if content == None:
            raise IOError("file {0} doesn't exists".format(filename))
        json_contents = json.loads("[" + content + "]")

        for json_content in json_contents:
            if isinstance(json_content, dict):
                # must be always a dict to append all json dict

                if merge_content == None:
                    merge_content = json.loads("{}")
                merge_content = common.merge_object(merge_content, json_content)

            elif isinstance(json_content, list):
                # must be always a list to append all json arrays
                if merge_content == None:
                    merge_content = json.loads("[]")
                    for arr in json_content:
                        merge_content.append(arr)

                if merge_content == None:
                    raise IOError("{0} doesn't exists ".format(filename))

        if not want_json:
            return json.dumps(merge_content)
        _logger.debug("content=[{}]".format(json.dumps(merge_content)))
        return merge_content

    def _get_include_path(self, json_match):
        """ return the list of path to include """
        if isinstance(json_match, dict):
            paths = json_match["path"]
            if isinstance(paths, str):
                paths = paths.split(";")
                # TODO property to manage

        else:
            paths = json_match.split(";")
            _logger.debug("_revolveContent: paths {0}".format(paths))
        return paths

    def _is_condition_include(self, json_match):
        """ return true if a condition doesn't exists or if the condition is evaluation is true else false """
        if isinstance(json_match, dict) and "condition" in json_match:
            condition = json_match["condition"]
            if condition != None and isinstance(condition, str):
                return simple_eval(condition)
            else:
                return True  
                # TODO property to manage

        return True  

    def _get_include_match(self, matches):
        found_match = None
        for match in matches:
            if match != "":
                found_match = match
                break
        return found_match

    def _find_match_include(self, a_json):
        w_res = []

        if isinstance(a_json, dict) :
            if "$include" in a_json.keys():
                return [a_json]
            else:
                for a_sub_json in a_json.values():
                    w_sub_res = self._find_match_include(a_sub_json)
                    if w_sub_res:
                        w_res.extend(w_sub_res)
        elif isinstance(a_json, list):
            for a_elem in a_json:
                w_sub_res = self._find_match_include(a_elem)
                if w_sub_res:
                    w_res.extend(w_sub_res)
          
        return w_res

    def _resolve_content(self, resource):
        """
        return a resolve content with all include file content
        """
        _logger.debug("_revolveContent")

        contents = resource.get_contents();
        if contents != None:
            resolved_contents = []
            for content in contents:
                resolved_content = content
                # apply regexp to remove content
                # load str as json and find all $include match 
                resolved_json = json.loads(resolved_content)
                for found_match in self._find_match_include(resolved_json):
                    # found_match = self._get_include_match(matches)

                    _logger.debug("_revolveContent: match found {0}".format(found_match))
                    sub_contents = []
                    # match_json = json.loads(found_match)
                    match_json = found_match
                    found_match = json.dumps(found_match)
                    if match_json != None:
                        if "$include" in match_json.keys():
                            # match is { $include: "file:///path of a file"}
                            # check condition property if it exists 
                            include_matched = match_json["$include"]
                            if(self._is_condition_include(include_matched)):
                                for path in self._get_include_path(include_matched):
                                    _logger.debug("_revolveContent: $include - subContentPath {0}".format(path))
    
                                    sub_content = self._get_content(path, resource)
                                    if sub_content != None and sub_content != "":
                                        sub_contents.append(sub_content)
                                if len(sub_contents) > 0 :
                                    resolved_content = resolved_content.replace(found_match, str.join(",", sub_contents))
                                else:
                                    resolved_content = resolved_content.replace(found_match, "{}")
                            else:
                                resolved_content = resolved_content.replace(found_match, "{}")

                # apply regexp to remove content
                for matches in self._merge.findall(resolved_content):
                    found_match = self._get_include_match(matches)

                    _logger.debug("_revolveContent: match found {0}".format(found_match))
                    sub_contents = []
                    # load json to get the path and resolve it
                    idx_sep = found_match.find(":")
                    end_coma = found_match.endswith(",")
                    start_coma = found_match.startswith(",")

                    if end_coma:
                        match_json = json.loads(found_match[idx_sep + 1:-1])
                    else:
                        match_json = json.loads(found_match[idx_sep + 1:])

                    if match_json != None:
                        resolved_content = json.loads(
                            resolved_content.replace(found_match, "," if end_coma and start_coma else ""))
                        for path in match_json:
                            _logger.debug("_revolveContent: $merge - subContentPath {0}".format(path))
                            # merge this json with the current one
                            w_content = self._get_content(path, resource)
                            if w_content != None:
                                _logger.debug("_revolveContent: $merge - subContentPath not null {0}, content={1}".format(path, w_content))
                                to_merges = json.loads("[" + w_content + "]")
                                for to_merge in to_merges:
                                    resolved_content = common.merge_object(resolved_content, to_merge)
                            else:
                                _logger.debug("_revolveContent: $merge - subContentPath not null {0}".format(path))

                        resolved_content = json.dumps(resolved_content)

                        # replace match by list of subcontent
                resolved_contents.append(resolved_content);

            # check if the json is ok and reformat it for the correct application of the regexp
            resource.set_contents(resolved_contents)

    def _check_no_import_files(self, a_filename, a_json):
        """ check if the json contains import-file property and raise an excpetion if it's the case  """
        if "import-files" in a_json:
            raise CBadResourceException("file=[{}] has 'import-files' property, please check your composition file in conf".format(a_filename))

    def _remove_comment(self, resource):
        """
        change the content to remove all possible comment // or /* ...*/
        """
        _logger.debug("_removeComment")

        contents = resource.get_contents()
        if contents != None:
            # apply regexp to remove content
            contents_no_comment = []
            for idx, content in enumerate(contents):
                content_no_comment = content
                for match in self._check.findall(content):
                    if match != None and match.find("/") != -1 and len(match) > 1:
                        _logger.debug("match comment {0}".format(match))

                        if self._check_slash.search(match) == None:
                            _logger.debug("_removeComment: match found {0}".format(match))
                            idx = match.find("/")
                            content_no_comment = content_no_comment.replace(match[idx:], "")
                try:
                    w_json_loaded = json.loads(content_no_comment)
                    self._check_no_import_files(resource.get_full_filename(), w_json_loaded)
                    # check if we have import-file as a property
                    content_no_comment = json.dumps(w_json_loaded)
                except Exception as e:
                    raise CBadResourceException(
                        "not valid json for file {0}, Error {1}".format(resource.read_files_name[idx], e.__str__()))
                contents_no_comment.append(content_no_comment)

            resource.set_contents(contents_no_comment)


@ComponentFactory('cohorte-file-includer-factory')
@Provides(cohorte.SERVICE_FILE_INCLUDER)
@Requires("_finder", cohorte.SERVICE_FILE_FINDER)
@Instantiate('cohorte-file-includer')
class FileIncluder(FileIncluderAbs):
    """
    Simple component that resolve a comment json that can include using { $include : "path"} a subfile json content
    """

    def __init__(self):
        super(FileIncluder, self).__init__()
        self._finder = None

    # override
    def _get_finder(self):
        """
            return the finder component instance abstract method
        """
        return self._finder

    @Validate
    def validate(self, context):
        _logger.info(" validating")

        _logger.info("validated")

    @Invalidate
    def invalidate(self, context):
        _logger.info("invalidating")

        _logger.info("invalidated")
