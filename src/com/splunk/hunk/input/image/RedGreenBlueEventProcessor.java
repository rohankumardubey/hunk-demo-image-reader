package com.splunk.hunk.input.image;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.splunk.hunk.input.Utils;

public class RedGreenBlueEventProcessor implements ImageEventProcessor {

	private final static String[] labels;
	static {
		labels = new String[3];
		labels[RGBUtils.RED_IDX] = "red";
		labels[RGBUtils.GREEN_IDX] = "green";
		labels[RGBUtils.BLUE_IDX] = "blue";
	}

	@Override
	public Map<String, String> createEventFromImage(BufferedImage image) {
		return createEventKeyValues(RGBUtils.getPixelRgbs(image));
	}

	private Map<String, String> createEventKeyValues(long[] rgbs) {
		long totalBytes = 0;
		for (int i = 0; i < rgbs.length; i++) {
			totalBytes += rgbs[i];
		}

		Map<String, String> kvs = new HashMap<String, String>();
		for (int i = 0; i < rgbs.length; i++) {
			kvs.put(labels[i], "" + Utils.divideLongs(totalBytes, rgbs[i]));
		}
		return kvs;
	}
}
