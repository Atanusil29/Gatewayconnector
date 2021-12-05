package com.opentext.exstream.proximus.struct;

import org.springframework.util.Base64Utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class MessageFileType extends Default {
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private String fileName;
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private String fileCategory;
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private String fileFormat;
	private String fileEncoding;
	private String fileAlias;
	private String fileLanguage;
	private String fileContent;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileCategory() {
		return fileCategory;
	}
	public void setFileCategory(String fileCategory) {
		this.fileCategory = fileCategory;
	}
	public String getFileFormat() {
		return fileFormat;
	}
	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}
	public String getFileEncoding() {
		return fileEncoding;
	}
	public void setFileEncoding(String fileEncoding) {
		this.fileEncoding = fileEncoding;
	}
	public String getFileAlias() {
		return fileAlias;
	}
	public void setFileAlias(String fileAlias) {
		this.fileAlias = fileAlias;
	}
	public String getFileLanguage() {
		return fileLanguage;
	}
	public void setFileLanguage(String fileLanguage) {
		this.fileLanguage = fileLanguage;
	}
	@JsonProperty("fileContent")
	public String getContent() {
		return fileContent;
	}
	@JsonProperty("fileContent")
	public void setContent(String fileContent) {
		this.fileContent = fileContent;
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
