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
package com.splunk.hunk.input.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.HashMap;
import java.util.Map;

import com.splunk.hunk.input.ImageEventProcessor;
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
		float[][][] pctBuckets = normalizedBuckets(hsbBuckets, image);
		return toMap(pctBuckets);
	}

	private long[][][] createBuckets(BufferedImage image) {
		final long[][][] rgbBuckets = new long[b_buckets][s_buckets][h_buckets];
		convertTo2DWithoutUsingGetRGB(image, new PixelListener() {
			@Override
			public void gotPixel(int pixel) {
				putPixelInBucket(rgbBuckets, pixel);
			}
		});
		return rgbBuckets;
	}

	private void putPixelInBucket(long[][][] hsbBuckets, int pixel) {
		Color c = new Color(pixel);
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

	private float[][][] normalizedBuckets(long[][][] buckets,
			BufferedImage image) {
		int totalPixels = image.getWidth() * image.getHeight();
		float[][][] pctBuckets = new float[b_buckets][s_buckets][h_buckets];
		for (int i = 0; i < buckets.length; i++) {
			for (int j = 0; j < buckets[i].length; j++) {
				for (int k = 0; k < buckets[i][j].length; k++) {
					float f = (float) Utils.getPercentage(buckets[i][j][k],
							totalPixels);
					pctBuckets[i][j][k] = f > 0.0001f ? f : 0.0f;
				}
			}
		}
		return pctBuckets;
	}

	private Map<String, Object> toMap(float[][][] pctBuckets) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("colors", pctBuckets);
		return map;
	}

	private interface PixelListener {
		void gotPixel(int pixel);
	}

	// Experimental
	// http://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image

	private static void convertTo2DWithoutUsingGetRGB(BufferedImage image,
			PixelListener listener) {
		DataBuffer dataBuffer = image.getRaster().getDataBuffer();
		if (dataBuffer instanceof DataBufferByte) {
			final byte[] pixels = ((DataBufferByte) dataBuffer).getData();
			final boolean hasAlphaChannel = image.getAlphaRaster() != null;

			if (hasAlphaChannel) {
				final int pixelLength = 4;
				for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
					int argb = 0;
					argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
					argb += ((int) pixels[pixel + 1] & 0xff); // blue
					argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
					argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
					listener.gotPixel(argb);
				}
			} else {
				final int pixelLength = 3;
				for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
					int argb = 0;
					argb += -16777216; // 255 alpha
					argb += ((int) pixels[pixel] & 0xff); // blue
					argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
					argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
					listener.gotPixel(argb);
				}
			}
		} else if (dataBuffer instanceof DataBufferInt) {
			int[] pixels = ((DataBufferInt) dataBuffer).getData();
			for (int i = 0; i < pixels.length; i++)
				listener.gotPixel(pixels[i]);
		}
	}
}
