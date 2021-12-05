package com.opentext.exstream.proximus.connector;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URLConnection;

import org.apache.commons.io.FileUtils;

import com.opentext.exstream.proximus.struct.MessageFileType;

public class MessageFileUtil {

	public static MessageFileType createMessageFile(String path, String category, byte[] content, String alias, String encoding, String language) throws Exception {
		return createMessageFile(path, category, content, alias, encoding, language, false);
	}

	public static MessageFileType createMessageFile(String path, String category, byte[] content, String alias, String encoding, String language, boolean forceUtf8) throws Exception {
		if (path == null || category == null) {
			throw new IllegalArgumentException("Mandatory arguments are null");
		}

		if (content == null) {
			content = FileUtils.readFileToByteArray(new File(path));
			if (forceUtf8) {
				content = new String(content, "UTF-16").getBytes("UTF-8");
			}
		}

		String fileFormat = null;

		// Guess file format from alias extension
		if (alias != null && alias.length() > 0) {
			fileFormat = getFormatFromName(alias);
		}

		// Guess file format from filename extension
		if (fileFormat == null) {
			fileFormat = getFormatFromName(path);
		}

		// Guess file format from content
		if (fileFormat == null) {
			InputStream is = new BufferedInputStream(new ByteArrayInputStream(content));
			fileFormat = URLConnection.guessContentTypeFromStream(is);
		}

		if (fileFormat == null) {
			throw new IllegalArgumentException("Unable to determine file format: " + path);
		}

		MessageFileType messageFile = new MessageFileType();
		messageFile.setFileName(path);
		messageFile.setFileCategory(category);
		messageFile.setFileFormat(fileFormat);
		messageFile.setFileEncoding(encoding);
		messageFile.setFileAlias(alias);
		messageFile.setFileLanguage(language);
		messageFile.setContent(content);
		return messageFile;
	}

	private static String getFormatFromName(String name) {
		String format = null;

		String filename = name.toLowerCase();
		if (filename.endsWith(".html")) {
			format = "text/html";
		} else if (filename.endsWith(".pdf")) {
			format = "application/pdf";
		} else if (filename.endsWith(".txt")) {
			format = "text/plain";
		} else if (filename.endsWith(".xml")) {
			format = "application/xml";
		} else if (filename.endsWith(".json")) {
			format = "application/json";
		}

		return format;
	}

}
