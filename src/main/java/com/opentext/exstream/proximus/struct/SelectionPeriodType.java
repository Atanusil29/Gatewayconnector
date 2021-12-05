package com.opentext.exstream.proximus.struct;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
public class SelectionPeriodType extends Default {
	private String startTimestamp;
	private String endTimestamp;
	public String getStartTimestamp() {
		return startTimestamp;
	}
	public void setStartTimestamp(String startTimestamp) {
		this.startTimestamp = startTimestamp;
	}
	public String getEndTimestamp() {
		return endTimestamp;
	}
	public void setEndTimestamp(String endTimestamp) {
		this.endTimestamp = endTimestamp;
	}
	
}
