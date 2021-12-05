package com.opentext.exstream.proximus.struct;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
public class EventResponseType extends Default {

    private ResultType result;

	public ResultType getResult() {
		return result;
	}

	public void setResult(ResultType result) {
		this.result = result;
	}
}
