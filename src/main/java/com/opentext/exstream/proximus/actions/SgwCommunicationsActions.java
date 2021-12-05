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
import com.opentext.exstream.proximus.struct.exstream.SgwCommunicationsRequestType;
import com.opentext.exstream.proximus.struct.exstream.SgwContentType;

public class SgwCommunicationsActions {

	private static final CustomLogger log = new CustomLogger(SgwCommunicationsActions.class);

	public static String post(ExstreamSession session, String serviceName, String serviceVersion, String async, byte[] driver) throws ExstreamException {
		log.debug("post with driver");

		SgwContentType content = new SgwContentType();
		content.setContentType("text/xml");
		content.setDataBytes(driver);

		SgwCommunicationsRequestType body = new SgwCommunicationsRequestType();
		body.setContent(content);

		return post(session, serviceName, serviceVersion, async, body.toString());
	}

	public static String post(ExstreamSession session, String serviceName, String serviceVersion, String async, String body) throws ExstreamException {
		log.debug("post with request body");

		String url = session.getSgwUrl() + "/v1/communications";

		HttpHeaders headers = session.generateHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

		HttpEntity<byte[]> request = new HttpEntity<byte[]>(body.getBytes(), headers);

		UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(url)
				.queryParam("name", serviceName)
				.queryParam("version", serviceVersion)
				.queryParam("async", async)
				.build();

		ResponseEntity<String> httpResponse = session.processRequest(uriComponents.toUriString(), HttpMethod.POST, request, String.class);

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonResponse = objectMapper.readTree(httpResponse.getBody());

			//XXX - change to trace ???
			if (log.isDebugEnabled()) {
				log.debug(JsonUtil.toPrettyString(jsonResponse));
			}

			String status = jsonResponse.get("status").asText();
			if ("success".equals(status)) {
				return jsonResponse.get("data").get("id").asText();
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
