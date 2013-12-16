#!/bin/bash -eu

script_dir=$(dirname $0)
libs="$script_dir"/lib
build="$script_dir"/build
classes="$build"/classes
jar="$build"/hunk-image-reader.jar

mkdir -p "$classes"

javac -classpath "$libs/*" \
    -d "$classes" \
    `find "$script_dir"/src -name "*.java"`

mkdir -p $build
(cd "$classes" && jar cf $jar com)
echo "Created jar: $jar"
