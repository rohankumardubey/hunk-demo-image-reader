package com.splunk.hunk.input;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.log4j.Logger;

import com.splunk.mr.input.BaseSplunkRecordReader;
import com.splunk.mr.input.VixInputSplit;

public class ImageRecordReader extends BaseSplunkRecordReader {

	private static Logger logger = Logger
			.getLogger(BaseSplunkRecordReader.class);

	private Text key = new Text();
	private Text value = new Text();
	private Path path;
	private TarArchiveInputStream tarIn;

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
		path = split.getPath();
		FileSystem fs = FileSystem.get(context.getConfiguration());
		tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(
				fs.open(path)));
		logger.debug("All setup with path: " + path);
		totalBytesToRead = split.getLength() - split.getStart();
	}

	@Override
	public Text getCurrentKey() throws IOException, InterruptedException {
		return key;
	}

	@Override
	public Text getCurrentValue() throws IOException, InterruptedException {
		logger.info("Getting value: " + value.toString());
		return value;
	}

	private final LinkedList<KV> queue = new LinkedList<KV>();
	private TarArchiveEntry nextEntry;

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		while (queue.isEmpty() && thereAreBytesToRead())
			tryPopulatingQueue();

		if (!queue.isEmpty()) {
			setNextKeyValue();
			return true;
		} else {
			return false;
		}
	}

	private boolean thereAreBytesToRead() {
		return tarIn.getBytesRead() < totalBytesToRead;
	}

	private void tryPopulatingQueue() throws IOException {
		nextEntry = tarIn.getNextTarEntry();
		if (nextEntry != null && nextEntry.isFile() && !nextEntry.isLink()) {
			BufferedImage image = readTarEntry();
			if (image != null) {
				long[] rgbs = getPixelRgbs(image);
				queueEvent(rgbs);
			}
		} else {
			IOUtils.toByteArray(tarIn, nextEntry.getSize());
		}
	}

	private void setNextKeyValue() {
		KV kv = queue.pop();
		logger.info("Setting kv: " + kv.toString());
		key.set(kv.k);
		value.set(kv.v);

	}

	private BufferedImage readTarEntry() throws IOException {
		InputStream imageIn = new BoundedInputStream(tarIn, nextEntry.getSize());
		return ImageIO.read(imageIn);
	}

	private static int RED_IDX = 0;
	private static int GREEN_IDX = 1;
	private static int BLUE_IDX = 2;
	private final static String[] labels;
	static {
		labels = new String[3];
		labels[RED_IDX] = "red";
		labels[BLUE_IDX] = "blue";
		labels[GREEN_IDX] = "green";
	}

	private long[] getPixelRgbs(BufferedImage image) {
		long[] rgbs = new long[] { 0, 0, 0 };
		for (int x = 0; x < image.getWidth(); x++)
			for (int y = 0; y < image.getHeight(); y++)
				processPixel(rgbs, image, x, y);
		return rgbs;
	}

	private void processPixel(long[] rgbs, BufferedImage image, int x, int y) {
		Color color = new Color(image.getRGB(x, y));
		rgbs[RED_IDX] += color.getRed();
		rgbs[GREEN_IDX] += color.getGreen();
		rgbs[BLUE_IDX] += color.getBlue();
	}

	private void queueEvent(long[] rgbs) {
		String eventString = "image=" + nextEntry.getName();
		long totalBytes = 0;
		for (int i = 0; i < rgbs.length; i++) {
			totalBytes += rgbs[i];
		}
		for (int i = 0; i < rgbs.length; i++) {
			eventString += " " + labels[i] + "="
					+ divideLongs(totalBytes, rgbs[i]);
		}
		logger.info("queuing event: " + eventString);
		queue.offer(new KV(new Text(path.toString()), new Text(eventString)));
	}

	private double divideLongs(long totalBytes, long rgb) {
		if (rgb == 0 || totalBytes == 0) {
			return 0;
		} else {
			double divide = new Long(rgb).doubleValue()
					/ new Long(totalBytes).doubleValue();
			return divide * 100;
		}
	}

	@Override
	public void close() throws IOException {
		try {
			tarIn.close();
		} catch (Exception ignore) {
		}
		super.close();
	}

	private static class KV {
		final Text k;
		final Text v;

		public KV(Text k, Text v) {
			this.k = k;
			this.v = v;
		}

		@Override
		public String toString() {
			return "KV [k=" + k + ", v=" + v + "]";
		}
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return new Double(divideLongs(tarIn.getBytesRead(), totalBytesToRead))
				.floatValue();
	}
}
