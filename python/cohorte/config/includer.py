#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE file finder

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
import cohorte
import glob
import json
import logging
import os
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate, \
    Validate, Invalidate
import re


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

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 0, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

regexp_replaceVar = re.compile("\\$\\{(.+?)\\}", re.MULTILINE)

def replaceVars(params, contents):
    """
    @param params : a dictionnary 
    @param content : content with variable to be replace 
    @return a string with variable identified by k from the query string by the value 
    """
    if isinstance(contents, str):
        contents = [contents]
    replaceContents = []

    if params != None:
        for content in contents:
            replaceContent = content
            for match in regexp_replaceVar.findall(content):
                if match in params:
                    replaceContent = replaceContent.replace("${" + match + "}", params[match][0])
                else:
                    replaceContent = replaceContent.replace("${" + match + "}", "") 
            replaceContents.append(replaceContent)
    return replaceContents



class CBadResourceException(Exception):
    
    def __init__(self, message):
        self._message = message

    def __str__(self):
        return self._message


class CResource(object):
    
    
    
    def getTag(self):
        return self.tag
    
    def getParams(self):
        return self.parms
    
    def getPath(self):
        return self.path
    
    def getDirPath(self):
        return self.dirpath
    
    def getContents(self):
        return self.contents
    
    def setContents(self, contents):
        self.contents = contents
        
  

    def readAll(self):
        """
        return the merge content of all files that correspond to the resource
        """
        list = []
        if self.contents != None:
            for content in self.contents:
                if self.tag != None:
                    list.append(json.dumps(json.loads(content)[self.tag], indent=2))
                else:
                    list.append(content)
            
            return ",".join(list)
        return None
    
    """
    describe a resource that can be file, a http or memory or any kind
    """
    def __init__(self, path, parent_resource=None):
        """
        construct a resource from a string e.g file:///mypath/foo.txt?k1=v1&#mytag 
        @param resource : parent resource , use if path is relative 
        @param path : path of the current resource to create 
        """
        self.protocolIdx = 0
        self.path = None
        self.params = None
        self.type = "file"
        self.dirpath = None
        self.contents = None
        self.tag = None
        self._setPath(path, parent_resource)
    
    
    def _setPath(self, path, parent_resource=None):
        """
        @param path : path of the files to load. can identified one file by multiple file via a regexp or wildchar
        """
        if path != None:
            self.queryIdx = path.find("?")
            self.tagIdx = path.find("#")    
            path = self._initType(path)
            path = self._initPath(path, parent_resource)
            path = self._initQuery(path)
            if  self.tagIdx != -1:
                self.tag = path[self.tagIdx + 1:]
                         
            self.contents = self._initContents()
        else:
            raise CBadResourceException("path parameter has 'None' value")
    
    def _initType(self, path):
        """ 
        return the protocol to use for resolving the url content 
        @param path : a string that start with protocol:// 
        @return : a type that identified which protocol to use : can be file, http or memory
        """
        if path != None:
            self.type = "file"
             # prefix that identify the protocol e.g file://
            if path.startswith("memory://"):
                self.type = "memory"
                self.protocolIdx = 9
            elif path.startswith("http://"):
                self.type = "http"
                self.protocolIdx = 7
            elif path.startswith("file://"):
                self.protocolIdx = 7

        return path
    
    
    def _initQuery(self, path):
        """
        return the query string parameter key=value that can be in the url. the substring starts with '#' will be ignored
        @param path : a string that can contain key value a.g foo?k=v&p=c
        @return : a param dictionnary that contain the key value 
        """
        if self.queryIdx != -1:
           
            res_path = path
           # set query if exists
            if self.tagIdx != -1:
                query = path[self.queryIdx + 1:self.tagIdx]
            else:
                query = path[self.queryIdx + 1:]
    
            self.params = urlparse.parse_qs(query)
            
            
        return path
        
    def _initContentsFile(self):
        """
        return the contents of the files identified by the path 
        @return : a list of String 
        """
        # TODO manage path is a regexp  
        contents = []
        listFile = glob.glob(self.path)
        if len(listFile) == 0:
               return None
           
        for file in glob.glob(self.path):
            with open(file, encoding='utf8') as ofile:
                contents.append("\n".join(ofile.readlines()))
                       
        return contents
    
    
    def _initContents(self):
        """
        return the content of the url with the variable replace 
        @return : a  list of String 
        """
        if self.type == "file":
            self.contents = self._initContentsFile()
        else:
            # not manager
            self.content = None
                          
        if self.params != None:
            self.contents = replaceVars(self.params, self.contents)
            
        return self.contents
        
    
    def _initPath(self, path, parent_resource=None):
        """
        return the absolute path of the resource to read
        @param path : path of the resource to read 
        @param parent_resource : a parent resource that include the current in order to resolve the relative path if it's necessary
        @return : a String 
        """
        if path != None:
                 
                
            if self.queryIdx != -1:
                self.path = path[self.protocolIdx:self.queryIdx]
            elif self.tagIdx != -1: 
                self.path = path[self.protocolIdx:self.tagIdx]
            else:
                self.path = path[self.protocolIdx:]

    
            if not self.path.startswith("/") and parent_resource != None:
                self.path = parent_resource.dirpath + os.sep + self.path
            
            self.dirpath = os.sep.join(self.path.split(os.sep)[:-1])
    
        return path
    
   
    
   
    
    
    
