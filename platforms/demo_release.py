#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --

import imp
import os
import shutil
import tempfile
import zipfile

# ------------------------------------------------------------------------------

# Prefix of a PSEM2M Base directory
BASE_PREFIX = "base-"

# Controller.py path, relative to the small install home
CONTROLLER_PATH = os.path.join("bin", "controller.py")

# Extra modules file name
EXTRA_MODULES_FILE = "python_modules.py"

# Extra modules entry
EXTRA_MODULES_ENTRY = "EXTRA_MODULES"

# PSEM2M Home directory
PSEM2M_HOME = "psem2m.home"

# Small install home directory
SMALL_INSTALL_HOME = "small.home"

# ------------------------------------------------------------------------------

def accept_file(filename):
    """
    Tests if the given filename can be stored or not in a release ZIP file
    
    :param filename: A full file-path
    :return: True if the file is accepted in the ZIP, else False
    """
    name_parts = filename.split(os.sep)
    name = name_parts[-1]

    if name == EXTRA_MODULES_FILE:
        # Ignore the extra modules file
        return False

    if "var" in name_parts[:-1]:
        # Ignore files from the var directory
        return False

    if name[0] == '.':
        # Hidden files are ignored
        return False

    if name.endswith(".dat") or name.endswith(".pyc"):
        # Binary/temporary files
        return False

    # All tests passed
    return True


def find_bases(root=os.curdir):
    """
    Retrieves the list of bases found in the given root directory
    
    :param root: Directory to search in
    :return: Found bases (list)
    """
    bases = []

    files = os.listdir(root)
    for filename in files:
        filepath = os.path.join(root, filename)
        if os.path.isdir(filepath) and filename.startswith(BASE_PREFIX):
            bases.append(filepath)

    return bases


def make_zip(zip_name, files_dict):
    """
    Makes a ZIP file according to the given dictionary
    
    :param zip_name: Name of the output ZIP file
    :param files_dict: A file path -> zip path mapping
    """
    print("Outzip = " + zip_name)

    outzip = zipfile.ZipFile(zip_name, "w")
    try:
        for fullname, zipname in files_dict.items():
            if zipname is None:
                zipname = fullname

            if accept_file(zipname):
                outzip.write(fullname, zipname)

    finally:
        outzip.close()


# ------------------------------------------------------------------------------

def make_monitor_zip(dev_root, home, base, node, outdir=os.curdir):
    """
    Prepares the ZIP file for the main PSEM2M node
    """
    print("Working on Monitor: {node}...".format(node=node))

    # Compute the files to store
    outfiles = {}

    # Copy existing files
    for zipdir in (home, base):
        parent_dir = os.path.abspath(os.path.join(zipdir, '..'))

        for root, dirs, files in os.walk(zipdir, followlinks=True):
            for filename in files:
                # Get the full file name
                fullname = os.path.join(root, filename)

                # Get the ZIP name, relative to the root of the name
                zipname = os.path.relpath(fullname, parent_dir)

                if accept_file(zipname):
                    # Add the file to the ZIP (ignore /var directory)
                    outfiles[fullname] = zipname

    # Add development files
    extend_home_files(dev_root, outfiles)

    # Make the ZIP
    make_zip(os.path.join(outdir, node + "-monitor.zip"), outfiles)
    print("Monitor {node} done.".format(node=node))


def update_controller(home, node, zipfiles):
    """
    Changes the ``controller.py`` file content to match the node name
    
    The returned file must be closed after use.
    
    :param home: Small install home directory
    :param node: Node name
    :param zipfiles: ZIP file entries
    :return: The temporary controller file
    """
    # Escape the node name
    node = node.replace('"', r'\"')

    # Forge the controller path
    controller = os.path.abspath(os.path.join(home, CONTROLLER_PATH))

    # Make a temporary file
    outfile = tempfile.NamedTemporaryFile()

    # Write the new controller
    with open(controller) as infile:
        for line in infile:
            line = line.rstrip()
            if line.startswith("DEFAULT_NODE = "):
                # Replace the node line
                line = 'DEFAULT_NODE = "{node}"'.format(node=node)

            # Write the line
            outfile.write(line)
            outfile.write(os.linesep)

    # Update the ZIP entries

    # Default entry...
    zip_path = os.path.join(SMALL_INSTALL_HOME, CONTROLLER_PATH)

    if controller in zipfiles:
        # Remove the old entry
        zip_path = zipfiles[controller]
        del zipfiles[controller]

    else:
        print("Warning: {name} not in ZIP entries".format(name=controller))

    # Add the new entry
    zipfiles[outfile.name] = zip_path

    return outfile


