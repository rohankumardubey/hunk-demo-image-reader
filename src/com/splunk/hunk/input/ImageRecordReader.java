package com.splunk.hunk.input;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.splunk.hunk.input.image.ImageEventProcessor;
import com.splunk.hunk.input.image.RedGreenBlueEventProcessor;
import com.splunk.mr.input.BaseSplunkRecordReader;
import com.splunk.mr.input.VixInputSplit;

public class ImageRecordReader extends BaseSplunkRecordReader {

	private static Logger logger = Logger
			.getLogger(BaseSplunkRecordReader.class);

	private final LinkedList<Map<String, String>> eventQueue = new LinkedList<Map<String, String>>();
	private Text key = new Text();
	private Text value = new Text();
	private TarArchiveInputStream tarIn;
	private ImageEventProcessor imagePreProcessor;

	private long totalBytesToRead;

	@Override
	public String getName() {
		return "image";
	}

	@Override
	public Pattern getFilePattern() {
		return Pattern.compile("\\.tgz$");
	}

	@Override
	public void vixInitialize(VixInputSplit split, TaskAttemptContext context)
			throws IOException, InterruptedException {
		FileSystem fs = FileSystem.get(context.getConfiguration());
		tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(
				fs.open(split.getPath())));
		totalBytesToRead = split.getLength() - split.getStart();
		imagePreProcessor = new RedGreenBlueEventProcessor();
	}

	@Override
	public Text getCurrentKey() throws IOException, InterruptedException {
		return key;
	}

	@Override
	public Text getCurrentValue() throws IOException, InterruptedException {
		return value;
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		while (eventQueue.isEmpty() && thereAreBytesToRead())
			tryPopulatingQueue(tarIn.getNextTarEntry());

		if (!eventQueue.isEmpty()) {
			setNextValue(eventQueue.pop());
			return true;
		} else {
			return false;
		}
	}

	private boolean thereAreBytesToRead() {
		return tarIn.getBytesRead() < totalBytesToRead;
	}

	private void tryPopulatingQueue(TarArchiveEntry entry) throws IOException {
		if (entry != null && isFile(entry))
			putImageInQueue(entry);
		else
			tarIn.skip(entry.getSize());
	}

	private void putImageInQueue(TarArchiveEntry entry) throws IOException {
		BufferedImage image = readImage(entry);
		if (image != null)
			eventQueue.offer(createImageEvent(entry, image));
		else
			logger.debug("Could not read image: " + entry.getName());
	}

	private Map<String, String> createImageEvent(TarArchiveEntry entry,
			BufferedImage image) {
		Map<String, String> imageData = imagePreProcessor
				.createEventFromImage(image);
		imageData.put("image", entry.getName());
		return imageData;
	}

	private boolean isFile(TarArchiveEntry entry) {
		return entry.isFile() && !entry.isLink();
	}

	private BufferedImage readImage(TarArchiveEntry entry) throws IOException {
		return ImageIO.read(new BoundedInputStream(tarIn, entry.getSize()));
	}

	private void setNextValue(Map<String, String> event) {
		value.set(eventAsJson(event));
	}

	private String eventAsJson(Map<String, String> event) {
		try {
			return new ObjectMapper().writeValueAsString(event);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return new Double(Utils.divideLongs(tarIn.getBytesRead(),
				totalBytesToRead)).floatValue();
	}

	@Override
	public void close() throws IOException {
		try {
			tarIn.close();
		} catch (Exception ignore) {
		}
		super.close();
	}
}
