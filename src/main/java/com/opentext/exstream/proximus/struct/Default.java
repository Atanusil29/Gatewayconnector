package com.opentext.exstream.proximus.struct;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Default {

    @Override
	public String toString(){
		String value = null;
		try {
			value = new ObjectMapper().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return value;
	}

	public String toPrettyString(){
		String value = null;
		try {
			// new logic, produces the same output as previous.
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			value = mapper.writeValueAsString(this);
			
			// old logic that was removed due to library conflicts in Communication Server
			//JsonNode json = new ObjectMapper().valueToTree(this);
			//value = json.toPrettyString();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public String writeValue(File file){
		String value = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			mapper.writeValue(file, this);
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
