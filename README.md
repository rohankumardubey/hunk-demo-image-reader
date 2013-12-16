## Description
This is a proof of concept image record reader for Hunk.

## Algorithm
Given images -> Return buckets of pixels, where the buckets contain pixels of the same color.
The buckets are normalized so that the sum of all the buckets values = 100.

for each image:
  for each pixel in image:
    put pixel in bucket

## Images formats:
The images can either be stored in a .tgz (tar gzip) or a hadoop MapFile.

## Readers:
Reader.class -> format
com.splunk.hunk.input.ImageRecordReader.class -> .tgz
com.splunk.hunk.input.SequenceImageRecordReader.class -> Hadoop MapFile

The Hadoop mapfile can be created with com.splunk.hunk.output.StoresImages.class.

## Build me!

This project needs the Hunk jars to be built or developed on. Currently Hunk jars come with Splunk packages built for 64-bit *nix.
To build me, you can either set environment variable $SPLUNK_HOME to a Splunk installation/extraction and use the build script, or do everything yourself.

### Automated
1. Set environment variable $SPLUNK_HOME to a path where Splunk is installed or extracted.
2. Run `ant` in the project directory.
3. Dependencies have been fetched and you should have a build in the build/ directory.

### IKEA
1. Get all the jars defined in .classpath or ivy.xml and put them in the lib/ directory.
2. Extract a Splunk with Hunk jars and copy the jars from $SPLUNK_HOME/bin/jars to lib/
3. Run the ./create-jar.sh or manually enter the commands that's in the script.
4. You should now have a .jar somewhere

## Hunk configuration
1. Build the project first, with the descrptions above.

2. Configure your virtual index either through the UI or indexes.conf with the following key values:
  vix.splunk.jars = <path-to-repo>/build/hunk-image-reader.jar
  vix.splunk.search.recordreader = com.splunk.hunk.input.ImageRecordReader,com.splunk.hunk.input.SequenceImageRecordReader
  vix.splunk.search.recordreader.image.regex = \.tgz$
  vix.splunk.search.recordreader.seq-image-reader.regex = data$

