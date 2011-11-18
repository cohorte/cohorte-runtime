#!/usr/bin/python
#-- Content-Encoding: utf-8 --

import javaobj
import os

cache = None
cache_file = None

def get_cache_file(isolate="isolate-cache"):
    """
    Tries to find the default cache file
    """
    global cache_file

    psem2m_base = os.getenv("PSEM2M_BASE")
    if not psem2m_base:
        psem2m_base = "./base"

    cache_file = psem2m_base + "/var/storage/" + isolate + "/cache.data"
    return cache_file


def read_cache(filename=get_cache_file()):
    """
    Reads the cache given file
    """
    global cache

    with open(filename) as data:
        cache = javaobj.load(data)

    return cache

cache = read_cache()

