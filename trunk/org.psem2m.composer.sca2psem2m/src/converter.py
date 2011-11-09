'''
Entry point of the SCA XML converter

Created on 8 nov. 2011

@author: Thomas Calmant
'''

from optparse import OptionParser
from psem2m.composer.scaconverter import SCAConverter
import fnmatch
import os
import sys
import json

def find_files(path, pattern="*", recursive=False):
    """
    Finds all files in the given path corresponding to the given pattern
    
    @param path: Path to look into
    @param pattern: Pattern that the file must match
    @param recursive: Look into sub-folders
    
    @return: An array of file paths
    """
    result_list = []

    if os.path.isfile(path) and fnmatch.fnmatch(path, pattern):
        # The path points to a file matching the pattern
        result_list = [os.path.abspath(path)]

    elif os.path.isdir(path):
        # Walk into the path
        for node in os.listdir(path):
            node_path = os.path.normpath(path + os.path.sep + node)

            # Found a directory, look into it if needed
            if os.path.isdir(node_path) and recursive:
                result_list.extend(find_files(node, pattern, recursive))

            elif os.path.isfile(node_path) and fnmatch.fnmatch(node, pattern):
                result_list.append(os.path.abspath(node_path))

    return result_list


def find_composite_files(paths_list, recursive):
    """
    Returns the list of *.composite files in the given directories, and the
    files indicated with a full path.
    
    @param paths_list: An array of paths
    @param recursive: When a directory is found in paths, look recursively into 
    it
    
    @return: The found files list
    """
    if not paths_list:
        return []

    result_list = []

    for path in paths_list:
        result_list.extend(find_files(path, "*.composite", recursive))

    return result_list


def open_output_file(filename):
    """
    Opens or creates a file with the given name. Returns the standard output
    if the filename is None, empty or "-".
    
    @param filename: The name of the file to open
    """

    # Test standard output names
    if not filename or filename == "-":
        return sys.stdout

    # Open a file on disk
    try:
        return open(filename, "w")

    except IOError as error:
        print >> sys.stderr, "Error opening '" + str(filename) + "' :", error
        return None


def main():
    """
    Entry point
    """
    # Prepare the options parser
    parser = OptionParser()
    parser.add_option("-C", "--root-composite", dest="root_composite", \
                      default=None, metavar="ROOT",
                      help="Sets the root targetNamespace of the composition to return (optional)")
    parser.add_option("-o", "--outfile", dest="output", default="-", \
                help="Write the result into FILE ('-' for standard output, default)", \
                metavar="FILE")
    parser.add_option("-r", "--recursive", dest="recursive", default=False,
                      help="When a directory is found in paths, look recursively into it. Deactivated by default.")

    (options, args) = parser.parse_args()

    # Compute the input file list
    if len(args) == 0:
        input_files_paths = find_composite_files([os.getcwd()], \
                                                 options.recursive)

    else:
        input_files_paths = find_composite_files(args, options.recursive)

    if not input_files_paths:
        # No files found
        print >> sys.stderr, "No *.composite files found. Abandon"
        return 1

    # Compute the output file
    output_file = open_output_file(options.output)
    if not output_file:
        print >> sys.stderr, "Can't open output file. Abandon."
        return 2

    # Convert the model
    converter = SCAConverter()
    psem2m_model = converter.convert(input_files_paths)

    # Write its JSON version
    json_output = json.dumps(psem2m_model.to_bean(), sort_keys=True, indent=4)
    output_file.write(json_output)

    # Close the output file
    if output_file != sys.stdout:
        output_file.close()

    return 0

if __name__ == '__main__':
    sys.exit(main())
