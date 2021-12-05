package com.opentext.exstream.proximus.actions;

import com.opentext.exstream.proximus.common.CustomLogger;
import com.opentext.exstream.proximus.server.GatewayException;
import com.opentext.exstream.proximus.server.GatewaySession;
import com.opentext.exstream.proximus.struct.EventRequestType;
import com.opentext.exstream.proximus.struct.EventResponseType;

public class FetchEventsAction {
	private static final CustomLogger log = new CustomLogger(FetchEventsAction.class);

	public static EventResponseType fetchEvents(GatewaySession session, EventRequestType request) throws GatewayException{
		log.debug("FetchEventsAction");
		//EventResponseType response = session.fetchEvents(request);
		EventResponseType response = session.postRequest("/v1/cdcgFetchEvents", request, EventResponseType.class);
		return response;
	}

}
