package com.opentext.exstream.proximus.jobtracking;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.opentext.exstream.proximus.actions.FetchEventsAction;
import com.opentext.exstream.proximus.actions.OtdsActions;
import com.opentext.exstream.proximus.actions.SendMessageAction;
import com.opentext.exstream.proximus.actions.SgwDocumentsActions;
import com.opentext.exstream.proximus.common.JsonUtil;
import com.opentext.exstream.proximus.server.ExstreamException;
import com.opentext.exstream.proximus.server.ExstreamSession;
import com.opentext.exstream.proximus.server.GatewayException;
import com.opentext.exstream.proximus.server.GatewaySession;
import com.opentext.exstream.proximus.struct.EventRequestType;
import com.opentext.exstream.proximus.struct.SelectionPeriodType;
import com.opentext.exstream.proximus.struct.SendMessageType;
import com.opentext.exstream.proximus.struct.exstream.SgwDocument;
import com.opentext.exstream.proximus.struct.exstream.WhereCondition;

public class Test {

	private static void testGateway() {
		GatewaySession session = new GatewaySession("http://localhost:8080/gateway-mock-server");

		SendMessageType sendMessage = new SendMessageType();
		sendMessage.setClientName("OpenText");
		sendMessage.setChannelName("EMAIL");

		try {
			SendMessageAction.sendMessage(session, sendMessage);
		} catch (GatewayException e) {
			e.printStackTrace();
		}

		EventRequestType eventRequest = new EventRequestType();
		eventRequest.setClientName("OpenText");
		eventRequest.setChannelName("EMAIL");
		eventRequest.setEventPageSize(256);

		Calendar calendar = Calendar.getInstance();
		Date endDate = calendar.getTime();
		calendar.add(Calendar.DATE, -7);
		Date startDate = calendar.getTime();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		SelectionPeriodType selectionPeriod = new SelectionPeriodType();
		selectionPeriod.setStartTimestamp(sdf.format(startDate));
		selectionPeriod.setEndTimestamp(sdf.format(endDate));
		eventRequest.setSelectionPeriod(selectionPeriod);

		System.out.println(eventRequest.toPrettyString());

		try {
			FetchEventsAction.fetchEvents(session, eventRequest);
		} catch (GatewayException e) {
			e.printStackTrace();
		}
	}

	private static void testExstream() {
		System.out.println("Truststore exists? " + new File("src/test/resources/truststore.jks").isFile());
		System.setProperty("javax.net.ssl.trustStore", "src/test/resources/truststore.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
		System.setProperty("javax.net.ssl.trustStoreType", "JKS");

		String otdsMtaUrl = "https://exstream16-otds:8443";
		String otdsTenantUrl = "https://exstream16-otds:8443";
		String sgwUrl = "https://exstream16:2719";
		String username = "tenantadmin";
		String password = "xxx";

		ExstreamSession exstreamSession = new ExstreamSession(otdsMtaUrl, otdsTenantUrl, sgwUrl, username, password);
		try {
			JsonNode otdsVersion = OtdsActions.otdsVersion(exstreamSession);
			System.out.println(JsonUtil.toPrettyString(otdsVersion));

			String otdsTicket = OtdsActions.otdsCredentials(exstreamSession);
			System.out.println("OTDS ticket: " + otdsTicket);

			long startTime = System.currentTimeMillis();
			for (int i = 0; i < 1; i ++) {
				List<WhereCondition> params = new ArrayList<WhereCondition>();
				params.add(new WhereCondition("EQ", "processingattemptid", "B4A9E048-1ED3-46C0-B113-ECF8DC42B7D4"));
				params.add(new WhereCondition("EQ", "processingstate", "5"));

				List<SgwDocument> docs = SgwDocumentsActions.getDocuments(exstreamSession, params);
				System.out.println("Get documents: " + docs.size());

/*
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, -7);
				Date myDate = calendar.getTime();

				params.clear();
				params.add(new WhereCondition("EQ", "processingstate", "8"));
				params.add(new WhereCondition("EQ", "connectorname", "Custom Connector EMAIL"));
				params.add(new WhereCondition("LT", "lastupdatetime", sdf.format(myDate)));
				docs = SgwDocumentsActions.getDocuments(exstreamSession, params);
				System.out.println("Get documents: " + docs.size());

				int count = SgwDocumentsActions.updateDocuments(exstreamSession, "B4A9E048-1ED3-46C0-B113-ECF8DC42B7D4", "5", "Updated from API");
				System.out.println("Updated documents: " + count);

				SgwTracker tracker = SgwTrackersActions.getTrackers(exstreamSession, "5E25A7C0-6B04-6D4F-AA9A-9599521F35E8");
				System.out.println("Fields in tracker: " + tracker.getFields().keySet().size());

				String serviceState = SgwServicesActions.getServiceState(exstreamSession, "ejercicio", "1");
				System.out.println("Service state: " + serviceState);
				serviceState = SgwServicesActions.getServiceState(exstreamSession, "ejercicio", "2");
				System.out.println("Service state: " + serviceState);

				String newTracker = SgwCommunicationsActions.post(exstreamSession, "ejercicio", "1", "true", "driver-file".getBytes());
				System.out.println("New tracker: " + newTracker);
*/
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			long endTime = System.currentTimeMillis();
			System.out.println(String.format("Done (%.1f secs)", (endTime - startTime) / 1000.0f));

		} catch (ExstreamException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if (args.length > 0 && "testGateway".equals(args[0]))
			testGateway();
		else
			testExstream();
	}

}
