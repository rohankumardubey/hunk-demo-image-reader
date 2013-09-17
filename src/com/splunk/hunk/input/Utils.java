package com.splunk.hunk.input;

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
}
