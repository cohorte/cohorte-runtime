#!/usr/bfiles/in/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE file include unit test 



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


import unittest
import os
import json
try:
    # Python 3
    # pylint: disable=F0401,E0611
    import urllib.parse as urlparse
except ImportError:
    # Python 2
    # pylint: disable=F0401
    import urlparse
    
from cohorte.config import includer as includer
# unit test 
##############@                


testCases = [
   [ "empty", "empty" ], 
   [ "module_noComment", "noComment" ],
   [ "module_slashComment", "noComment" ],
   [ "module_slashStarComment", "noComment" ],
   [ "module_allComment", "noComment" ],
   [ "module_testDef", "testDef" ],
   [ "module_allCommentAndFile", "noComment2" ],
   [ "module_allMultiPath", "noCommentMutliPath" ] ,

]


class testIncluder(unittest.TestCase):

    def setUp(self):
        self.include = includer.FileIncluder()


    def test_replaceVar(self):
        print("test_replaceVar")
        self.assertEqual(includer.replaceVars(urlparse.parse_qs("t=2&t2=3"), "${t} foo ${t2}"),"2 foo 3","test replace vars")
        print("\t ok : case ${t} foo ${t2} -> 2 foo 3")


    def test_include(self):
        print("test_include")
        pathFiles = os.path.dirname(__file__)+os.sep+"files"+os.sep
        for case in testCases:
            result = None
            filepathOut = pathFiles+"out"+os.sep+case[1]+".json"
            filepathIn = pathFiles+"in"+os.sep+case[0]+".json"
        
            with open(filepathOut,encoding='utf8') as fileResult:
                result = json.dumps(json.loads("\n".join(fileResult.readlines())),indent=4)
            caseinfo = "test case {0}".format(case[0])
            self.assertEqual(self.include.getContent(filepathIn),result,caseinfo)
            print("\t ok :case "+caseinfo)

        


if __name__ == "__main__":    # call all test
   unittest.main()