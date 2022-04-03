package com.opentext.exstream.proximus.jobtracking;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.springframework.util.Base64Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opentext.exstream.proximus.actions.SgwCommunicationsActions;
import com.opentext.exstream.proximus.actions.SgwTrackersActions;
import com.opentext.exstream.proximus.common.CustomLogger;
import com.opentext.exstream.proximus.struct.exstream.SgwTracker;
import com.opentext.exstream.proximus.struct.exstream.WhereCondition;

public class FailedTrackers extends CommandBase {

	private static CustomLogger logger = new CustomLogger(FailedTrackers.class);

	// Regular expression to extract data from external id
	//private static final Pattern PATTERN = Pattern.compile("([a-zA-Z0-9-]+) \\((.*)\\) (.*)");
	private static final Pattern PATTERN = Pattern.compile("([^ ]*) ([^ ]*) \\((.*)\\) (.*)");

	// Simple date format patterns
	private static final String SDF_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss";

	// Last execution file
	protected static final String LAST_EXECUTION_FILE = "failed-trackers-last-execution.txt";

	// Synchronization settings
	private Date syncStart, syncEnd;

	private boolean trackerExists(String tracker) throws Exception {
		List<WhereCondition> params = new ArrayList<WhereCondition>();
		params.add(new WhereCondition("EQ", Constants.EXTERNAL_ID, tracker));
		List<SgwTracker> trackers = SgwTrackersActions.getTrackers(exstreamSession, params);
		if (trackers.size() > 1) {
			throw new Exception("Multiple trackers received, there could be some system error");
		}
		return trackers.size() == 1;
	}

	private void sendRequest(String projectName, String serviceName, String requestUUID, String nextSequence) throws Exception {
		// Request body = original driver file + updated sequence number
		File projectDir = new File(jobTrackerConfig.getCustomFolder(), projectName);
		File driversDir = new File(projectDir, "drivers");
		byte[] file = FileUtils.readFileToByteArray(new File(driversDir, requestUUID + ".json"));
		String driverFile = Base64Utils.encodeToString(file);

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode contentNode = mapper.createObjectNode();
		contentNode.put("contentType", "text/xml");
		contentNode.put("data", driverFile);

		ObjectNode rootNode = mapper.createObjectNode();
		rootNode.set("content", contentNode);
		rootNode.put("sequence", nextSequence);

		String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
		logger.debug("Retry request [{}]", jsonString);

		// Service name = project name
		String id = SgwCommunicationsActions.post(exstreamSession, serviceName, "*", "true", jsonString);
		logger.info("Submitted new retry job with tracker ID [{}]", id);
	}

	private void checkTrackers() throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		List<WhereCondition> params = new ArrayList<WhereCondition>();
		String[] range = {sdf.format(syncStart), sdf.format(syncEnd)};
		params.add(new WhereCondition("RANGE", Constants.LAST_UPDATE_TIME, range));
		params.add(new WhereCondition("EQ", Constants.PROCESSING_STATE, Constants.STATE_FAILURE));

		int countUnknownExternalId = 0;
		int countWithoutRetry = 0;
		int countAlreadyRetried = 0;
		int countRetried = 0;

		int pageNumber = 0;
		int pageLimit = 50;
		int counter = -1;
		boolean loop = true;

		while (loop) {
			pageNumber++;

			List<SgwTracker> trackers = SgwTrackersActions.getTrackers(exstreamSession, params, counter, pageLimit);
			logger.info("Page: {} - Trackers: {}", pageNumber, trackers.size());

			for (SgwTracker tracker : trackers) {
				String externalId = tracker.getField("externalid");
				Matcher m = PATTERN.matcher(externalId);

				if (m.find() && m.groupCount() == 4) {
					String projectName = m.group(1);
					String serviceName = m.group(2);
					String requestUUID = m.group(3);
					String channels = m.group(4);

					String[] split = channels.split(" -> ");
					if (split.length != 2) {
						logger.info("External ID without next sequence [{}]. Don't need to process", externalId);
						countWithoutRetry++;
						continue;
					}

					String nextSequence = split[1];
					String[] sequence = split[0].split("\\.");
					if (sequence.length != 3) {
						throw new Exception("Unable to parse sequence [" + split[0] + "]");
					}

					String retryJob = String.format("%s %s (%s) %s.%s", projectName, serviceName, requestUUID, sequence[0], nextSequence);
					logger.debug("Next tracker should have external ID [{}]", retryJob);

					if (trackerExists(retryJob)) {
						logger.info("Tracker with external ID [{}] has been already retried", externalId);
						countAlreadyRetried++;
						continue;
					}

					logger.info("Tracker with external ID must be retried [{}]", externalId);
					sendRequest(projectName, serviceName, requestUUID, nextSequence);
					countRetried++;

				} else {
					logger.warn("Unable to parse external ID [{}]. It will not be processed", externalId);
					countUnknownExternalId++;
					continue;
				}
			}

			counter += trackers.size();
			if (trackers.size() < pageLimit) {
				loop = false;
			}
		}

