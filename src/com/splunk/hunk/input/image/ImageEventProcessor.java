package com.splunk.hunk.input.image;

import java.awt.image.BufferedImage;

public interface ImageEventProcessor {

	String createEventFromImage(BufferedImage image);
}
