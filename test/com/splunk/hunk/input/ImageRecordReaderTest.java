// Copyright (C) 2013 Splunk Inc.
//
// Splunk Inc. licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
