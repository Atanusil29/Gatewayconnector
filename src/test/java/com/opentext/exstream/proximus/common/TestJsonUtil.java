package com.opentext.exstream.proximus.common;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opentext.exstream.proximus.server.ExstreamException;

public class TestJsonUtil {

	@Test
	public void test_json() {
		String jsonstring = "{\"test\":[{\"xxx\":1}]}";
		String expected = "{\r\n  \"test\" : [ {\r\n    \"xxx\" : 1\r\n  } ]\r\n}";
		try {
			JsonNode json = getJson(jsonstring);
			String actual = JsonUtil.toPrettyString(json);
			Assert.assertEquals(expected, actual);
		} catch (ExstreamException e) {
			Assert.fail(e.getMessage());
		}
		
		
		Assert.assertTrue(true);
	}

	private static JsonNode getJson(String body) throws ExstreamException{
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			JsonNode json = objectMapper.readTree(body);
			return json;
		} catch (Exception e) {
			throw new ExstreamException("Unable to parse JSON response body", e);
		}
	}

}
