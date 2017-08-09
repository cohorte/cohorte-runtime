# Cohorte configuration parser

This package contains the modules which can parse JSON files or the description of isolate.

* ``finder``: looks for a file with a given name in ``$COHORTE_HOME`` and ``$COHORTE_BASE``;
* ``parser``: parses the content of a JSON file to create objects for the Cohorte Runtime model;
* ``reader``: handles the comments, the ``import-file`` and ``from-file`` entries in JSON files. This allows to generate a string containing a JSON object or array, readable by the JSON parser from Python standard library while the file on-disk contains comments and includes data from other JSON files.

## Public API

The services provided by the ``finder`` and ``reader`` modules can be used by any component of the Cohorte application.
The ``parser`` module should be used by the Cohorte Runtime components only.

### Includer
TODO

### Finder

The finder is accessible through the ``cohorte.SERVICE_FILE_FINDER`` service, and provides the following methods:

* ``add_custom_root(root_dir)``: adds a directory where to look for files;
* ``remove_custom_root(root_dir)``: removes a custom root added with ``add_custom_root()``; 
* ``find_rel(file_name, base_file) -> generator(file_paths)``: looks for a file, relatively to a base file. This method is a generator.
  The file will be looked for in the same folder as the base file, in the ``$COHORTE_BASE`` then ``$COHORTE_HOME`` directories, then in the custom roots.
* ``find_gen(pattern, base_dir=None, recursive=True) -> gen(file_paths)``: looks for a file matching a file pattern (with ``?`` and ``*``). This method is a generator.
  The file will be looked for in the ``$COHORTE_BASE`` then ``$COHORTE_HOME`` directories, then in the custom roots.

### Reader

The reader parses JSON files that can contain comments and references to other JSON files. It uses the ``finder`` to find the files referenced in JSON. It provides the ``cohorte.SERVICE_FILE_READER`` service, with the following methods:

* ``merge_object(local: dict, imported: dict) -> dict``: merges recursively two dictionaries (parsed JSON objects).
  The values from *local* have priority over *imported* ones. Arrays of objects are also merged: values from *imported* are appended to the ones from *local*, avoiding duplicates.
  The objects to merge are recognized according the content of their ``id``, ``uid`` or ``name`` entries.
* ``load_file(filename, base_file=None, overridden_props=None: dict, log_error=True) -> parsed content``: reads the given JSON file, handling merges with referenced JSON files.
  The *base_file* will be given to the ``finder`` to find the file with the given name. *log_error* allows to hide read errors from logs, generally because the caller will log them with more contextual information.
  The *overridden_props* defines the properties to override in the referenced files (during recursive imports).

## File format

### Comments

The ``reader`` supports Javascript-style comments:

```javascript
{
    /* Some Comment */
    // Single-line comment
    /*
    Multi-lines
    comment
    */
    "answer": 42 // In-line comment
}
```

Comments are removed by the ``reader`` before giving the file to the JSON parser. Therefore, it is likely that the parser exceptions will indicate invalid lines in the JSON file.


### Merge import

The ``import-files`` entry can be used to merge the content of the current JSON object with the content other JSON files.
An array with multiple file names can be given as value for this entry: they will be merged from first to last, i.e. the properties of the last file will have priority over the ones from the first file.

The configuration file named ``boot-common.js`` has the following content:
```javascript
/**
 * Boot configuration common for all isolates
 */
{
	/*
	 * List of bundles
	 */
	"bundles" : [
	// The remote shell is common to all isolates
	{
		"name" : "pelix.shell.core"
	}, {
		"name" : "pelix.shell.ipopo"
	}, {
		"name" : "pelix.shell.remote"
	} ],

	/*
	 * The boot components
	 */
	"composition" : [
	// The remote shell port should be overridden
	{
		"factory" : "ipopo-remote-shell-factory",
		"name" : "pelix-remote-shell",
		"properties" : {
			"pelix.shell.port" : 0
		}
	} ],
	
	/*
	 * The isolate properties
	 */
	"properties" : {
		"psem2m.compatible" : false
	}
}
```


It is imported by the configuration file named ``boot-java.js``, which has the following content:
```javascript
/**
 * Boot configuration for Java isolates
 */
{
	/* Common Python stack */
	"import-files" : "boot-common.js",

	/*
	 * Bundles
	 */
	"bundles" : [ {
		"name" : "cohorte.java.java"
	} ],

	/*
	 * Components
	 */
	"composition" : [
	/* Configuration of common components */
	{
		"name" : "pelix-remote-shell",
		"properties" : {
			// Random shell port
			"pelix.shell.port" : 8000
		}
	},

	/* JVM runner */
	{
		"factory" : "cohorte-java-runner-factory",
		"name" : "cohorte-java-runner"
	} ]
}
```

The resulting string, given by the ``reader`` to the JSON parser will be:
```javascript
{
	"bundles" : [ {
		"name" : "pelix.shell.core"
	}, {
		"name" : "pelix.shell.ipopo"
	}, {
		"name" : "pelix.shell.remote"
	}, {
		"name" : "cohorte.java.java"
	} ],
	"composition" : [
	{
	    "factory" : "ipopo-remote-shell-factory",
		"name" : "pelix-remote-shell",
		"properties" : {
			"pelix.shell.port" : 8000
		}
	},
	{
		"factory" : "cohorte-java-runner-factory",
		"name" : "cohorte-java-runner"
	} ],
	"properties" : {
		"psem2m.compatible" : false
	}
}
```

### In-place import

The in-place import allows to replace an object with the content of another JSON file.
The JSON object containing a ``from-file`` entry will be replaced by the content of the JSON given as value. It is possible to use an ``overriddenProperties`` entry to override the content of the ``properties`` entries found in the imported file.
In Cohorte Runtime configuration, the ``from-file`` entries have been replaced by ``import-files``.

Consider a file named ``some-file.js`` with the following content:
```javascript
{
   // Some comment
   "factory": "my-factory",
   "name": "my-instance",
   "properties": {
      "answer": 41
   }
}
```

Consider a file named ``main-file.js``, which includes ``some-file.js`` along with its own content:
```javascript
{
   "content": [
   {
       /* This is a sample, so don't mind the names... */
       "factory": "my-other-factory",
       "name": "my-other-instance",
       "properties": {
          "kind": "good"
       }
   },
   {
       "from-file": "some-file.js",
       "overriddenProperties": {
           "answer": 42
       }
   },
   {
       "factory": "my-other-factory",
       "name": "my-other-instance",
       "properties": {
          "kind": "bad"
       }
   }
   ]
}
```

The resulting string that the ``reader`` will give to the JSON parser will then be:
```javascript
{
   "content": [
   {
       /* This is a sample, so don't mind the names... */
       "factory": "my-other-factory",
       "name": "my-other-instance",
       "properties": {
          "kind": "good"
       }
   },
   {
       "factory": "my-factory",
       "name": "my-instance",
       "properties": {
           "answer": 42
       }
   },
   {
       "factory": "my-other-factory",
       "name": "my-other-instance",
       "properties": {
          "kind": "bad"
       }
   }
   ]
}
```
