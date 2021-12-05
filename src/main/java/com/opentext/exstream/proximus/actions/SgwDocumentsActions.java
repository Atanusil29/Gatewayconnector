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
import com.opentext.exstream.proximus.struct.exstream.SgwDocument;
import com.opentext.exstream.proximus.struct.exstream.SgwUpdateDocumentRequestType;
import com.opentext.exstream.proximus.struct.exstream.WhereCondition;

public class SgwDocumentsActions {

	private static final CustomLogger log = new CustomLogger(SgwDocumentsActions.class);

	public static List<SgwDocument> getDocuments(ExstreamSession session, List<WhereCondition> params) throws ExstreamException {
		return getDocuments(session, params, -1, 50);
	}

	public static List<SgwDocument> getDocuments(ExstreamSession session, List<WhereCondition> params, int after, int limit) throws ExstreamException {
		log.debug("getDocuments");

		String url = session.getSgwUrl() + "/v1/documents";

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
				.queryParam("where_typeid", "outputqueueobject")
				.queryParam("where_filter", whereFilter)
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
				List<SgwDocument> docs = new ArrayList<SgwDocument>();

				Iterator<JsonNode> itNodes = jsonResponse.get("data").elements();
				while (itNodes.hasNext()) {
					SgwDocument doc = new SgwDocument();

					JsonNode node = itNodes.next();
					Iterator<String> itFields = node.fieldNames();
					while (itFields.hasNext()) {
						String field = itFields.next();
						if (node.get(field).isTextual()) {
							doc.addField(field, node.get(field).asText());
						} else {
							log.warn("JSON field is ignored: {}", field);
						}
					}

					docs.add(doc);
				}

				return docs;

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

	public static int updateDocuments(ExstreamSession session, String attemptId, String state, String message) throws ExstreamException {
		log.debug("updateDocuments");

		String url = session.getSgwUrl() + "/v1/documents";

		HttpHeaders headers = session.generateHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
		
		if (message == null || message.isEmpty()){
			message = "Outputconnectorupdatingthemessage";
		}

		SgwUpdateDocumentRequestType body = new SgwUpdateDocumentRequestType();
		body.setStatusmessage(message);
		body.setProcessingstate(state);

		HttpEntity<byte[]> request = new HttpEntity<byte[]>(body.toString().getBytes(), headers);

		UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(url)
				.queryParam("where_typeid", "outputqueueobject")
				.queryParam("where_filter", "[\"EQ\",\"processingattemptid\",\"{attemptId}\"]")
				.queryParam("guid_format", "false")
				.buildAndExpand(attemptId);

		ResponseEntity<String> httpResponse = session.processRequest(uriComponents.toUriString(), HttpMethod.PUT, request, String.class);

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonResponse = objectMapper.readTree(httpResponse.getBody());

			//XXX - change to trace ???
			if (log.isDebugEnabled()) {
				log.debug(JsonUtil.toPrettyString(jsonResponse));
			}

			return jsonResponse.get("data").asInt();

		} catch (JsonMappingException e) {
			throw new ExstreamException("Unable to parse JSON response", e);
		} catch (JsonProcessingException e) {
			throw new ExstreamException("Unable to parse JSON response", e);
		} catch (IOException e) {
			throw new ExstreamException("Unable to parse JSON response", e);
		}
	}

}
