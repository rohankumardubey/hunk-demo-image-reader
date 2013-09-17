package com.splunk.hunk.input;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class RedGreenBlueEventProcessor implements ImageEventProcessor {

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

	@Override
	public String createEventFromImage(BufferedImage image) {
		return createEventString(getPixelRgbs(image));
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

	private String createEventString(long[] rgbs) {
		long totalBytes = 0;
		for (int i = 0; i < rgbs.length; i++) {
			totalBytes += rgbs[i];
		}
		StringBuffer eventString = new StringBuffer();
		for (int i = 0; i < rgbs.length; i++) {
			eventString.append(" " + labels[i] + "="
					+ Util.divideLongs(totalBytes, rgbs[i]));
		}
		return eventString.toString();
	}
}