def make_small_install_zip(small_install, node, outdir=os.curdir):
    """
    Prepares the ZIP for a small install
    
    :param small_install: Small install root directory
    :param node: Node name
    """
    print("Working on small node: {node}...".format(node=node))

    # Small install PSEM2M home
    home = os.path.abspath(os.path.join(small_install, SMALL_INSTALL_HOME))

    # Small install PSEM2M base
    base = os.path.abspath(os.path.join(small_install, BASE_PREFIX + node))

    # Compute the files to store
    outfiles = {}

    for zipdir in (home, base):
        for root, dirs, files in os.walk(zipdir, followlinks=True):
            for filename in files:
                # Get the full file name
                fullname = os.path.abspath(os.path.join(root, filename))

                # Get the ZIP name, relative to the root of the name
                zipname = os.path.relpath(fullname, small_install)

                if accept_file(zipname):
                    # Add the file to the ZIP (ignore /var directory)
                    outfiles[fullname] = zipname

    # Replace the controller.py file
    tmp_file = update_controller(home, node, outfiles)
    make_zip(os.path.join(outdir, node + "-small.zip"), outfiles)

    # Clean up the temporary file
    tmp_file.close()
    print("{node} Done.".format(node=node))


def make_small_installs_zips(small_install, monitor_node, outdir=os.curdir):
    """
    Prepares the ZIP files for all small install bases, except the monitor node
    (if found)
    
    Small install bases are always named as follow : ``base-{node}``
    
    :param small_install: Small installs root directory
    :param monitor_node: PSEM2M Monitor node (excluded)
    """
    bases = find_bases(small_install)

    for base in bases:
        node = os.path.basename(base)[len(BASE_PREFIX):]
        if node == monitor_node:
            # Ignore monitor node
            print("Monitor node ({node}) ignored.".format(node=monitor_node))

        else:
            # Make small.home + base ZIP
            make_small_install_zip(small_install, node, outdir)

# ------------------------------------------------------------------------------

def request_input(prompt, default_value=None, accept_empty=False):
    """
    Requests an input from the user.
    
    :param prompt: The beginning of the prompt (the ':' will be added)
    :param default_value: A default value, if the user types nothing
    :param accept_empty: If True, accepts empty values (i.e. if the default
                         value is empty)
    :return: The user or the default entry
    """
    # Forge the prompt
    if default_value is not None:
        prompt = "{prompt} [{default}]: ".format(prompt=prompt,
                                                 default=default_value)

    elif accept_none:
        prompt = "{prompt} []: ".format(prompt=prompt)

    else:
        prompt = "{prompt}: ".format(prompt=prompt)

    while True:
        # Ask user for an entry
        entry = raw_input(prompt)
        if not entry:
            entry = default_value

        if not entry:
            if accept_empty:
                # Empty value accepted
                return entry

            else:
                print "Please, type something"

        else:
            # Valid entry
            return entry


def select_list(choices):
    """
    Retrieves the item selected by the user, None to exit
    
    :param choices: Available choices (iterable)
    :return: Index of the selected item
    :raise EOFError: User asked to stop
    """
    i = 1
    for choice in choices:
        print("{idx:3d}: {name}".format(idx=i, name=choice))
        i += 1

    print("Press q to quit.")

    selection = None
    while selection is None:
        raw_selection = raw_input("Selection: ")
        if raw_selection == 'q' or raw_selection == 'quit':
            raise EOFError

        try:
            selection = int(raw_selection)
            # Selection is 1-based (human friendly)
            if selection < 1 or selection > len(choices):
                print("Selection out of range")
                selection = None

        except:
            # Ignore conversion errors
            pass

    # Selection is 1-based (human friendly)
    return choices[selection - 1]

# ------------------------------------------------------------------------------

def read_extra_modules(dev_root, base):
    """
    Reads the extra modules file in the given base
    
    :param dev_root: Development root directory
    :param base: A PSEM2M Base directory
    :return: The extra modules dictionary
    """
    filename = os.path.join(base, EXTRA_MODULES_FILE)
    if not os.path.exists(filename):
        # File not found
        print("(No extra modules file found)")
        return {}

    # Load the file (let errors be raised)
    module = imp.load_source('python_modules', filename)
    try:
        extra_entries = getattr(module, EXTRA_MODULES_ENTRY)

    except AttributeError:
        # No entry found
        extra_entries = {}

    # Normalize entries
    for modname, srcdir in extra_entries.items():
        if not os.path.isabs(srcdir):
            # Relative path found 
            newdir = os.path.abspath(os.path.join(dev_root, srcdir))
            extra_entries[modname] = newdir

    return extra_entries


