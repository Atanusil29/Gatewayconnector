package com.opentext.exstream.proximus.struct;

import org.springframework.util.Base64Utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
public class FileType extends Default {
	private String name;
	private String content;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@JsonProperty("content")
	public String getContent() {
		return content;
	}
	@JsonProperty("content")
	public void setContent(String content) {
		this.content = content;
	}
	@JsonIgnore
	public byte[] getContentBytes() {
		return Base64Utils.decodeFromString(getContent());
	}
	@JsonIgnore
	public void setContent(byte[] bytes) {
		setContent(Base64Utils.encodeToString(bytes));
	}
	
}
