package com.splunk.hunk.input.entry;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class TarEntryReader implements EntryReader {

	public InputStream getHdfsFileStream(String fsUri, String path,
			String filename) throws IOException {
		System.out.println("looking for filename: " + filename);
		return getFileInTar(filename, getInputStreamToPath(fsUri, path));
	}

	private static TarArchiveInputStream getInputStreamToPath(String fsUri,
			String path) throws IOException {
		FileSystem fileSystem = FileSystem.get(URI.create(fsUri),
				new Configuration());
		FSDataInputStream open = fileSystem.open(new Path(path));
		TarArchiveInputStream tarIn = new TarArchiveInputStream(
				new GzipCompressorInputStream(open));
		return tarIn;
	}

	private static InputStream getFileInTar(String filename,
			TarArchiveInputStream tarIn) throws IOException {
		TarArchiveEntry entry;
		while ((entry = tarIn.getNextTarEntry()) != null)
			if (isFile(entry) && isFileWereLookingFor(entry, filename))
				return new BoundedInputStream(tarIn, entry.getSize());
			else
				tarIn.skip(entry.getSize());
		throw new RuntimeException("Could not find filename: " + filename);
	}

	private static boolean isFile(TarArchiveEntry entry) {
		return entry.isFile() && !entry.isSymbolicLink();
	}

	private static boolean isFileWereLookingFor(TarArchiveEntry entry,
			String filename) {
		String entryName = new java.io.File(entry.getName()).getName();
		System.out.println("Entry: " + entryName);
		return entryName.equals(filename);
	}
}
