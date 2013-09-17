package com.splunk.hunk.input.image;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class RGBUtils {

	public static int RED_IDX = 0;
	public static int GREEN_IDX = 1;
	public static int BLUE_IDX = 2;

	public static long[] getPixelRgbs(BufferedImage image) {
		long[] rgbs = new long[] { 0, 0, 0 };
		for (int x = 0; x < image.getWidth(); x++)
			for (int y = 0; y < image.getHeight(); y++)
				processPixel(rgbs, image, x, y);
		return rgbs;
	}

	private static void processPixel(long[] rgbs, BufferedImage image, int x,
			int y) {
		Color color = new Color(image.getRGB(x, y));
		rgbs[RED_IDX] += color.getRed();
		rgbs[GREEN_IDX] += color.getGreen();
		rgbs[BLUE_IDX] += color.getBlue();
	}
}
