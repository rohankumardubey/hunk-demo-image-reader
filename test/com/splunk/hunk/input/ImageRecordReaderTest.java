package com.splunk.hunk.input;

import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

public class ImageRecordReaderTest {

	private ImageRecordReader imageRecordReader;

	@Before
	public void setup() {
		imageRecordReader = new ImageRecordReader();
	}

	private void assertMatchesFile(String file) {
		Pattern pattern = imageRecordReader.getFilePattern();
		assertTrue(pattern.matcher(file).find());
	}

	@Test
	public void getFilePattern_jpg_matches() {
		assertMatchesFile("image.jpg");
	}

	@Test
	public void getFilePattern_jpeg_matches() {
		assertMatchesFile("image.jpeg");
	}

	@Test
	public void getFilePattern_JPG_matches() {
		assertMatchesFile("image.JPG");
	}

	@Test
	public void getFilePattern_JPEG_matches() {
		assertMatchesFile("image.JPEG");
	}

	@Test
	public void getFilePattern_png_matches() {
		assertMatchesFile("image.png");
	}

	@Test
	public void getFilePattern_PNG_matches() {
		assertMatchesFile("image.PNG");
	}
}
