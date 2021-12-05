package com.opentext.exstream.proximus.actions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.opentext.exstream.proximus.server.GatewayException;
import com.opentext.exstream.proximus.server.GatewaySession;
import com.opentext.exstream.proximus.server.TestGatewayDefaults;
import com.opentext.exstream.proximus.struct.EventRequestType;
import com.opentext.exstream.proximus.struct.EventResponseType;

public class TestFetchEventsAction extends TestGatewayDefaults {

	private GatewaySession session = null;

	@Before
	public void getSession(){
		setupSSLProperties();

		if (session != null) {
			return;
		}
		session = new GatewaySession(baseurl);
	}
	
	@Test
	public void test_fetchevent_200() throws GatewayException{
    	EventRequestType request = new EventRequestType();
    	request.setClientName("TEST_200");

		EventResponseType response = FetchEventsAction.fetchEvents(session, request);
		Assert.assertNotNull(response.getResult().getEventList());
	}

	@Test
	public void test_fetchevent_400() throws GatewayException{
    	EventRequestType request = new EventRequestType();
    	request.setClientName("TEST_400");

    	try{
        	@SuppressWarnings("unused")
    		EventResponseType response = FetchEventsAction.fetchEvents(session, request);
			Assert.fail("exception expected");
		} catch (Exception e) {
			//e.printStackTrace();
			Assert.assertEquals("Gateway bad request: HTTP "+ HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

}