		if (counter >= 0) {
			logger.info("****************************************");
			logger.info("Processed failed trackers: {}", counter + 1);
			logger.info("****************************************");
			logger.info("Retried: {}", countRetried);
			logger.info("Already retried in the past: {}", countAlreadyRetried);
			logger.info("Doesn't need to retry: {}", countWithoutRetry);
			logger.info("****************************************");
			logger.info("Unknown external ID: {}", countUnknownExternalId);
			logger.info("****************************************");
		}
	}

	protected void execute() throws Exception {
		logger.info("Checking failed trackers from '{}' to '{}'", syncStart, syncEnd);

		long startTime = System.currentTimeMillis();
		checkTrackers();
		long endTime = System.currentTimeMillis();
		logger.info("Completed in {} secs", String.format("%.1f", (endTime - startTime) / 1000.0f));

		writeLastExecution(LAST_EXECUTION_FILE, syncEnd);
	}

	@Override
	protected void initializeWithoutLock() throws Exception {
		super.initializeWithoutLock();
	}

	protected void commandline(String[] args) throws Exception {
		if (args.length == 1) {
			if ("-last".equals(args[0])) {
				try {
					syncStart = readLastExecution(LAST_EXECUTION_FILE);
					syncEnd = new Date();
				} catch (Exception e) {
					System.out.println("Unable to read last execution file: " + e.getMessage());
					throw new Exception("syncStart/syncEnd not set");
				}
			}
		}
		else if (args.length == 2) {
			if ("-minutes".equals(args[0])) {
				try {
					int minutes = Integer.parseInt(args[1]);
					Calendar calendar = Calendar.getInstance();
					syncEnd = calendar.getTime();
					calendar.add(Calendar.MINUTE, -minutes);
					//syncStart = calendar.getTime();
					//Added by Atanu on 03/04/2022 to fix the Orchestration issue where if a batch took for than the argumented minutes then there is a gab and double communication issue
					syncStart = readLastExecution(LAST_EXECUTION_FILE);
				} catch (NumberFormatException e) {
					System.out.println("Unable to parse integer: " + args[1]);
					throw new Exception("syncStart/syncEnd not set");
				}
			}
		}
		else if (args.length == 3) {
			if ("-range".equals(args[0])) {
				SimpleDateFormat sdf = new SimpleDateFormat(SDF_TIMESTAMP);
				try {
					syncStart = sdf.parse(args[1]);
				} catch (ParseException e) {
					System.out.println("Unable to parse timestamp: " + args[1]);
				}
				try {
					syncEnd = sdf.parse(args[2]);
				} catch (ParseException e) {
					System.out.println("Unable to parse timestamp: " + args[2]);
				}
				if (syncStart == null || syncEnd == null) {
					throw new Exception("syncStart/syncEnd not set");
				}
			}
		}

		if (syncStart == null || syncEnd == null) {
			helpMessage();
			throw new Exception("syncStart/syncEnd not set");
		}
	}

	private void helpMessage() {
		System.out.println("Please select among three synchronization modes.");
		System.out.println(" -last                          : Since last execution (timestamp stored on file)");
		System.out.println(" -minutes <integer>             : Since last X minutes");
		System.out.println(" -range <timestamp> <timestamp> : Time range, timestamp as '" + SDF_TIMESTAMP + "'");
	}

	public static void process(String[] args) throws Exception {
		FailedTrackers cmd = new FailedTrackers();
		cmd.initialize();
		cmd.initializeWithoutLock();
		cmd.commandline(args);
		cmd.execute();
		cmd.close();
	}

	public static void main(String[] args) {
		try{
			FailedTrackers.process(args);
		} catch (Exception e) {
			logger.error("Unable to complete failed trackers", e);
			System.exit(-1);
		}
	}

}
