#!/bin/bash -eu

script_dir=$(cd $(dirname $0) && pwd)
libs="$script_dir"/lib
classes="$script_dir"/classes
build="$script_dir"/build
jar="$build"/hunk-image-reader.jar

mkdir -p "$classes"

javac -classpath "$libs/*" \
    -d "$classes" \
    `find "$script_dir"/src -name "*.java"`

mkdir -p $build
(cd "$classes" && jar cf $jar com)
echo "Created jar: $jar"
