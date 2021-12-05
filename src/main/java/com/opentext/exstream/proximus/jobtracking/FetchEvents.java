package com.opentext.exstream.proximus.jobtracking;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.opentext.exstream.proximus.actions.FetchEventsAction;
import com.opentext.exstream.proximus.actions.SgwDocumentsActions;
import com.opentext.exstream.proximus.common.CustomLogger;
import com.opentext.exstream.proximus.common.FileLock;
import com.opentext.exstream.proximus.server.GatewaySession;
import com.opentext.exstream.proximus.struct.EventListType;
import com.opentext.exstream.proximus.struct.EventRequestType;
import com.opentext.exstream.proximus.struct.EventResponseType;
import com.opentext.exstream.proximus.struct.SelectionPeriodType;
import com.opentext.exstream.proximus.struct.TagType;
import com.opentext.exstream.proximus.struct.exstream.SgwDocument;
import com.opentext.exstream.proximus.struct.exstream.WhereCondition;

public class FetchEvents extends CommandBase {

	private static CustomLogger logger = new CustomLogger(FetchEvents.class);

	// Gateway events
	private static final String EVENT_DELIVERED = "DELIVERED";
	private static final String EVENT_UNDELIVERABLE = "UNDELIVERABLE";
	private static final List<String> VALID_EVENTS = Arrays.asList(EVENT_DELIVERED, EVENT_UNDELIVERABLE);

	// Simple date format patterns
	private static final String SDF_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss";
	private static final String SDF_FETCH_EVENTS = "yyyy-MM-dd'T'HH:mm:ss";

	// Last execution file
	private static final String LAST_EXECUTION_FILE = "fetch-events-last-execution.txt";

	// Synchronization settings
	private Date syncStart, syncEnd;

	// Gateway handlers
	private GatewayConfiguration gatewayConfig;
	private GatewaySession gatewaySession;

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

	private void processEvent(int eventNumber, EventListType event) throws Exception {
		String eventKind = event.getEvent().getKind().toUpperCase();

		logger.info("Event: {} - Kind: {}", eventNumber, eventKind);

		// Skip events not related to delivery status
		if (!VALID_EVENTS.contains(eventKind)) {
			logger.debug("Event not in process list, skipping event...");
			return;
		}

		// Extract additional information from event
		String qualifier = event.getEvent().getQualifier();

		String attemptId = null;
		for (TagType tag : event.getTag()) {
			if (tag.getName().equals(jobTrackerConfig.getTagName())) {
				attemptId = tag.getValue();
				break;
			}
		}

		if (attemptId == null) {
			logger.info("Event does not contain PartID tag {}, skipping event...",jobTrackerConfig.getTagName());
			//logger.error("Unable to find PartID in tags to correlate event to document");
			return;
		}

		logger.info("PartID: [{}] - Qualifier: [{}]", attemptId, qualifier);

		// Check if document is still in holding state
		List<WhereCondition> params = new ArrayList<WhereCondition>();
		params.add(new WhereCondition("EQ", "processingattemptid", attemptId));
		params.add(new WhereCondition("EQ", "processingstate", Constants.STATE_HOLDING));
		List<SgwDocument> docs = SgwDocumentsActions.getDocuments(exstreamSession, params);

		// If document is in holding state, update the state
		if (docs.size() == 0) {
			logger.info("Document not returned, probably state is not holding");
		} else if (docs.size() == 1) {
			String state = EVENT_DELIVERED.equals(eventKind) ? Constants.STATE_SUCCESS : Constants.STATE_FAILURE;
			updateDocument(docs.get(0), state, qualifier);
		} else {
			throw new IOException("Unexpected number of received documents: " + docs.size());
		}
	}

	private void fetchChannelEvents(EventRequestType eventRequest) throws Exception {
		//XXX - change to trace ???
		if (logger.isDebugEnabled()) {
			logger.debug(eventRequest.toPrettyString());
		}

		int iteration = 0;
		int eventNumber = 0;
		boolean loop = true;

		while (loop) {
			iteration++;
			EventResponseType response = FetchEventsAction.fetchEvents(gatewaySession, eventRequest);

			String selectionRef = response.getResult().getSelectionReference();
			if (selectionRef != null && selectionRef.length() > 0) {
				eventRequest.setSelectionPeriod(null);
				eventRequest.setSelectionReference(selectionRef);
			} else {
				loop = false;
			}

			//XXX - change to trace ???
			if (logger.isDebugEnabled()) {
				logger.debug(response.toPrettyString());
			}

			int returnedEvents = response.getResult().getEventList().length;
			logger.info("Iteration: {} - Events to process: {}", iteration, returnedEvents);

			for (EventListType event : response.getResult().getEventList()) {
				eventNumber++;
				processEvent(eventNumber, event);
			}
		}

		logger.info("Total processed events: {}", eventNumber);
	}

	private void fetchEvents(Date start, Date end) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(SDF_FETCH_EVENTS);

		SelectionPeriodType selectionPeriod = new SelectionPeriodType();
		selectionPeriod.setStartTimestamp(sdf.format(start));
		selectionPeriod.setEndTimestamp(sdf.format(end));

		for (String channel : jobTrackerConfig.getChannelName()) {

			EventRequestType eventRequest = new EventRequestType();
			eventRequest.setClientName(jobTrackerConfig.getClientName());
			eventRequest.setSelectionPeriod(selectionPeriod);
			eventRequest.setEventPageSize(jobTrackerConfig.getEventPageSize());
			eventRequest.setChannelName(channel);
			fetchChannelEvents(eventRequest);
		}
	}

	protected void execute() throws Exception {
		logger.info("Synchronizing events from '{}' to '{}'", syncStart, syncEnd);

		long startTime = System.currentTimeMillis();
		fetchEvents(syncStart, syncEnd);
		long endTime = System.currentTimeMillis();
		logger.info("Fetch events completed in {} secs", String.format("%.1f", (endTime - startTime) / 1000.0f));

		writeLastExecution(LAST_EXECUTION_FILE, syncEnd);
	}

	@Override
	protected void initialize() throws Exception {
		super.initialize();

		gatewayConfig = new GatewayConfiguration();
		gatewaySession = new GatewaySession(gatewayConfig.getGatewayUrl());
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
					syncStart = calendar.getTime();
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
		System.out.println("Please select among three event synchronization modes.");
		System.out.println(" -last                          : Since last execution (timestamp stored on file)");
		System.out.println(" -minutes <integer>             : Since last X minutes");
		System.out.println(" -range <timestamp> <timestamp> : Time range, timestamp as '" + SDF_TIMESTAMP + "'");
	}
	
	public static void process(String[] args) throws Exception{
		FetchEvents fetchEvents = new FetchEvents();
		fetchEvents.initialize();
		fetchEvents.commandline(args);
		fetchEvents.execute();
		fetchEvents.close();
	}

	public static void main(String[] args) {
		try {
			process(args);
		} catch (Exception e) {
			logger.error("Unable to complete fetch events", e);
			FileLock.release();
			System.exit(-1);
		}
	}

}
