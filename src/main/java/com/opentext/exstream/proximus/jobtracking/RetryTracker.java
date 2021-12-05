package com.opentext.exstream.proximus.jobtracking;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.opentext.exstream.proximus.actions.SgwCommunicationsActions;
import com.opentext.exstream.proximus.actions.SgwDocumentsActions;
import com.opentext.exstream.proximus.actions.SgwTrackersActions;
import com.opentext.exstream.proximus.common.CustomLogger;
import com.opentext.exstream.proximus.server.ExstreamException;
import com.opentext.exstream.proximus.struct.Notification;
import com.opentext.exstream.proximus.struct.exstream.SgwDocument;
import com.opentext.exstream.proximus.struct.exstream.SgwTracker;
import com.opentext.exstream.proximus.struct.exstream.WhereCondition;

public class RetryTracker extends CommandBase {

	private static CustomLogger logger = new CustomLogger(RetryTracker.class);

	private static final String WORKFLOW_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	public void processTrackers() throws ExstreamException, IOException {
		List<String>retryTrackers = jobTrackerList.getRetryList();
		List<String>processedTrackers = jobTrackerList.getProcessedList();
		processTrackers(retryTrackers, processedTrackers);
	}
	
	public void processTrackers(List<String>retryTrackers, List<String>processedTrackers) throws ExstreamException, IOException {

		for (String trackerId : retryTrackers){
			logger.info("processing tracker: {}", trackerId);
			if (processedTrackers.contains(trackerId)){
				logger.info("tracker already processed: {}", trackerId);
				continue;
			}
			
			logger.debug("retrieve tracker: {}", trackerId);
			SgwTracker tracker = SgwTrackersActions.getTrackers(exstreamSession, trackerId);
			if (tracker == null){
				logger.warn("Unknown tracker id - ignoring: {}", trackerId);
				continue;
			}
			String trackerState = tracker.getField("processingstate");
			String trackerMessage = tracker.getField("statusmessage");

			if (trackerState.equals(Constants.STATE_INPROGRESS)){
				logger.info("tracker still in-progress: {}", trackerId);
				continue;
			}

			if (trackerState.equals(Constants.STATE_SUCCESS)){
				logger.info("tracker finished successfully: {}", trackerId);
				submitWorkFlowItem(trackerId,Constants.STATE_SUCCESS,trackerMessage);
				jobTrackerList.addToProcessedList(trackerId);
				continue;
			}

			if (trackerState.equals(Constants.STATE_FAILURE)){
				logger.info("tracker finished with errors: {}", trackerId);
				int rc = triggerRetryRequest(trackerId);
				if (rc == 0)
					submitWorkFlowItem(trackerId,Constants.STATE_FAILURE,trackerMessage);
				else
					submitWorkFlowItem(trackerId,Constants.STATE_RETRY,trackerMessage);

				jobTrackerList.addToProcessedList(trackerId);
				continue;
			}
		}
		logger.debug("Processed all trackers");

		jobTrackerList.deleteJobRetryList();
		jobTrackerList.deleteJobProcessedList();
		
	}

	private void submitWorkFlowItem(String trackerId, String state, String message) {
		logger.debug("submitting workflow item, trackerId: {}, state: {}", trackerId, state);

		SimpleDateFormat sdf = new SimpleDateFormat(WORKFLOW_TIMESTAMP);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String datetime = sdf.format(new Date());

		File inputFile = new File(jobTrackerConfig.getWorkflowFolderReady(), trackerId + ".xml");
		if (!inputFile.exists()){
			logger.warn("workflow item not found, trackerId: {}", trackerId);
			return;
		}
		
		String outputfolder;
		String workflowstep;
		String workflowstepNum = "05";
		if (state==Constants.STATE_SUCCESS || state==Constants.STATE_FAILURE){
			outputfolder = jobTrackerConfig.getWorkflowFolderSubmit();
			workflowstep = (state==Constants.STATE_SUCCESS ? "WORKFLOW_COMPLETED" : "WORKFLOW_ERROR");
		} else {
			outputfolder = jobTrackerConfig.getWorkflowFolderDisgard();
			workflowstep = "WORKFLOW_RETRY";
		}
		try{
			File o = new File(outputfolder);
			o.mkdirs();
		} catch (Exception e) {
			logger.error("Exception creating output folder: {}; {} ", outputfolder, ExceptionUtils.getRootCauseMessage(e));
			logger.error("workflow item not submitted, trackerId: {}", trackerId);
			return;
		}

		String filedateformat = "yyyyMMdd_HHmmss";
		SimpleDateFormat sdf2 = new SimpleDateFormat(filedateformat);
		String filedatetime = sdf2.format(new Date());

		try {
			logger.debug("preparing to read input notification");
			Notification notification = Notification.unmarshal(inputFile);

			//updating notification
			notification.setEventTime(datetime);
			notification.setStepTime(datetime);
			notification.setStep(workflowstep);
			notification.setQualifier(formatQualifier(message));
			notification.setWorkflowStarted("No");
			notification.setChannelSelected("No");

			logger.debug("preparing to write output notification");
			String filename = "notificationxml_input_file_" + workflowstepNum + "_" + workflowstep + "_" + notification.getUUID() + "_" + filedatetime + ".xml";
			File outputFile = new File(outputfolder, filename);
			notification.marshal(outputFile);

			inputFile.delete();
			
		} catch (Exception e) {
			logger.error("Exception submitting notification: {}; {} ", outputfolder, ExceptionUtils.getRootCauseMessage(e));
			logger.error("workflow item not submitted, trackerId: {}", trackerId);
		}
	}

