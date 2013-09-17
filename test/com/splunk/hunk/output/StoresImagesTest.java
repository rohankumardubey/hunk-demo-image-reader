package com.splunk.hunk.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StoresImagesTest {

	private static final String FILE_CONTENT = "one";
	private File dir;
	private File outSeq;
	private StoresImages storesImages;
	private Path seqFilePath;
	private LocalFileSystem fs;

	@Before
	public void setup() throws IOException {
		fs = FileSystem.getLocal(new Configuration());

		dir = createTempDir();
		outSeq = createTempFile(dir, "out.seq");

		seqFilePath = new Path(outSeq.getAbsolutePath());
		storesImages = new StoresImages(fs, new Path(dir.getAbsolutePath()),
				seqFilePath);
	}

	@After
	public void teardown() {
		FileUtils.deleteQuietly(dir);
	}

	@Test
	public void createSequenceFile__returnExistingSequenceFile()
			throws IOException {
		Path created = storesImages.createSequenceFile();
		assertEquals(created, seqFilePath);
		assertTrue(fs.exists(seqFilePath));
	}

	@Test
	public void createSequenceFile_filesToWrite_canReadSequenceFilesValues()
			throws IOException, InstantiationException, IllegalAccessException {
		createTempFileWithContent(dir, "one");
		createTempFileWithContent(dir, "two");
		storesImages.createSequenceFile();
		verifyContentWasWrittenToSequenceFile();
	}

	private void verifyContentWasWrittenToSequenceFile() throws IOException,
			InstantiationException, IllegalAccessException {
		Reader reader = new SequenceFile.Reader(fs, seqFilePath, fs.getConf());
		try {
			BytesWritable value = storesImages.valueClass.newInstance();
			while (reader.next(storesImages.keyClass.newInstance(), value)
					&& value.getLength() != 0)
				assertEquals(FILE_CONTENT, getStringValue(value));
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	private String getStringValue(BytesWritable value) throws IOException {
		return new String(IOUtils.toByteArray(new BoundedInputStream(
				new ByteArrayInputStream(value.getBytes()), value.getLength())));
	}

	@Test
	public void createSequenceFile_dirsInDirs_recursivelyAddsFiles()
			throws IOException, InstantiationException, IllegalAccessException {
		File dir2 = new File(dir, "dir2");
		dir2.mkdirs();
		createTempFileWithContent(dir2, "one");
		storesImages.createSequenceFile();
		verifyContentWasWrittenToSequenceFile();
	}

	private File createTempDir() throws IOException {
		File dir = File.createTempFile("foo", "bar");
		dir.delete();
		dir.mkdirs();
		return dir;
	}

	private File createTempFileWithContent(File dir, String name)
			throws IOException {
		File f = createTempFile(dir, name);
		FileUtils.write(f, FILE_CONTENT);
		return f;
	}

	private File createTempFile(File dir, String name) throws IOException {
		File f = new File(dir, name);
		f.createNewFile();
		f.deleteOnExit();
		return f;
	}
}
