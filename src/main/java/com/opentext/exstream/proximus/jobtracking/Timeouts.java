package com.opentext.exstream.proximus.jobtracking;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.opentext.exstream.proximus.actions.SgwDocumentsActions;
import com.opentext.exstream.proximus.common.CustomLogger;
import com.opentext.exstream.proximus.struct.exstream.SgwDocument;
import com.opentext.exstream.proximus.struct.exstream.WhereCondition;

public class Timeouts extends CommandBase {

	private static CustomLogger logger = new CustomLogger(Timeouts.class);

	// Simple date format patterns
	private static final String SDF_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss";
	private static final String SDF_GET_DOCUMENTS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	// Synchronization settings
	private Date syncTimeout;

	private void updateDocument(SgwDocument doc, String state, String message) throws Exception {
		String attemptId = doc.getField("processingattemptid");
		int updated = SgwDocumentsActions.updateDocuments(exstreamSession, attemptId, state, message);
		if (updated == 1) {
			if (Constants.STATE_FAILURE.equals(state) || Constants.STATE_SUCCESS.equals(state)) {
				//need to retry irrespective of success or failure, as other documents could have failed tracker status
				String trackerId = doc.getField("trackerid");
				jobTrackerList.addToRetryList(trackerId);
			}
		} else {
			throw new IOException("Unexpected number of updated documents: " + updated);
		}
	}

	private void timeoutConnectorDocuments(Date lastUpdateTime, String connector) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(SDF_GET_DOCUMENTS);

		List<WhereCondition> params = new ArrayList<WhereCondition>();
		params.add(new WhereCondition("LT", "lastupdatetime", sdf.format(lastUpdateTime)));
		params.add(new WhereCondition("EQ", "processingstate", Constants.STATE_HOLDING));
		if (connector != null) {
			params.add(new WhereCondition("EQ", "connectorname", connector));
			logger.info("Processing documents for connector [{}]", connector);
		}

		String state = JobTrackerConfiguration.TIMEOUT_SUCCESS.equals(jobTrackerConfig.getTimeoutState()) ? Constants.STATE_SUCCESS : Constants.STATE_FAILURE;

		int pageNumber = 0;
		int pageLimit = 50;
		int counter = -1;
		boolean loop = true;

		while (loop) {
			pageNumber++;

			// Iterate until result is empty as get documents is paginated
			// Pagination is required to avoid issues with SGW
			List<SgwDocument> docs = SgwDocumentsActions.getDocuments(exstreamSession, params, counter, pageLimit);
			logger.info("Page: {} - Timed-out documents: {}", pageNumber, docs.size());

			// Although it is possible to update many documents with a single request
			// Not sure if results of get documents and update documents are consistent
			// It seems safer to update documents one by one based on previous list
			for (SgwDocument doc : docs) {
				updateDocument(doc, state, jobTrackerConfig.getTimeoutMessage());
			}
			
			counter += docs.size();
			if (docs.size() < pageLimit) {
				loop = false;
			}

		}

		logger.info("Total processed documents: {}", counter + 1);
	}

	private void timeoutDocuments(Date lastUpdateTime) throws Exception {
		if (jobTrackerConfig.getConnectorName() == null || jobTrackerConfig.getConnectorName().size() == 0) {
			timeoutConnectorDocuments(lastUpdateTime, null);
		} else {
			for (String connector : jobTrackerConfig.getConnectorName()) {
				timeoutConnectorDocuments(lastUpdateTime, connector);
			}
		}
	}

	protected void execute() throws Exception {
		logger.info("Timeout documents older than '{}'", syncTimeout);

		long startTime = System.currentTimeMillis();
		timeoutDocuments(syncTimeout);
		long endTime = System.currentTimeMillis();
		logger.info("Timeout documents completed in {} secs", String.format("%.1f", (endTime - startTime) / 1000.0f));
	}

	@Override
	protected void initializeWithoutLock() throws Exception {
		super.initializeWithoutLock();
	}

	protected void commandline(String[] args) throws Exception {
		if (args.length == 2) {
			if ("-timeout".equals(args[0])) {
				SimpleDateFormat sdf = new SimpleDateFormat(SDF_TIMESTAMP);
				try {
					syncTimeout = sdf.parse(args[1]);
				} catch (ParseException e) {
					System.out.println("Unable to parse timestamp: " + args[1]);
				}
			}
			if ("-minutes".equals(args[0])) {
				try {
					int minutes = Integer.parseInt(args[1]);
					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.MINUTE, -minutes);
					syncTimeout = calendar.getTime();
				} catch (NumberFormatException e) {
					System.out.println("Unable to parse integer: " + args[1]);
				}
			}
		}

		if (syncTimeout == null) {
			helpMessage();
			throw new Exception("syncTimeout not set");
		}
	}

	private void helpMessage() {
		System.out.println("Please specify the required arguments.");
		System.out.println(" -timeout <timestamp> : Documents updated before timestamp will timeout, timestamp as '" + SDF_TIMESTAMP + "'");
		System.out.println(" -minutes <integer>   : Documents updated more than X minutes will timeout");
	}
	
	public static void process(String[] args) throws Exception{
		Timeouts timeouts = new Timeouts();
		timeouts.commandline(args);
		timeouts.initialize();
		timeouts.initializeWithoutLock();
		timeouts.execute();
		timeouts.close();
	}

	public static void main(String[] args) {
		try{
			Timeouts.process(args);
		} catch (Exception e) {
			logger.error("Unable to complete timeouts", e);
			System.exit(-1);
		}
	}

}
