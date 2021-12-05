package com.opentext.exstream.proximus.jobtracking;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.opentext.exstream.proximus.common.CustomLogger;


/**
 * Unit test for simple App.
 */
public class TestJobTrackerList
{	
	private static CustomLogger logger = new CustomLogger(TestJobTrackerList.class);
	private JobTrackerConfiguration jobTrackerConfig; 
	private JobTrackerList jobTrackerList;

	public TestJobTrackerList() throws IOException{
		jobTrackerConfig = new JobTrackerConfiguration(); 
		jobTrackerList = new JobTrackerList(jobTrackerConfig.getJobTrackerFolder());
	}
	
	@Test
	public void test_retryList_empty_list()
	{
		logger.info("test empty retry list");
		try {
			//start by deleting list
			jobTrackerList.deleteJobRetryList();
			
			List<String> retryList = jobTrackerList.getRetryList();
			
			Assert.assertEquals(0,retryList.size());

			//end by deleting list
			jobTrackerList.deleteJobRetryList();

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void test_retryList_populate_list()
	{
		logger.info("test populate retry list");
		
		try {
			//start by deleting list
			jobTrackerList.deleteJobRetryList();

			jobTrackerList.addToRetryList("1111-processed");
			jobTrackerList.addToRetryList("2222-inprogress");
			jobTrackerList.addToRetryList("3333-error");
			jobTrackerList.addToRetryList("4444-success");
			
			List<String> retryList = jobTrackerList.getRetryList();
			
			Assert.assertEquals(4,retryList.size());

			//end by deleting list
			jobTrackerList.deleteJobRetryList();

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void test_retryList_load_list()
	{
		logger.info("test load retry list");

		try {
			//start by deleting list
			jobTrackerList.deleteJobRetryList();

			jobTrackerList.addToRetryList("1111-processed");
			jobTrackerList.addToRetryList("2222-inprogress");
			jobTrackerList.addToRetryList("3333-error");
			jobTrackerList.addToRetryList("4444-success");
			
			jobTrackerList.reset();
			
			List<String> retryList = jobTrackerList.getRetryList();
			
			Assert.assertEquals(4,retryList.size());

			//end by deleting list
			jobTrackerList.deleteJobRetryList();

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}


	@Test
	public void test_processedList_empty_list()
	{
		logger.info("test empty processed list");
		
		try {
			//start by deleting list
			jobTrackerList.deleteJobProcessedList();
			
			List<String> processedList = jobTrackerList.getProcessedList();
			
			Assert.assertEquals(0,processedList.size());

			//end by deleting list
			jobTrackerList.deleteJobProcessedList();

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void test_processedList_populate_list()
	{
		logger.info("test populate processed list");
		
		try {
			//start by deleting list
			jobTrackerList.deleteJobProcessedList();

			jobTrackerList.addToProcessedList("1111-processed");
			
			List<String> processedList = jobTrackerList.getProcessedList();
			
			Assert.assertEquals(1,processedList.size());

			//end by deleting list
			jobTrackerList.deleteJobProcessedList();

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void test_processedList_load_list()
	{
		logger.info("test load processed list");
		
		try {
			//start by deleting list
			jobTrackerList.deleteJobProcessedList();

			jobTrackerList.addToProcessedList("1111-processed");

			jobTrackerList.reset();

			List<String> processedList = jobTrackerList.getProcessedList();
			
			Assert.assertEquals(1,processedList.size());

			//end by deleting list
			jobTrackerList.deleteJobProcessedList();

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
