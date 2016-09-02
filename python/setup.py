#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
iPOPO installation script

:author: Thomas Calmant
:copyright: Copyright 2014-2016, Cohorte Technologies (ex. isandlaTech)
:license: Apache License 2.0
:version: 1.1.0
:status: Beta

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

# Module version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

import os

try:
    from setuptools import setup
except ImportError:
    from distutils.core import setup

# ------------------------------------------------------------------------------


def read(fname):
    """
    Utility method to read the content of a whole file
    """
    with open(os.path.join(os.path.dirname(__file__), fname)) as fd:
        return fd.read()

setup(
    name='Cohorte-Python',
    version=__version__,
    license='Apache License 2.0',
    description='A service-oriented component model framework',
    author='Thomas Calmant',
    author_email='thomas.calmant@isandlatech.com',
    packages=[
        'cohorte',
        'cohorte.boot',
        'cohorte.boot.loaders',
        'cohorte.boot.looper',
        'cohorte.cocoapy',
        'cohorte.composer',
        'cohorte.composer.isolate',
        'cohorte.composer.isolate.agents',
        'cohorte.composer.node',
        'cohorte.composer.node.criteria',
        'cohorte.composer.node.criteria.distance',
        'cohorte.composer.node.criteria.reliability',
        'cohorte.composer.top',
        'cohorte.composer.top.criteria',
        'cohorte.composer.top.criteria.distance',
        'cohorte.composer.top.criteria.reliability',
        'cohorte.config',
        'cohorte.debug',
        'cohorte.forker',
        'cohorte.forker.starters',
        'cohorte.instruments',
        'cohorte.java',
        'cohorte.local',
        'cohorte.monitor',
        'cohorte.repositories',
        'cohorte.repositories.java',
        'cohorte.repositories.python',
        'cohorte.shell',
        'cohorte.utils',
        'cohorte.vote'],
    classifiers=[
        'Development Status :: 4 - Beta',
        'Environment :: Console',
        'Intended Audience :: Developers',
        'License :: OSI Approved :: Apache Software License',
        'Operating System :: OS Independent',
        'Programming Language :: Python :: 3.3',
        'Programming Language :: Python :: 3.4',
        'Topic :: Software Development :: Libraries :: Application Frameworks'
    ],
    install_requires=[
        'iPOPO >= 0.5.7',
        'Cohorte-Herald >= 1.0.0'
    ],
    extras_require={
        'java': ['JPype1-py3']
    }
)
