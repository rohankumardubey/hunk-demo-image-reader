package com.splunk.hunk.input.entry;

import java.io.IOException;
import java.io.InputStream;

public interface EntryReader {

	public InputStream getHdfsFileStream(String fsUri, String path, String key)
			throws IOException;
}