	private String formatQualifier(String message) {
		String msg = message.replaceAll("\r", "").replace("\n", " ");
		return msg;
	}

	/**
	 * Creates and triggers the retry request for failed job
	 * 
	 * 1) retrieve documents that need retry
	 *      {{SGW_URL}}/v1/documents?where_typeid=outputqueueobject&where_filter=[["EQ","originaltrackerid","{{trackerid}}"],["EQ","processingstate","6"]]&guid_format=false
	 * 2) build retry driver file:
	 *      &lt;failures&gt;&lt;customers&gt;{{comma list of customerinrun}}&lt;/customers&gt;&lt;inputdocument&gt;{{docinputid}}&lt;/inputdocument&gt;&lt;/failures&gt;
	 * 3) (optional) look up retry service status and version - not required, can use version=* instead
	 *      {{SGW_URL}}/v1/services?where_servicename={{SGW_ServiceName}}&fields=id,servicename,serviceversion,state,servicetype
	 * 4) send request to retry service {{retryqueue}}
	 *      {{SGW_URL}}/v1/communications?name={{retryqueue}}&version={{serviceversion}}&async=true
	 *      {{SGW_URL}}/v1/communications?name={{retryqueue}}&version=*&async=true
	 * 
	 * @param trackerId
	 * @return returns the number of items sent for retry
	 */
	private int triggerRetryRequest(String trackerId) {
		logger.debug("building retry request for tracker: {}", trackerId);
		try {
			List<WhereCondition> params = new ArrayList<WhereCondition>();
			params.add(new WhereCondition("EQ", "originaltrackerid", trackerId));
			params.add(new WhereCondition("EQ", "processingstate", Constants.STATE_FAILURE));
			List<SgwDocument> docList = SgwDocumentsActions.getDocuments(exstreamSession, params);

			ArrayList<Integer> custList = new ArrayList<Integer>();
			String docinputid = null;
			String retryqueue = null;
			String custnumber = null;
			for (SgwDocument doc : docList){
				logger.debug("found document: {}, input id: {}, retry service: {}, customer: {}", 
						doc.getField("processingattemptid"), doc.getField("docinputid"), 
						doc.getField("retryqueue"), doc.getField("customerinrun"));
				
				retryqueue = doc.getField("retryqueue");
				docinputid = doc.getField("docinputid");
				custnumber = doc.getField("customerinrun");
				
				//TODO Add additional logic to check the storedvariables parameter before adding to list
				//     Need to work out if this is real requirement or not
				//     potentially document that is not mandatory could be sent to queue where ExternalJobComplete=False
				//XXX - additional logic for separate retryqueue/docinputid values. I don't think this is possible for Exstream applications
				if (retryqueue != null && !retryqueue.equals("")){
					custList.add(Integer.parseInt(custnumber));
				}
				
			}
			if (custList.size() == 0){
				logger.info("No documents added to customer list, no retry job: {}", trackerId);
				return 0;
			}
			
			Collections.sort(custList);
			String customers = custList.stream().map(String::valueOf).collect(Collectors.joining(","));
			String requestXml = String.format("<failures><customers>%s</customers><inputdocument>%s</inputdocument></failures>", customers, docinputid);

			logger.debug("Sending retry request: {} - {}", retryqueue, requestXml);
			String id = SgwCommunicationsActions.post(exstreamSession, retryqueue, "*", "true", requestXml.getBytes());
			logger.info("Submitted retry job: {} -> {} ", trackerId, id);

			return custList.size();
			
		} catch (ExstreamException e) {
			logger.error("Exception processing retry request: {}", e.getMessage(), e);
			return 0;
		}
	}

	protected void execute() throws Exception {
		long startTime = System.currentTimeMillis();
		processTrackers();
		long endTime = System.currentTimeMillis();
		logger.info("Assured delivery completed in {} secs", String.format("%.1f", (endTime - startTime) / 1000.0f));
	}

	@Override
	protected void initialize() throws Exception {
		super.initialize();
	}
	
	public static void process(String[] args) throws Exception {
		RetryTracker retry = new RetryTracker();
		retry.initialize();
		retry.execute();
		retry.close();
	}

	public static void main(String[] args) {
		try {
			RetryTracker.process(args);
		} catch (Exception e) {
			logger.error("Unable to complete assured delivery", e);
			System.exit(-1);
		}
	}

}
