#!/bin/bash -eu

if [ $# == 0 ] && [ "$SPLUNK_HOME" == "" ]; then
    echo "Usage: Either pass \$SPLUNK_HOME as the first argument or have the \$SPLUNK_HOME\
environment variable set"
fi

if [ $# == 1 ]; then
    splunk_home="$1"
else
    splunk_home="$SPLUNK_HOME"
fi

script_dir=$(dirname $0)
libs_dir="$script_dir"/lib
jars_dir="$splunk_home"/bin/jars

echo "Copying hunk jars from splunk..."
cp -v "$jars_dir"/SplunkMR-*.jar "$libs_dir"
