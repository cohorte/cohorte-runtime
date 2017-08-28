#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
isolate stater environment parmeter component
provide the env paremter to set for all isolate

:author: Aurelien Pisu
:license: Apache Software License 2.0
:version: 1.0.0

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
import cohorte
import logging
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Property, Instantiate, Validate, Invalidate


# Pelix framework
# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory('cohorte-forker-starter-environment-factory')
@Provides(cohorte.forker.SERVICE_ENV_STARTER)
@Instantiate('cohorte-forker-starter-environment')
class EnvironmentStarter(object):
    
    def __init__(self):
        # dict of env key value
        self._envs = {}
        
        # bundle context 
        self._context = None
    
    def get_envs(self):
        return self._envs
    
    def _extract_env(self, property_str):
        """
            @param property_str : string that represent the environment variable e.g myprop=myvalue
            @return :  return a tuple with property name and value that contain the property name and property value  or None if the string is not correct
        """
        if property_str.find("=") != -1:
            split = property_str.split("=")
            if len(split) == 2:
                return (split[0], split[1])
            
            
        return None
        
    @Validate
    def validate(self, context):
        self._context = context
        envs = self._context.get_property(cohorte.PROP_ENV_STARTER)
        # we get a list of string that should be "key=value" or "key=value;key=value
        if envs != None:
            for envs_str in envs:
                if envs_str.find(";") != -1:
                   # several env properties
                   for env_str in envs_str.split(";"):
                       res = self._extract_env(env_str)
                       if res != None:
                           name, value = res
                           self._envs[name] = value
            
                else:
                    # only one 
                    res = self._extract_env(envs_str)
                    if res != None:
                        name, value = res
                        self._envs[name] = value

    @Invalidate
    def invalidate(self, context):
        self._envs = None

    
