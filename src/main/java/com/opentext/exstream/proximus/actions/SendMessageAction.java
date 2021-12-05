package com.opentext.exstream.proximus.actions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.opentext.exstream.proximus.common.CustomLogger;
import com.opentext.exstream.proximus.server.GatewayException;
import com.opentext.exstream.proximus.server.GatewaySession;
import com.opentext.exstream.proximus.struct.SendMessageType;

public class SendMessageAction {
	private static final CustomLogger log = new CustomLogger(SendMessageAction.class);

	public static String sendMessage(GatewaySession session, SendMessageType request) throws GatewayException{
		
		String dateformat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String datetime = sdf.format(new Date());
		log.debug("SendMessageAction");
		//session.sendMessage(request);
		session.postRequest("/v1/cdcgSendMessage", request, null);
		
		return datetime;
	}

}
