package com.splunk.hunk.output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.MapFile.Writer;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;

public class StoresImages {

	public final Class<Text> keyClass = Text.class;
	public final Class<BytesWritable> valueClass = BytesWritable.class;
	private Path inputDir;
	private Path outputSeqFile;
	private FileSystem inFs;
	private FileSystem outFs;

	public StoresImages(FileSystem inFs, Path inputDir, FileSystem outFs,
			Path outputSeqFile) {
		this.inFs = inFs;
		this.inputDir = inputDir;
		this.outFs = outFs;
		this.outputSeqFile = outputSeqFile;
	}

	public Path storeImages() throws IOException {
		Writer writer = null;
		try {
			writer = createWriter();
			writer.setIndexInterval(2);
			TreeMap<String, Path> paths = getSortedPaths(writer,
					inFs.listStatus(inputDir));
			for (Entry<String, Path> entry : paths.entrySet())
				appendFileContent(writer, entry.getValue());
		} finally {
			IOUtils.closeQuietly(writer);
		}
		return outputSeqFile;
	}

	private Writer createWriter() throws IOException {
		return new MapFile.Writer(outFs.getConf(), outFs, outputSeqFile.toUri()
				.getPath(), keyClass, valueClass, CompressionType.NONE);
	}

	private TreeMap<String, Path> getSortedPaths(Writer writer,
			FileStatus[] listStatus) throws IOException {
		TreeMap<String, Path> paths = new TreeMap<String, Path>();
		for (FileStatus f : listStatus)
			if (!f.isDir())
				paths.put(getKeyFromPath(f.getPath()), f.getPath());
			else
				paths.putAll(getSortedPaths(writer,
						inFs.listStatus(f.getPath())));
		return paths;
	}

	private void appendFileContent(Writer writer, Path path) throws IOException {
		ByteArrayOutputStream fileBytes = readFilesBytes(path);
		writer.append(new Text(getKeyFromPath(path)), new BytesWritable(
				fileBytes.toByteArray()));
	}

	public static String getKeyFromPath(Path p)
			throws UnsupportedEncodingException {
		return p.toString().toLowerCase().replaceAll(" ", "");
	}

	private ByteArrayOutputStream readFilesBytes(Path p) throws IOException {
		ByteArrayOutputStream fileBytes = null;
		FSDataInputStream open = null;
		try {
			fileBytes = new ByteArrayOutputStream();
			open = inFs.open(p);
			IOUtils.copyLarge(open, fileBytes);
		} finally {
			IOUtils.closeQuietly(open);
			IOUtils.closeQuietly(fileBytes);
		}
		return fileBytes;
	}

	public static StoresImages create(String dirFsUri, String dir,
			String outFsUri, String output) throws IOException {
		return new StoresImages(FileSystem.get(URI.create(dirFsUri),
				new Configuration()), new Path(dir), FileSystem.get(
				URI.create(outFsUri), new Configuration()), new Path(output));
	}
}
