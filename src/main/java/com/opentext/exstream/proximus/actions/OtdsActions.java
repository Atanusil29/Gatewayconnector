package com.opentext.exstream.proximus.actions;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opentext.exstream.proximus.common.CustomLogger;
import com.opentext.exstream.proximus.common.JsonUtil;
import com.opentext.exstream.proximus.server.ExstreamException;
import com.opentext.exstream.proximus.server.ExstreamSession;
import com.opentext.exstream.proximus.struct.exstream.OtdsAuthenticationRequestType;

public class OtdsActions {

	private static final CustomLogger log = new CustomLogger(OtdsActions.class);

	public static JsonNode otdsVersion(ExstreamSession session) throws ExstreamException {
		log.debug("otdsVersion");

		String url = session.getOtdsMtaUrl() + "/otdsws/rest/systemconfig/version";

		ResponseEntity<String> httpResponse = session.processRequest(url, HttpMethod.GET, null, String.class);
		JsonNode response = getJson(httpResponse.getBody());
		
		//XXX - change to trace ???
		if (log.isDebugEnabled()){
			log.debug(JsonUtil.toPrettyString(response));
		}

		return response;
	}

	public static String otdsCredentials(ExstreamSession session) throws ExstreamException {
		log.debug("otdsCredentials");

		String url = session.getOtdsTenantUrl() + "/otdsws/rest/authentication/credentials";

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

		OtdsAuthenticationRequestType body = new OtdsAuthenticationRequestType();
		body.setUserName(session.getUsername());
		body.setPassword(session.getPassword());
		
		//XXX - change to trace ???
		if (log.isDebugEnabled()){
			log.debug(body.toPrettyString());
		}

		HttpEntity<byte[]> request = new HttpEntity<byte[]>(body.toString().getBytes(), headers);

		ResponseEntity<String> httpResponse = session.processRequest(url, HttpMethod.POST, request, String.class, false);
		JsonNode response = getJson(httpResponse.getBody());

		//XXX - change to trace ???
		if (log.isDebugEnabled()){
			log.debug(JsonUtil.toPrettyString(response));
		}

		return response.get("ticket").asText();
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
