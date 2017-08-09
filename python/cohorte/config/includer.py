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
import re
import os
import json
import logging
try:
    # Python 3
    # pylint: disable=F0401,E0611
    import urllib.parse as urlparse
except ImportError:
    # Python 2
    # pylint: disable=F0401
    import urlparse

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate, \
    Validate, Invalidate
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

def replaceVars(params,content):
    """
    @param params : a dictionnary 
    @param content : content with variable to be replace 
    @return a string with variable identified by k from the query string by the value 
    """
    replaceContent = content

    if params != None:
        for match in regexp_replaceVar.findall(content):
            if match in params:
                replaceContent = replaceContent.replace("${"+match+"}",params[match][0])   
            else:
                replaceContent = replaceContent.replace("${"+match+"}","")   
    return replaceContent
    

class CBadResourceException(Exception):
    
    def __init__(self, message):
        self._message = message

    def __str__(self):
        return self._message

    

class CResource(object):
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
        self.queryIdx = path.find("?")
        self.tagIdx = path.find("#")
        self.path = None
        self.params =None
        self.type = "file"
        self.dirpath = None
        self.content = None
        if path != None:
            path = self._initType(path)
            path = self._initPath(path, parent_resource)
            path = self._initQuery(path)
            if  self.tagIdx!=-1:
                self.tag = path[self.tagIdx:]
                         
            self.content = self._initContent()
        else:
            raise CBadResourceException("path parameter has 'None' value")
    
    def _initType(self,path):
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
    
    
    def _initQuery(self,path):
        """
        return the query string parameter key=value that can be in the url. the substring starts with '#' will be ignored
        @param path : a string that can contain key value a.g foo?k=v&p=c
        @return : a param dictionnary that contain the key value 
        """
        if self.queryIdx != - 1:
           
            res_path = path
           # set query if exists
            if self.tagIdx!= -1:
                query = path[self.queryIdx+1:self.tagIdx]
            else:
                query = path[self.queryIdx+1:]
    
            self.params = urlparse.parse_qs(query)
            
            
        return path
        
      
    def _initContent(self):
        """
        return the content of the file with the variable replace 
        @return : a String 
        """
        content = None
        with open(self.path,encoding='utf8') as file:
            content="\n".join(file.readlines())
        return replaceVars(self.params,content) if self.params != None else content
        
    
    def _initPath(self, path, parent_resource=None):
        """
        return the absolute path of the resource to read
        @param path : path of the resource to read 
        @param parent_resource : a parent resource that include the current in order to resolve the relative path if it's necessary
        @return : a String 
        """
        if path != None:
                 
                
            if self.queryIdx!= - 1:
                self.path =  path[self.protocolIdx:self.queryIdx]
            elif self.tagIdx != -1: 
                self.path =  path[self.protocolIdx:self.tagIdx]
            else:
                self.path = path[self.protocolIdx:]

    
            if not self.path.startswith("/") and parent_resource !=None:
                self.path = parent_resource.dirpath+os.sep+self.path
            
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

    def getContent(self, filepath, parent_resource=None):
        """
        return a resolved content json string without commentary
        """
        if filepath != None:
            _logger.info("getContent {0}".format(filepath))
            # return a resolve content json 
            resource = CResource(filepath,parent_resource)
    
            #remove comment 
            resource.content = self._removeComment(resource.content)
            # resolve content 
            resource.content = self._resolveContent(resource);
     
            return resource.content
        return None
  
    
    def _resolveContent(self, resource):
        """
        return a resolve content with all include file content 
        """
        _logger.info("_revolveContent")

        resolvedContent = resource.content;
        if resolvedContent != None:
            # apply regexp to remove content
            for matches in self._include.findall(resource.content):
                _logger.debug("_revolveContent: match found {0}".format(matches))
                match = matches[0]
                subContents = []
                matchJson = self._getJson(match)
                if matchJson != None:
                    if  matchJson["$include"] != None:
                        paths = matchJson["$include"].split(";")
                        _logger.debug("_revolveContent: paths {0}".format(paths))

                        # match is { $include: "file:///path of a file"}
                        for path in paths:
                            _logger.debug("_revolveContent: subContentPath {0}".format(path))
                            subContent = self.getContent(path,resource)
                            subContents.append(subContent)
                         
                        # replace match by list of subcontent 
                        resolvedContent = resolvedContent.replace(match,str.join(",",subContents));
        return self._formatJson(resolvedContent)
        
 
    
    def _removeComment(self,content):
        """
        change the content to remove all possible comment // or /* ...*/
        """
        _logger.info("_removeComment")
        contentNoComment = content
        if contentNoComment != None:
            # apply regexp to remove content
           

            for matches in self._all.findall(content):
                for match in matches:
                    if match != None and match.find("/") != -1:
                        if self._check.search(match) != None or  self._checkSlash.search(match) == None:  
                            _logger.debug("_removeComment: match found {0}".format(match))
                            idx = match.find("/")
                            # get the content of tis file
                            contentNoComment = contentNoComment.replace(match[idx:], "")

        return self._formatJson(contentNoComment)
           
        
 
    def _getJson(self,content):
        """
        check if the content is a correct Json string.
        @return True if json is ok, else false
        """
        return  json.loads(content)
    
    def _formatJson(self,content):
        """
        check if the content is a correct Json string.
        @return True if json is ok, else false
        """
        return  json.dumps(self._getJson(content),indent=4)
     
    @Validate
    def validate(self, context):
        pass
    
    @Invalidate
    def invalidate(self, context):
        pass
    

    