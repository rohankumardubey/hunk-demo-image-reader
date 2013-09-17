package com.splunk.hunk.input.image;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ColorBucket2dProcessorTest {

	@Test
	public void getBucketNumber_bucketSize1value0_0() {
		assertEquals(0, ColorBucket2dProcessor.getBucketNumber(0, 1));
	}

	@Test
	public void getBucketNumber_bucketSize1value1_1() {
		assertEquals(1, ColorBucket2dProcessor.getBucketNumber(1, 1));
	}

	@Test
	public void getBucketNumber_bucketSize25value49_1() {
		assertEquals(1, ColorBucket2dProcessor.getBucketNumber(49, 25));
	}

	@Test
	public void getBucketNumber_bucketSize25value50_2() {
		assertEquals(2, ColorBucket2dProcessor.getBucketNumber(50, 25));
	}

	@Test
	public void getBucketNumber_realValues_lastBucket() {
		assertEquals(ColorBucket2dProcessor.buckets - 1,
				ColorBucket2dProcessor.getBucketNumber(
						ColorBucket2dProcessor.bucketsMaxValue,
						ColorBucket2dProcessor.bucketSize));
	}
}
