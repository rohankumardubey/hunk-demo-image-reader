#!/bin/bash -eu

script_dir=$(dirname $0)
libs="$script_dir"/lib
classes="$script_dir"/classes

mkdir -p "$classes"

javac -classpath "$libs/*" \
    -d "$classes" \
    `find "$script_dir"/src -name "*.java"`

(cd "$classes" && jar cf ../hunk-image-reader.jar com)
