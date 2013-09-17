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
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileRecordReader;
import org.apache.log4j.Logger;

import com.splunk.hunk.input.ImageRecordReader.ImageEventProcessor;
import com.splunk.hunk.input.image.HsbBucketProcessor;
import com.splunk.mr.input.BaseSplunkRecordReader;
import com.splunk.mr.input.VixInputSplit;

public class SequenceImageRecordReader extends BaseSplunkRecordReader {

	private static final Logger gLogger = Logger
			.getLogger(SequenceImageRecordReader.class);
	private SequenceFileRecordReader<Text, BytesWritable> recordReader;
	private ImageEventProcessor imageProcessor;

	@Override
	public void vixInitialize(VixInputSplit split, TaskAttemptContext context)
			throws IOException, InterruptedException {
		recordReader = new SequenceFileRecordReader<Text, BytesWritable>();
		gLogger.info("Init: " + split.getPath());
		recordReader.initialize(split, context);
		imageProcessor = new HsbBucketProcessor();
	}

	@Override
	public Text getCurrentValue() throws IOException, InterruptedException {
		BufferedImage image = getImageFromValue(recordReader.getCurrentValue());
		String keyAsString = getKeyAsString();
		gLogger.info("Key: " + keyAsString);
		Map<String, Object> event;
		if (image != null) {
			event = imageProcessor.createEventFromImage(image);
		} else {
			event = new HashMap<String, Object>();
			event.put("value", "image was null");
		}
		event.put("image", keyAsString);
		return new Text(Utils.eventAsJson(event));
	}

	private String getKeyAsString() throws IOException, InterruptedException {
		return recordReader.getCurrentKey().toString();
	}

	private BufferedImage getImageFromValue(BytesWritable bytes)
			throws IOException, InterruptedException {
		gLogger.info("bytes len: " + bytes.getLength());
		try {
			return ImageIO.read(inputStreamFromBytesWritable(bytes));
		} catch (Exception e) {
			gLogger.info("Got exception when reading image: " + e.getMessage());
			return null;
		}
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
		return Pattern.compile("\\.map/data$");
	}

	@Override
	public Text getCurrentKey() throws IOException, InterruptedException {
		throw new UnsupportedOperationException();
	}
}
