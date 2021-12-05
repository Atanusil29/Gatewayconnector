package com.opentext.exstream.proximus.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import com.opentext.exstream.proximus.struct.exstream.SgwTracker;
import com.opentext.exstream.proximus.struct.exstream.WhereCondition;

public class SgwTrackersActions {

	private static final CustomLogger log = new CustomLogger(SgwTrackersActions.class);

	public static SgwTracker getTrackers(ExstreamSession session, String trackerId) throws ExstreamException {
		log.debug("getTrackers");

		String url = session.getSgwUrl() + "/v1/trackers";

		HttpHeaders headers = session.generateHeaders();
		HttpEntity<String> request = new HttpEntity<String>("", headers);

		UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(url)
				.queryParam("where_trackerid", trackerId)
				.queryParam("limit", "1")
				.queryParam("guid_format", "false")
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
				
				SgwTracker tracker = new SgwTracker();
				JsonNode data = jsonResponse.get("data");
				if (data.size() == 0){
					log.warn("tracker not found: {}", trackerId);
					return null;
				}
				JsonNode node = data.get(0);
				Iterator<String> itFields = node.fieldNames();
				while (itFields.hasNext()) {
					String field = itFields.next();
					if (node.get(field).isTextual()) {
						tracker.addField(field, node.get(field).asText());
					} else {
						log.warn("JSON field is ignored: {}", field);
					}
				}

				return tracker;

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

	public static List<SgwTracker> getTrackers(ExstreamSession session, List<WhereCondition> params) throws ExstreamException {
		return getTrackers(session, params, -1, 50);
	}

	public static List<SgwTracker> getTrackers(ExstreamSession session, List<WhereCondition> params, int after, int limit) throws ExstreamException {
		log.debug("getTrackers");

		String url = session.getSgwUrl() + "/v1/trackers";

		HttpHeaders headers = session.generateHeaders();
		HttpEntity<String> request = new HttpEntity<String>("", headers);

		String whereFilter = "";
		for (WhereCondition param : params) {
			if (param.getValue() != null) {
				whereFilter += String.format("[\"%s\",\"%s\",\"%s\"],", param.getOperator(), param.getName(), param.getValue());
			} else {
				whereFilter += String.format("[\"%s\",\"%s\"", param.getOperator(), param.getName());
				for (int i = 0; i < param.getValues().length; i++) {
					whereFilter += String.format(",\"%s\"", param.getValues()[i]);
				}
				whereFilter += "],";
			}
		}

		if (whereFilter.length() > 0) {
			whereFilter = whereFilter.substring(0, whereFilter.length() - 1);
			whereFilter = "[" + whereFilter + "]";
		}

		UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(url)
				.queryParam("filter", whereFilter)
				.queryParam("guid_format", "false")
				.queryParam("after", after)
				.queryParam("limit", limit)
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
				List<SgwTracker> trackers = new ArrayList<SgwTracker>();

				Iterator<JsonNode> itNodes = jsonResponse.get("data").elements();
				while (itNodes.hasNext()) {
					SgwTracker tracker = new SgwTracker();

					JsonNode node = itNodes.next();
					Iterator<String> itFields = node.fieldNames();
					while (itFields.hasNext()) {
						String field = itFields.next();
						if (node.get(field).isTextual()) {
							tracker.addField(field, node.get(field).asText());
						} else {
							log.warn("JSON field is ignored: {}", field);
						}
					}

					trackers.add(tracker);
				}

				return trackers;

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
