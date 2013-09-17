package com.splunk.hunk.input;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

public class Utils {

	public static double getPercentage(long rgb, long totalBytes) {
		if (rgb == 0 || totalBytes == 0) {
			return 0;
		} else {
			double divide = new Long(rgb).doubleValue()
					/ new Long(totalBytes).doubleValue();
			return divide * 100;
		}
	}

	public static String eventAsJson(Map<String, Object> event)
			throws IOException {
		return new ObjectMapper().writeValueAsString(event);
	}
}
