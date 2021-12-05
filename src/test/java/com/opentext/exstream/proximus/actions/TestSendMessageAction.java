package com.opentext.exstream.proximus.actions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.opentext.exstream.proximus.server.GatewayException;
import com.opentext.exstream.proximus.server.GatewaySession;
import com.opentext.exstream.proximus.server.TestGatewayDefaults;
import com.opentext.exstream.proximus.struct.SendMessageType;

public class TestSendMessageAction extends TestGatewayDefaults {

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
	public void test_sendMessage_200() throws GatewayException{
    	SendMessageType request = new SendMessageType();
    	request.setClientName("TEST_200");

		SendMessageAction.sendMessage(session, request);
		Assert.assertTrue(true);
	}

	@Test
	public void test_sendMessage_400() throws GatewayException{
		SendMessageType request = new SendMessageType();
    	request.setClientName("TEST_400");

    	try{
    		SendMessageAction.sendMessage(session, request);
			Assert.fail("exception expected");
		} catch (Exception e) {
			//e.printStackTrace();
			Assert.assertEquals("Gateway bad request: HTTP "+ HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

}
