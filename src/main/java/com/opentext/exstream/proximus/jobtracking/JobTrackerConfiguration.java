package com.opentext.exstream.proximus.jobtracking;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.opentext.exstream.proximus.common.PropertyReader;

public class JobTrackerConfiguration {

	// Possible values for timed-out documents
	public static final String TIMEOUT_SUCCESS = "SUCCESS";
	public static final String TIMEOUT_FAILURE = "FAILURE";

	private static final String JOB_TRACKER_CONFIG_FILE = "jobtracker.properties";

	private String clientName;
	private List<String> channelName;
	private int eventPageSize;

	private String tagName;
	private List<String> connectorName;
	private String timeoutState;
	private String timeoutMessage;

	private String otdsMtaUrl;
	private String otdsTenantUrl;
	private String sgwUrl;
	private String sgwUsername;
	private String sgwPassword;
	private String customFolder; 

	private String workflowFolderPending;
	private String workflowFolderReady;
	private String workflowFolderSubmit;
	private String workflowFolderDisgard;
	
	private String jobTrackerFolder = null;
	public String fileLockType = null;

	public JobTrackerConfiguration() throws IOException {
		Properties jobTrackerProperties = PropertyReader.loadPropertiesFileFromClasspath(JOB_TRACKER_CONFIG_FILE);
		String tmp;

		clientName = jobTrackerProperties.getProperty("gateway.client");
		tmp = jobTrackerProperties.getProperty("gateway.channel");
		if (tmp != null && tmp.length() > 0) {
			channelName = new ArrayList<String>();
			for (String entry : tmp.split(";")) {
				channelName.add(entry.trim());
			}
		}
		eventPageSize = Integer.parseInt(jobTrackerProperties.getProperty("gateway.eventpagesize"));

		tagName = jobTrackerProperties.getProperty("sync.tag.name");
		tmp = jobTrackerProperties.getProperty("sync.connector.name");
		if (tmp != null && tmp.length() > 0) {
			connectorName = new ArrayList<String>();
			for (String entry : tmp.split(";")) {
				connectorName.add(entry.trim());
			}
		}
		timeoutState = jobTrackerProperties.getProperty("sync.timeout.state");
		if (!TIMEOUT_SUCCESS.equals(timeoutState) && !TIMEOUT_FAILURE.equals(timeoutState)) {
			throw new IllegalArgumentException("Invalid timeout state: " + timeoutState);
		}
		timeoutMessage = jobTrackerProperties.getProperty("sync.timeout.message");

		otdsMtaUrl = jobTrackerProperties.getProperty("exstream.otds.mta.url");
		otdsTenantUrl = jobTrackerProperties.getProperty("exstream.otds.tenant.url");
		sgwUrl = jobTrackerProperties.getProperty("exstream.sgw.url");
		sgwUsername = jobTrackerProperties.getProperty("exstream.sgw.username");
		sgwPassword = jobTrackerProperties.getProperty("exstream.sgw.password");
		customFolder = jobTrackerProperties.getProperty("exstream.custom.folder");

		workflowFolderPending = jobTrackerProperties.getProperty("workflow.folder.pending");
		workflowFolderReady   = jobTrackerProperties.getProperty("workflow.folder.ready");
		workflowFolderSubmit  = jobTrackerProperties.getProperty("workflow.folder.submit");
		workflowFolderDisgard = jobTrackerProperties.getProperty("workflow.folder.disgard");

		setJobTrackerFolder(jobTrackerProperties.getProperty("job.tracker.folder"));

		fileLockType = jobTrackerProperties.getProperty("file.lock.type");
	}

	public String getClientName() {
		return clientName;
	}

	public List<String> getChannelName() {
		return channelName;
	}

	public int getEventPageSize() {
		return eventPageSize;
	}

	public String getTagName() {
		return tagName;
	}

	public List<String> getConnectorName() {
		return connectorName;
	}

	public String getTimeoutState() {
		return timeoutState;
	}

	public String getTimeoutMessage() {
		return timeoutMessage;
	}

	public String getOtdsMtaUrl() {
		return otdsMtaUrl;
	}

	public String getOtdsTenantUrl() {
		return otdsTenantUrl;
	}

	public String getSgwUrl() {
		return sgwUrl;
	}

	public String getSgwUsername() {
		return sgwUsername;
	}

	public String getSgwPassword() {
		return sgwPassword;
	}

	public String getCustomFolder() {
		return customFolder;
	}

	public String getWorkflowFolderPending() {
		return workflowFolderPending;
	}

	public void setWorkflowFolderPending(String workflowFolderPending) {
		this.workflowFolderPending = workflowFolderPending;
	}

	public String getWorkflowFolderReady() {
		return workflowFolderReady;
	}

	public void setWorkflowFolderReady(String workflowFolderReady) {
		this.workflowFolderReady = workflowFolderReady;
	}

	public String getWorkflowFolderSubmit() {
		return workflowFolderSubmit;
	}

	public void setWorkflowFolderSubmit(String workflowFolderSubmit) {
		this.workflowFolderSubmit = workflowFolderSubmit;
	}

	public String getWorkflowFolderDisgard() {
		return workflowFolderDisgard;
	}

	public void setWorkflowFolderDisgard(String workflowFolderDisgard) {
		this.workflowFolderDisgard = workflowFolderDisgard;
	}

	public void setJobTrackerFolder(String jobTrackerFolder) {
		this.jobTrackerFolder = jobTrackerFolder;
		if (jobTrackerFolder != null && !jobTrackerFolder.equals("")){
			File p = new File(jobTrackerFolder);
			p.mkdirs();
		}
	}
	public String getJobTrackerFolder() {
		return jobTrackerFolder;
	}

}
