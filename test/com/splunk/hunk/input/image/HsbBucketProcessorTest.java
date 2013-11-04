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
package com.splunk.hunk.input.image;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.Arrays;

import org.junit.Test;

public class HsbBucketProcessorTest {

	@Test
	public void getBucketNumber_bucketSize1value0_0() {
		assertEquals(0,
				HsbBucketProcessor.getBucketNumber(0, 1, Float.MAX_VALUE));
	}

	@Test
	public void getBucketNumber_bucketSize1value1_1() {
		assertEquals(1,
				HsbBucketProcessor.getBucketNumber(1, 1, Float.MAX_VALUE));
	}

	@Test
	public void getBucketNumber_bucketSize25value49_1() {
		assertEquals(1,
				HsbBucketProcessor.getBucketNumber(49, 25, Float.MAX_VALUE));
	}

	@Test
	public void getBucketNumber_bucketSize25value50_2() {
		assertEquals(2,
				HsbBucketProcessor.getBucketNumber(50, 25, Float.MAX_VALUE));
	}

	@Test
	public void getBucketNumber_realHValues_lastBucket() {
		assertEquals(HsbBucketProcessor.h_buckets - 1,
				HsbBucketProcessor.getBucketNumber(
						HsbBucketProcessor.h_maxValue,
						HsbBucketProcessor.h_bucketSize,
						HsbBucketProcessor.h_maxValue));
	}

	@Test
	public void getBucketNumber_realSValues_lastBucket() {
		assertEquals(HsbBucketProcessor.s_buckets - 1,
				HsbBucketProcessor.getBucketNumber(
						HsbBucketProcessor.s_maxValue,
						HsbBucketProcessor.s_bucketSize,
						HsbBucketProcessor.s_maxValue));
	}

	@Test
	public void getBucketNumber_realBValues_lastBucket() {
		assertEquals(HsbBucketProcessor.b_buckets - 1,
				HsbBucketProcessor.getBucketNumber(
						HsbBucketProcessor.b_maxValue,
						HsbBucketProcessor.b_bucketSize,
						HsbBucketProcessor.b_maxValue));
	}

	@Test
	public void getBucketNumber_realBValuesMinus0_1_lastBucket() {
		assertEquals(HsbBucketProcessor.b_buckets - 1,
				HsbBucketProcessor.getBucketNumber(
						HsbBucketProcessor.b_maxValue - 0.1f,
						HsbBucketProcessor.b_bucketSize,
						HsbBucketProcessor.b_maxValue));
	}

	@Test
	public void getBucketNumber_size0_5value0_4_0() {
		assertEquals(0,
				HsbBucketProcessor.getBucketNumber(0.4f, 0.5f, Float.MAX_VALUE));
	}

	@Test
	public void getBucketNumber_size0_5value0_6_1() {
		assertEquals(1,
				HsbBucketProcessor.getBucketNumber(0.6f, 0.5f, Float.MAX_VALUE));
	}

	@Test
	public void getBucketNumber_size0_2value0_4_2() {
		assertEquals(2,
				HsbBucketProcessor.getBucketNumber(0.4f, 0.2f, Float.MAX_VALUE));
	}

	@Test
	public void getBucketNumber_size0_2value0_39_1() {
		assertEquals(1, HsbBucketProcessor.getBucketNumber(0.39f, 0.2f,
				Float.MAX_VALUE));
	}

	@Test
	public void getBucketNumber_size1value2max2_1() {
		assertEquals(1, HsbBucketProcessor.getBucketNumber(2, 1, 2));
	}

	@Test
	public void hsbColors() {
		System.out.println(Arrays.toString(Color.RGBtoHSB(255, 0, 0, null)));
	}
}
