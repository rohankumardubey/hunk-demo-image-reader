package com.splunk.hunk.input.entry;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.MapFile.Reader;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

public class MapEntryReader implements EntryReader {

	@Override
	public InputStream getHdfsFileStream(String fsUri, String path, String key)
			throws IOException {
		FileSystem fs = FileSystem.get(URI.create(fsUri), new Configuration());
		String mapDir = new File(path).getParent();
		Reader reader = new MapFile.Reader(fs, mapDir, fs.getConf());
		return getEntryStream(path, key, reader);
	}

	private InputStream getEntryStream(String path, String key, Reader reader)
			throws IOException {
		try {
			BytesWritable value = new BytesWritable();
			Writable writable = reader.get(new Text(key), value);
			return toInputStream(path, key, value, writable);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	private InputStream toInputStream(String path, String key,
			BytesWritable value, Writable writable) {
		if (writable == null)
			throw new RuntimeException("Could not find key: " + key
					+ ", at path: " + path);
		else
			return new BoundedInputStream(new ByteArrayInputStream(
					value.getBytes()), value.getLength());
	}

}
