// Copyright (C) 2013 Splunk Inc.
//
// Splunk Inc. licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.splunk.hunk.input;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.input.BoundedInputStream;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileRecordReader;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.splunk.hunk.input.image.HsbBucketProcessor;
import com.splunk.mr.input.BaseSplunkRecordReader;
import com.splunk.mr.input.VixInputSplit;

/**
 * Preprocesses images stored in a {@link SequenceFile}. The real processing of
 * the image is all done by the @{link
 * {@link SequenceImageRecordReader#imageProcessor} </br> This is an example of
 * using an existing {@link RecordReader} to retrieve records that I can
 * preprocess before returning them to the underlying framework.
 */
public class SequenceImageRecordReader extends BaseSplunkRecordReader {

	private static final String EXCEPTION = "exception";
	private static final Logger gLogger = Logger
			.getLogger(SequenceImageRecordReader.class);
	// The RecordReader I didn't have to write
	private SequenceFileRecordReader<Text, BytesWritable> recordReader;
	private ImageEventProcessor imageProcessor;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void vixInitialize(VixInputSplit split, TaskAttemptContext context)
			throws IOException, InterruptedException {
		recordReader = new SequenceFileRecordReader<Text, BytesWritable>();
		recordReader.initialize(split.getSplit(), context);
		imageProcessor = new HsbBucketProcessor();
	}

	@Override
	public Text getCurrentValue() throws IOException, InterruptedException {
		return new Text(objectMapper.writeValueAsString(getNextEvent()));
	}

	public void serializeCurrentValueTo(java.io.OutputStream out)
			throws IOException, InterruptedException {
		Map<String, Object> event = getNextEvent();
		objectMapper.writeValue(out, event);
	}

	private Map<String, Object> getNextEvent() throws IOException,
			InterruptedException {
		BytesWritable imageBytes = recordReader.getCurrentValue();

		Map<String, Object> event = createMap();
		BufferedImage image = getImageFromValue(imageBytes);
		if (image == null)
			event.put(EXCEPTION, "Java libraries could not read image");
		else
			processImage(image, event);
		return event;
	}

	@SuppressWarnings("serial")
	private Map<String, Object> createMap() throws IOException,
			InterruptedException {
		return new HashMap<String, Object>() {
			{
				put("image", getKeyAsString());
			}
		};
	}

	private BufferedImage getImageFromValue(BytesWritable bytes)
			throws IOException, InterruptedException {
		try {
			return ImageIO.read(inputStreamFromBytesWritable(bytes));
		} catch (Exception e) {
			gLogger.info("Got exception when reading image: " + e.getMessage());
			return null;
		}
	}

	private void processImage(BufferedImage image, Map<String, Object> event) {
		try {
			event.putAll(imageProcessor.createEventFromImage(image));
		} catch (Exception e) {
			event.put(EXCEPTION, "Processing image raised: "
					+ e.getClass().getSimpleName());
		}
	}

	private String getKeyAsString() throws IOException, InterruptedException {
		return recordReader.getCurrentKey().toString();
	}

	private BoundedInputStream inputStreamFromBytesWritable(BytesWritable bytes) {
		return new BoundedInputStream(
				new ByteArrayInputStream(bytes.getBytes()), bytes.getLength());
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		return recordReader.nextKeyValue();
	}

	@Override
	public String getName() {
		return "seq-image-reader";
	}

	@Override
	public Pattern getFilePattern() {
		// the sequence file created by a MapFile is named data
		return Pattern.compile("data$");
	}

	@Override
	public Text getCurrentKey() throws IOException, InterruptedException {
		throw new UnsupportedOperationException(
				"This should never be called by Hunk");
	}

	@Override
	public String getOutputDataFormat() {
		// Let's Splunk know in advance that we're outputting json.
		return "json";
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		// Get this for free when reusing this record reader!
		return recordReader.getProgress();
	}

}
