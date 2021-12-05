package com.opentext.exstream.proximus.common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHelper {
	private static final Logger log = LoggerFactory.getLogger(FileHelper.class);

	/**
	 * Read a data file and returns the content as a string.
	 * @param dataFile The data file to read 
	 * @return The data file content as a string.
	 */
	public static String readFileAsString(String dataFile){
		return readFileAsString(dataFile,Charset.defaultCharset());
	}
	/**
	 * Read a data file and returns the content as a string.
	 * @param dataFile The data file to read
	 * @param codepage The codepage to interpret the data file with
	 * @return The data file content as a string.
	 */
	public static String readFileAsString(String dataFile, String codepage){
		return readFileAsString(dataFile,Charset.forName(codepage));
	}
	/**
	 * Read a data file and returns the content as a string.
	 * @param dataFile The data file to read 
	 * @param charset The charset to interpret the data file with
	 * @return The data file content as a string.
	 */
	public static String readFileAsString(String dataFile, Charset charset){
	    String dataStr = "";
    	try {
	        byte[] bytes = readFileAsBytes(dataFile);
	        dataStr = new String(bytes, charset);
	    } catch (Exception e) {
	    	log.error("Exception occurred while reading data file", e);
	    }
	    return dataStr;
    }

	/**
	 * Read a data file and returns the content as a byte array.
	 * @param dataFile The data file to read
	 * @return The data file content as a byte array
	 */
	public static byte[] readFileAsBytes(String dataFile){
		byte[] bytes = null;
		try {
	        FileInputStream is = new FileInputStream(dataFile);
	        bytes = new byte[is.available()];
	        is.read(bytes);
	        is.close();
	    } catch (Exception e) {
	    	log.error("Exception occurred while reading data file", e);
	    }
        return bytes;
    }

	/**
	 * Read a data file and returns the content as a byte array.
	 * @param dataFile The data file to write to
	 * @param bytes The data file content as a byte array
	 */
	public static void writeFileAsBytes(String dataFile, byte[] bytes){
		try {
	        FileOutputStream os = new FileOutputStream(dataFile);
	        os.write(bytes);
	        os.close();
	    } catch (Exception e) {
	        log.error("Exception occurred while writing data file", e);
	    }
    }

}
