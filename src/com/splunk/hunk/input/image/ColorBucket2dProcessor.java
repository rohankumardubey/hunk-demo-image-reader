package com.splunk.hunk.input.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.splunk.hunk.input.ImageRecordReader.ImageEventProcessor;

public class ColorBucket2dProcessor implements ImageEventProcessor {

	public static final int bucketsMaxValue = 255; // rgb color max value
	public static final int buckets = 10;
	public static final int bucketSize = bucketsMaxValue / (buckets - 1);
	private static final int rgbColors = 3;

	private static final int red = 0;
	private static final int green = 1;
	private static final int blue = 2;

	@Override
	public Map<String, Object> createEventFromImage(BufferedImage image) {
		long[][] rgbBuckets = createBuckets(image);
		return toMap(rgbBuckets);
	}

	private long[][] createBuckets(BufferedImage image) {
		long[][] rgbBuckets = new long[rgbColors][buckets];
		for (int x = 0; x < image.getWidth(); x++)
			for (int y = 0; y < image.getHeight(); y++)
				putPixelInBucket(rgbBuckets, image, x, y);
		return rgbBuckets;
	}

	private void putPixelInBucket(long[][] rgbBuckets, BufferedImage image,
			int x, int y) {
		Color color = new Color(image.getRGB(x, y));
		putColorInBucket(rgbBuckets, red, color.getRed());
		putColorInBucket(rgbBuckets, green, color.getGreen());
		putColorInBucket(rgbBuckets, blue, color.getBlue());
	}

	private void putColorInBucket(long[][] rgbBuckets, int colorIndex,
			int colorValue) {
		rgbBuckets[colorIndex][getBucketNumber(colorValue, bucketSize)] += 1;
	}

	public static int getBucketNumber(int colorValue, int bucketSize) {
		return colorValue / bucketSize;
	}

	private Map<String, Object> toMap(long[][] rgbBuckets) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("red", rgbBuckets[red]);
		map.put("green", rgbBuckets[green]);
		map.put("blue", rgbBuckets[blue]);
		return map;
	}
}
