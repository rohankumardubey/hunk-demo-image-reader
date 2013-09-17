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

## Hunk configuration
1. Create a jar file with:
  ./create-jar.sh <path-to-hunk-project>
  where <path-to-hunk-project> could be /Users/petterik/perforce/si-staging/hadoop

2. In indexes.conf:
  vix.splunk.jars = <path-to-depo>/hunk-image-reader.jar
  vix.splunk.search.recordreader = com.splunk.hunk.input.ImageRecordReader,com.splunk.hunk.input.SequenceImageRecordReader
  vix.splunk.search.recordreader.image.regex = \.tgz$
  vix.splunk.search.recordreader.seq-image-reader.regex = data$
