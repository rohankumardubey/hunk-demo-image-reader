package com.splunk.hunk.input.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.splunk.hunk.input.ImageRecordReader.ImageEventProcessor;
import com.splunk.hunk.input.Utils;

public class HsbBucketProcessor implements ImageEventProcessor {

	// Max values
	public static final float h_maxValue = 1.0f;
	public static final float s_maxValue = 1.0f;
	public static final float b_maxValue = 1.0f;

	// Bucket sizes
	public static final int h_buckets = 10;
	public static final float h_bucketSize = (float) (h_maxValue / h_buckets);
	public static final int s_buckets = 3;
	public static final float s_bucketSize = (float) (s_maxValue / s_buckets);
	public static final int b_buckets = 2;
	public static final float b_bucketSize = (float) (b_maxValue / b_buckets);

	@Override
	public Map<String, Object> createEventFromImage(BufferedImage image) {
		long[][][] hsbBuckets = createBuckets(image);
		double[][][] pctBuckets = normalizedBuckets(hsbBuckets, image);
		return toMap(pctBuckets);
	}

	private long[][][] createBuckets(BufferedImage image) {
		long[][][] rgbBuckets = new long[b_buckets][s_buckets][h_buckets];
		for (int x = 0; x < image.getWidth(); x++)
			for (int y = 0; y < image.getHeight(); y++)
				putPixelInBucket(rgbBuckets, image, x, y);
		return rgbBuckets;
	}

	private void putPixelInBucket(long[][][] hsbBuckets, BufferedImage image,
			int x, int y) {
		Color c = new Color(image.getRGB(x, y));
		float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(),
				null);

		int hBucket = getBucketNumber(hsb[0], h_bucketSize, h_maxValue);
		int sBucket = getBucketNumber(hsb[1], s_bucketSize, s_maxValue);
		int bBucket = getBucketNumber(hsb[2], b_bucketSize, b_maxValue);
		hsbBuckets[bBucket][sBucket][hBucket] += 1;
	}

	public static int getBucketNumber(float colorValue, float bucketSize,
			float maxValue) {
		if (colorValue >= maxValue)
			colorValue = maxValue - 0.1f;
		return (int) Math.floor(colorValue / bucketSize);
	}

	private double[][][] normalizedBuckets(long[][][] buckets,
			BufferedImage image) {
		int totalPixels = image.getWidth() * image.getHeight();
		double[][][] pctBuckets = new double[b_buckets][s_buckets][h_buckets];
		for (int i = 0; i < buckets.length; i++)
			for (int j = 0; j < buckets[i].length; j++)
				for (int k = 0; k < buckets[i][j].length; k++)
					pctBuckets[i][j][k] = Utils.getPercentage(buckets[i][j][k],
							totalPixels);
		return pctBuckets;
	}

	private Map<String, Object> toMap(double[][][] pctBuckets) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("colors", toJsonObject(pctBuckets));
		return map;
	}

	private Object toJsonObject(double[][][] pctBuckets) {
		Map<Integer, Object> bMap = new HashMap<Integer, Object>();
		for (int i = 0; i < pctBuckets.length; i++)
			bMap.put(i, toJsonObject(pctBuckets[i]));
		return bMap;
	}

	private Object toJsonObject(double[][] shBuckets) {
		Map<Integer, Object> sMap = new HashMap<Integer, Object>();
		for (int i = 0; i < shBuckets.length; i++)
			sMap.put(i, toJsonObject(shBuckets[i]));
		return sMap;
	}

	private Object toJsonObject(double[] hBuckets) {
		Map<Integer, Object> hMap = new HashMap<Integer, Object>();
		for (int i = 0; i < hBuckets.length; i++)
			hMap.put(i, hBuckets[i]);
		return hMap;
	}
}
