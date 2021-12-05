package com.opentext.exstream.proximus.struct;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
public class MessageType extends Default {
	private String sender;
	private String[] recipient;
	private String subject;
	private FileType[] file;
	
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String[] getRecipient() {
		return recipient;
	}
	public void setRecipient(String[] recipient) {
		this.recipient = recipient;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public FileType[] getFile() {
		return file;
	}
	public void setFileTypes(FileType[] file) {
		this.file = file;
	}
}
