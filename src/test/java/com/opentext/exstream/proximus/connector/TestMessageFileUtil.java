package com.opentext.exstream.proximus.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.util.Base64Utils;

import com.opentext.exstream.proximus.struct.MessageFileType;

public class TestMessageFileUtil {

	private final File resourcesDirectory = new File("src/test/resources");
	private final byte[] content = "1234567890".getBytes();

	@Test
	public final void testNullArguments() throws Exception {
		try {
			MessageFileUtil.createMessageFile("path", null, null, null, null, null);
			fail("Exception not thrown");
		} catch (IllegalArgumentException e) {
		}

		try {
			MessageFileUtil.createMessageFile(null, "category", null, null, null, null);
			fail("Exception not thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public final void testStringArguments() throws Exception {
		MessageFileType messageFile = MessageFileUtil.createMessageFile("file.html", "category", content, "alias", "encoding", "language");

		assertEquals("file.html", messageFile.getFileName());
		assertEquals("category", messageFile.getFileCategory());
		assertEquals("alias", messageFile.getFileAlias());
		assertEquals("encoding", messageFile.getFileEncoding());
		assertEquals("language", messageFile.getFileLanguage());
	}

	@Test
	public final void testContent() throws Exception {
		// if there is no content, content is read from file
		try {
			MessageFileUtil.createMessageFile("file.html", "category", null, null, null, null);
			fail("Exception not thrown");
		} catch (FileNotFoundException e) {
		}

		MessageFileType messageFile;

		File file = new File(resourcesDirectory, "html-without-doctype");
		messageFile = MessageFileUtil.createMessageFile(file.getAbsolutePath(), "category", null, null, null, null);
		assertEquals(Base64Utils.encodeToString(FileUtils.readFileToByteArray(file)), messageFile.getContent());

		// if there is content, content is always used
		messageFile = MessageFileUtil.createMessageFile("file.html", "category", content, null, null, null);
		assertEquals(Base64Utils.encodeToString(content), messageFile.getContent());
	}

	@Test
	public final void testFileFormatFromAlias() throws Exception {
		String[] alias = {"file.html", "file.pdf", "file.txt", "file.xml", "file.json"};
		String[] formats = {"text/html", "application/pdf", "text/plain", "application/xml", "application/json"};

		for (int i = 0; i < alias.length; i++) {
			MessageFileType messageFile;

			messageFile = MessageFileUtil.createMessageFile("path", "category", content, alias[i], null, null);
			assertEquals(formats[i], messageFile.getFileFormat());
			messageFile = MessageFileUtil.createMessageFile("path", "category", content, alias[i].toUpperCase(), null, null);
			assertEquals(formats[i], messageFile.getFileFormat());
			messageFile = MessageFileUtil.createMessageFile("file.html", "category", content, alias[i], null, null);
			assertEquals(formats[i], messageFile.getFileFormat());
		}
	}

	@Test
	public final void testFileFormatFromPath() throws Exception {
		String[] paths = {"file.html", "file.pdf", "file.txt", "file.xml", "file.json"};
		String[] formats = {"text/html", "application/pdf", "text/plain", "application/xml", "application/json"};

		for (int i = 0; i < paths.length; i++) {
			MessageFileType messageFile;

			messageFile = MessageFileUtil.createMessageFile(paths[i], "category", content, "alias", null, null);
			assertEquals(formats[i], messageFile.getFileFormat());
			messageFile = MessageFileUtil.createMessageFile(paths[i], "category", content, null, null, null);
			assertEquals(formats[i], messageFile.getFileFormat());
			messageFile = MessageFileUtil.createMessageFile(paths[i].toUpperCase(), "category", content, null, null, null);
			assertEquals(formats[i], messageFile.getFileFormat());
		}
	}

	@Test
	public final void testFileFormatFromContent() throws Exception {
		String[] paths = {"html-with-doctype", "html-without-doctype", "xml"};
		String[] formats = {"text/html", "text/html", "application/xml"};

		for (int i = 0; i < paths.length; i++) {
			MessageFileType messageFile;

			File file = new File(resourcesDirectory, paths[i]);
			messageFile = MessageFileUtil.createMessageFile(file.getAbsolutePath(), "category", null, null, null, null);
			assertEquals(formats[i], messageFile.getFileFormat());
		}
	}

}
