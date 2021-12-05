package com.opentext.exstream.proximus.common;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.opentext.exstream.proximus.jobtracking.JobTrackerConfiguration;

public class TestLock {

	private String jobTrackerFolder;
	private String jobTrackerType;

	public TestLock() throws IOException{
		JobTrackerConfiguration jobTrackerConfig = new JobTrackerConfiguration(); 
		jobTrackerFolder = jobTrackerConfig.getJobTrackerFolder();
		jobTrackerType = jobTrackerConfig.fileLockType;
	}

	@Test
	public void test_lock() {
		boolean lock = FileLock.lock(jobTrackerFolder, jobTrackerType);
		Assert.assertTrue(lock);
		FileLock.release();
		FileLock.release();
	}

}
