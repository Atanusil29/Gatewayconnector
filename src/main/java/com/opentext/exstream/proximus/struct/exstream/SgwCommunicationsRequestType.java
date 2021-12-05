package com.opentext.exstream.proximus.struct.exstream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.opentext.exstream.proximus.struct.Default;

public class SgwCommunicationsRequestType extends Default {

	@JsonInclude(JsonInclude.Include.ALWAYS)
	private SgwContentType content;

	public SgwContentType getContent() {
		return content;
	}

	public void setContent(SgwContentType content) {
		this.content = content;
	}

}
