package com.splunk.hunk.input.image;

import java.awt.image.BufferedImage;
import java.util.Map;

import com.splunk.hunk.input.ImageRecordReader.ImageEventProcessor;

public class FaceDetectionProcessor implements ImageEventProcessor {

	@Override
	public Map<String, Object> createEventFromImage(BufferedImage image) {
		throw new UnsupportedOperationException();
	}

}
