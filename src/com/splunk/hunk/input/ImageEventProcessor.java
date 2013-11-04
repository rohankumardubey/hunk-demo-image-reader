package com.splunk.hunk.input;

import java.awt.image.BufferedImage;
import java.util.Map;

public interface ImageEventProcessor {

	/**
	 * @return key values with data from the image.
	 */
	Map<String, Object> createEventFromImage(BufferedImage image);
}
