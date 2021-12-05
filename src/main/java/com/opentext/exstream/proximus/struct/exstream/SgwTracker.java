package com.opentext.exstream.proximus.struct.exstream;

import java.util.HashMap;
import java.util.Map;

public class SgwTracker {

	private Map<String, String> fields = new HashMap<String, String>();

	public void addField(String name, String value) {
		fields.put(name, value);
	}

	public String getField(String name) {
		return fields.get(name);
	}

	public Map<String, String> getFields() {
		return fields;
	}

}
