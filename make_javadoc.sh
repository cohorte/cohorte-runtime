#!/bin/sh

# iPOJO Annotations library
if [ -z $IPOJO_ANNOTATIONS ]
then
    IPOJO_ANNOTATIONS="platforms/org.apache.felix.ipojo.annotations-1.9.0-SNAPSHOT.jar"
fi

# Root projects containers (space separated folders)
if [ -z $ROOT_FOLDERS ]
then
    ROOT_FOLDERS="trunk/java/"
fi

# Class path
if [ -z $LIB_PATH ]
then
    LIB_PATH="$IPOJO_ANNOTATIONS:platforms/felix/*"
fi

# Javadoc output folder
if [ -z $OUTPUT_FOLDER ]
then
    OUTPUT_FOLDER="javadoc/"
fi

# Maxdepth 2 : _root_/_project_/src
# SRC_PATH=`find $ROOT_FOLDERS -maxdepth 2 -type d -name src | tr "\\n" ":"`
SRC_PATH=`find $ROOT_FOLDERS -type d -name src | tr "\\n" ":"`

# Remove previous run information
rm -fr "$OUTPUT_FOLDER"
mkdir -p "$OUTPUT_FOLDER"

# Do the job...
javadoc -d "$OUTPUT_FOLDER" \
    -sourcepath "$SRC_PATH" \
    -classpath "$LIB_PATH" \
    -subpackages org.psem2m \
    2>"$OUTPUT_FOLDER/log.txt"

