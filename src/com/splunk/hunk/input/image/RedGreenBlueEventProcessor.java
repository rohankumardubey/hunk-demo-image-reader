package com.splunk.hunk.input.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.splunk.hunk.input.ImageRecordReader.ImageEventProcessor;
import com.splunk.hunk.input.Utils;

public class RedGreenBlueEventProcessor implements ImageEventProcessor {

	@Override
	public Map<String, String> createEventFromImage(BufferedImage image) {
		RGB imageRgbs = RGB.getImageRgbs(image);
		return createEventKeyValues(imageRgbs);
	}

	private Map<String, String> createEventKeyValues(RGB rgbs) {
		Map<String, String> kvs = new HashMap<String, String>();
		kvs.put("red", "" + rgbs.getRedPercentage());
		kvs.put("green", "" + rgbs.getGreenPercentage());
		kvs.put("blue", "" + rgbs.getBluePercentage());
		return kvs;
	}

	public static class RGB {

		private final long red;
		private final long green;
		private final long blue;

		public RGB(long red, long green, long blue) {
			this.red = red;
			this.green = green;
			this.blue = blue;
		}

		public double getRedPercentage() {
			return getColorPercentage(red);
		}

		public double getGreenPercentage() {
			return getColorPercentage(green);
		}

		public double getBluePercentage() {
			return getColorPercentage(blue);
		}

		private double getColorPercentage(long color) {
			return Utils.getPercentage(red + green + blue, color);
		}

		private static final int RED_IDX = 0;
		private static final int GREEN_IDX = 1;
		private static final int BLUE_IDX = 2;

		public static RGB getImageRgbs(BufferedImage image) {
			long[] rgbs = new long[] { 0, 0, 0 };
			for (int x = 0; x < image.getWidth(); x++)
				for (int y = 0; y < image.getHeight(); y++)
					processPixel(rgbs, image, x, y);
			return new RGB(rgbs[RED_IDX], rgbs[GREEN_IDX], rgbs[BLUE_IDX]);
		}

		private static void processPixel(long[] rgbs, BufferedImage image,
				int x, int y) {
			Color color = new Color(image.getRGB(x, y));
			rgbs[RED_IDX] += color.getRed();
			rgbs[GREEN_IDX] += color.getGreen();
			rgbs[BLUE_IDX] += color.getBlue();
		}
	}
}
