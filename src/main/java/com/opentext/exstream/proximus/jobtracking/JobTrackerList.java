package com.opentext.exstream.proximus.jobtracking;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.opentext.exstream.proximus.common.CustomLogger;

public class JobTrackerList {
	private static CustomLogger logger = new CustomLogger(JobTrackerList.class);
	
	private static final String FILE_ENCODING = "ISO-8859-1";
	
	// Text file with list of jobs with failed documents
	private static final String JOB_RETRY_LIST_FILE = "job-retry-list.txt";
	private static final String JOB_PROCESSED_LIST_FILE = "job-processed-list.txt";

	private String jobTrackerFolder = null;
	private List<String> retryList = null;
	private List<String> processedList = null;
	
	private File retryFile = new File(jobTrackerFolder, JOB_RETRY_LIST_FILE);
	private File processedFile = new File(jobTrackerFolder, JOB_PROCESSED_LIST_FILE);
	
	public JobTrackerList(String jobTrackerFolder) {
		this.jobTrackerFolder = jobTrackerFolder;
		retryFile = new File(jobTrackerFolder, JOB_RETRY_LIST_FILE);
		processedFile = new File(jobTrackerFolder, JOB_PROCESSED_LIST_FILE);
	}

	public void addToRetryList(String trackerId) throws IOException {
		if (retryList == null){
			logger.info("Loading job retry list");
			retryList = loadList(retryFile);
			logger.info("{} trackers", retryList.size());
		}

		if (!retryList.contains(trackerId)) {
			addToList(retryFile, retryList, trackerId);
			logger.info("Added tracker to job retry list: {}", trackerId);
		}
	}
	
	public void addToProcessedList(String trackerId) throws IOException {
		if (processedList == null){
			logger.info("Loading job processed list");
			processedList = loadList(processedFile);
			logger.info("{} trackers", processedList.size());
		}

		if (!processedList.contains(trackerId)) {
			addToList(processedFile, processedList, trackerId);
			logger.info("Added tracker to job processed list: {}", trackerId);
		}
	}
	
	public List<String> getRetryList() throws IOException{
		if (retryList == null){
			logger.info("Loading job retry list");
			retryList = loadList(retryFile);
			logger.info("{} trackers", retryList.size());
		}
		return retryList;
	}
	
	public void setRetryList(List<String> retryList) throws IOException{
		logger.info("Saving job retry list");
		this.retryList = retryList;
		saveList(retryFile, retryList);
	}
	
	public List<String> getProcessedList() throws IOException{
		if (processedList == null){
			logger.info("Loading job processed list");
			processedList = loadList(processedFile);
			logger.info("{} trackers", processedList.size());
		}
		
		return processedList;
	}
	
	public void setProcessedList(List<String> processedList) throws IOException{
		logger.info("Saving job processed list");
		this.processedList = processedList;
		saveList(processedFile, processedList);
	}
	
	public synchronized void deleteJobRetryList() throws IOException {
		retryList = null;
		if (retryFile.isFile()) retryFile.delete();
	}
	
	public synchronized void deleteJobProcessedList() throws IOException {
		processedList = null;
		if (processedFile.isFile()) processedFile.delete();
	}
	
	public synchronized void reset(){
		retryList = null;
		processedList = null;
	}
	
	private synchronized void saveList(File file, List<String> list) throws IOException {
		if (list!= null && list.size()==0){
			FileUtils.writeLines(file, FILE_ENCODING, list, false);
		}else{
			if (file.isFile())
				file.delete();
		}
	}
	
	private synchronized List<String> loadList(File file) throws IOException {
		if (!file.isFile()) {
			file.createNewFile();
		}
		List<String> list = FileUtils.readLines(file, FILE_ENCODING);
		return list;
	}
	
	private synchronized void addToList(File file, List<String> list, String trackerId) throws IOException {
		FileUtils.writeLines(file, FILE_ENCODING, Arrays.asList(trackerId), true);
		list.add(trackerId);
	}
	
}