@ComponentFactory('cohorte-file-includer-factory')
@Provides(cohorte.SERVICE_FILE_INCLUDER)
@Instantiate('cohorte-file-includer')
class FileIncluder(object):
    """
    Simple component that resolve a comment json that can include using { $include : "path"} a subfile json content
    """
    def __init__(self):
       # TODO check what to init
       self._all = re.compile("(/\\*+.*(\n|.)*?\\*.*(\n|.)*?.*\\*+/)|(.*//.*$)", re.MULTILINE)
       self._check = re.compile("(/\\*+.*(\n|.)*?\\*.*(\n|.)*?.*\\*+/)", re.MULTILINE)
       self._checkSlash = re.compile("(\".*//.*\")", re.MULTILINE)
       self._include = re.compile("((\\n)*\\{(\\n)*\\s*\"\\$include\"\\s*:(\\s*\".*\"\\s*)\\})", re.MULTILINE)

    def _getContent(self, filepath, parent_resource=None):
        """
        return a resolved content json string without commentary
        """
        content = None
        if filepath != None:
            _logger.info("getContent {0}".format(filepath))
            
            
          
            # return a resolve content json 
            resource = CResource(filepath, parent_resource)
            # remove comment and content of the resource
            self._removeComment(resource)
            
            # resolve content 
            self._resolveContent(resource);
            
                           
            return resource.readAll()
        return None
    
    def getContent(self, filepath):
        """
        return a resolved content json string without commentary
        """
        content = self._getContent(filepath)
        if content == None :
            raise IOError("[Errno 2] No such file or directory: '{0}'".format(filepath))
        return content
    
    def _resolveContent(self, resource):
        """
        return a resolve content with all include file content 
        """
        _logger.info("_revolveContent")

        contents = resource.getContents();
        if contents != None:
            resolvedContents = []
            for content in contents:
                resolvedContent = content
                # apply regexp to remove content
                for matches in self._include.findall(resolvedContent):
                    _logger.debug("_revolveContent: match found {0}".format(matches))
                    match = matches[0]
                    subContents = []
                    # load json to get the path and resolve it
                    matchJson = json.loads(match)
                    if matchJson != None:
                        if  matchJson["$include"] != None:
                            paths = matchJson["$include"].split(";")
                            _logger.debug("_revolveContent: paths {0}".format(paths))
    
                            # match is { $include: "file:///path of a file"}
                            for path in paths:
                                _logger.debug("_revolveContent: subContentPath {0}".format(path))
                                subContent = self._getContent(path, resource)
                                subContents.append(subContent)
                             
                            # replace match by list of subcontent 
                            resolvedContent = resolvedContent.replace(match, str.join(",", subContents))
                # check if it's json and format it 
                resolvedContent = json.dumps(json.loads(resolvedContent), indent=2)
                resolvedContents.append(resolvedContent);
                            
                            
            # check if the json is ok and reformat it for the correct application of the regexp
            resource.setContents(resolvedContents)         

 
    
    def _removeComment(self, resource):
        """
        change the content to remove all possible comment // or /* ...*/
        """
        _logger.info("_removeComment")
        contents = resource.getContents()
        if contents != None:
            # apply regexp to remove content
            contentsNoComment = []
            for content in contents:
                contentNoComment = content
                for matches in self._all.findall(content):
                    for match in matches:
                        if match != None and match.find("/") != -1:
                            if self._check.search(match) != None or  self._checkSlash.search(match) == None:  
                                _logger.debug("_removeComment: match found {0}".format(match))
                                idx = match.find("/")
                                contentNoComment = contentNoComment.replace(match[idx:], "")
                # check if it's json file and format it 
                contentNoComment = json.dumps(json.loads(contentNoComment), indent=2)
                contentsNoComment.append(contentNoComment)
                       
            resource.setContents(contentsNoComment)         
       
           
        
 
  

     
    @Validate
    def validate(self, context):
        pass
    
    @Invalidate
    def invalidate(self, context):
        pass
    

    
