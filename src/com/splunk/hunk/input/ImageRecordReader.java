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
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.splunk.hunk.input.image.ColorBucket2dProcessor;
import com.splunk.mr.input.BaseSplunkRecordReader;
import com.splunk.mr.input.VixInputSplit;

public class ImageRecordReader extends BaseSplunkRecordReader {

	public interface ImageEventProcessor {

		/**
		 * @return key values with data from the image
		 */
		Map<String, Object> createEventFromImage(BufferedImage image);
	}

	private static final Logger logger = Logger
			.getLogger(ImageRecordReader.class);

	private final LinkedList<Map<String, Object>> eventQueue = new LinkedList<Map<String, Object>>();
	private Text key = new Text();
	private Text value = new Text();
	private TarArchiveInputStream tarIn;
	private ImageEventProcessor imagePreProcessor;

	private long totalBytesToRead;

	// -- Interesting stuff start here

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
		imagePreProcessor = new ColorBucket2dProcessor();
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

	private boolean isFile(TarArchiveEntry entry) {
		return entry.isFile() && !entry.isLink();
	}

	private void putImageInQueue(TarArchiveEntry entry) throws IOException {
		BufferedImage image = readImage(entry);
		if (image != null)
			eventQueue.offer(createImageEvent(entry, image));
		else
			logger.warn("Could not read image: " + entry.getName());
	}

	private BufferedImage readImage(TarArchiveEntry entry) throws IOException {
		return ImageIO.read(new BoundedInputStream(tarIn, entry.getSize()));
	}

	private Map<String, Object> createImageEvent(TarArchiveEntry entry,
			BufferedImage image) {
		Map<String, Object> imageData = imagePreProcessor
				.createEventFromImage(image);
		imageData.put("image", entry.getName());
		return imageData;
	}

	private void setNextValue(Map<String, Object> event) throws IOException {
		value.set(eventAsJson(event));
	}

	private String eventAsJson(Map<String, Object> event) throws IOException {
		return new ObjectMapper().writeValueAsString(event);
	}

	// -- The end of the interesting stuff

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return new Double(Utils.getPercentage(tarIn.getBytesRead(),
				totalBytesToRead)).floatValue();
	}

	@Override
	public void close() throws IOException {
		IOUtils.closeQuietly(tarIn);
		super.close();
	}

	@Override
	public String getName() {
		return "image";
	}

	@Override
	public Text getCurrentKey() throws IOException, InterruptedException {
		return key;
	}
}
