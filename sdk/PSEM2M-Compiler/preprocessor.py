#!/usr/bin/python
#-- Content-Encoding: utf-8 --
'''
Created on 8 déc. 2011

@author: Thomas Calmant
'''

import ant_generator
import eclipse_reader
import glob
import logging
import os
import subprocess
import sys

ROOT = "/home/tcalmant/programmation/workspaces/psem2m-ivy-hybrid"
TARGET_PLATFORM = ROOT + "/platforms/felix"

def get_projects(root_directory):
    """
    Retrieves all Eclipse projects recursively
    
    @param root_directory: Root directory for recursive search
    """
    project_files = glob.glob(root_directory + os.sep + "*" \
                              + os.sep + ".project")
    projects = []
    
    for project_file in project_files:
        try:
            projects.append(eclipse_reader.read_project(project_file))
            
        except:
            logging.warn("Error reading file.", exc_info=True)
    
    return projects


def main(compilation_name="PSEM2M"):
    """
    Entry point
    """
    
    # TODO: handle program parameters
    
    result = 0
    
    try:
        print "--> Get Projects..."
        projects = get_projects(ROOT)
        
        print "--> Resolve links..."
        for project in projects:
            project.resolve_links(projects)
            
        print "--> Generate Ant files..."
        ant_generator.prepare_ant_files(compilation_name, ROOT, \
                                        projects, [TARGET_PLATFORM])
        
        print "--> Compile time !"
        root_ant_file = ROOT + os.sep + "build.xml"
        result = subprocess.call(["ant", "-f", root_ant_file, "package"])
        
    except:
        logging.error("Error during treatment.", exc_info=True)
        result = 1
    
    try:
        print "--> Clean up the mess"
        for project in projects:
            project.clean()
            
    except:
        logging.warn("Error during cleanup.", exc_info=True)
    
    return result


if __name__ == '__main__':
    logging.basicConfig()
    sys.exit(main())