def prepare_zip_directory(zip_files, srcdir, zipdir):
    """
    Updates the ZIP entries dictionary to add the given source directory to
    the ZIP directory
    
    :param zip_files: ZIP entries dictionary
    :param srcdir: Source directory
    :param zipdir: Directory in the ZIP file
    """
    for root, dirs, files in os.walk(srcdir):
        for filename in files:
            # Get the full path
            fullpath = os.path.join(root, filename)

            if accept_file(fullpath):
                # File passed filters, compute its ZIP name
                relpath = os.path.relpath(fullpath, srcdir)
                zip_files[fullpath] = os.path.join(zipdir, relpath)


def extend_home_files(dev_root, zip_files, home_dir=PSEM2M_HOME):
    """
    Extends the ZIP entries with the content of the forker and base development
    projects
    
    :param dev_root: Root of the PSEM2M development directory
    :param zip_files: ZIP entries dictionary
    :param home_dir: Name of the PSEM2M Home directory
    """
    # Python projects root directory in the development tree
    python_root = os.path.join(dev_root, "trunk", "python")

    # Module -> source directory
    directories = {
                   # Special case: copy both psem2m module and root content
                   ".": os.path.join(python_root, "psem2m.forker", "src"),
                   "base": os.path.join(python_root, "psem2m.base", "src"),
                   }

    for modname, srcdir in directories.items():
        # Source directory
        indir = os.path.join(srcdir, modname)

        # Output ZIP directory
        outdir = os.path.join(home_dir, "bin", modname)

        # Prepare the ZIP entries
        prepare_zip_directory(zip_files, indir, outdir)


def update_small_install(dev_root, small_install, extra_modules):
    """
    Updates the small install home content
    
    :param dev_root: PSEM2M Development root directory
    :param small_install: Small install root directory
    :param extra_modules: Extra Python modules, read from the current base
    """
    python_root = os.path.join(dev_root, "trunk", "python")
    outdir_root = os.path.join(small_install, SMALL_INSTALL_HOME, "bin")

    # Module -> source directory
    directories = {
                   "base": os.path.join(python_root, "psem2m.base", "src"),
                   "psem2m": os.path.join(python_root, "psem2m.forker", "src"),
                   }

    if extra_modules is not None:
        directories.update(extra_modules)

    for modname, srcdir in directories.items():
        # Clean up existing files
        outdir = os.path.join(outdir_root, modname)
        if os.path.isdir(outdir):
            shutil.rmtree(outdir)

        # Copy the tree
        shutil.copytree(os.path.join(srcdir, modname), outdir, False)

# ------------------------------------------------------------------------------

def expand_path(path):
    """
    Expands the given path and tries to return an absolute path
    
    :param path: A path
    :return: If possible, an expanded path
    """
    return os.path.abspath(os.path.expandvars(os.path.expanduser(path)))


def main():
    """
    Entry point
    
    :raise EOFError: User asked to stop
    """
    # Find the development root
    dev_root = request_input("Development root", os.path.abspath('..'))

    # Find small-install
    small_install = request_input("Small Install path",
                                  os.path.join(os.curdir, "small-install"))

    # Select the PSEM2M base of the demo
    root = os.curdir
    home = request_input("PSEM2M Home", os.path.join(root, PSEM2M_HOME))
    base = select_list(find_bases(root))

    # Select the output directory
    outdir = request_input("Output directory", "~")

    # Normalize
    home = expand_path(home)
    base = expand_path(base)
    dev_root = expand_path(dev_root)
    small_install = expand_path(small_install)
    outdir = expand_path(outdir)

    # Prepare the output directory
    if not os.path.exists(outdir):
        os.makedirs(outdir)

    print("""Using:
- PSEM2M Base:      {base}
- small-install:    {small}
- Output directory: {output}
""".format(base=base, small=small_install, output=outdir))

    # Ask for the monitor node name
    monitor_node = request_input("Monitor node", "stratus")

    # 0. Update Small install
    extra_modules = read_extra_modules(dev_root, base)
    update_small_install(dev_root, small_install, extra_modules)

    # 1. ZIP the base
    make_monitor_zip(dev_root, home, base, monitor_node, outdir)

    # 2. ZIP the other nodes
    make_small_installs_zips(small_install, monitor_node, outdir)


if __name__ == "__main__":
    try:
        main()

    except (EOFError, KeyboardInterrupt):
        print("Bye !")
