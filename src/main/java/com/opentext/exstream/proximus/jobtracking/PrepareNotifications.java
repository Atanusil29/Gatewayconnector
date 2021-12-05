package com.opentext.exstream.proximus.jobtracking;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.opentext.exstream.proximus.actions.SgwDocumentsActions;
import com.opentext.exstream.proximus.common.CustomLogger;
import com.opentext.exstream.proximus.struct.exstream.SgwDocument;
import com.opentext.exstream.proximus.struct.exstream.WhereCondition;

public class PrepareNotifications extends CommandBase {

	private static CustomLogger logger = new CustomLogger(PrepareNotifications.class);

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
			logger.error("Unexpected number of updated documents, id: {}, size: {} ", attemptId, updated);
		}
	}

	private void prepareDocument(String attemptId) throws Exception {
		// Check if document is still in holding state
		List<WhereCondition> params = new ArrayList<WhereCondition>();
		params.add(new WhereCondition("EQ", "processingattemptid", attemptId));
		params.add(new WhereCondition("EQ", "processingstate", Constants.STATE_HOLDING));
		List<SgwDocument> docs = SgwDocumentsActions.getDocuments(exstreamSession, params);

		// If document is in holding state, update the state
		if (docs.size() == 0) {
			logger.info("Unable to find document, probably state is not holding");
		} else if (docs.size() == 1) {
			//always update notification as successful
			updateDocument(docs.get(0), Constants.STATE_SUCCESS, "success");
		} else {
			logger.error("Unexpected number of received documents, id: {}, size: {} ", attemptId, docs.size());
		}
	}

	private void prepareNotifications() throws Exception {
		int docCount = 0;
		
		File pendingFolder = new File(jobTrackerConfig.getWorkflowFolderPending());
		File readyFolder = new File(jobTrackerConfig.getWorkflowFolderPending());
		
		//make sure folders exist
		pendingFolder.mkdirs();
		readyFolder.mkdirs();
		
		//get list of files in pending folder
		File [] notifications = pendingFolder.listFiles(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
		        return name.endsWith(".xml");
		    }
		});

		//process all the files found
		for (File notification : notifications) {
			String attemptId = FilenameUtils.getBaseName(notification.getName());
			logger.debug("preparing document id: {}", attemptId);
			prepareDocument(attemptId);
			FileUtils.moveFileToDirectory(notification, readyFolder, true);
			++docCount;
		}
		logger.info("Total notifications prepared: {}", docCount);
	}

	protected void execute() throws Exception {
		logger.info("Prepare notifications");
		long startTime = System.currentTimeMillis();
		prepareNotifications();
		long endTime = System.currentTimeMillis();
		logger.info("Prepare notifications completed in {} secs", String.format("%.1f", (endTime - startTime) / 1000.0f));
	}


	public static void main(String[] args) {
		PrepareNotifications prepNotifications = new PrepareNotifications();
		try {
			prepNotifications.initialize();
			prepNotifications.execute();
			prepNotifications.close();
			
		} catch (Exception e) {
			logger.error("Unable to complete preparation of notifications", e);
		}
	}

}
