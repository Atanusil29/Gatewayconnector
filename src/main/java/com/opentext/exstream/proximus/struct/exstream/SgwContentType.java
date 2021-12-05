package com.opentext.exstream.proximus.struct.exstream;

import org.springframework.util.Base64Utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.opentext.exstream.proximus.struct.Default;

public class SgwContentType extends Default {

	@JsonInclude(JsonInclude.Include.ALWAYS)
	private String contentType;

	@JsonInclude(JsonInclude.Include.ALWAYS)
	private String data;

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@JsonIgnore
	public byte[] getDataBytes() {
		return Base64Utils.decodeFromString(getData());
	}

	@JsonIgnore
	public void setDataBytes(byte[] bytes) {
		setData(Base64Utils.encodeToString(bytes));
	}

}
