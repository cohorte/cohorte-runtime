#!/usr/bin/env python
#-- Content-Encoding: utf-8 --
'''
Created on 6 févr. 2012

@author: Thomas Calmant
'''

import os.path
import shutil
import sys
import tarfile

# The files to include
def get_files(root):
    """
    Retrieves the files in the current directory with no extension or with the
    .py or .jar extension
    """
    files = []

    for filename in os.listdir(root):

        split_ext = os.path.splitext(filename)
        if len(split_ext) != 2:
            # No extension found, add it
            files.append(filename)

        else:
            ext = split_ext[1]
            if ext == ".py" or ext == ".jar":
                files.append(filename)

    return files


def to_archive(files, archive_name, root_name=None):
    """
    Make a tar.gz file of the given files
    """
    archive = tarfile.open(archive_name, "w:gz", dereference=True)

    for filename in files:

        if root_name is not None:
            archive_name = os.path.join(root_name, filename)
        else:
            archive_name = None

        archive.add(filename, archive_name)

    archive.close()


def install(files, install_dir):
    """
    Installs the files at the given location
    """
    if not os.path.exists(install_dir):
        os.mkdir(install_dir)

    for filename in files:
        print filename, "-->", os.path.join(install_dir, filename)
        shutil.copy(filename, install_dir)


def main():
    if len(sys.argv) < 2:
        print "Missing parameter (sdist or install)"
        return

    action = sys.argv[1]
    root_name = os.path.basename(os.getcwd())

    if action == "sdist":
        files = get_files(os.getcwd())

        if len(sys.argv) == 3:
            out_file = sys.argv[2]
        else:
            out_file = root_name + ".tar.gz"

        to_archive(files, out_file, root_name)

    elif action == "install":
        files = get_files(os.getcwd())

        if len(sys.argv) == 3:
            out_dir = sys.argv[2]
        else:
            out_dir = "/opt/PSEM2M-Compiler"

        install(files, out_dir)

    else:
        print "Unknown parameter :", sys.argv[0]

if __name__ == "__main__":
    main()
