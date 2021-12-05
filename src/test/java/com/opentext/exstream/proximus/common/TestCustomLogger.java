package com.opentext.exstream.proximus.common;

import org.junit.Assert;
import org.junit.Test;

public class TestCustomLogger {

	@Test
	public void test_logger() {
		//can't really test the STRS loggers in JUnit, so this is really just basic test 
		CustomLogger logger = new CustomLogger(TestCustomLogger.class);
		logger.trace("test trace message");
		logger.debug("test debug message");
		logger.info ("test info message");
		logger.warn ("test warn message");
		logger.error("test error message");
		logger.severe("test severe (error) message");
		Assert.assertTrue(true);
	}

}
