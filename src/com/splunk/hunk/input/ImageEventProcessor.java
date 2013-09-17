package com.splunk.hunk.input;

import java.awt.image.BufferedImage;

public interface ImageEventProcessor {

	String createEventFromImage(BufferedImage image);
}
