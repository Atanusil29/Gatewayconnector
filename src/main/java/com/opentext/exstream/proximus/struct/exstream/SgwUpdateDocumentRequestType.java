package com.opentext.exstream.proximus.struct.exstream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.opentext.exstream.proximus.struct.Default;

public class SgwUpdateDocumentRequestType extends Default {

	@JsonInclude(JsonInclude.Include.ALWAYS)
	private String statusmessage;

	@JsonInclude(JsonInclude.Include.ALWAYS)
	private String processingstate;

	public String getStatusmessage() {
		return statusmessage;
	}

	public void setStatusmessage(String statusmessage) {
		this.statusmessage = statusmessage;
	}

	public String getProcessingstate() {
		return processingstate;
	}

	public void setProcessingstate(String processingstate) {
		this.processingstate = processingstate;
	}

}
