#!/bin/bash -eux

script_dir=$(dirname $0)
hunk_dir=$1
hunk_libs="$hunk_dir"/libs
classes="$script_dir"/classes

mkdir -p "$classes"

(cd $hunk_dir && ant build-1.0)

javac -classpath "$hunk_libs/common/*":"$hunk_libs/1.0/*":"$hunk_dir/classes/1.0" \
    -d "$classes" \
    `find "$script_dir"/src -name "*.java"`

(cd "$classes" && jar cf ../hunk-image-reader.jar com)
