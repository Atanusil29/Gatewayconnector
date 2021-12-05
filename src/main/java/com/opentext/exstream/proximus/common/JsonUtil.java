package com.opentext.exstream.proximus.common;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtil {
	public static String toPrettyString(JsonNode node){
		String value = null;
		try {
			// new logic, produces the same output as previous.
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			Object o = mapper.readValue(node.toString(), Object.class);
			value = mapper.writeValueAsString(o);
			
			// old logic that was removed due to library conflicts in Communication Server
			//JsonNode json = new ObjectMapper().valueToTree(this);
			//value = json.toPrettyString();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
		
	}
}
