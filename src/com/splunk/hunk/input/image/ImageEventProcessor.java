package com.splunk.hunk.input.image;

import java.awt.image.BufferedImage;
import java.util.Map;

public interface ImageEventProcessor {

	Map<String, String> createEventFromImage(BufferedImage image);
}
