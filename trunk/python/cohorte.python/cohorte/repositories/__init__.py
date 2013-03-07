#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Repositories of bundles and component factories

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

SERVICE_REPOSITORY_ARTIFACTS = "cohorte.repositories.artifacts"
""" Specification of a repository of artifacts """

SERVICE_REPOSITORY_FACTORIES = "cohorte.repositories.factories"
""" Specification of a repository of component factories """

PROP_REPOSITORY_LANGUAGE = "cohorte.repository.language"
""" Language of implementation of the artifacts handled by the repository """

PROP_FACTORY_MODEL = "cohorte.repository.factory.model"
""" Name of the component model handling the factories """
