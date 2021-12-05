package com.opentext.exstream.proximus.actions;

import java.io.IOException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opentext.exstream.proximus.common.CustomLogger;
import com.opentext.exstream.proximus.common.JsonUtil;
import com.opentext.exstream.proximus.server.ExstreamException;
import com.opentext.exstream.proximus.server.ExstreamSession;

public class SgwServicesActions {

	private static final CustomLogger log = new CustomLogger(SgwServicesActions.class);
	public static String getServiceState(ExstreamSession session, String serviceName, String serviceVersion) throws ExstreamException {
		log.debug("getServiceState");

		String url = session.getSgwUrl() + "/v1/services";

		HttpHeaders headers = session.generateHeaders();
		HttpEntity<String> request = new HttpEntity<String>("", headers);

		UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(url)
				.queryParam("where_servicename", serviceName)
				.queryParam("where_serviceversion", serviceVersion)
				.build();

		ResponseEntity<String> httpResponse = session.processRequest(uriComponents.toUriString(), HttpMethod.GET, request, String.class);

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonResponse = objectMapper.readTree(httpResponse.getBody());

			//XXX - change to trace ???
			if (log.isDebugEnabled()) {
				log.debug(JsonUtil.toPrettyString(jsonResponse));
			}

			String status = jsonResponse.get("status").asText();
			if ("success".equals(status)) {
				JsonNode content = jsonResponse.get("data").get("content");
				if (content.size() == 0) {
					return null;
				} else if (content.size() == 1) {
					return content.get(0).get("state").asText();
				} else {
					throw new ExstreamException("Unexpected number of services in JSON response");
				}
			} else {
				throw new ExstreamException("Unexpected status in JSON response: " + status);
			}

		} catch (JsonMappingException e) {
			throw new ExstreamException("Unable to parse JSON response", e);
		} catch (JsonProcessingException e) {
			throw new ExstreamException("Unable to parse JSON response", e);
		} catch (IOException e) {
			throw new ExstreamException("Unable to parse JSON response", e);
		}
	}

	public static String getServiceVersion(ExstreamSession session, String serviceName) throws ExstreamException {
		log.debug("getServiceVersion");

		String url = session.getSgwUrl() + "/v1/services";

		HttpHeaders headers = session.generateHeaders();
		HttpEntity<String> request = new HttpEntity<String>("", headers);

		UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(url)
				.queryParam("where_servicename", serviceName)
				.build();

		ResponseEntity<String> httpResponse = session.processRequest(uriComponents.toUriString(), HttpMethod.GET, request, String.class);

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonResponse = objectMapper.readTree(httpResponse.getBody());

			//XXX - change to trace ???
			if (log.isDebugEnabled()) {
				log.debug(JsonUtil.toPrettyString(jsonResponse));
			}

			String status = jsonResponse.get("status").asText();
			if ("success".equals(status)) {
				JsonNode content = jsonResponse.get("data").get("content");
				if (content.size() == 0)
					throw new ExstreamException("No service found");
				for (JsonNode node : content){
					if ("started".equalsIgnoreCase(node.get("state").asText()))
						return node.get("serviceversion").asText();
				}
				throw new ExstreamException("No service is not running");
			} else {
				throw new ExstreamException("Unexpected status in JSON response: " + status);
			}

		} catch (JsonMappingException e) {
			throw new ExstreamException("Unable to parse JSON response", e);
		} catch (JsonProcessingException e) {
			throw new ExstreamException("Unable to parse JSON response", e);
		} catch (IOException e) {
			throw new ExstreamException("Unable to parse JSON response", e);
		}
	}

}
