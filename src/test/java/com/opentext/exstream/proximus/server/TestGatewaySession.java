package com.opentext.exstream.proximus.server;


import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.opentext.exstream.proximus.server.GatewaySession;
import com.opentext.exstream.proximus.struct.EventRequestType;
import com.opentext.exstream.proximus.struct.EventResponseType;


/**
 * Unit test for simple App.
 */
public class TestGatewaySession extends TestGatewayDefaults
{

	private GatewaySession session;

	
	public TestGatewaySession(){
		setupSSLProperties();
		//setupTLSSettings();
		
		session = new GatewaySession(baseurl,"admin","xxx");
    	session.setSleepInterval(500);
    	session.setSleepFactor(2);
    	session.setMaxRetries(4);
	}


	@Test
	public void test_fetchEvent()
    {
        try {
        	EventRequestType request = new EventRequestType();
        	request.setClientName("TEST_200");
	        EventResponseType response = session.postRequest("/v1/cdcgFetchEvents", request, EventResponseType.class);
			Assert.assertNotNull(response.getResult());
		} catch (Exception e) {
			//e.printStackTrace();
			Assert.fail(e.getMessage());
		}
    }

	@Test
	public void test_fetchEvent_400()
    {
        try {
        	EventRequestType request = new EventRequestType();
        	request.setClientName("TEST_400");
        	@SuppressWarnings("unused")
	        EventResponseType response = session.postRequest("/v1/cdcgFetchEvents", request, EventResponseType.class);
			Assert.fail("exception expected");
			
		} catch (Exception e) {
			//e.printStackTrace();
			Assert.assertEquals("Gateway bad request: HTTP "+ HttpStatus.BAD_REQUEST, e.getMessage());
		}
    }
	
	@Test
	public void test_fetchEvent_401()
    {
        try {
        	EventRequestType request = new EventRequestType();
        	request.setClientName("TEST_401");
        	@SuppressWarnings("unused")
	        EventResponseType response = session.postRequest("/v1/cdcgFetchEvents", request, EventResponseType.class);
			Assert.fail("exception expected");
			
		} catch (Exception e) {
			//e.printStackTrace();
			Assert.assertEquals("Gateway authentication failed: HTTP "+ HttpStatus.UNAUTHORIZED, e.getMessage());
		}
    }
	@Test
	public void test_fetchEvent_403()
    {
        try {
        	EventRequestType request = new EventRequestType();
        	request.setClientName("TEST_403");
        	@SuppressWarnings("unused")
	        EventResponseType response = session.postRequest("/v1/cdcgFetchEvents", request, EventResponseType.class);
			Assert.fail("exception expected");
			
		} catch (Exception e) {
			//e.printStackTrace();
			Assert.assertEquals("Gateway authentication failed: HTTP "+ HttpStatus.FORBIDDEN, e.getMessage());
		}
    }
	@Test
	public void test_fetchEvent_404()
    {
        try {
        	EventRequestType request = new EventRequestType();
        	request.setClientName("TEST_404");
        	@SuppressWarnings("unused")
	        EventResponseType response = session.postRequest("/v1/cdcgFetchEvents", request, EventResponseType.class);
			Assert.fail("exception expected");
			
		} catch (Exception e) {
			//e.printStackTrace();
			Assert.assertEquals("Gateway service not found: HTTP "+ HttpStatus.NOT_FOUND, e.getMessage());
		}
    }
	@Test
	public void test_fetchEvent_502()
    {
        try {
        	EventRequestType request = new EventRequestType();
        	request.setClientName("TEST_502");
        	@SuppressWarnings("unused")
	        EventResponseType response = session.postRequest("/v1/cdcgFetchEvents", request, EventResponseType.class);
			Assert.fail("exception expected");
			
		} catch (Exception e) {
			//e.printStackTrace();
			Assert.assertEquals("Gateway server is not available: HTTP "+ HttpStatus.BAD_GATEWAY, e.getMessage());
		}
    }
	@Test
	public void test_fetchEvent_504()
    {
        try {
        	EventRequestType request = new EventRequestType();
        	request.setClientName("TEST_504");
        	@SuppressWarnings("unused")
	        EventResponseType response = session.postRequest("/v1/cdcgFetchEvents", request, EventResponseType.class);
			Assert.fail("exception expected");
			
		} catch (Exception e) {
			//e.printStackTrace();
			Assert.assertEquals("Gateway server is not available: HTTP "+ HttpStatus.GATEWAY_TIMEOUT, e.getMessage());
		}
    }

	@Test
	public void test_fetchEvent_connectionfail()
    {
        try {
        	GatewaySession session = new GatewaySession("https://localhost:8888/gateway-mock-server","admin","xxx");
        	session.setSleepInterval(500);
        	session.setSleepFactor(2);
        	session.setMaxRetries(2);
        	EventRequestType request = new EventRequestType();
        	@SuppressWarnings("unused")
			EventResponseType response = session.postRequest("/v1/cdcgFetchEvents", request, EventResponseType.class);
			Assert.fail("exception expected");
			
		} catch (Exception e) {
			//e.printStackTrace();
			Assert.assertEquals("Gateway server connection failed: Connection refused: connect", e.getMessage());
		}
    }

}
