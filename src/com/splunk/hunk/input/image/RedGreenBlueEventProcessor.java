package com.splunk.hunk.input.image;

import java.awt.image.BufferedImage;

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
	public String createEventFromImage(BufferedImage image) {
		return createEventString(RGBUtils.getPixelRgbs(image));
	}

	private String createEventString(long[] rgbs) {
		long totalBytes = 0;
		for (int i = 0; i < rgbs.length; i++) {
			totalBytes += rgbs[i];
		}
		StringBuffer eventString = new StringBuffer();
		for (int i = 0; i < rgbs.length; i++) {
			eventString.append(" " + labels[i] + "="
					+ Utils.divideLongs(totalBytes, rgbs[i]));
		}
		return eventString.toString();
	}
}
