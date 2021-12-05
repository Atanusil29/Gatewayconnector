package com.opentext.exstream.proximus.server;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.opentext.exstream.proximus.struct.EventRequestType;
import com.opentext.exstream.proximus.struct.EventResponseType;


/**
 * Unit test for simple App.
 */
public class TestGatewayMockService extends TestGatewayDefaults
{

	private String url = String.format("%s/v1/cdcgFetchEvents", baseurl);
	
	public TestGatewayMockService(){
		setupSSLProperties();
		//setupTLSSettings();
	}

	@Test
	public void test_fetchEvents()
    {
        try {
        	EventRequestType eventRequest = new EventRequestType();
	        byte[] bytes = eventRequest.toString().getBytes();
	
	        RestTemplate restTemplate = new RestTemplate();
			HttpHeaders requestheaders = new HttpHeaders();
			requestheaders.putAll(getHttpHeaders());
			requestheaders.add("content-type", "application/json");
	        
	        HttpEntity<byte[]> request = new HttpEntity<byte[]>(bytes,requestheaders);
	
	        ResponseEntity<EventResponseType> httpResponse = restTemplate.exchange(url, HttpMethod.POST, request, EventResponseType.class);
			@SuppressWarnings("unused")
	        HttpHeaders header = httpResponse.getHeaders();
	        EventResponseType body = httpResponse.getBody();
	        String json = body.toString();
			Assert.assertTrue( json.startsWith("{\"result\":"));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
    }


}
